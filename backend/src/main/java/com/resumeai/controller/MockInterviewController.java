package com.resumeai.controller;

import com.resumeai.dto.AnswerSubmitDTO;
import com.resumeai.dto.GradingResultDTO;
import com.resumeai.dto.InterviewQuestionDTO;
import com.resumeai.dto.InterviewRequestDTO;

import com.resumeai.service.MockInterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mock-interview")
public class MockInterviewController {

    @Autowired
    private MockInterviewService interviewService;

    @PostMapping("/questions")
    public ResponseEntity<?> getQuestions(@RequestBody InterviewRequestDTO request) {
        try {
            List<InterviewQuestionDTO> questions = interviewService.generateQuestions(request);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submit(@RequestBody AnswerSubmitDTO submission) {
        try {
            GradingResultDTO result = interviewService.gradeAnswers(submission);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
