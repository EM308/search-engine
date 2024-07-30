package searchengine.services.CRUD;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.dto.SiteDto;
import searchengine.model.Site;
import searchengine.repositories.SiteRepository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SiteCRUDService implements CRUDService<SiteDto> {
    @Autowired
    private final SiteRepository siteRepository;
    @Override
    public ResponseEntity<?> getById(Integer id) {
        Site site;
        try {
            site = siteRepository.findById(id).orElseThrow();
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Error("Категория с ID " + id + " не найдена."));
        }
        return new ResponseEntity<>(mapToDto(site), HttpStatus.OK);
    }
    @Override
    public Collection<SiteDto> getAll() {
        return siteRepository.findAll().stream().map(SiteCRUDService::mapToDto).toList();
    }
    @Override
    public ResponseEntity<SiteDto> create(SiteDto item) {
        Site site = mapToEntity(item);
        site.setStatusTime(new Timestamp(new Date().getTime()));
        siteRepository.save(site);
        return new ResponseEntity<>(mapToDto(site), HttpStatus.CREATED);
    }
    @Override
    public ResponseEntity<SiteDto> update(SiteDto item) {
        Site site = mapToEntity(item);
        site.setStatusTime(new Timestamp(new Date().getTime()));
        Optional<Site> optionalSite = siteRepository.findByName(site.getName());
        site.setId(optionalSite.get().getId());
        siteRepository.save(site);
        return new ResponseEntity<>(mapToDto(site), HttpStatus.OK);
    }
    @Override
    public ResponseEntity<?> delete(Integer id) {
        try {
            siteRepository.findById(id).orElseThrow();
        } catch (Exception ex){
            return new ResponseEntity<>(new Error("Категория с id" + id + " не найдена."), HttpStatus.NOT_FOUND);
        }
        siteRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    public static SiteDto mapToDto(Site site){
        SiteDto siteDto = new SiteDto();
        siteDto.setId(site.getId());
        siteDto.setStatus(site.getStatus());
        siteDto.setStatusTime(new Timestamp(new Date().getTime()));
        siteDto.setLastError(site.getLastError());
        siteDto.setUrl(site.getUrl());
        siteDto.setName(site.getName());
        return siteDto;
    }
    public static Site mapToEntity(SiteDto siteDto){
        Site site = new Site();
        site.setId(siteDto.getId());
        site.setStatus(siteDto.getStatus());
        site.setStatusTime(new Timestamp(new Date().getTime()));
        site.setLastError(siteDto.getLastError());
        site.setUrl(siteDto.getUrl());
        site.setName(siteDto.getName());
        return site;
    }
}
