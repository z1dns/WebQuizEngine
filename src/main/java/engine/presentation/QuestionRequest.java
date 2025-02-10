package engine.presentation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class QuestionRequest {
    @NotEmpty
    private String title;
    @NotEmpty
    private String text;
    @NotNull
    @Size(min = 2)
    private List<String> options;
    private List<Integer> answer;

    public QuestionRequest(String title, String text, List<String> options, List<Integer> answer) {
        this.title = title;
        this.text = text;
        this.options = options;
        this.answer = answer;
    }

    public QuestionRequest() {
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return options;
    }

    public List<Integer> getAnswer() {
        return answer;
    }

    @Override
    public String toString() {
        return "QuestionRequest{" +
                "title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", options=" + options +
                ", answer=" + answer +
                '}';
    }
}
