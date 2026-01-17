package com.exammanagement.service;

import com.exammanagement.dto.*;
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
public class ExamExecutionService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ExamAttemptRepository examAttemptRepository;

    @Autowired
    private ResultService resultService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ExamAttemptDTO startExam(StartExamDTO startExamDTO) {
        Exam exam = examRepository.findByAccessCode(startExamDTO.getAccessCode())
                .orElseThrow(() -> new RuntimeException("Exam not found or invalid access code"));

        if (!exam.getIsActive()) {
            throw new RuntimeException("Exam is not active");
        }

        // Check if exam has start/end dates
        LocalDateTime now = LocalDateTime.now();
        if (exam.getStartDate() != null && now.isBefore(exam.getStartDate())) {
            throw new RuntimeException("Exam has not started yet");
        }
        if (exam.getEndDate() != null && now.isAfter(exam.getEndDate())) {
            throw new RuntimeException("Exam has expired");
        }

        // Find or create candidate
        Candidate candidate = candidateRepository.findByEmail(startExamDTO.getEmail())
                .orElseGet(() -> {
                    Candidate newCandidate = new Candidate();
                    newCandidate.setName(startExamDTO.getName());
                    newCandidate.setEmail(startExamDTO.getEmail());
                    newCandidate.setCandidateId(startExamDTO.getCandidateId());
                    newCandidate.setCreatedAt(LocalDateTime.now());
                    newCandidate.setUpdatedAt(LocalDateTime.now());
                    return candidateRepository.save(newCandidate);
                });

        // Check if candidate is already invited to this exam
        if (!exam.getCandidates().contains(candidate)) {
            exam.getCandidates().add(candidate);
            examRepository.save(exam);
        }

        // Check if candidate already has an attempt
        Optional<ExamAttempt> existingAttempt = examAttemptRepository.findByExamAndCandidate(exam, candidate);
        if (existingAttempt.isPresent()) {
            ExamAttempt attempt = existingAttempt.get();
            if (attempt.getIsSubmitted()) {
                throw new RuntimeException("You have already submitted this exam");
            }
            // Return existing attempt if not submitted
            return convertAttemptToDTO(attempt, true); // true = include correct answers for taking exam
        }

        // Create new exam attempt
        ExamAttempt attempt = new ExamAttempt();
        attempt.setExam(exam);
        attempt.setCandidate(candidate);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setTimeRemainingSeconds(exam.getDurationMinutes() * 60);
        attempt.setIsSubmitted(false);
        attempt.setIsAutoSubmitted(false);
        attempt.setCreatedAt(LocalDateTime.now());

        // Randomize question order for this attempt
        List<Question> questions = new ArrayList<>(exam.getQuestions());
        Collections.shuffle(questions);

        // Create AttemptAnswer entries with randomized order
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            AttemptAnswer answer = new AttemptAnswer();
            answer.setExamAttempt(attempt);
            answer.setQuestion(question);
            answer.setQuestionOrder(i);
            answer.setSelectedOptionIndices(new ArrayList<>());
            attempt.getAnswers().add(answer);
        }

        ExamAttempt savedAttempt = examAttemptRepository.save(attempt);
        return convertAttemptToDTO(savedAttempt, true);
    }

    public ExamAttemptDTO getExamAttempt(Long attemptId, boolean includeCorrectAnswers) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Exam attempt not found"));

        if (attempt.getIsSubmitted()) {
            throw new RuntimeException("Exam has already been submitted");
        }

        // Check if time has expired
        LocalDateTime now = LocalDateTime.now();
        int elapsedSeconds = (int) java.time.Duration.between(attempt.getStartedAt(), now).getSeconds();
        int remainingSeconds = attempt.getTimeRemainingSeconds() - elapsedSeconds;

        if (remainingSeconds <= 0) {
            // Auto-submit exam
            submitExamAuto(attempt);
            throw new RuntimeException("Exam time has expired");
        }

        attempt.setTimeRemainingSeconds(remainingSeconds);
        ExamAttempt updatedAttempt = examAttemptRepository.save(attempt);

        return convertAttemptToDTO(updatedAttempt, includeCorrectAnswers);
    }

    public ExamAttemptDTO saveAnswers(Long attemptId, List<AnswerSubmissionDTO> answers) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Exam attempt not found"));

        if (attempt.getIsSubmitted()) {
            throw new RuntimeException("Exam has already been submitted");
        }

        // Check if time has expired
        LocalDateTime now = LocalDateTime.now();
        int elapsedSeconds = (int) java.time.Duration.between(attempt.getStartedAt(), now).getSeconds();
        int remainingSeconds = attempt.getTimeRemainingSeconds() - elapsedSeconds;

        if (remainingSeconds <= 0) {
            submitExamAuto(attempt);
            throw new RuntimeException("Exam time has expired");
        }

        // Update answers
        Map<Long, AnswerSubmissionDTO> answerMap = answers.stream()
                .collect(Collectors.toMap(AnswerSubmissionDTO::getQuestionId, a -> a));

        for (AttemptAnswer attemptAnswer : attempt.getAnswers()) {
            AnswerSubmissionDTO submission = answerMap.get(attemptAnswer.getQuestion().getId());
            if (submission != null) {
                attemptAnswer.setSelectedOptionIndices(submission.getSelectedOptionIndices());
            }
        }

        attempt.setTimeRemainingSeconds(remainingSeconds);
        ExamAttempt savedAttempt = examAttemptRepository.save(attempt);
        return convertAttemptToDTO(savedAttempt, true);
    }

    public ResultDTO submitExam(Long attemptId, SubmitExamDTO submitExamDTO) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Exam attempt not found"));

        if (attempt.getIsSubmitted()) {
            throw new RuntimeException("Exam has already been submitted");
        }

        // Save final answers
        if (submitExamDTO.getAnswers() != null) {
            Map<Long, AnswerSubmissionDTO> answerMap = submitExamDTO.getAnswers().stream()
                    .collect(Collectors.toMap(AnswerSubmissionDTO::getQuestionId, a -> a));

            for (AttemptAnswer attemptAnswer : attempt.getAnswers()) {
                AnswerSubmissionDTO submission = answerMap.get(attemptAnswer.getQuestion().getId());
                if (submission != null) {
                    attemptAnswer.setSelectedOptionIndices(submission.getSelectedOptionIndices());
                }
            }
        }

        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setIsSubmitted(true);
        attempt.setIsAutoSubmitted(false);
        ExamAttempt savedAttempt = examAttemptRepository.save(attempt);

        // Evaluate and create result
        ResultDTO result = resultService.evaluateAndCreateResult(savedAttempt);
        return result;
    }

    private void submitExamAuto(ExamAttempt attempt) {
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setIsSubmitted(true);
        attempt.setIsAutoSubmitted(true);
        examAttemptRepository.save(attempt);
        // Auto-evaluate
        resultService.evaluateAndCreateResult(attempt);
    }

    private ExamAttemptDTO convertAttemptToDTO(ExamAttempt attempt, boolean includeCorrectAnswers) {
        ExamAttemptDTO dto = new ExamAttemptDTO();
        dto.setId(attempt.getId());
        dto.setExamId(attempt.getExam().getId());
        dto.setExamName(attempt.getExam().getName());
        dto.setCandidateId(attempt.getCandidate().getId());
        dto.setCandidateName(attempt.getCandidate().getName());
        dto.setCandidateEmail(attempt.getCandidate().getEmail());
        dto.setStartedAt(attempt.getStartedAt().format(FORMATTER));
        dto.setTimeRemainingSeconds(attempt.getTimeRemainingSeconds());
        dto.setIsSubmitted(attempt.getIsSubmitted());
        dto.setIsAutoSubmitted(attempt.getIsAutoSubmitted());

        if (attempt.getSubmittedAt() != null) {
            dto.setSubmittedAt(attempt.getSubmittedAt().format(FORMATTER));
        }

        // Convert answers
        List<AttemptAnswerDTO> answerDTOs = attempt.getAnswers().stream()
                .sorted(Comparator.comparing(AttemptAnswer::getQuestionOrder))
                .map(answer -> {
                    AttemptAnswerDTO answerDTO = new AttemptAnswerDTO();
                    answerDTO.setId(answer.getId());
                    answerDTO.setQuestionId(answer.getQuestion().getId());
                    answerDTO.setQuestionText(answer.getQuestion().getQuestionText());
                    answerDTO.setParagraph(answer.getQuestion().getParagraph());
                    answerDTO.setImageUrl(answer.getQuestion().getImageUrl());
                    answerDTO.setQuestionOrder(answer.getQuestionOrder());
                    answerDTO.setSelectedOptionIndices(new ArrayList<>(answer.getSelectedOptionIndices()));

                    // Convert options
                    List<QuestionOptionDTO> optionDTOs = answer.getQuestion().getOptions().stream()
                            .sorted(Comparator.comparing(QuestionOption::getOptionIndex))
                            .map(option -> {
                                QuestionOptionDTO optionDTO = new QuestionOptionDTO();
                                optionDTO.setOptionIndex(option.getOptionIndex());
                                optionDTO.setOptionText(option.getOptionText());
                                optionDTO.setOptionImageUrl(option.getOptionImageUrl());
                                return optionDTO;
                            })
                            .collect(Collectors.toList());
                    answerDTO.setOptions(optionDTOs);

                    // Include correct answers only for examiners
                    if (includeCorrectAnswers) {
                        answerDTO.setCorrectAnswerIndices(new ArrayList<>(answer.getQuestion().getCorrectAnswerIndices()));
                    }

                    return answerDTO;
                })
                .collect(Collectors.toList());

        dto.setAnswers(answerDTOs);

        if (attempt.getResult() != null) {
            dto.setResult(convertResultToDTO(attempt.getResult()));
        }

        return dto;
    }

    private ResultDTO convertResultToDTO(Result result) {
        ResultDTO dto = new ResultDTO();
        dto.setId(result.getId());
        dto.setExamAttemptId(result.getExamAttempt().getId());
        dto.setTotalQuestions(result.getTotalQuestions());
        dto.setCorrectAnswers(result.getCorrectAnswers());
        dto.setWrongAnswers(result.getWrongAnswers());
        dto.setPercentage(result.getPercentage());
        dto.setEvaluatedAt(result.getEvaluatedAt().format(FORMATTER));
        return dto;
    }
}
