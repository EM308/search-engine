package searchengine.services.CRUD;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.LemmaDto;
import searchengine.model.Lemma;
import searchengine.model.Site;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.SiteRepository;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LemmaCRUDService implements CRUDService<LemmaDto> {
    @Autowired
    private final LemmaRepository lemmaRepository;
    @Autowired
    private final SiteRepository siteRepository;
    @Override
    public ResponseEntity<?> getById(Integer id) {
        Lemma lemma;
        try {
            lemma = lemmaRepository.findById(id).orElseThrow();
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Error("Категория с ID " + id + " не найдена."));
        }
        return new ResponseEntity<>(mapToDto(lemma), HttpStatus.OK);
    }
    @Override
    public Collection<LemmaDto> getAll() {
        return lemmaRepository.findAll().stream().map(LemmaCRUDService::mapToDto).toList();
    }
    @Override
    public ResponseEntity<LemmaDto> create(LemmaDto item) {
        Lemma lemma = mapToEntity(item);
        Optional<Site> site = siteRepository.findByName(item.getSite());
        lemma.setSiteId(site.get());
        lemmaRepository.save(lemma);
        return new ResponseEntity<>(mapToDto(lemma), HttpStatus.CREATED);
    }
    @Override
    public ResponseEntity<LemmaDto> update(LemmaDto item) {
        Lemma lemma = mapToEntity(item);
        Lemma lemmaFind = lemmaRepository.findByLemma(item.getLemma()).get();
        lemma.setId(lemmaFind.getId());
        lemma.setSiteId(lemmaFind.getSiteId());
        lemmaRepository.save(lemma);
        return new ResponseEntity<>(mapToDto(lemma), HttpStatus.OK);
    }
    @Override
    public ResponseEntity<?> delete(Integer id) {
        try {
            lemmaRepository.findById(id).orElseThrow();
        } catch (Exception ex){
            return new ResponseEntity<>(new Error("Категория с id" + id + " не найдена."), HttpStatus.NOT_FOUND);
        }
        lemmaRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    public static LemmaDto mapToDto(Lemma lemma){
        LemmaDto lemmaDto = new LemmaDto();
        lemmaDto.setLemma(lemma.getLemma());
        lemmaDto.setSite(lemma.getSiteId().getUrl());
        lemmaDto.setFrequency(lemma.getFrequency());
        return lemmaDto;
    }
    public static Lemma mapToEntity(LemmaDto lemmaDto){
        Lemma lemma = new Lemma();
        lemma.setFrequency(lemmaDto.getFrequency());
        lemma.setLemma(lemmaDto.getLemma());
        return lemma;
    }
}
