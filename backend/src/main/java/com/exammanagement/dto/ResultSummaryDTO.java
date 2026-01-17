package com.exammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultSummaryDTO {
    private Long resultId;
    private String examName;
    private String candidateName;
    private String candidateEmail;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Double percentage;
    private String evaluatedAt;
}
