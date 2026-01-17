package com.exammanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "exam_attempt_id", nullable = false, unique = true)
    private ExamAttempt examAttempt;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Integer correctAnswers;

    @Column(nullable = false)
    private Integer wrongAnswers;

    @Column(nullable = false)
    private Double percentage; // Score percentage

    @Column(nullable = false)
    private LocalDateTime evaluatedAt = LocalDateTime.now();

    @Column
    private Boolean resultEmailed = false; // For bonus feature: email notification
}
