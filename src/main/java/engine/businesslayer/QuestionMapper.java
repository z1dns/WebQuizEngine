package engine.businesslayer;

import org.springframework.stereotype.Component;
import engine.presentation.QuestionResponse;

@Component
public class QuestionMapper {
    QuestionResponse convertQuestionToResponse(Question question) {
        return new QuestionResponse(question.getId(), question.getTitle(), question.getText(), question.getOptions());
    }
}
