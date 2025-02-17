package engine.businesslayer.mappers;

import engine.businesslayer.Question;
import org.springframework.stereotype.Component;
import engine.presentation.DTO.QuestionDTO;

@Component
public class QuestionMapper {
    public QuestionDTO convertQuestionToResponse(Question question) {
        return new QuestionDTO(question.getId(), question.getTitle(), question.getText(), question.getOptions());
    }
}
