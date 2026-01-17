package com.exammanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer durationMinutes; // Exam duration in minutes

    @Column(nullable = false)
    private Integer totalQuestions; // Number of questions in exam

    // Difficulty distribution
    @Column(nullable = false)
    private Integer easyQuestions = 0;

    @Column(nullable = false)
    private Integer mediumQuestions = 0;

    @Column(nullable = false)
    private Integer hardQuestions = 0;

    @Column(nullable = false, unique = true)
    private String accessCode; // Unique exam access code (e.g., CODE1234)

    @ManyToMany
    @JoinTable(
        name = "exam_questions",
        joinColumns = @JoinColumn(name = "exam_id"),
        inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions = new ArrayList<>(); // Questions selected for this exam

    @ManyToMany
    @JoinTable(
        name = "exam_candidates",
        joinColumns = @JoinColumn(name = "exam_id"),
        inverseJoinColumns = @JoinColumn(name = "candidate_id")
    )
    private List<Candidate> candidates = new ArrayList<>(); // Candidates invited to this exam

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamAttempt> attempts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy; // Examiner who created the exam

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column
    private LocalDateTime startDate; // Optional: When exam becomes available

    @Column
    private LocalDateTime endDate; // Optional: When exam expires

    @Column(nullable = false)
    private Boolean isActive = true;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
