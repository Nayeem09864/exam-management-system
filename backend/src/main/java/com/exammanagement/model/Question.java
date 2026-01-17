package com.exammanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(columnDefinition = "TEXT")
    private String paragraph; // Optional paragraph

    @Column(columnDefinition = "TEXT")
    private String imageUrl; // Optional image URL

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionOption> options = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "question_correct_answers", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_index")
    private List<Integer> correctAnswerIndices = new ArrayList<>(); // For multiple correct answers

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Column(nullable = false)
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String solution; // Visible only to examiners

    @Column(columnDefinition = "TEXT")
    private String explanation; // Visible only to examiners

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
