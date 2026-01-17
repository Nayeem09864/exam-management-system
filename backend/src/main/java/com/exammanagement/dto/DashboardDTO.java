package com.exammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private Integer totalExams;
    private Integer totalCandidates;
    private Integer totalQuestions;
    private Integer totalAttempts;
    private Integer submittedAttempts;
    private List<ExamSummaryDTO> exams = new ArrayList<>();
    private List<ResultSummaryDTO> recentResults = new ArrayList<>();
}
