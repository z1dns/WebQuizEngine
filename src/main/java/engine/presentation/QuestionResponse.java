package engine.presentation;

import java.util.List;

public record QuestionResponse(Long id, String title, String text, List<String> options) {
}
