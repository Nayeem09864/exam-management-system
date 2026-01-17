package com.exammanagement.controller;

import com.exammanagement.dto.QuestionDTO;
import com.exammanagement.model.DifficultyLevel;
import com.exammanagement.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "http://localhost:4200")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping
    public ResponseEntity<?> createQuestion(@Valid @RequestBody QuestionDTO questionDTO, Authentication authentication) {
        try {
            String username = authentication.getName();
            QuestionDTO created = questionService.createQuestion(questionDTO, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating question: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionDTO questionDTO) {
        try {
            QuestionDTO updated = questionService.updateQuestion(id, questionDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Question not found: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable Long id) {
        try {
            QuestionDTO question = questionService.getQuestionById(id);
            return ResponseEntity.ok(question);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Question not found: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<QuestionDTO>> getAllQuestions(
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String startDate) {
        
        List<QuestionDTO> questions = questionService.filterQuestions(difficulty, topic, startDate);
        return ResponseEntity.ok(questions);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        try {
            questionService.deleteQuestion(id);
            return ResponseEntity.ok().body("Question deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Question not found: " + e.getMessage());
        }
    }
}
