package com.exammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerSubmissionDTO {
    private Long questionId;
    private List<Integer> selectedOptionIndices = new ArrayList<>();
}
