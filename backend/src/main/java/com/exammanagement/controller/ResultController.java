package com.exammanagement.controller;

import com.exammanagement.dto.ResultDTO;
import com.exammanagement.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/results")
@CrossOrigin(origins = "http://localhost:4200")
public class ResultController {

    @Autowired
    private ResultService resultService;

    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<?> getResultByAttemptId(@PathVariable Long attemptId) {
        try {
            ResultDTO result = resultService.getResultByAttemptId(attemptId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Result not found: " + e.getMessage());
        }
    }

    @GetMapping("/exam/{examId}")
    public ResponseEntity<List<ResultDTO>> getResultsByExamId(@PathVariable Long examId) {
        List<ResultDTO> results = resultService.getResultsByExamId(examId);
        return ResponseEntity.ok(results);
    }
}
