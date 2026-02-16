package com.example.backend.quizTests;

import com.example.backend.enums.QuestionType;
import com.example.backend.enums.QuizMode;
import com.example.backend.enums.QuizStatus;
import com.example.backend.exceptions.IllegalActionException;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.models.Question;
import com.example.backend.models.QuestionOption;
import com.example.backend.models.Quiz;
import com.example.backend.models.UserModel;
import com.example.backend.repositories.ImageRepository;
import com.example.backend.repositories.QuizRepository;
import com.example.backend.requests.CreateQuestionOption;
import com.example.backend.requests.CreateQuestionRequest;
import com.example.backend.requests.CreateQuizRequest;
import com.example.backend.responses.QuizResponse;
import com.example.backend.responses.QuizSafeResponse;
import com.example.backend.services.image.ImageStorageService;
import com.example.backend.services.quiz.QuizService;
import com.example.backend.services.session.SessionService;
import com.example.backend.services.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import com.example.backend.models.*;
import org.mockito.*;

import java.util.*;

/**
 * Tests for QuizService using the real request DTOs from com.example.backend.requests.
 * Adjust imports if your DTOs are in a different package.
 */
@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock private UserService userService;
    @Mock private ImageStorageService storage;
    @Mock private QuizRepository quizRepository;
    @Mock private SessionService sessionService;
    @Mock private ImageRepository imageRepository;

    @InjectMocks private QuizService quizService;

    private UserModel owner;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        owner = new UserModel();
        owner.setId(UUID.randomUUID());
        owner.setEmail("owner@test.com");

        quiz = new Quiz();
        quiz.setId(UUID.randomUUID());
        quiz.setOwner(owner);
        quiz.setMode(QuizMode.PUBLIC);
        quiz.setStatus(QuizStatus.PUBLISHED);
        quiz.setTitle("Sample Quiz");
    }

    // -------------------- getQuizById --------------------

    @Test
    void getQuizById_whenOwner_returnsNonNull() {
        when(quizRepository.findById(quiz.getId())).thenReturn(Optional.of(quiz));
        when(userService.ownerChecker("owner@test.com", owner.getId())).thenReturn(true);

        Object result = quizService.getQuizById(quiz.getId(), "owner@test.com");

        assertNotNull(result);
        verify(quizRepository, times(1)).findById(quiz.getId());
        verify(userService, times(1)).ownerChecker("owner@test.com", owner.getId());
    }

    @Test
    void getQuizById_whenPublicAndNotOwner_returnsNonNull() {
        when(quizRepository.findById(quiz.getId())).thenReturn(Optional.of(quiz));
        when(userService.ownerChecker("other@test.com", owner.getId())).thenReturn(false);

        Object result = quizService.getQuizById(quiz.getId(), "other@test.com");

        assertNotNull(result);
        verify(quizRepository, times(1)).findById(quiz.getId());
        verify(userService, times(1)).ownerChecker("other@test.com", owner.getId());
    }

    @Test
    void getQuizById_whenPrivateAndNotOwner_throwsResourceNotFound() {
        quiz.setMode(QuizMode.PRIVATE);
        quiz.setStatus(QuizStatus.DRAFT);

        when(quizRepository.findById(quiz.getId())).thenReturn(Optional.of(quiz));
        when(userService.ownerChecker("other@test.com", owner.getId())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> quizService.getQuizById(quiz.getId(), "other@test.com"));

        verify(quizRepository, times(1)).findById(quiz.getId());
    }

    @Test
    void getQuizById_whenNotFound_throwsResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(quizRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> quizService.getQuizById(id, "any@test.com"));

        verify(quizRepository, times(1)).findById(id);
    }

    // -------------------- getQuizByIdAndMode --------------------

    @Test
    void getQuizByIdAndMode_whenModePrivate_throwsResourceNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> quizService.getQuizByIdAndMode(UUID.randomUUID(), QuizMode.PRIVATE, "any@test.com"));
    }

    @Test
    void getQuizByIdAndMode_whenPublicAndOwner_returnsNonNull() {
        when(quizRepository.findByModeAndId(QuizMode.PUBLIC, quiz.getId())).thenReturn(Optional.of(quiz));
        when(userService.ownerChecker("owner@test.com", owner.getId())).thenReturn(true);

        Object result = quizService.getQuizByIdAndMode(quiz.getId(), QuizMode.PUBLIC, "owner@test.com");

        assertNotNull(result);
        verify(quizRepository, times(1)).findByModeAndId(QuizMode.PUBLIC, quiz.getId());
    }

    @Test
    void getQuizByIdAndMode_whenPublicAndNotOwner_returnsNonNull() {
        when(quizRepository.findByModeAndId(QuizMode.PUBLIC, quiz.getId())).thenReturn(Optional.of(quiz));
        when(userService.ownerChecker("other@test.com", owner.getId())).thenReturn(false);

        Object result = quizService.getQuizByIdAndMode(quiz.getId(), QuizMode.PUBLIC, "other@test.com");

        assertNotNull(result);
        verify(quizRepository, times(1)).findByModeAndId(QuizMode.PUBLIC, quiz.getId());
    }

    @Test
    void getQuizByIdAndMode_whenNotFound_throwsResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(quizRepository.findByModeAndId(QuizMode.PUBLIC, id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> quizService.getQuizByIdAndMode(id, QuizMode.PUBLIC, "any@test.com"));

        verify(quizRepository, times(1)).findByModeAndId(QuizMode.PUBLIC, id);
    }

    // -------------------- createQuiz --------------------

    @Test
    void createQuiz_withValidRequest_savesQuizAndSetsImage() throws Exception {
        // Build request with one question and two options (one correct)
        CreateQuestionOption opt1 = new CreateQuestionOption("A", true, 1);
        CreateQuestionOption opt2 = new CreateQuestionOption("B", false, 2);
        CreateQuestionRequest qReq = new CreateQuestionRequest(
                "What is A?",
                List.of(opt1, opt2),
                QuestionType.SINGLE_CHOICE,
                10,
                30,
                1
        );
        CreateQuizRequest request = new CreateQuizRequest(
                "Title",
                "Description",
                QuizStatus.DRAFT,
                QuizMode.PRIVATE,
                List.of(qReq)
        );

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("image.png");
        when(file.getContentType()).thenReturn("image/png");
        when(storage.save(any(), eq(file))).thenReturn("images/123.png");

        when(userService.getUserByEmail("owner@test.com")).thenReturn(owner);
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> {
            Quiz toSave = invocation.getArgument(0);
            if (toSave.getId() == null) toSave.setId(UUID.randomUUID());
            return toSave;
        });

        Quiz saved = quizService.createQuiz(request, "owner@test.com", file);

        assertNotNull(saved);
        assertEquals("Title", saved.getTitle());
        assertEquals(owner, saved.getOwner());
        assertNotNull(saved.getQuizVersions());
        assertFalse(saved.getQuizVersions().isEmpty());
        assertNotNull(saved.getImage());
        assertEquals("image.png", saved.getImage().getFileName());
        verify(storage, times(1)).save(any(), eq(file));
        verify(quizRepository, times(1)).save(any(Quiz.class));
    }

    @Test
    void createQuiz_withInvalidQuestionOrders_throwsIllegalActionException() {
        // Two questions with orders 2 and 3 (invalid)
        CreateQuestionRequest q1 = new CreateQuestionRequest(
                "Q1", List.of(new CreateQuestionOption("A", true, 1), new CreateQuestionOption("B", false, 2)),
                QuestionType.SINGLE_CHOICE, 5, 10, 2
        );
        CreateQuestionRequest q2 = new CreateQuestionRequest(
                "Q2", List.of(new CreateQuestionOption("C", true, 1), new CreateQuestionOption("D", false, 2)),
                QuestionType.SINGLE_CHOICE, 5, 10, 3
        );
        CreateQuizRequest request = new CreateQuizRequest(
                "Bad Orders",
                "desc",
                QuizStatus.DRAFT,
                QuizMode.PRIVATE,
                List.of(q1, q2)
        );

        when(userService.getUserByEmail("owner@test.com")).thenReturn(owner);

        assertThrows(IllegalActionException.class,
                () -> quizService.createQuiz(request, "owner@test.com", null));

        verify(quizRepository, never()).save(any());
    }

    @Test
    void createQuiz_withMultipleCorrectOptions_throwsIllegalActionException() {
        CreateQuestionRequest qReq = new CreateQuestionRequest(
                "Q1",
                List.of(new CreateQuestionOption("A", true, 1), new CreateQuestionOption("B", true, 2)),
                QuestionType.SINGLE_CHOICE,
                5,
                10,
                1
        );
        CreateQuizRequest request = new CreateQuizRequest(
                "Multiple Correct",
                "desc",
                QuizStatus.DRAFT,
                QuizMode.PRIVATE,
                List.of(qReq)
        );

        when(userService.getUserByEmail("owner@test.com")).thenReturn(owner);

        assertThrows(IllegalActionException.class,
                () -> quizService.createQuiz(request, "owner@test.com", null));

        verify(quizRepository, never()).save(any());
    }

    // -------------------- validation helpers --------------------

    @Test
    void validateOptionOrders_validSequence_returnsTrue() {
        QuestionOption o1 = new QuestionOption();
        o1.setOptionOrder(1);
        o1.setText("A");
        o1.setCorrect(true);

        QuestionOption o2 = new QuestionOption();
        o2.setOptionOrder(2);
        o2.setText("B");
        o2.setCorrect(false);

        Set<QuestionOption> options = new HashSet<>(List.of(o1, o2));
        assertTrue(quizService.validateOptionOrders(options));
    }

    @Test
    void validateOptionOrders_missingSequence_returnsFalse() {
        QuestionOption o1 = new QuestionOption();
        o1.setOptionOrder(2);
        o1.setText("A");
        o1.setCorrect(true);

        QuestionOption o2 = new QuestionOption();
        o2.setOptionOrder(3);
        o2.setText("B");
        o2.setCorrect(false);

        Set<QuestionOption> options = new HashSet<>(List.of(o1, o2));
        assertFalse(quizService.validateOptionOrders(options));
    }

    @Test
    void validateQuestionOrders_validSequence_returnsTrue() {
        Question q1 = new Question();
        q1.setOrderNumber(1);
        q1.setText("Q1");

        Question q2 = new Question();
        q2.setOrderNumber(2);
        q2.setText("Q2");

        List<Question> questions = List.of(q1, q2);
        assertTrue(quizService.validateQuestionOrders(questions));
    }

    @Test
    void validateQuestionOrders_missingSequence_returnsFalse() {
        Question q1 = new Question();
        q1.setOrderNumber(2);
        q1.setText("Q1");

        Question q2 = new Question();
        q2.setOrderNumber(3);
        q2.setText("Q2");

        List<Question> questions = List.of(q1, q2);
        assertFalse(quizService.validateQuestionOrders(questions));
    }

    @Test
    void validateOptions_trueFalseWithTwoOptions_returnsTrue() {
        QuestionOption t = new QuestionOption();
        t.setOptionOrder(1);
        t.setText("True");
        t.setCorrect(true);

        QuestionOption f = new QuestionOption();
        f.setOptionOrder(2);
        f.setText("False");
        f.setCorrect(false);

        Set<QuestionOption> options = new HashSet<>(List.of(t, f));
        assertTrue(quizService.validateOptions(QuestionType.TRUE_FALSE, options));
    }

    @Test
    void validateOptions_trueFalseWithOneOption_throwsIllegalActionException() {
        QuestionOption t = new QuestionOption();
        t.setOptionOrder(1);
        t.setText("True");
        t.setCorrect(true);

        Set<QuestionOption> options = new HashSet<>(List.of(t));
        assertThrows(IllegalActionException.class,
                () -> quizService.validateOptions(QuestionType.TRUE_FALSE, options));
    }
}
