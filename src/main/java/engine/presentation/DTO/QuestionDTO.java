package engine.presentation.DTO;

import java.util.List;

public record QuestionDTO(Long id, String title, String text, List<String> options) {
}
