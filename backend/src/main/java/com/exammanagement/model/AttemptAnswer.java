package com.exammanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attempt_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttemptAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_attempt_id", nullable = false)
    private ExamAttempt examAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private Integer questionOrder; // Order of question in this attempt (randomized)

    @ElementCollection
    @CollectionTable(name = "attempt_selected_options", joinColumns = @JoinColumn(name = "attempt_answer_id"))
    @Column(name = "option_index")
    private java.util.List<Integer> selectedOptionIndices = new java.util.ArrayList<>(); // Selected options by index
}
