package com.exammanagement.service;

import com.exammanagement.dto.QuestionDTO;
import com.exammanagement.dto.QuestionOptionDTO;
import com.exammanagement.model.DifficultyLevel;
import com.exammanagement.model.Question;
import com.exammanagement.model.QuestionOption;
import com.exammanagement.model.User;
import com.exammanagement.repository.QuestionRepository;
import com.exammanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public QuestionDTO createQuestion(QuestionDTO questionDTO, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = new Question();
        question.setQuestionText(questionDTO.getQuestionText());
        question.setParagraph(questionDTO.getParagraph());
        question.setImageUrl(questionDTO.getImageUrl());
        question.setDifficultyLevel(questionDTO.getDifficultyLevel());
        question.setTopic(questionDTO.getTopic());
        question.setSolution(questionDTO.getSolution());
        question.setExplanation(questionDTO.getExplanation());
        question.setCreatedBy(user);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());

        // Set correct answer indices
        question.setCorrectAnswerIndices(questionDTO.getCorrectAnswerIndices());

        // Create options
        for (int i = 0; i < questionDTO.getOptions().size(); i++) {
            QuestionOptionDTO optionDTO = questionDTO.getOptions().get(i);
            QuestionOption option = new QuestionOption();
            option.setQuestion(question);
            option.setOptionIndex(i);
            option.setOptionText(optionDTO.getOptionText());
            option.setOptionImageUrl(optionDTO.getOptionImageUrl());
            question.getOptions().add(option);
        }

        Question savedQuestion = questionRepository.save(question);
        return convertToDTO(savedQuestion);
    }

    public QuestionDTO updateQuestion(Long id, QuestionDTO questionDTO) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        question.setQuestionText(questionDTO.getQuestionText());
        question.setParagraph(questionDTO.getParagraph());
        question.setImageUrl(questionDTO.getImageUrl());
        question.setDifficultyLevel(questionDTO.getDifficultyLevel());
        question.setTopic(questionDTO.getTopic());
        question.setSolution(questionDTO.getSolution());
        question.setExplanation(questionDTO.getExplanation());

        // Update correct answer indices
        question.setCorrectAnswerIndices(questionDTO.getCorrectAnswerIndices());

        // Clear existing options and add new ones
        question.getOptions().clear();
        for (int i = 0; i < questionDTO.getOptions().size(); i++) {
            QuestionOptionDTO optionDTO = questionDTO.getOptions().get(i);
            QuestionOption option = new QuestionOption();
            option.setQuestion(question);
            option.setOptionIndex(i);
            option.setOptionText(optionDTO.getOptionText());
            option.setOptionImageUrl(optionDTO.getOptionImageUrl());
            question.getOptions().add(option);
        }

        Question updatedQuestion = questionRepository.save(question);
        return convertToDTO(updatedQuestion);
    }

    public QuestionDTO getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        return convertToDTO(question);
    }

    public List<QuestionDTO> getAllQuestions() {
        return questionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<QuestionDTO> filterQuestions(DifficultyLevel difficulty, String topic, String startDate) {
        List<Question> questions;
        
        if (difficulty != null && topic != null && !topic.isEmpty()) {
            questions = questionRepository.findByDifficultyAndTopic(difficulty, topic);
        } else if (difficulty != null && startDate != null && !startDate.isEmpty()) {
            LocalDateTime date = LocalDateTime.parse(startDate + " 00:00:00", FORMATTER);
            questions = questionRepository.findByDifficultyAndDate(difficulty, date);
        } else if (difficulty != null) {
            questions = questionRepository.findByDifficultyLevel(difficulty);
        } else if (topic != null && !topic.isEmpty()) {
            questions = questionRepository.findByTopic(topic);
        } else if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime date = LocalDateTime.parse(startDate + " 00:00:00", FORMATTER);
            questions = questionRepository.findByCreatedAtBetween(date, LocalDateTime.now());
        } else {
            questions = questionRepository.findAll();
        }

        return questions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("Question not found");
        }
        questionRepository.deleteById(id);
    }

    private QuestionDTO convertToDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setParagraph(question.getParagraph());
        dto.setImageUrl(question.getImageUrl());
        dto.setDifficultyLevel(question.getDifficultyLevel());
        dto.setTopic(question.getTopic());
        dto.setSolution(question.getSolution());
        dto.setExplanation(question.getExplanation());
        dto.setCorrectAnswerIndices(question.getCorrectAnswerIndices());

        // Convert options
        List<QuestionOptionDTO> optionDTOs = question.getOptions().stream()
                .sorted((o1, o2) -> o1.getOptionIndex().compareTo(o2.getOptionIndex()))
                .map(option -> {
                    QuestionOptionDTO optionDTO = new QuestionOptionDTO();
                    optionDTO.setOptionIndex(option.getOptionIndex());
                    optionDTO.setOptionText(option.getOptionText());
                    optionDTO.setOptionImageUrl(option.getOptionImageUrl());
                    return optionDTO;
                })
                .collect(Collectors.toList());
        dto.setOptions(optionDTOs);

        if (question.getCreatedBy() != null) {
            dto.setCreatedBy(question.getCreatedBy().getUsername());
        }
        if (question.getCreatedAt() != null) {
            dto.setCreatedAt(question.getCreatedAt().format(FORMATTER));
        }
        if (question.getUpdatedAt() != null) {
            dto.setUpdatedAt(question.getUpdatedAt().format(FORMATTER));
        }

        return dto;
    }
}
