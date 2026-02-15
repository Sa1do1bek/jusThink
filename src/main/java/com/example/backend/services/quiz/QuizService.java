package com.example.backend.services.quiz;

import com.example.backend.enums.QuestionType;
import com.example.backend.enums.QuizMode;
import com.example.backend.enums.QuizStatus;
import com.example.backend.enums.Role;
import com.example.backend.exceptions.IllegalActionException;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.models.*;
import com.example.backend.repositories.ImageRepository;
import com.example.backend.repositories.QuizRepository;
import com.example.backend.requests.*;
import com.example.backend.responses.QuizResponse;
import com.example.backend.responses.QuizSafeResponse;
import com.example.backend.services.image.ImageStorageService;
import com.example.backend.services.session.SessionService;
import com.example.backend.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService implements IQuizService {

    private final UserService userService;
    private final ImageStorageService storage;
    private final QuizRepository quizRepository;
    private final SessionService sessionService;
    private final ImageRepository imageRepository;

    @Transactional(readOnly = true)
    public Object getQuizById(UUID id, String email) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found!"));

        boolean isOwner = userService.ownerChecker(email, quiz.getOwner().getId());
        boolean isPublic = quiz.getMode() == QuizMode.PUBLIC && quiz.getStatus() == QuizStatus.PUBLISHED;

        if (isOwner) return this.getQuizVersion(quiz);
        if (isPublic) return this.getQuizSafeResponse(quiz);

        throw new ResourceNotFoundException("Quiz not found!");
    }

    private QuizSafeResponse getQuizSafeResponse(Quiz quiz) {
        return this.converterToQuizSafeResponse(quiz);
    }

    private QuizResponse getQuizVersion(Quiz quiz) {
        return this.converterToQuizVersionResponse(quiz);
    }

    @Transactional(readOnly = true)
    public Quiz getQuizByIdForAdmin(UUID id, String email) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found!"));
    }

    @Transactional(readOnly = true)
    public List<Quiz> getAllQuizzesForUsers() {
        return quizRepository.findByStatus(QuizStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Object getQuizByIdAndMode(UUID id, QuizMode mode, String email) {
        if (mode.equals(QuizMode.PRIVATE)) throw new ResourceNotFoundException("Quiz not found!");
        Quiz quiz = quizRepository.findByModeAndId(mode, id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found!"));

        if (userService.ownerChecker(
                email, quiz.getOwner().getId()
        ))
            return this.getQuizVersion(quiz);
        if (quiz.getMode() == QuizMode.PUBLIC &&
                quiz.getStatus() == QuizStatus.PUBLISHED)
            return this.getQuizSafeResponse(quiz);
        throw new ResourceNotFoundException("Quiz not found!");
    }

    @Transactional
    public Quiz createQuiz(CreateQuizRequest request, String userEmail, MultipartFile imageFile) {
        Quiz quiz = new Quiz();
        quiz.setTitle(request.title());
        quiz.setDescription(request.description());
        quiz.setStatus(request.status() != null ? request.status() : QuizStatus.DRAFT);
        quiz.setMode(request.mode() != null ? request.mode() : QuizMode.PRIVATE);
        quiz.setOwner(userService.getUserByEmail(userEmail));

        QuizVersion quizVersion = new QuizVersion();
        quizVersion.setQuiz(quiz);
        quizVersion.setVersionId(1);

        List<Question> questions = request.questions().stream()
                .map(qReq -> createQuestion(qReq, quizVersion))
                .collect(Collectors.toList());
        if (validateQuestionOrders(questions))
            throw new IllegalActionException("Question orders are not correctly inputted!");

        if (imageFile != null && !imageFile.isEmpty()) {
            Image image = new Image();

            image.setFileName(imageFile.getOriginalFilename());
            image.setFileType(imageFile.getContentType());
            image.setPath(storage.save(quiz.getId(), imageFile));

            quiz.setImage(image);
        }

        quizVersion.setQuestions(questions);
        quiz.setQuizVersions(new ArrayList<>(List.of(quizVersion)));

        return quizRepository.save(quiz);
    }

    private Question createQuestion(CreateQuestionRequest request, QuizVersion quizVersion) {
        Question question = new Question();
        question.setQuizVersion(quizVersion);
        question.setText(request.text());
        question.setQuestionType(request.questionType());
        question.setScore(request.score());
        question.setTimeInSeconds(request.timeInSeconds());
        question.setOrderNumber(request.orderNumber());

        Set<QuestionOption> options = request.options().stream()
                .map(optReq -> createOption(optReq, question))
                .collect(Collectors.toSet());

        if (!validateOptions(question.getQuestionType(), options))
            throw new IllegalActionException("Question violates the rules!");

        validateSingleCorrectOption(options, question.getText());
        if (!validateOptionOrders(options))
            throw new IllegalActionException("Question option orders are not correctly inputted!");

        question.setQuestionOptions(options);
        return question;
    }

    private QuestionOption createOption(CreateQuestionOption optReq, Question question) {
        QuestionOption option = new QuestionOption();
        option.setQuestion(question);
        option.setText(optReq.text());
        option.setCorrect(optReq.isCorrect());
        option.setOptionOrder(optReq.optionOrder());
        return option;
    }

    private void validateSingleCorrectOption(Set<QuestionOption> options, String questionText) {
        long correctCount = options.stream().filter(QuestionOption::isCorrect).count();
        if (correctCount != 1) {
            throw new IllegalActionException(
                    "Question '" + questionText + "' must have exactly one correct option."
            );
        }
    }

    public boolean validateOptionOrders(Set<QuestionOption> options) {
        if (options == null || options.isEmpty()) return false;
        Set<Integer> orders = options.stream()
                .map(QuestionOption::getOptionOrder)
                .collect(Collectors.toSet());

        for (int i = 1; i <= options.size(); i++) {
            if (!orders.contains(i)) return false;
        }
        return true;
    }

    public boolean validateQuestionOrders(List<Question> questions) {
        if (questions == null || questions.isEmpty()) return false;

        Set<Integer> orders = questions.stream()
                .map(Question::getOrderNumber)
                .collect(Collectors.toSet());

        for (int i = 1; i <= questions.size(); i++) {
            if (!orders.contains(i)) return false;
        }

        return true;
    }

    public boolean validateOptions(
            QuestionType questionType,
            Set<QuestionOption> options
    ) {
        if (questionType.equals(QuestionType.TRUE_FALSE))
            if (options.size() != 2)
                throw new IllegalActionException("Question type is " + QuestionType.TRUE_FALSE +
                        " but a number of options is " + options.size() +
                        " which violates the rule of this type of question!");
        return true;
    }

    @Transactional
    public Quiz updateQuiz(UUID quizId, UpdateQuizRequest request, String email, MultipartFile imageFile) {
        Quiz oldQuiz = quizRepository.findById(quizId)
                .filter(q -> q.getOwner().getEmail().equals(email))
                .orElseThrow(() ->
                        new IllegalActionException("Current user cannot update this quiz!")
                );

        request.title().ifPresent(oldQuiz::setTitle);
        request.description().ifPresent(oldQuiz::setDescription);
        request.status().ifPresent(oldQuiz::setStatus);
        request.mode().ifPresent(oldQuiz::setMode);

        QuizVersion latestVersion = oldQuiz.getQuizVersions().stream()
                .max(Comparator.comparing(QuizVersion::getVersionId))
                .orElse(null);

        QuizVersion newVersion = new QuizVersion();
        newVersion.setQuiz(oldQuiz);
        newVersion.setVersionId(
                (latestVersion != null ? latestVersion.getVersionId() : 0) + 1
        );

        List<Question> mergedQuestions =
                mergeQuestions(request, latestVersion, newVersion);

        if (validateQuestionOrders(mergedQuestions))
            throw new IllegalActionException("Question orders are not correctly inputted!");

        if (imageFile != null && !imageFile.isEmpty()) {
            Image image = new Image();

            image.setFileName(imageFile.getOriginalFilename());
            image.setFileType(imageFile.getContentType());
            image.setPath(storage.save(oldQuiz.getId(), imageFile));

            oldQuiz.setImage(image);
        }

        newVersion.setQuestions(mergedQuestions);
        oldQuiz.getQuizVersions().add(newVersion);

        return quizRepository.save(oldQuiz);
    }

    private List<Question> mergeQuestions(
            UpdateQuizRequest request,
            QuizVersion latestVersion,
            QuizVersion newVersion
    ) {
        Map<UUID, Question> oldQuestionMap =
                latestVersion == null
                        ? Map.of()
                        : latestVersion.getQuestions().stream()
                        .collect(Collectors.toMap(Question::getId, q -> q));

        Set<UUID> touchedQuestionIds = new HashSet<>();
        List<Question> result = new ArrayList<>();

        if (request.questions().isPresent()) {
            for (UpdateQuestionRequest qReq : request.questions().get()) {
                Question question =
                        updateOrCloneQuestion(qReq, latestVersion, newVersion);

                qReq.id().ifPresent(touchedQuestionIds::add);
                result.add(question);
            }

            for (Question oldQ : oldQuestionMap.values()) {
                if (!touchedQuestionIds.contains(oldQ.getId())) {
                    result.add(cloneQuestion(oldQ, newVersion));
                }
            }

        } else if (latestVersion != null) {
            result = latestVersion.getQuestions().stream()
                    .map(q -> cloneQuestion(q, newVersion))
                    .collect(Collectors.toList());
        }

        return result;
    }

    private Question updateOrCloneQuestion(
            UpdateQuestionRequest request,
            QuizVersion latestVersion,
            QuizVersion newVersion
    ) {
        List<Question> oldQuestions = latestVersion != null
                ? latestVersion.getQuestions()
                : List.of();

        UUID qId = request.id().orElse(null);

        Question question = new Question();
        Set<QuestionOption> options;

        if (qId == null) {
            question.setQuizVersion(newVersion);

            question.setText(request.text().orElseThrow(
                    () -> new IllegalActionException("Question must have text!")
            ));

            question.setQuestionType(request.questionType().orElseThrow(
                    () -> new IllegalActionException("Question must have type !")
            ));

            question.setScore(request.score().orElseThrow(
                    () -> new IllegalActionException("Question must have score!")
            ));

            question.setTimeInSeconds(request.timeInSeconds().orElseThrow(
                    () -> new IllegalActionException("Question must have time!")
            ));

            options = request.options()
                    .map(optList -> optList.stream()
                            .map(opt -> updateOption(opt, question))
                            .collect(Collectors.toSet()))
                    .orElseThrow(
                            () -> new IllegalActionException("Question must have options!")
                    );

        } else {
            Question oldQuestion = oldQuestions.stream()
                    .filter(q -> q.getId().equals(qId))
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException("No matching old question found for ID: " + qId)
                    );

            question.setQuizVersion(newVersion);

            question.setText(request.text().orElse(oldQuestion.getText()));
            question.setQuestionType(request.questionType().orElse(oldQuestion.getQuestionType()));
            question.setScore(request.score().orElse(oldQuestion.getScore()));
            question.setTimeInSeconds(request.timeInSeconds().orElse(oldQuestion.getTimeInSeconds()));

            options = request.options()
                    .map(optList -> optList.stream()
                            .map(opt -> updateOption(opt, question))
                            .collect(Collectors.toSet()))
                    .orElseGet(() -> oldQuestion.getQuestionOptions().stream()
                            .map(opt -> cloneOption(opt, question))
                            .collect(Collectors.toSet()));
        }

        if (!validateOptions(question.getQuestionType(), options))
            throw new IllegalActionException("Question violates the rules!");

        if (!validateOptionOrders(options))
            throw new IllegalActionException("Question option orders are not correctly inputted!");

        question.setQuestionOptions(options);
        return question;
    }


    private QuestionOption updateOption(UpdateQuestionOption req, Question question) {
        QuestionOption option = new QuestionOption();
        option.setQuestion(question);
        req.text().ifPresent(option::setText);
        req.isCorrect().ifPresent(option::setCorrect);
        req.optionOrder().ifPresent(option::setOptionOrder);
        return option;
    }

    private Question cloneQuestion(Question oldQuestion, QuizVersion quizVersion) {
        Question clone = new Question();
        clone.setQuizVersion(quizVersion);
        clone.setText(oldQuestion.getText());
        clone.setQuestionType(oldQuestion.getQuestionType());
        clone.setScore(oldQuestion.getScore());
        clone.setTimeInSeconds(oldQuestion.getTimeInSeconds());

        Set<QuestionOption> clonedOptions = oldQuestion.getQuestionOptions().stream()
                .map(opt -> cloneOption(opt, clone))
                .collect(Collectors.toSet());

        clone.setQuestionOptions(clonedOptions);
        return clone;
    }

    private QuestionOption cloneOption(QuestionOption oldOption, Question question) {
        QuestionOption clone = new QuestionOption();
        clone.setQuestion(question);
        clone.setText(oldOption.getText());
        clone.setCorrect(oldOption.isCorrect());
        clone.setOptionOrder(oldOption.getOptionOrder());
        return clone;
    }

    @Transactional
    public void deleteQuiz(UUID quizId, String email) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found!"));

        if (!quiz.getOwner().getEmail().equals(email) && !quiz.getOwner().getRole().getName().equals(Role.ADMIN.name())) {
            throw new IllegalActionException("Current user cannot delete this quiz!");
        }

        quiz.setStatus(QuizStatus.ARCHIVED);
    }

    public QuizResponse converterToQuizVersionResponse(Quiz quiz) {
        QuizVersion latestVersion = quiz.getQuizVersions().stream()
                .max(Comparator.comparing(QuizVersion::getVersionId))
                .orElse(null);

        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                userService.converterToUserResponse(quiz.getOwner()),
                quiz.getCreatedAt(),
                sessionService.converterToQuizVersionResponse(latestVersion)
        );
    }

    public QuizSafeResponse converterToQuizSafeResponse(Quiz quiz) {
        return new QuizSafeResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                userService.converterToUserResponse(quiz.getOwner()),
                quiz.getCreatedAt()
        );
    }

    public void deleteImageByQuizId(UUID quizId, String email) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        UserModel currentUser = userService.getUserByEmail(email);
        if ((!quiz.getOwner().getId().equals(currentUser.getId())) && (!currentUser.getRole().getName().equals(Role.ADMIN.name())))
            try {
                throw new IllegalAccessException("Current user cannot access to this action!");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        Image image = quiz.getImage();
        if (image == null) return;

        storage.delete(image.getPath());
        imageRepository.delete(image);
    }
}