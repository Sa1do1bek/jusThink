package com.example.backend.repositories;

import com.example.backend.models.QuizVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizVersionRepository extends JpaRepository<QuizVersion, UUID> {
    List<QuizVersion> findByQuizId(UUID quizId);
}
