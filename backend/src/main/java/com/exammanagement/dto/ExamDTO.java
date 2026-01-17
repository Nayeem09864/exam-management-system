package com.exammanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamDTO {
    private Long id;

    @NotBlank(message = "Exam name is required")
    private String name;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @NotNull(message = "Total questions is required")
    @Min(value = 1, message = "At least 1 question is required")
    private Integer totalQuestions;

    @NotNull(message = "Easy questions count is required")
    private Integer easyQuestions = 0;

    @NotNull(message = "Medium questions count is required")
    private Integer mediumQuestions = 0;

    @NotNull(message = "Hard questions count is required")
    private Integer hardQuestions = 0;

    private String accessCode;

    private List<Long> questionIds = new ArrayList<>(); // For creating exam with specific questions
    private List<Long> candidateIds = new ArrayList<>(); // For inviting candidates

    private String createdBy;
    private String createdAt;
    private String updatedAt;
    private String startDate;
    private String endDate;
    private Boolean isActive = true;
    
    // Additional info
    private Integer totalAttempts;
    private Integer submittedAttempts;
}
