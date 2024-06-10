package com.testing.stn.controller;

import com.testing.stn.model.Result;
import com.testing.stn.model.User;
import com.testing.stn.repository.ResultRepository;
import com.testing.stn.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResultRepository resultRepository;

    @GetMapping("/user/dashboard")
    public String userDashboard(Principal principal, Model model) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        List<Result> results = resultRepository.findByUserId(user.getId());
        model.addAttribute("results", results);
        return "user_dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        List<Result> results = resultRepository.findAll();
        model.addAttribute("results", results);
        return "admin_dashboard";
    }
}
