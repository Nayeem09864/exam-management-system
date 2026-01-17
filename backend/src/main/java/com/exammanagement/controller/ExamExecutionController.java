package com.exammanagement.controller;

import com.exammanagement.dto.*;
import com.exammanagement.service.ExamExecutionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-execution")
@CrossOrigin(origins = "http://localhost:4200")
public class ExamExecutionController {

    @Autowired
    private ExamExecutionService examExecutionService;

    @PostMapping("/start")
    public ResponseEntity<?> startExam(@Valid @RequestBody StartExamDTO startExamDTO) {
        try {
            ExamAttemptDTO attempt = examExecutionService.startExam(startExamDTO);
            return ResponseEntity.ok(attempt);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error starting exam: " + e.getMessage());
        }
    }

    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<?> getExamAttempt(
            @PathVariable Long attemptId,
            @RequestParam(defaultValue = "false") boolean includeCorrectAnswers,
            Authentication authentication) {
        try {
            // Only examiners can see correct answers
            boolean isExaminer = authentication != null && 
                    authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            ExamAttemptDTO attempt = examExecutionService.getExamAttempt(attemptId, isExaminer);
            return ResponseEntity.ok(attempt);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error getting exam attempt: " + e.getMessage());
        }
    }

    @PostMapping("/attempt/{attemptId}/save")
    public ResponseEntity<?> saveAnswers(
            @PathVariable Long attemptId,
            @RequestBody List<AnswerSubmissionDTO> answers) {
        try {
            ExamAttemptDTO updated = examExecutionService.saveAnswers(attemptId, answers);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error saving answers: " + e.getMessage());
        }
    }

    @PostMapping("/attempt/{attemptId}/submit")
    public ResponseEntity<?> submitExam(
            @PathVariable Long attemptId,
            @RequestBody(required = false) SubmitExamDTO submitExamDTO) {
        try {
            if (submitExamDTO == null) {
                submitExamDTO = new SubmitExamDTO();
                submitExamDTO.setAttemptId(attemptId);
            }
            ResultDTO result = examExecutionService.submitExam(attemptId, submitExamDTO);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error submitting exam: " + e.getMessage());
        }
    }
}
