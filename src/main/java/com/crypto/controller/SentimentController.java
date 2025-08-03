package com.crypto.controller;

import com.crypto.service.SentimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;

@Controller
public class SentimentController {
    
    @Autowired
    private SentimentService sentimentService;
    
    @GetMapping("/")
    public String dashboard(Model model) {
        Map<String, Object> sentimentData = sentimentService.getLatestSentiment();
        model.addAttribute("sentiment", sentimentData);
        return "dashboard";
    }
    
    @GetMapping("/api/sentiment")
    @ResponseBody
    public Map<String, Object> getSentimentApi() {
        return sentimentService.getLatestSentiment();
    }
    
    @GetMapping("/api/analyze")
    @ResponseBody
    public Map<String, Object> runAnalysis() {
        return sentimentService.runNewAnalysis();
    }
}