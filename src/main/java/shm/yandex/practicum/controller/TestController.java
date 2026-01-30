package shm.yandex.practicum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    @GetMapping("/")
    @ResponseBody
    public String home() {
        return "Spring MVC loaded!";
    }

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "Test endpoint work!";
    }
}