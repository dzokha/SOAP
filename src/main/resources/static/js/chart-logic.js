function drawGCChart(data) {
    console.log("Dữ liệu nhận được để vẽ:", data); // Thêm dòng này để debug
    if (!data || data.length === 0) {
        console.error("Dữ liệu trống, không thể vẽ biểu đồ!");
        return;
    }

    d3.select("#gc-chart-container").selectAll("*").remove();
    
    const margin = {top: 30, right: 30, bottom: 50, left: 60};
    const width = 800 - margin.left - margin.right;
    const height = 400 - margin.top - margin.bottom;

    const svg = d3.select("#gc-chart-container")
        .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
        .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

    // SỬA TRỤC X: Sử dụng scalePoint cho các nhãn chuỗi (1-5, 6-10...)
    const xKey = data[0].x !== undefined ? 'x' : 'percentage';
    const yKey = data[0].y !== undefined ? 'y' : 'count';

    const x = d3.scalePoint()
        .domain(data.map(d => d[xKey]))
        .range([0, width]);

    const y = d3.scaleLinear()
        .domain([0, d3.max(data, d => d[yKey])])
        .range([height, 0]);

    // Vẽ Trục
    svg.append("g")
        .attr("transform", `translate(0,${height})`)
        .call(d3.axisBottom(x).tickValues(x.domain().filter((d,i) => !(i%5)))); // Giảm bớt nhãn cho đỡ rối

    svg.append("g").call(d3.axisLeft(y));

    // Đường kẻ
    const line = d3.line()
        .x(d => x(d[xKey]))
        .y(d => y(+d[yKey])) // <--- SỬA DÒNG NÀY: Thêm dấu + trước d[yKey]
        .curve(d3.curveMonotoneX);

    svg.append("path")
        .datum(data)
        .attr("fill", "none")
        .attr("stroke", "#e67e22")
        .attr("stroke-width", 2)
        .attr("d", line);
}

/**
 * Hàm vẽ biểu đồ Adapter Content chuẩn FastQC (Có Tooltip tương tác)
 */
function drawAdapterChart(selector, data) {
    if (!data || data.length === 0) return;

    // 1. Tự động thêm CSS cho Tooltip vào trang nếu chưa có
    if (!document.getElementById("d3-tooltip-style")) {
        const style = document.createElement('style');
        style.id = "d3-tooltip-style";
        style.innerHTML = `
            .d3-tooltip {
                position: absolute; padding: 8px 12px; font: 13px sans-serif;
                background: rgba(44, 62, 80, 0.95); color: #fff; border-radius: 6px;
                pointer-events: none; opacity: 0; z-index: 1000; box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            }
        `;
        document.head.appendChild(style);
    }

    // Tạo (hoặc lấy) thẻ div làm tooltip
    let tooltip = d3.select("body").select(".d3-tooltip");
    if (tooltip.empty()) {
        tooltip = d3.select("body").append("div").attr("class", "d3-tooltip");
    }

    // Xóa SVG cũ
    d3.select(selector).selectAll("*").remove();

    // Thiết lập kích thước
    const margin = {top: 20, right: 220, bottom: 60, left: 50}; 
    const width = 850 - margin.left - margin.right;
    const height = 350 - margin.top - margin.bottom;

    const svg = d3.select(selector)
        .append("svg")
            .attr("width", "100%") 
            .attr("viewBox", `0 0 ${width + margin.left + margin.right} ${height + margin.top + margin.bottom}`)
        .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

    // Lấy nhãn trục X
    let allLabels = [];
    data.forEach(series => {
        if(series.points) series.points.forEach(p => { if (!allLabels.includes(p.x)) allLabels.push(p.x); });
    });

    const x = d3.scalePoint().domain(allLabels).range([0, width]);

    // CHÚ Ý: Cố định trục Y chuẩn FastQC từ 0 đến 100%
    const y = d3.scaleLinear().domain([0, 100]).range([height, 0]);

    // Kẻ lưới nền mờ cho dễ nhìn
    svg.append("g")
        .attr("class", "grid")
        .call(d3.axisLeft(y).tickSize(-width).tickFormat("").ticks(5))
        .selectAll("line")
        .attr("stroke", "#e0e0e0")
        .attr("stroke-dasharray", "3,3");

    // Vẽ trục X và Y
    svg.append("g")
        .attr("transform", `translate(0,${height})`)
        .call(d3.axisBottom(x).tickValues(x.domain().filter((d,i) => !(i%5))))
        .selectAll("text").attr("transform", "rotate(-45)").style("text-anchor", "end");

    svg.append("g").call(d3.axisLeft(y).ticks(5).tickFormat(d => d + "%"));

    const color = d3.scaleOrdinal(d3.schemeCategory10);
    const line = d3.line().x(d => x(d.x)).y(d => y(+d.y)).curve(d3.curveMonotoneX);

    // Vẽ từng đường Adapter
    data.forEach((series, index) => {
        if(series.points && series.points.length > 0) {
            // Vẽ đường (Path) với hiệu ứng tương tác
            svg.append("path")
                .datum(series.points)
                .attr("fill", "none")
                .attr("stroke", color(index))
                .attr("stroke-width", 2.5)
                .attr("d", line)
                .style("cursor", "pointer")
                .on("mouseover", function(event) {
                    // Làm mờ các đường khác, tô đậm đường hiện tại
                    svg.selectAll("path").attr("opacity", 0.2);
                    d3.select(this).attr("stroke-width", 5).attr("opacity", 1);
                    
                    // Lấy giá trị lớn nhất của đường này để hiển thị trên tooltip
                    const maxVal = d3.max(series.points, d => +d.y).toFixed(3);
                    
                    tooltip.transition().duration(200).style("opacity", 1);
                    tooltip.html(`<strong>${series.adapterName}</strong><br/>Max Value: ${maxVal}%`)
                           .style("left", (event.pageX + 15) + "px")
                           .style("top", (event.pageY - 28) + "px");
                })
                .on("mouseout", function() {
                    svg.selectAll("path").attr("opacity", 1).attr("stroke-width", 2.5);
                    tooltip.transition().duration(500).style("opacity", 0);
                });

            // Vẽ ghi chú (Legend)
            const legendGroup = svg.append("g")
                .style("cursor", "pointer")
                // Cho phép di chuột vào Text cũng highlight đường được
                .on("mouseover", () => {
                    svg.selectAll("path").attr("opacity", 0.2);
                    svg.selectAll("path").filter((d, i) => i === index).attr("stroke-width", 5).attr("opacity", 1);
                })
                .on("mouseout", () => {
                    svg.selectAll("path").attr("opacity", 1).attr("stroke-width", 2.5);
                });

            legendGroup.append("rect")
                .attr("x", width + 20).attr("y", index * 22)
                .attr("width", 12).attr("height", 12).attr("rx", 2)
                .style("fill", color(index));

            legendGroup.append("text")
                .attr("x", width + 38).attr("y", index * 22 + 10)
                .style("font-size", "12px").style("fill", "#333")
                .text(series.adapterName);
        }
    });

    // Tiêu đề trục
    svg.append("text").attr("text-anchor", "middle").attr("x", width / 2).attr("y", height + 50)
       .style("font-weight", "600").style("font-size", "14px").text("Position in read (bp)");

    svg.append("text").attr("text-anchor", "middle").attr("transform", "rotate(-90)")
       .attr("y", -35).attr("x", -height / 2)
       .style("font-weight", "600").style("font-size", "14px").text("% Cumulative Adapter");
}

/**
 * Hàm vẽ biểu đồ Per Base Sequence Quality (Box-and-Whisker) chuẩn FastQC
 * @param {string} selector - ID của thẻ div chứa biểu đồ
 * @param {Array} data - Mảng dữ liệu chất lượng từ API
 */
function drawPerBaseQualityChart(selector, data) {
    if (!data || data.length === 0) return;

    d3.select(selector).selectAll("*").remove();

    const margin = {top: 30, right: 30, bottom: 60, left: 50}; 
    const width = 850 - margin.left - margin.right;
    const height = 400 - margin.top - margin.bottom;

    const svg = d3.select(selector)
        .append("svg")
            .attr("width", "100%") 
            .attr("viewBox", `0 0 ${width + margin.left + margin.right} ${height + margin.top + margin.bottom}`)
        .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

    // Trục X (Sử dụng scaleBand để tạo độ rộng cho các hộp vàng)
    const x = d3.scaleBand()
        .domain(data.map(d => d.base))
        .range([0, width])
        .paddingInner(0.2)
        .paddingOuter(0.2);

    // Trục Y (Điểm Phred Quality Score thường nằm trong khoảng 0 đến 40)
    const y = d3.scaleLinear()
        .domain([0, 40])
        .range([height, 0]);

    // --- BƯỚC 1: VẼ BACKGROUND MÀU CHUẨN FASTQC ---
    const bgGroup = svg.append("g").attr("class", "background-zones");
    
    // Vùng Xanh (Chất lượng tốt: 28 - 40)
    bgGroup.append("rect")
        .attr("x", 0).attr("width", width)
        .attr("y", y(40)).attr("height", y(28) - y(40))
        .attr("fill", "#c8e6c9").attr("opacity", 0.7);

    // Vùng Vàng (Chất lượng trung bình: 20 - 28)
    bgGroup.append("rect")
        .attr("x", 0).attr("width", width)
        .attr("y", y(28)).attr("height", y(20) - y(28))
        .attr("fill", "#fff9c4").attr("opacity", 0.7);

    // Vùng Đỏ (Chất lượng kém: 0 - 20)
    bgGroup.append("rect")
        .attr("x", 0).attr("width", width)
        .attr("y", y(20)).attr("height", y(0) - y(20))
        .attr("fill", "#ffcdd2").attr("opacity", 0.7);

    // --- BƯỚC 2: VẼ TRỤC ---
    svg.append("g")
        .attr("transform", `translate(0,${height})`)
        .call(d3.axisBottom(x).tickValues(x.domain().filter((d,i) => !(i%2)))) // Giảm nhãn trục X cho đỡ rối
        .selectAll("text")
        .attr("transform", "rotate(-45)")
        .style("text-anchor", "end");

    svg.append("g").call(d3.axisLeft(y));

    // Lưới ngang
    svg.append("g")
        .attr("class", "grid")
        .call(d3.axisLeft(y).tickSize(-width).tickFormat(""))
        .selectAll("line")
        .attr("stroke", "#e0e0e0").attr("stroke-dasharray", "3,3");

    // --- BƯỚC 3: VẼ BOX VÀ WHISKER ---
    const boxWidth = x.bandwidth();
    const boxGroup = svg.append("g").attr("class", "box-plots");

    data.forEach(d => {
        const center = x(d.base) + boxWidth / 2;

        // Vẽ Whisker dọc (từ 10th đến 90th percentile)
        boxGroup.append("line")
            .attr("x1", center).attr("x2", center)
            .attr("y1", y(d.uw)).attr("y2", y(d.lw))
            .attr("stroke", "black");

        // Vẽ vạch ngang ở đỉnh whisker trên
        boxGroup.append("line")
            .attr("x1", center - boxWidth/4).attr("x2", center + boxWidth/4)
            .attr("y1", y(d.uw)).attr("y2", y(d.uw))
            .attr("stroke", "black");

        // Vẽ vạch ngang ở đáy whisker dưới
        boxGroup.append("line")
            .attr("x1", center - boxWidth/4).attr("x2", center + boxWidth/4)
            .attr("y1", y(d.lw)).attr("y2", y(d.lw))
            .attr("stroke", "black");

        // Vẽ Hộp Vàng (từ Q1 đến Q3)
        boxGroup.append("rect")
            .attr("x", x(d.base))
            .attr("width", boxWidth)
            .attr("y", y(d.q3))
            .attr("height", y(d.q1) - y(d.q3))
            .attr("fill", "yellow")
            .attr("stroke", "black");

        // Vẽ đường Median (Trung vị - màu đỏ)
        boxGroup.append("line")
            .attr("x1", x(d.base)).attr("x2", x(d.base) + boxWidth)
            .attr("y1", y(d.median)).attr("y2", y(d.median))
            .attr("stroke", "red").attr("stroke-width", 1.5);
    });

    // --- BƯỚC 4: VẼ ĐƯỜNG TRUNG BÌNH (MEAN LINE) ---
    const meanLine = d3.line()
        .x(d => x(d.base) + boxWidth / 2)
        .y(d => y(d.mean))
        .curve(d3.curveMonotoneX);

    svg.append("path")
        .datum(data)
        .attr("fill", "none")
        .attr("stroke", "blue")
        .attr("stroke-width", 1.5)
        .attr("d", meanLine);

    // --- BƯỚC 5: TIÊU ĐỀ TRỤC ---
    svg.append("text")
        .attr("text-anchor", "middle")
        .attr("x", width / 2)
        .attr("y", height + margin.bottom - 10)
        .style("font-weight", "bold")
        .text("Position in read (bp)");

    svg.append("text")
        .attr("text-anchor", "middle")
        .attr("transform", "rotate(-90)")
        .attr("y", -margin.left + 15)
        .attr("x", -height / 2)
        .style("font-weight", "bold")
        .text("Quality Scores (Phred)");
}