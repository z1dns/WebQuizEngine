package engine.businesslayer;

import engine.businesslayer.exceptions.ForbiddenActionException;
import engine.businesslayer.exceptions.QuizNotFoundException;
import engine.persistence.*;
import engine.presentation.AnswerDTO;
import engine.presentation.AnswerRequest;
import engine.presentation.QuestionRequest;
import engine.presentation.QuestionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
public class WebQuizService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebQuizService.class);
    private static final AnswerDTO CORRECT_ANSWER = new AnswerDTO(true, "Congratulations, you're right!");
    private static final AnswerDTO WRONG_ANSWER = new AnswerDTO(false, "Wrong answer! Please, try again.");

    private final QuestionMapper questionMapper;
    private final QuestionRepository questionRepository;
    private final AppUserRepository appUserRepository;
    private final CompletedQuizRepository completedQuizRepository;

    public WebQuizService(QuestionMapper questionMapper,
                          QuestionRepository questionRepository,
                          AppUserRepository appUserRepository,
                          CompletedQuizRepository completedQuizRepository) {
        this.questionMapper = questionMapper;
        this.questionRepository = questionRepository;
        this.appUserRepository = appUserRepository;
        this.completedQuizRepository = completedQuizRepository;
    }

    public QuestionResponse addNewQuiz(UserDetails user, QuestionRequest questionRequest) {
        LOGGER.info("New questionRequest {} for user {}", questionRequest, user);
        Optional<AppUser> optionalUser = appUserRepository.findAppUserByUsername(user.getUsername());
        AppUser appUser = optionalUser.orElseThrow(() -> new RuntimeException("Unknown user " + user));
        Question question = new Question(
                appUser,
                questionRequest.getTitle(),
                questionRequest.getText(),
                questionRequest.getOptions(),
                questionRequest.getAnswer() == null ? new ArrayList<>() : questionRequest.getAnswer());
        questionRepository.save(question);
        LOGGER.info("Successfully added new question {}", question);
        return questionMapper.convertQuestionToResponse(question);
    }

    public Optional<QuestionResponse> getQuizById(Long id) {
        Optional<Question> question = questionRepository.findById(id);
        LOGGER.info("Get question{} by id{}", question, id);
        return question.map(questionMapper::convertQuestionToResponse);
    }

    public List<QuestionResponse> getAllQuizzes() {
        LOGGER.info("All quizzes requested");
        return StreamSupport.stream(questionRepository.findAll().spliterator(), false)
                .map(questionMapper::convertQuestionToResponse)
                .toList();
    }

    public Page<QuestionView> getQuizzesByPage(Integer page) {
        return questionRepository.findAllBy(PageRequest.of(page, 10));
    }

    public Page<CompletedQuizView> getCompletedQuizzes(UserDetails user, Integer page) {
        Optional<AppUser> appUser = appUserRepository.findAppUserByUsername(user.getUsername());
        Long userId = appUser.isPresent() ? appUser.get().getId() : 0L;
        return completedQuizRepository.findAllByUserId(userId, PageRequest.of(page, 10));
    }

    public Optional<AnswerDTO> getAnswer(UserDetails user, Long questionId, AnswerRequest answer) {
        LOGGER.info("Requested answer {} for question with id:{}", answer, questionId);
        Optional<Question> question = questionRepository.findById(questionId);
        Optional<AppUser> appUser = appUserRepository.findAppUserByUsername(user.getUsername());
        LOGGER.info("Answer{} for question{} by id{}", answer.getAnswer(), question, questionId);
        if (question.isEmpty() || appUser.isEmpty()) {
            return Optional.empty();
        }
        boolean isCorrectAnswer = answer.getAnswer().equals(question.get().getAnswers());
        if (isCorrectAnswer) {
            completedQuizRepository.save(new CompletedQuiz(question.get(), appUser.get(), LocalDateTime.now()));
            return Optional.of(CORRECT_ANSWER);
        } else {
            return Optional.of(WRONG_ANSWER);
        }
    }

    public void deleteQuiz(UserDetails user, Long questionId) throws ForbiddenActionException {
        Optional<Question> question = questionRepository.findById(questionId);
        LOGGER.info("User {} attempts to delete question {} with id:{}", user, question, questionId);
        if (question.isEmpty()) {
            throw new QuizNotFoundException("There is no quiz with id:" + questionId);
        }
        Optional<AppUser> appUser = appUserRepository.findAppUserByUsername(user.getUsername());
        Long userId = appUser.isPresent() ? appUser.get().getId() : 0L;
        if (userId.equals(questionId)) {
            questionRepository.deleteById(questionId);
        } else {
            throw new ForbiddenActionException("Delete action for another user!");
        }
    }
}
