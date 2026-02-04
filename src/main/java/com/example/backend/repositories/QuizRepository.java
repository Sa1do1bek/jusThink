package com.example.backend.repositories;

import com.example.backend.enums.QuizMode;
import com.example.backend.enums.QuizStatus;
import com.example.backend.models.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    List<Quiz> findByStatus(QuizStatus status);
    Optional<Quiz> findByModeAndId(QuizMode mode, UUID id);
}
