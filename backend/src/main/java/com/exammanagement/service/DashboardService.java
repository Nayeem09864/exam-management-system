package com.exammanagement.service;

import com.exammanagement.dto.*;
import com.exammanagement.model.*;
import com.exammanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DashboardService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ExamAttemptRepository examAttemptRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    public DashboardDTO getDashboard(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DashboardDTO dashboard = new DashboardDTO();

        // Get all exams created by this user
        List<Exam> exams = examRepository.findByCreatedBy(user);
        dashboard.setTotalExams(exams.size());

        // Get total candidates
        long totalCandidates = candidateRepository.count();
        dashboard.setTotalCandidates((int) totalCandidates);

        // Get total questions created by this user
        long totalQuestions = questionRepository.findByCreatedByUsername(username).size();
        dashboard.setTotalQuestions((int) totalQuestions);

        // Get exam statistics
        List<ExamSummaryDTO> examSummaries = new ArrayList<>();
        for (Exam exam : exams) {
            ExamSummaryDTO summary = new ExamSummaryDTO();
            summary.setExamId(exam.getId());
            summary.setExamName(exam.getName());
            summary.setAccessCode(exam.getAccessCode());
            summary.setTotalQuestions(exam.getTotalQuestions());
            summary.setDurationMinutes(exam.getDurationMinutes());

            List<ExamAttempt> attempts = examAttemptRepository.findByExam(exam);
            summary.setTotalAttempts(attempts.size());
            summary.setSubmittedAttempts((int) attempts.stream()
                    .filter(ExamAttempt::getIsSubmitted)
                    .count());

            List<Result> results = resultRepository.findByExamAttempt_Exam(exam);
            if (!results.isEmpty()) {
                double avgPercentage = results.stream()
                        .mapToDouble(Result::getPercentage)
                        .average()
                        .orElse(0.0);
                summary.setAverageScore(Math.round(avgPercentage * 100.0) / 100.0);
            }

            summary.setTotalCandidates(exam.getCandidates().size());
            summary.setIsActive(exam.getIsActive());

            if (exam.getCreatedAt() != null) {
                summary.setCreatedAt(exam.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            examSummaries.add(summary);
        }

        dashboard.setExams(examSummaries);

        // Get participation statistics
        long totalAttempts = examAttemptRepository.count();
        long submittedAttempts = examAttemptRepository.findByIsSubmitted(true).size();
        dashboard.setTotalAttempts((int) totalAttempts);
        dashboard.setSubmittedAttempts((int) submittedAttempts);

        // Get recent results
        List<ResultSummaryDTO> recentResults = new ArrayList<>();
        List<Result> allResults = resultRepository.findAll();
        allResults.sort((r1, r2) -> r2.getEvaluatedAt().compareTo(r1.getEvaluatedAt()));
        
        for (Result result : allResults.stream().limit(10).collect(Collectors.toList())) {
            if (exams.contains(result.getExamAttempt().getExam())) {
                ResultSummaryDTO resultSummary = new ResultSummaryDTO();
                resultSummary.setResultId(result.getId());
                resultSummary.setExamName(result.getExamAttempt().getExam().getName());
                resultSummary.setCandidateName(result.getExamAttempt().getCandidate().getName());
                resultSummary.setCandidateEmail(result.getExamAttempt().getCandidate().getEmail());
                resultSummary.setTotalQuestions(result.getTotalQuestions());
                resultSummary.setCorrectAnswers(result.getCorrectAnswers());
                resultSummary.setWrongAnswers(result.getWrongAnswers());
                resultSummary.setPercentage(result.getPercentage());
                resultSummary.setEvaluatedAt(result.getEvaluatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                recentResults.add(resultSummary);
            }
        }

        dashboard.setRecentResults(recentResults);

        return dashboard;
    }
}
