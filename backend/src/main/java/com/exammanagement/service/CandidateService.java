package com.exammanagement.service;

import com.exammanagement.dto.CandidateDTO;
import com.exammanagement.model.Candidate;
import com.exammanagement.model.Exam;
import com.exammanagement.repository.CandidateRepository;
import com.exammanagement.repository.ExamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ExamRepository examRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CandidateDTO createCandidate(CandidateDTO candidateDTO) {
        // Check if email already exists
        if (candidateRepository.findByEmail(candidateDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Candidate with email " + candidateDTO.getEmail() + " already exists");
        }

        // Check if candidateId already exists (if provided)
        if (candidateDTO.getCandidateId() != null && !candidateDTO.getCandidateId().isEmpty()) {
            if (candidateRepository.findByCandidateId(candidateDTO.getCandidateId()).isPresent()) {
                throw new RuntimeException("Candidate with ID " + candidateDTO.getCandidateId() + " already exists");
            }
        }

        Candidate candidate = new Candidate();
        candidate.setName(candidateDTO.getName());
        candidate.setEmail(candidateDTO.getEmail());
        candidate.setCandidateId(candidateDTO.getCandidateId());
        candidate.setCreatedAt(LocalDateTime.now());
        candidate.setUpdatedAt(LocalDateTime.now());

        Candidate saved = candidateRepository.save(candidate);
        return convertToDTO(saved);
    }

    public List<CandidateDTO> importCandidatesFromFile(MultipartFile file) {
        List<CandidateDTO> importedCandidates = new ArrayList<>();
        String contentType = file.getContentType();

        try {
            if (contentType != null && (contentType.equals("text/csv") || 
                contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))) {
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue; // Skip header row
                    }

                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        String name = parts[0].trim();
                        String email = parts[1].trim();
                        String candidateId = parts.length > 2 ? parts[2].trim() : null;

                        if (!name.isEmpty() && !email.isEmpty()) {
                            try {
                                CandidateDTO candidateDTO = new CandidateDTO();
                                candidateDTO.setName(name);
                                candidateDTO.setEmail(email);
                                candidateDTO.setCandidateId(candidateId);
                                
                                CandidateDTO created = createCandidate(candidateDTO);
                                importedCandidates.add(created);
                            } catch (Exception e) {
                                // Skip duplicate or invalid entries
                                System.err.println("Skipped candidate: " + email + " - " + e.getMessage());
                            }
                        }
                    }
                }
                reader.close();
            } else {
                throw new RuntimeException("Unsupported file type. Please upload CSV or Excel file.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error importing candidates: " + e.getMessage());
        }

        return importedCandidates;
    }

    public CandidateDTO updateCandidate(Long id, CandidateDTO candidateDTO) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        // Check if email is being changed and if new email already exists
        if (!candidate.getEmail().equals(candidateDTO.getEmail())) {
            if (candidateRepository.findByEmail(candidateDTO.getEmail()).isPresent()) {
                throw new RuntimeException("Candidate with email " + candidateDTO.getEmail() + " already exists");
            }
        }

        candidate.setName(candidateDTO.getName());
        candidate.setEmail(candidateDTO.getEmail());
        candidate.setCandidateId(candidateDTO.getCandidateId());
        candidate.setUpdatedAt(LocalDateTime.now());

        Candidate updated = candidateRepository.save(candidate);
        return convertToDTO(updated);
    }

    public CandidateDTO getCandidateById(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        return convertToDTO(candidate);
    }

    public List<CandidateDTO> getAllCandidates() {
        return candidateRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void deleteCandidate(Long id) {
        if (!candidateRepository.existsById(id)) {
            throw new RuntimeException("Candidate not found");
        }
        candidateRepository.deleteById(id);
    }

    public CandidateDTO assignCandidateToExams(Long candidateId, List<Long> examIds) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        List<Exam> exams = examIds.stream()
                .map(id -> examRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Exam not found: " + id)))
                .collect(Collectors.toList());

        candidate.getExams().addAll(exams);
        Candidate updated = candidateRepository.save(candidate);
        return convertToDTO(updated);
    }

    private CandidateDTO convertToDTO(Candidate candidate) {
        CandidateDTO dto = new CandidateDTO();
        dto.setId(candidate.getId());
        dto.setName(candidate.getName());
        dto.setEmail(candidate.getEmail());
        dto.setCandidateId(candidate.getCandidateId());

        if (candidate.getCreatedAt() != null) {
            dto.setCreatedAt(candidate.getCreatedAt().format(FORMATTER));
        }
        if (candidate.getUpdatedAt() != null) {
            dto.setUpdatedAt(candidate.getUpdatedAt().format(FORMATTER));
        }

        if (candidate.getExams() != null) {
            dto.setExamIds(candidate.getExams().stream()
                    .map(Exam::getId)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
