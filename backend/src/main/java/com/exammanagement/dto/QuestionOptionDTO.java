package com.exammanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionDTO {
    private Integer optionIndex;

    @NotBlank(message = "Option text is required")
    private String optionText;

    private String optionImageUrl;
}
