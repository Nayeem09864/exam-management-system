package com.exammanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exam_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private Integer timeRemainingSeconds; // Time remaining when started

    @Column(nullable = false)
    private Boolean isSubmitted = false;

    @Column(nullable = false)
    private Boolean isAutoSubmitted = false; // Auto-submitted due to timeout

    // Randomized question order for this attempt
    @OneToMany(mappedBy = "examAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionOrder ASC")
    private List<AttemptAnswer> answers = new ArrayList<>();

    @OneToOne(mappedBy = "examAttempt", cascade = CascadeType.ALL)
    private Result result;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
