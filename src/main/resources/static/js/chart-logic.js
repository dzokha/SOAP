function drawGCChart(data) {
    // 1. Định nghĩa kích thước
    const margin = {top: 30, right: 30, bottom: 50, left: 60};
    const width = 800 - margin.left - margin.right;
    const height = 400 - margin.top - margin.bottom;

    // 2. Tạo đối tượng SVG
    const svg = d3.select("#gc-chart-container")
        .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
        .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

    // 3. Thiết lập thang đo (Scales)
    // Trục X: Từ 0 đến 100% GC
    const x = d3.scaleLinear()
        .domain([0, 100])
        .range([0, width]);

    // Trục Y: Số lượng đoạn đọc (tự động theo dữ liệu lớn nhất)
    const y = d3.scaleLinear()
        .domain([0, d3.max(data, d => d.count)])
        .range([height, 0]);

    // 4. Vẽ Trục (Axes)
    svg.append("g")
        .attr("transform", `translate(0,${height})`)
        .call(d3.axisBottom(x).ticks(10).tickFormat(d => d + "%"));

    svg.append("g")
        .call(d3.axisLeft(y));

    // 5. Tạo đường biểu đồ (Line Generator)
    const line = d3.line()
        .x(d => x(d.percentage))
        .y(d => y(d.count))
        .curve(d3.curveMonotoneX); // Làm đường cong mượt hơn

    // 6. Vẽ đường lên SVG
    svg.append("path")
        .datum(data)
        .attr("fill", "none")
        .attr("stroke", "#e67e22") // Màu cam đặc trưng của biểu đồ GC
        .attr("stroke-width", 2)
        .attr("d", line);

    // 7. Thêm nhãn (Labels)
    svg.append("text")
        .attr("x", width / 2)
        .attr("y", height + 40)
        .style("text-anchor", "middle")
        .text("Mean GC content (%)");
}

// TEST: Dữ liệu giả lập để bạn thấy ngay kết quả
const mockData = [
    {percentage: 35, count: 10}, {percentage: 40, count: 50},
    {percentage: 45, count: 120}, {percentage: 50, count: 200},
    {percentage: 55, count: 110}, {percentage: 60, count: 40}
];
drawGCChart(mockData);