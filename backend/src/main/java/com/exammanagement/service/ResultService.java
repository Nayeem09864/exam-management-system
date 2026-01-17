package com.exammanagement.service;

import com.exammanagement.dto.ResultDTO;
import com.exammanagement.model.*;
import com.exammanagement.repository.ExamAttemptRepository;
import com.exammanagement.repository.ExamRepository;
import com.exammanagement.repository.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResultService {

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private ExamAttemptRepository examAttemptRepository;

    @Autowired
    private ExamRepository examRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ResultDTO evaluateAndCreateResult(ExamAttempt examAttempt) {
        // Check if result already exists
        if (examAttempt.getResult() != null) {
            return convertToDTO(examAttempt.getResult());
        }

        Result result = new Result();
        result.setExamAttempt(examAttempt);
        result.setEvaluatedAt(LocalDateTime.now());
        result.setResultEmailed(false);

        int totalQuestions = examAttempt.getAnswers().size();
        int correctAnswers = 0;
        int wrongAnswers = 0;

        // Evaluate each answer
        for (AttemptAnswer attemptAnswer : examAttempt.getAnswers()) {
            Question question = attemptAnswer.getQuestion();
            List<Integer> selectedIndices = attemptAnswer.getSelectedOptionIndices();
            List<Integer> correctIndices = question.getCorrectAnswerIndices();

            // Sort both lists for comparison
            Collections.sort(selectedIndices);
            Collections.sort(correctIndices);

            if (selectedIndices.equals(correctIndices)) {
                correctAnswers++;
            } else {
                wrongAnswers++;
            }
        }

        result.setTotalQuestions(totalQuestions);
        result.setCorrectAnswers(correctAnswers);
        result.setWrongAnswers(wrongAnswers);

        // Calculate percentage
        double percentage = totalQuestions > 0 ? (correctAnswers * 100.0) / totalQuestions : 0.0;
        result.setPercentage(Math.round(percentage * 100.0) / 100.0);

        Result savedResult = resultRepository.save(result);
        examAttempt.setResult(savedResult);
        examAttemptRepository.save(examAttempt);

        return convertToDTO(savedResult);
    }

    public ResultDTO getResultByAttemptId(Long attemptId) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Exam attempt not found"));

        if (attempt.getResult() == null) {
            throw new RuntimeException("Result not found for this attempt");
        }

        return convertToDTO(attempt.getResult());
    }

    public List<ResultDTO> getResultsByExamId(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        List<Result> results = resultRepository.findByExamAttempt_Exam(exam);
        return results.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ResultDTO convertToDTO(Result result) {
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
