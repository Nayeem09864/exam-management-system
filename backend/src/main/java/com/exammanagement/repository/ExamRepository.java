package com.exammanagement.repository;

import com.exammanagement.model.Exam;
import com.exammanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    Optional<Exam> findByAccessCode(String accessCode);
    List<Exam> findByCreatedBy(User createdBy);
    List<Exam> findByIsActive(Boolean isActive);
}
