package engine.presentation.request;

import java.util.List;

public class AnswerRequest {
    private List<Integer> answer;

    public AnswerRequest(List<Integer> answer) {
        this.answer = answer;
    }

    public AnswerRequest() {

    }

    public List<Integer> getAnswer() {
        return answer;
    }

    @Override
    public String toString() {
        return "AnswerRequest{" +
                "answer=" + answer +
                '}';
    }
}
