package engine.presentation.DTO;

import java.util.List;

public interface PageableQuestionDTO {
    Long getId();
    String getTitle();
    String getText();
    List<String> getOptions();
}
