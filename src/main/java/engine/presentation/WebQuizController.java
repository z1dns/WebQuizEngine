package engine.presentation;

import engine.businesslayer.exceptions.ForbiddenActionException;
import engine.businesslayer.security.RegistrationService;
import engine.businesslayer.exceptions.QuizNotFoundException;
import engine.businesslayer.WebQuizService;
import engine.presentation.DTO.CompletedQuizDTO;
import engine.presentation.DTO.PageableQuestionDTO;
import engine.presentation.DTO.AnswerDTO;
import engine.presentation.DTO.QuestionDTO;
import engine.presentation.request.AnswerRequest;
import engine.presentation.request.QuestionRequest;
import engine.presentation.request.RegistrationRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
public class WebQuizController {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebQuizController.class);

    private final WebQuizService webQuizService;
    private final RegistrationService registrationService;

    public WebQuizController(WebQuizService webQuizService, RegistrationService registrationService) {
        this.webQuizService = webQuizService;
        this.registrationService = registrationService;
    }

    @PostMapping("/api/register")
    public ResponseEntity<Object> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        registrationService.registerUser(registrationRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/quizzes")
    public QuestionDTO addQuiz(@AuthenticationPrincipal UserDetails user,
                               @Valid @RequestBody QuestionRequest questionRequest) {
        return webQuizService.addNewQuiz(user, questionRequest);
    }

    @PostMapping("/api/quizzes/{id}/solve")
    public AnswerDTO answerQuiz(@AuthenticationPrincipal UserDetails user,
                                @PathVariable("id") Long id,
                                @RequestBody AnswerRequest answer) {
        return webQuizService.getAnswer(user, id, answer)
                .orElseThrow(() -> new QuizNotFoundException("Not found quiz for answer, quiz_id:" + id));
    }

    @GetMapping("/api/quizzes/{id}")
    public QuestionDTO getQuiz(@PathVariable("id") long id) {
        return webQuizService.getQuizById(id)
                .orElseThrow(() -> new QuizNotFoundException("Not found quiz, quiz_id:" + id));
    }

    @GetMapping("/api/quizzes")
    public Page<PageableQuestionDTO> getQuizzes(@RequestParam(name = "page") Integer page) {
        return webQuizService.getQuizzesByPage(page);
    }

    @GetMapping("/api/quizzes/completed")
    public Page<CompletedQuizDTO> getCompletedQuizzes(@AuthenticationPrincipal UserDetails user,
                                                      @RequestParam(name = "page") Integer page) {
        return webQuizService.getCompletedQuizzes(user, page);
    }

    //@GetMapping("/api/quizzes")
    //public List<QuestionResponse> getQuizzes() {
    //    return webQuizService.getAllQuizzes();
    //}

    @DeleteMapping("/api/quizzes/{id}")
    public ResponseEntity deleteQuiz(@AuthenticationPrincipal UserDetails user,
                                     @PathVariable("id") long id) {
        webQuizService.deleteQuiz(user, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception e) {
        LOGGER.warn("Error: {} with message: {}", e.getClass().getSimpleName(), e.getMessage());
        if (e instanceof QuizNotFoundException) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (e instanceof ForbiddenActionException) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }}
