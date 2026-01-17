package com.exammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitExamDTO {
    private Long attemptId;
    private List<AnswerSubmissionDTO> answers = new ArrayList<>();
}
