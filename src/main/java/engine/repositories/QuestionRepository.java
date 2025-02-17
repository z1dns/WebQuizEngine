package engine.repositories;

import engine.businesslayer.Question;
import engine.presentation.DTO.PageableQuestionDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface QuestionRepository extends CrudRepository<Question, Long> {
    Page<PageableQuestionDTO> findAllBy(Pageable pageable);
}
