package searchengine.services.CRUD;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.dto.IndexDto;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IndexCRUDService implements CRUDService<IndexDto> {
    @Autowired
    private final LemmaRepository lemmaRepository;
    @Autowired
    private final IndexRepository indexRepository;
    @Autowired
    private final PageRepository pageRepository;

    @Override
    public ResponseEntity<?> getById(Integer id) {
        Index index;
        try {
            index = indexRepository.findById(id).orElseThrow();
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Error("Категория с ID " + id + " не найдена."));
        }
        return new ResponseEntity<>(mapToDto(index), HttpStatus.OK);
    }
    @Override
    public Collection<IndexDto> getAll() {
        return indexRepository.findAll().stream().map(IndexCRUDService::mapToDto).toList();
    }
    @Override
    public ResponseEntity<IndexDto> create(IndexDto item) {
        Index index = mapToEntity(item);
        Optional<Lemma> lemma = lemmaRepository.findByLemma(item.getLemma());
        index.setLemmaId(lemma.get());
        Optional<Page> page = pageRepository.findByPath(item.getPage());
        index.setPageId(page.get());
        indexRepository.save(index);
        return new ResponseEntity<>(mapToDto(index), HttpStatus.CREATED);
    }
    @Override
    public ResponseEntity<IndexDto> update(IndexDto item) {
        Index index = mapToEntity(item);
        indexRepository.save(index);
        return new ResponseEntity<>(mapToDto(index), HttpStatus.OK);
    }
    @Override
    public ResponseEntity<?> delete(Integer id) {
        try {
            indexRepository.findById(id).orElseThrow();
        } catch (Exception ex){
            return new ResponseEntity<>(new Error("Категория с id" + id + " не найдена."), HttpStatus.NOT_FOUND);
        }
        indexRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    public static IndexDto mapToDto(Index index){
        IndexDto indexDto = new IndexDto();
        indexDto.setPage(index.getPageId().getPath());
        indexDto.setLemma(index.getLemmaId().getLemma());
        indexDto.setRank(index.getRank());
        return indexDto;
    }
    public static Index mapToEntity(IndexDto indexDto){
        Index index = new Index();
        index.setRank(indexDto.getRank());
        return index;
    }
}


