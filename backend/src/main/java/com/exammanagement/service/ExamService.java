package com.exammanagement.service;

import com.exammanagement.dto.ExamDTO;
import com.exammanagement.model.*;
import com.exammanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExamAttemptRepository examAttemptRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ExamDTO createExam(ExamDTO examDTO, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exam exam = new Exam();
        exam.setName(examDTO.getName());
        exam.setDurationMinutes(examDTO.getDurationMinutes());
        exam.setTotalQuestions(examDTO.getTotalQuestions());
        exam.setEasyQuestions(examDTO.getEasyQuestions());
        exam.setMediumQuestions(examDTO.getMediumQuestions());
        exam.setHardQuestions(examDTO.getHardQuestions());
        exam.setCreatedBy(user);
        exam.setCreatedAt(LocalDateTime.now());
        exam.setUpdatedAt(LocalDateTime.now());
        exam.setIsActive(examDTO.getIsActive() != null ? examDTO.getIsActive() : true);

        if (examDTO.getStartDate() != null && !examDTO.getStartDate().isEmpty()) {
            exam.setStartDate(LocalDateTime.parse(examDTO.getStartDate() + " 00:00:00", FORMATTER));
        }
        if (examDTO.getEndDate() != null && !examDTO.getEndDate().isEmpty()) {
            exam.setEndDate(LocalDateTime.parse(examDTO.getEndDate() + " 23:59:59", FORMATTER));
        }

        // Generate unique access code
        exam.setAccessCode(generateUniqueAccessCode());

        // Select questions based on difficulty distribution
        List<Question> selectedQuestions = selectQuestionsByDifficulty(
                examDTO.getEasyQuestions(),
                examDTO.getMediumQuestions(),
                examDTO.getHardQuestions(),
                examDTO.getQuestionIds()
        );

        if (selectedQuestions.size() < examDTO.getTotalQuestions()) {
            throw new RuntimeException("Not enough questions available matching the difficulty distribution");
        }

        exam.setQuestions(selectedQuestions.subList(0, examDTO.getTotalQuestions()));

        // Add candidates if specified
        if (examDTO.getCandidateIds() != null && !examDTO.getCandidateIds().isEmpty()) {
            List<Candidate> candidates = examDTO.getCandidateIds().stream()
                    .map(id -> candidateRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Candidate not found: " + id)))
                    .collect(Collectors.toList());
            exam.setCandidates(candidates);
        }

        Exam savedExam = examRepository.save(exam);
        return convertToDTO(savedExam);
    }

    private List<Question> selectQuestionsByDifficulty(int easy, int medium, int hard, List<Long> specificQuestionIds) {
        List<Question> selectedQuestions = new ArrayList<>();

        // If specific questions are provided, use them
        if (specificQuestionIds != null && !specificQuestionIds.isEmpty()) {
            List<Question> specificQuestions = questionRepository.findAllById(specificQuestionIds);
            selectedQuestions.addAll(specificQuestions);
        }

        // Add questions based on difficulty distribution
        if (easy > 0) {
            List<Question> easyQuestions = questionRepository.findByDifficultyLevel(DifficultyLevel.EASY);
            Collections.shuffle(easyQuestions);
            selectedQuestions.addAll(easyQuestions.stream().limit(easy).collect(Collectors.toList()));
        }

        if (medium > 0) {
            List<Question> mediumQuestions = questionRepository.findByDifficultyLevel(DifficultyLevel.MEDIUM);
            Collections.shuffle(mediumQuestions);
            selectedQuestions.addAll(mediumQuestions.stream().limit(medium).collect(Collectors.toList()));
        }

        if (hard > 0) {
            List<Question> hardQuestions = questionRepository.findByDifficultyLevel(DifficultyLevel.HARD);
            Collections.shuffle(hardQuestions);
            selectedQuestions.addAll(hardQuestions.stream().limit(hard).collect(Collectors.toList()));
        }

        return selectedQuestions;
    }

    private String generateUniqueAccessCode() {
        String code;
        do {
            code = "EXAM" + (1000 + new Random().nextInt(9000));
        } while (examRepository.findByAccessCode(code).isPresent());
        return code;
    }

    public ExamDTO getExamById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        return convertToDTO(exam);
    }

    public ExamDTO getExamByAccessCode(String accessCode) {
        Exam exam = examRepository.findByAccessCode(accessCode)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        return convertToDTO(exam);
    }

    public List<ExamDTO> getAllExams() {
        return examRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ExamDTO> getExamsByCreator(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return examRepository.findByCreatedBy(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ExamDTO updateExam(Long id, ExamDTO examDTO) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        exam.setName(examDTO.getName());
        exam.setDurationMinutes(examDTO.getDurationMinutes());
        exam.setIsActive(examDTO.getIsActive() != null ? examDTO.getIsActive() : exam.getIsActive());

        if (examDTO.getStartDate() != null && !examDTO.getStartDate().isEmpty()) {
            exam.setStartDate(LocalDateTime.parse(examDTO.getStartDate() + " 00:00:00", FORMATTER));
        }
        if (examDTO.getEndDate() != null && !examDTO.getEndDate().isEmpty()) {
            exam.setEndDate(LocalDateTime.parse(examDTO.getEndDate() + " 23:59:59", FORMATTER));
        }

        Exam updatedExam = examRepository.save(exam);
        return convertToDTO(updatedExam);
    }

    public void deleteExam(Long id) {
        if (!examRepository.existsById(id)) {
            throw new RuntimeException("Exam not found");
        }
        examRepository.deleteById(id);
    }

    public ExamDTO addCandidatesToExam(Long examId, List<Long> candidateIds) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        List<Candidate> candidates = candidateIds.stream()
                .map(id -> candidateRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Candidate not found: " + id)))
                .collect(Collectors.toList());

        exam.getCandidates().addAll(candidates);
        Exam updatedExam = examRepository.save(exam);
        return convertToDTO(updatedExam);
    }

    private ExamDTO convertToDTO(Exam exam) {
        ExamDTO dto = new ExamDTO();
        dto.setId(exam.getId());
        dto.setName(exam.getName());
        dto.setDurationMinutes(exam.getDurationMinutes());
        dto.setTotalQuestions(exam.getTotalQuestions());
        dto.setEasyQuestions(exam.getEasyQuestions());
        dto.setMediumQuestions(exam.getMediumQuestions());
        dto.setHardQuestions(exam.getHardQuestions());
        dto.setAccessCode(exam.getAccessCode());
        dto.setIsActive(exam.getIsActive());

        if (exam.getCreatedBy() != null) {
            dto.setCreatedBy(exam.getCreatedBy().getUsername());
        }
        if (exam.getCreatedAt() != null) {
            dto.setCreatedAt(exam.getCreatedAt().format(FORMATTER));
        }
        if (exam.getUpdatedAt() != null) {
            dto.setUpdatedAt(exam.getUpdatedAt().format(FORMATTER));
        }
        if (exam.getStartDate() != null) {
            dto.setStartDate(exam.getStartDate().format(FORMATTER));
        }
        if (exam.getEndDate() != null) {
            dto.setEndDate(exam.getEndDate().format(FORMATTER));
        }

        // Set question IDs
        dto.setQuestionIds(exam.getQuestions().stream()
                .map(Question::getId)
                .collect(Collectors.toList()));

        // Set candidate IDs
        dto.setCandidateIds(exam.getCandidates().stream()
                .map(Candidate::getId)
                .collect(Collectors.toList()));

        // Additional info
        List<ExamAttempt> attempts = examAttemptRepository.findByExam(exam);
        dto.setTotalAttempts(attempts.size());
        dto.setSubmittedAttempts((int) attempts.stream()
                .filter(ExamAttempt::getIsSubmitted)
                .count());

        return dto;
    }
}
