package com.pickmeup.jobstartup.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/login")
public class LoginController {
    @GetMapping("/")
    public String loginPage() {
        return "member/login";
    }

//    @GetMapping("/logout")
//    public String logout() {
//        return "logout";
//    }


}
