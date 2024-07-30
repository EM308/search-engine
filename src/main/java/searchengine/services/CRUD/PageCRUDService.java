package searchengine.services.CRUD;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.dto.PageDto;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PageCRUDService implements CRUDService<PageDto> {
    @Autowired
    private final PageRepository pageRepository;
    @Autowired
    private final SiteRepository siteRepository;
    @Override
    public ResponseEntity<?> getById(Integer id) {
        Page page;
        try {
            page = pageRepository.findById(id).orElseThrow();
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Error("Категория с ID " + id + " не найдена."));
        }
        return new ResponseEntity<>(mapToDto(page), HttpStatus.OK);
    }
    @Override
    public Collection<PageDto> getAll() {
        return pageRepository.findAll().stream().map(PageCRUDService::mapToDto).toList();
    }
    @Override
    public ResponseEntity<PageDto> create(PageDto item) {
        Page page = mapToEntity(item);
        Optional<Site> site = siteRepository.findByName(item.getSite());
        page.setSiteId(site.get());
        pageRepository.save(page);
        return new ResponseEntity<>(mapToDto(page), HttpStatus.CREATED);
    }
    public ResponseEntity<PageDto> createWithSite(PageDto item, Site site) {
        Page page = mapToEntity(item);
        page.setSiteId(site);
        pageRepository.save(page);
        return new ResponseEntity<>(mapToDto(page), HttpStatus.CREATED);
    }
    @Override
    public ResponseEntity<PageDto> update(PageDto item) {
        Page page = mapToEntity(item);
        pageRepository.save(page);
        return new ResponseEntity<>(mapToDto(page), HttpStatus.OK);
    }
    @Override
    public ResponseEntity<?> delete(Integer id) {
        try {
            pageRepository.findById(id).orElseThrow();
        } catch (Exception ex){
            return new ResponseEntity<>(new Error("Категория с id" + id + " не найдена."), HttpStatus.NOT_FOUND);
        }
        pageRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    public static PageDto mapToDto(Page page){
        PageDto pageDto = new PageDto();
        pageDto.setId(page.getId());
        pageDto.setSite(page.getSiteId().getName());
        pageDto.setPath(page.getPath());
        pageDto.setCode(page.getCode());
        pageDto.setContent(page.getContent());
        return pageDto;
    }
    public static Page mapToEntity(PageDto pageDto){
        Page page = new Page();
        page.setId(pageDto.getId());
        page.setPath(pageDto.getPath());
        page.setCode(pageDto.getCode());
        page.setContent(pageDto.getContent());
        return page;
    }
}
