package com.example.schedulemanager.controller;

import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SchedulePageController {

    @GetMapping({"/", "/calendar"})
    public String calendar(Model model) {
        model.addAttribute("today", LocalDate.now().toString());
        return "calendar";
    }
}
