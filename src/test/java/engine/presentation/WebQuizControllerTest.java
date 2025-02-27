package engine.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine.businesslayer.WebQuizService;
import engine.businesslayer.exceptions.ForbiddenActionException;
import engine.businesslayer.exceptions.QuizNotFoundException;
import engine.businesslayer.security.RegistrationService;
import engine.businesslayer.security.SecurityConfiguration;
import engine.presentation.DTO.AnswerDTO;
import engine.presentation.DTO.QuestionDTO;
import engine.presentation.request.AnswerRequest;
import engine.presentation.request.QuestionRequest;
import engine.presentation.request.RegistrationRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfiguration.class)
@WebMvcTest(WebQuizController.class)
public class WebQuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private WebQuizService webQuizService;

    @MockBean
    private RegistrationService registrationService;

    private static QuestionRequest questionRequest;
    private static AnswerRequest answerRequest;

    @BeforeAll
    static void setUp() {
        List<String> options = Arrays.asList("Option 1", "Option 2", "Option 3", "Option 4");
        questionRequest = new QuestionRequest("Test Title", "Test Text", options, Collections.singletonList(2));
        answerRequest = new AnswerRequest(Collections.singletonList(1));
    }

    @Test
    void registerUser_ShouldReturnOk() throws Exception {
        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password");

        doNothing().when(registrationService).registerUser(any(RegistrationRequest.class));

        mockMvc.perform(post("/api/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void registerUserWithInvalidEmail() throws Exception {
        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("test1example.com");
        registrationRequest.setPassword("password");

        doNothing().when(registrationService).registerUser(any(RegistrationRequest.class));

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult -> assertInstanceOf(MethodArgumentNotValidException.class, mvcResult.getResolvedException()));
    }

    @Test
    void registerUserWithTooShortPassword() throws Exception {
        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("pass");

        doNothing().when(registrationService).registerUser(any(RegistrationRequest.class));

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult -> assertInstanceOf(MethodArgumentNotValidException.class, mvcResult.getResolvedException()));
    }

    @Test
    void testUnauthorizedAddQuizRequest() throws Exception {
        QuestionDTO questionDTO = new QuestionDTO(1L,
                questionRequest.getTitle(),
                questionRequest.getText(),
                questionRequest.getOptions());

        when(webQuizService.addNewQuiz(any(UserDetails.class), any(QuestionRequest.class)))
                .thenReturn(questionDTO);

        mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAddQuizRequest() throws Exception {
        QuestionDTO questionDTO = new QuestionDTO(1L,
                questionRequest.getTitle(),
                questionRequest.getText(),
                questionRequest.getOptions());

        when(webQuizService.addNewQuiz(any(UserDetails.class), any(QuestionRequest.class)))
                .thenReturn(questionDTO);

        mockMvc.perform(post("/api/quizzes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.text").value("Test Text"))
                .andExpect(jsonPath("$.options", hasSize(4)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void solveNonExistingQuiz() throws Exception {
        when(webQuizService.getAnswer(any(UserDetails.class),any(Long.class), any(AnswerRequest.class)))
                .thenThrow(new QuizNotFoundException(""));

        mockMvc.perform(post("/api/quizzes/1/solve")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(QuizNotFoundException.class, result.getResolvedException()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void solveQuizSuccessfully() throws Exception {
        when(webQuizService.getAnswer(any(UserDetails.class),any(Long.class), any(AnswerRequest.class)))
                .thenReturn(Optional.of(new AnswerDTO(true, "Congratulations, you're right!")));

        mockMvc.perform(post("/api/quizzes/1/solve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.feedback").value("Congratulations, you're right!"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void solveQuizUnsuccessfully() throws Exception {
        when(webQuizService.getAnswer(any(UserDetails.class),any(Long.class), any(AnswerRequest.class)))
                .thenReturn(Optional.of(new AnswerDTO(false, "Wrong answer! Please, try again.")));

        mockMvc.perform(post("/api/quizzes/1/solve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.feedback").value("Wrong answer! Please, try again."));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getExistingQuiz() throws Exception {
        List<String> options = Arrays.asList("Option 1", "Option 2", "Option 3", "Option 4");
        when(webQuizService.getQuizById(any(Long.class)))
                .thenReturn(Optional.of(new QuestionDTO(1L, "Test Title", "Test Text", options)));

        mockMvc.perform(get("/api/quizzes/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.text").value("Test Text"))
                .andExpect(jsonPath("$.options", hasSize(4)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getNonExistingQuiz() throws Exception {
        when(webQuizService.getQuizById(any(Long.class)))
                .thenThrow(new QuizNotFoundException(""));

        mockMvc.perform(get("/api/quizzes/1")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(QuizNotFoundException.class, result.getResolvedException()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteExistingQuiz() throws Exception {
        doNothing().when(webQuizService).deleteQuiz(any(UserDetails.class), any(Long.class));

        mockMvc.perform(delete("/api/quizzes/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteNonExistingQuiz() throws Exception {
        doThrow(new QuizNotFoundException("")).when(webQuizService).deleteQuiz(any(UserDetails.class), any(Long.class));

        mockMvc.perform(delete("/api/quizzes/1")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(QuizNotFoundException.class, result.getResolvedException()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteQuizWithoutOwnership() throws Exception {
        doThrow(new ForbiddenActionException("")).when(webQuizService).deleteQuiz(any(UserDetails.class), any(Long.class));

        mockMvc.perform(delete("/api/quizzes/1")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(result -> assertInstanceOf(ForbiddenActionException.class, result.getResolvedException()));
    }
}
