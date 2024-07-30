package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.IndexDto;
import searchengine.dto.LemmaDto;
import searchengine.dto.PageDto;
import searchengine.dto.SiteDto;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.dto.response.IndexingResponse;
import searchengine.services.CRUD.IndexCRUDService;
import searchengine.services.CRUD.LemmaCRUDService;
import searchengine.services.CRUD.PageCRUDService;
import searchengine.services.CRUD.SiteCRUDService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Getter
public class IndexingPageService {
    @Autowired
    private final PageRepository pageRepository;
    @Autowired
    private final SiteRepository siteRepository;
    @Autowired
    private final SiteCRUDService siteCRUDService;
    @Autowired
    private final PageCRUDService pageCRUDService;
    @Autowired
    private final LemmaRepository lemmaRepository;
    @Autowired
    private final LemmaCRUDService lemmaCRUDService;
    @Autowired
    private final IndexCRUDService indexCRUDService;
    @Autowired
    private final IndexRepository indexRepository;
    @Autowired
    private final SitesList sites;
    public ResponseEntity<IndexingResponse> indexPage(String url) {
        url = url.replace("url=", "");
        url = url.replaceAll("%3A", ":");
        url = url.replaceAll("%2F", "/");
        IndexingResponse indexingResponse = new IndexingResponse();
        for (Site site: sites.getSites()) {
            if (url.startsWith(site.getUrl())) {
                try {
                    if (siteRepository.findByName(site.getName()).isEmpty()) {
                        siteCRUDService.create(createSiteDtoIndexing(site));
                    }
                    String path = url.replace(site.getUrl(), "");

                    deletePage(path);
                    final PageDto pageDto = createPage(url, path, site);
                    Runnable runnable = () -> indexing(pageDto);
                    new Thread(runnable).start();

                    indexingResponse.setResult(true);

                    return ResponseEntity.ok(indexingResponse);
                } catch (Exception e){
                    indexingResponse.setResult(false);
                    indexingResponse.setError("Указанная страница не найдена");
                    return new ResponseEntity<>(indexingResponse, HttpStatus.NOT_FOUND);
                }
            }
        }
        indexingResponse.setResult(false);
        indexingResponse.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");

        return ResponseEntity.ok(indexingResponse);
    }
    @SneakyThrows
    public void indexing(PageDto pageDto){
        Document page = Jsoup.parse(pageDto.getContent());
        HashMap<String, Integer> lemma = CreateLemma.getLemmaMap(page);
        for (Map.Entry<String, Integer> entry : lemma.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            LemmaDto lemmaDto = new LemmaDto();
            lemmaDto.setLemma(key);
            lemmaDto.setSite(pageDto.getSite());
            synchronized (lemmaRepository) {
                Optional<Lemma> lemmaModel = lemmaRepository.findByLemma(key);
                if (lemmaModel.isPresent()) {
                    lemmaDto.setFrequency(lemmaModel.get().getFrequency() + 1);
                    lemmaCRUDService.update(lemmaDto);
                } else {
                    lemmaDto.setFrequency(1);
                    lemmaCRUDService.create(lemmaDto);
                }
            }
            IndexDto indexDto = new IndexDto();
            indexDto.setPage(pageDto.getPath());
            indexDto.setLemma(key);
            indexDto.setRank(value.floatValue());
            indexCRUDService.create(indexDto);
        }
    }
    public static SiteDto createSiteDtoIndexing(Site site){
        SiteDto siteDto = new SiteDto();
        siteDto.setName(site.getName());
        siteDto.setUrl(site.getUrl());
        siteDto.setStatus("INDEXING");
        siteDto.setLastError("");
        return siteDto;
    }
    private void deletePage(String path){
        Optional<Page> pageOptional = pageRepository.findByPath(path);
        if(pageOptional.isPresent()){
            List<Index> indexList = indexRepository.findAllByPageId(pageOptional.get());
            indexList.forEach(e ->{
                Lemma lemma = e.getLemmaId();
                lemma.setFrequency(lemma.getFrequency() - 1);
                LemmaDto lemmaDto = new LemmaDto();
                lemmaDto.setFrequency(lemma.getFrequency());
                lemmaDto.setSite(lemma.getSiteId().getUrl());
                lemmaDto.setId(lemma.getId());
                lemmaDto.setLemma(lemma.getLemma());
                lemmaCRUDService.update(lemmaDto);
            });
            pageRepository.deleteById(pageOptional.get().getId());
        }
    }
    private PageDto createPage(String url, String path, Site site) throws IOException {
        PageDto pageDto = new PageDto();
        Document page = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .ignoreContentType(true)
                .timeout(0)
                .followRedirects(false)
                .get();;

        pageDto.setPath(path);
        pageDto.setSite(siteRepository.findByName(site.getName()).get().getName());
        int statusCode = page.connection().execute().statusCode();
        pageDto.setCode(statusCode);
        pageDto.setContent(String.valueOf(page));

        pageCRUDService.create(pageDto);
        return  pageDto;
    }
}
