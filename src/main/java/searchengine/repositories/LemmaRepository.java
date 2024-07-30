package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Site;
import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer>{
    @Override
    Optional<Lemma> findById(Integer integer);
    Optional<Lemma> findByLemma(String lemma);
    List<Lemma> findAllBySiteId(Site siteId);
    @Transactional
    List<Lemma> deleteBySiteId(Site siteId);
}
