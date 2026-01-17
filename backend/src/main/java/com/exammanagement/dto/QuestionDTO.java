package com.exammanagement.dto;

import com.exammanagement.model.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private Long id;

    @NotBlank(message = "Question text is required")
    private String questionText;

    private String paragraph;

    private String imageUrl;

    @NotNull(message = "At least 2 options are required")
    @Size(min = 2, message = "At least 2 options are required")
    private List<QuestionOptionDTO> options = new ArrayList<>();

    @NotNull(message = "At least one correct answer is required")
    @Size(min = 1, message = "At least one correct answer is required")
    private List<Integer> correctAnswerIndices = new ArrayList<>();

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficultyLevel;

    @NotBlank(message = "Topic is required")
    private String topic;

    private String solution;

    private String explanation;

    private String createdBy;

    private String createdAt;

    private String updatedAt;
}
