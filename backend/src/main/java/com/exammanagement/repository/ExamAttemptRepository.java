package com.exammanagement.repository;

import com.exammanagement.model.Candidate;
import com.exammanagement.model.Exam;
import com.exammanagement.model.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    List<ExamAttempt> findByExam(Exam exam);
    List<ExamAttempt> findByCandidate(Candidate candidate);
    Optional<ExamAttempt> findByExamAndCandidate(Exam exam, Candidate candidate);
    List<ExamAttempt> findByIsSubmitted(Boolean isSubmitted);
}
