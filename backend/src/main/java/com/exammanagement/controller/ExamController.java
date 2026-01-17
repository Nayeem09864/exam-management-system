package com.exammanagement.controller;

import com.exammanagement.dto.ExamDTO;
import com.exammanagement.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@CrossOrigin(origins = "http://localhost:4200")
public class ExamController {

    @Autowired
    private ExamService examService;

    @PostMapping
    public ResponseEntity<?> createExam(@Valid @RequestBody ExamDTO examDTO, Authentication authentication) {
        try {
            String username = authentication.getName();
            ExamDTO created = examService.createExam(examDTO, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating exam: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExamById(@PathVariable Long id) {
        try {
            ExamDTO exam = examService.getExamById(id);
            return ResponseEntity.ok(exam);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Exam not found: " + e.getMessage());
        }
    }

    @GetMapping("/access-code/{accessCode}")
    public ResponseEntity<?> getExamByAccessCode(@PathVariable String accessCode) {
        try {
            ExamDTO exam = examService.getExamByAccessCode(accessCode);
            return ResponseEntity.ok(exam);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Exam not found: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ExamDTO>> getAllExams(Authentication authentication) {
        List<ExamDTO> exams;
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            exams = examService.getAllExams();
        } else {
            String username = authentication.getName();
            exams = examService.getExamsByCreator(username);
        }
        return ResponseEntity.ok(exams);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExam(@PathVariable Long id, @Valid @RequestBody ExamDTO examDTO) {
        try {
            ExamDTO updated = examService.updateExam(id, examDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Exam not found: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExam(@PathVariable Long id) {
        try {
            examService.deleteExam(id);
            return ResponseEntity.ok().body("Exam deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Exam not found: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/candidates")
    public ResponseEntity<?> addCandidatesToExam(@PathVariable Long id, @RequestBody List<Long> candidateIds) {
        try {
            ExamDTO updated = examService.addCandidatesToExam(id, candidateIds);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error adding candidates: " + e.getMessage());
        }
    }
}
