package com.exammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamSummaryDTO {
    private Long examId;
    private String examName;
    private String accessCode;
    private Integer totalQuestions;
    private Integer durationMinutes;
    private Integer totalAttempts;
    private Integer submittedAttempts;
    private Integer totalCandidates;
    private Double averageScore;
    private Boolean isActive;
    private String createdAt;
}
