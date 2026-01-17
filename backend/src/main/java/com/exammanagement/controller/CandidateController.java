package com.exammanagement.controller;

import com.exammanagement.dto.CandidateDTO;
import com.exammanagement.service.CandidateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
@CrossOrigin(origins = "http://localhost:4200")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @PostMapping
    public ResponseEntity<?> createCandidate(@Valid @RequestBody CandidateDTO candidateDTO) {
        try {
            CandidateDTO created = candidateService.createCandidate(candidateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating candidate: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public ResponseEntity<?> importCandidates(@RequestParam("file") MultipartFile file) {
        try {
            List<CandidateDTO> imported = candidateService.importCandidatesFromFile(file);
            return ResponseEntity.ok(imported);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error importing candidates: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCandidateById(@PathVariable Long id) {
        try {
            CandidateDTO candidate = candidateService.getCandidateById(id);
            return ResponseEntity.ok(candidate);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Candidate not found: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<CandidateDTO>> getAllCandidates() {
        List<CandidateDTO> candidates = candidateService.getAllCandidates();
        return ResponseEntity.ok(candidates);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCandidate(@PathVariable Long id, @Valid @RequestBody CandidateDTO candidateDTO) {
        try {
            CandidateDTO updated = candidateService.updateCandidate(id, candidateDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error updating candidate: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCandidate(@PathVariable Long id) {
        try {
            candidateService.deleteCandidate(id);
            return ResponseEntity.ok().body("Candidate deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Candidate not found: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/exams")
    public ResponseEntity<?> assignCandidateToExams(@PathVariable Long id, @RequestBody List<Long> examIds) {
        try {
            CandidateDTO updated = candidateService.assignCandidateToExams(id, examIds);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error assigning exams: " + e.getMessage());
        }
    }
}
