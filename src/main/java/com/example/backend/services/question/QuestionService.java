package com.example.backend.services.question;

import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.models.Player;
import com.example.backend.models.PlayerAnswer;
import com.example.backend.models.Question;
import com.example.backend.models.QuestionOption;
import com.example.backend.repositories.QuestionRepository;
import com.example.backend.responses.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionService implements IQuestionService{


    private final QuestionRepository questionRepository;

    @Override
    public List<UUID> getAllQuestionIdsByQuizVersion(UUID quizVersionId) {
        return questionRepository.findAllByQuizVersion_Id(quizVersionId).stream().map(Question::getId).toList();
    }


    @Override
    public Question getQuestionById(UUID questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found!"));
    }

    public QuestionResponse converterToQuestionSafeResponse(Question question) {
        List<QuestionOptionResponse> responseList = question.getQuestionOptions()
                .stream()
                .map(this::converterToQuestionOptionResponse)
                .toList();

        return new QuestionResponse(
                question.getId(),
                question.getText(),
                question.getScore(),
                question.getTimeInSeconds(),
                question.getQuestionType(),
                responseList
        );
    }

    public QuestionForOwners converterToQuestionResponse(Question question) {
        List<OptionsForOwners> responseList = question.getQuestionOptions()
                .stream()
                .map(this::converterToOptionForOwner)
                .toList();

        return new QuestionForOwners(
                question.getId(),
                question.getText(),
                question.getScore(),
                question.getTimeInSeconds(),
                question.getQuestionType(),
                responseList
        );
    }

    public QuestionOptionResponse converterToQuestionOptionResponse(QuestionOption questionOption) {
        return new QuestionOptionResponse(
                questionOption.getId(),
                questionOption.getOptionOrder(),
                questionOption.getText()
        );
    }

    public OptionsForOwners converterToOptionForOwner(QuestionOption questionOption) {
        return new OptionsForOwners(
                questionOption.getId(),
                questionOption.getOptionOrder(),
                questionOption.getText(),
                questionOption.isCorrect()
        );
    }

    public PlayerAnswerResponse converterToPlayerAnswerResponse(PlayerAnswer playerAnswer) {
        return new PlayerAnswerResponse(
                playerAnswer.getOption().isCorrect(),
                playerAnswer.getOption().getQuestion().getScore()
        );
    }

    public PlayerResponse converterToPlayerResponse(Player player) {
        List<PlayerAnswerResponse> responseList = player.getPlayerAnswers() != null ?
                player.getPlayerAnswers()
                        .stream()
                        .map(this::converterToPlayerAnswerResponse)
                        .toList() : null;

        return new PlayerResponse(
                player.getId(),
                player.getNickName(),
                player.getCreatedAt(),
                responseList
        );
    }
}
