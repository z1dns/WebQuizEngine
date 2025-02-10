package engine.persistence;

import java.util.List;

public interface QuestionView {
    Long getId();
    String getTitle();
    String getText();
    List<String> getOptions();
}
