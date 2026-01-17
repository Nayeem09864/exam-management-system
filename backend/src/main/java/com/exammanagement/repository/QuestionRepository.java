package com.exammanagement.repository;

import com.exammanagement.model.DifficultyLevel;
import com.exammanagement.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByDifficultyLevel(DifficultyLevel difficultyLevel);
    List<Question> findByTopic(String topic);
    List<Question> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT q FROM Question q WHERE q.difficultyLevel = :difficulty AND q.topic = :topic")
    List<Question> findByDifficultyAndTopic(@Param("difficulty") DifficultyLevel difficulty, @Param("topic") String topic);
    
    @Query("SELECT q FROM Question q WHERE q.difficultyLevel = :difficulty AND q.createdAt >= :startDate")
    List<Question> findByDifficultyAndDate(@Param("difficulty") DifficultyLevel difficulty, @Param("startDate") LocalDateTime startDate);
    
    List<Question> findByCreatedByUsername(String username);
}
