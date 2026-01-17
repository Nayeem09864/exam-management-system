package com.exammanagement.repository;

import com.exammanagement.model.Exam;
import com.exammanagement.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByExamAttempt_Exam(Exam exam);
}
