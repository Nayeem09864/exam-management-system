package com.exammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultDTO {
    private Long id;
    private Long examAttemptId;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Double percentage;
    private String evaluatedAt;
}
