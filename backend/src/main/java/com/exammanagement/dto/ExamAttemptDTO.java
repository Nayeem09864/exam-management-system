package com.exammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamAttemptDTO {
    private Long id;
    private Long examId;
    private String examName;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String startedAt;
    private String submittedAt;
    private Integer timeRemainingSeconds;
    private Boolean isSubmitted;
    private Boolean isAutoSubmitted;
    private List<AttemptAnswerDTO> answers = new ArrayList<>();
    private ResultDTO result;
}
