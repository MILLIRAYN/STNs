package com.testing.stn.controller;

import com.testing.stn.model.*;
import com.testing.stn.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResultRepository resultRepository;

    @GetMapping("/{id}")
    public String getTest(@PathVariable Long id, Model model) {
        Test test = testRepository.findById(id).orElseThrow(() -> new RuntimeException("Test not found"));
        model.addAttribute("test", test);
        return "test";
    }

    @PostMapping("/submit")
    public String submitTest(@RequestParam Map<String, String> allParams, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login"; // Перенаправить на страницу логина, если пользователь не аутентифицирован
        }

        String testIdStr = allParams.get("testId");
        if (testIdStr == null || testIdStr.isEmpty()) {
            throw new IllegalArgumentException("Test ID is missing");
        }

        Long testId = Long.valueOf(testIdStr);
        Test test = testRepository.findById(testId).orElseThrow(() -> new RuntimeException("Test not found"));
        User user = userRepository.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));

        List<Question> questions = test.getQuestions();
        int correctAnswers = 0;
        for (int i = 0; i < questions.size(); i++) {
            String userAnswer = allParams.get("answers[" + i + "]");
            if (userAnswer != null && questions.get(i).getCorrectAnswer().equals(userAnswer)) {
                correctAnswers++;
            }
        }

        double percentage = ((double) correctAnswers / questions.size()) * 100;

        Result result = new Result();
        result.setUser(user);
        result.setTest(test);
        result.setCorrectAnswers(correctAnswers);
        result.setTotalQuestions(questions.size());
        result.setPercentage(percentage);

        resultRepository.save(result);

        model.addAttribute("correctAnswers", correctAnswers);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("percentage", percentage);
        return "result";
    }
    @GetMapping("/all")
    public String allAccess() {
        return "Public Content.";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public String userAccess() {
        return "User Content.";
    }

    @GetMapping("/mod")
    @PreAuthorize("hasRole('MODERATOR')")
    public String moderatorAccess() {
        return "Moderator Board.";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Admin Board.";
    }
}
