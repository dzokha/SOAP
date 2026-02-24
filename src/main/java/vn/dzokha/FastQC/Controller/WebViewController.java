package vn.dzokha.FastQC.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebViewController {

    // Khi người dùng gõ http://localhost:8080/
    @GetMapping("/")
    public String indexPage() {
        // Trả về tên file "index" (Spring tự hiểu là templates/index.html)
        return "index"; 
    }

    // Nếu sau này bạn có trang lịch sử: http://localhost:8080/history
    @GetMapping("/history")
    public String historyPage() {
        return "history"; // Trả về templates/history.html
    }
}