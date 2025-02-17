package engine.repositories;

import engine.businesslayer.CompletedQuiz;
import engine.presentation.DTO.CompletedQuizDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletedQuizRepository extends CrudRepository<CompletedQuiz, Long> {
    @Query("SELECT new engine.presentation.DTO.CompletedQuizDTO(q.question.id, q.completedAt) FROM CompletedQuiz q WHERE q.user.id = :userId ORDER BY q.completedAt DESC")
    Page<CompletedQuizDTO> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}
