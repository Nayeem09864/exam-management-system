package com.exammanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "question_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private Integer optionIndex; // 0, 1, 2, 3... for ordering

    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Column(columnDefinition = "TEXT")
    private String optionImageUrl; // Optional image for option
}
