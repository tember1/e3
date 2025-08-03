package com.crypto.service;

import org.springframework.stereotype.Service;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

@Service
public class SentimentService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, Object> cachedSentiment = new HashMap<>();
    
    public Map<String, Object> getLatestSentiment() {
        if (cachedSentiment.isEmpty()) {
            return runNewAnalysis();
        }
        return cachedSentiment;
    }
    
    public Map<String, Object> runNewAnalysis() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Fear & Greed Index
            Map<String, Object> fearGreed = getFearGreedIndex();
            result.put("fearGreed", fearGreed);
            
            // Calculate overall sentiment
            double overallSentiment = calculateOverallSentiment(result);
            result.put("overallSentiment", overallSentiment);
            result.put("mood", getMoodDescription(overallSentiment));
            result.put("timestamp", new Date().toString());
            
            cachedSentiment = result;
            
        } catch (Exception e) {
            result.put("error", "Failed to analyze sentiment: " + e.getMessage());
        }
        
        return result;
    }
    
    private Map<String, Object> getFearGreedIndex() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("https://api.alternative.me/fng/");
            
            return client.execute(request, response -> {
                String json = EntityUtils.toString(response.getEntity());
                Map<String, Object> data = objectMapper.readValue(json, Map.class);
                
                List<Map<String, Object>> fngData = (List<Map<String, Object>>) data.get("data");
                Map<String, Object> latest = fngData.get(0);
                
                Map<String, Object> result = new HashMap<>();
                result.put("value", Integer.parseInt((String) latest.get("value")));
                result.put("classification", latest.get("value_classification"));
                
                return result;
            });
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("value", 50);
            error.put("classification", "Neutral (Error)");
            return error;
        }
    }
    
    private double calculateOverallSentiment(Map<String, Object> data) {
        double score = 0.0;
        
        if (data.containsKey("fearGreed")) {
            Map<String, Object> fg = (Map<String, Object>) data.get("fearGreed");
            int value = (Integer) fg.get("value");
            score = (value - 50.0) / 50.0; // Normalize to -1 to 1
        }
        
        return Math.max(-1.0, Math.min(1.0, score));
    }
    
    private String getMoodDescription(double score) {
        if (score >= 0.6) return "Very Bullish 🚀";
        if (score >= 0.2) return "Bullish 📈";
        if (score >= -0.2) return "Neutral 😐";
        if (score >= -0.6) return "Bearish 📉";
        return "Very Bearish 💥";
    }
}