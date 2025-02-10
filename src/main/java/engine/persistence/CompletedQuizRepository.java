package engine.persistence;

import engine.businesslayer.CompletedQuiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletedQuizRepository extends CrudRepository<CompletedQuiz, Long> {
    @Query("SELECT new engine.persistence.CompletedQuizView(q.question.id, q.completedAt) FROM CompletedQuiz q WHERE q.user.id = :userId ORDER BY q.completedAt DESC")
    Page<CompletedQuizView> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}
