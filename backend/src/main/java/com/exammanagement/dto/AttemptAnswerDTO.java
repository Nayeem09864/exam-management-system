package com.exammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttemptAnswerDTO {
    private Long id;
    private Long questionId;
    private String questionText;
    private String paragraph;
    private String imageUrl;
    private Integer questionOrder;
    private List<QuestionOptionDTO> options = new ArrayList<>();
    private List<Integer> selectedOptionIndices = new ArrayList<>();
    private List<Integer> correctAnswerIndices = new ArrayList<>(); // Only visible to examiners
}
