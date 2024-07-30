package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.SiteDto;
import searchengine.model.Page;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.dto.response.IndexingResponse;
import searchengine.services.CRUD.PageCRUDService;
import searchengine.services.CRUD.SiteCRUDService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingService {
    @Autowired
    private final PageRepository pageRepository;
    @Autowired
    private final SiteRepository siteRepository;
    @Autowired
    private final SiteCRUDService siteCRUDService;
    @Autowired
    private final PageCRUDService pageCRUDService;
    @Autowired
    private final IndexRepository indexRepository;
    @Autowired
    private final LemmaRepository lemmaRepository;
    @Autowired
    private final SitesList sites;
    @Autowired
    private final IndexingPageService indexingPageService;
    private volatile static Boolean isIndexing = false;
    private ForkJoinPool forkJoinPool;
    public ResponseEntity<IndexingResponse> startIndexing(){
        IndexingResponse indexingResponse = new IndexingResponse();
        if (isIndexing){
            indexingResponse.setResult(false);
            indexingResponse.setError("Индексация уже запущена");
            return ResponseEntity.ok(indexingResponse);
        } else{
            Runnable runnable = this::indexing;
            new Thread(runnable).start();
            indexingResponse.setResult(true);
            return ResponseEntity.ok(indexingResponse);
        }
    }

    @SneakyThrows
    private void indexing(){
        isIndexing = true;
        forkJoinPool = new ForkJoinPool();
        createSites();
        SiteDto siteDto = new SiteDto();
        for (Site site: sites.getSites()) {
            try {
                Document page = Jsoup.connect(site.getUrl())
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .ignoreContentType(true)
                        .timeout(0)
                        .followRedirects(false)
                        .get();
                Elements div = page.select("a");
                searchengine.model.Site siteFind = siteRepository.findByUrl(site.getUrl()).get();
                siteDto = SiteCRUDService.mapToDto(siteFind);
                UrlSearch urlSearch = new UrlSearch(siteFind, div, pageCRUDService, pageRepository, indexingPageService);
                forkJoinPool.invoke(urlSearch);
                siteDto.setStatus("INDEXED");
                siteCRUDService.update(siteDto);
            }
            catch (Exception e) {
                if(!siteRepository.findByUrl(site.getUrl()).get().getStatus().equals("FAILED")) {
                    siteDto.setName(site.getName());
                    siteDto.setStatus("FAILED");
                    siteDto.setLastError(e.getMessage());
                    siteCRUDService.update(siteDto);
                }
            }
        }
        isIndexing = false;
    }
    public ResponseEntity<IndexingResponse> stopIndexing(){
        IndexingResponse indexingResponse = new IndexingResponse();
        try {
            forkJoinPool.shutdownNow();
            isIndexing = false;

            for (Site site : sites.getSites()){
                if(siteRepository.findByName(site.getName()).get().getStatus().equals("INDEXING")) {
                    SiteDto siteDto = new SiteDto();
                    siteDto.setName(site.getName());
                    siteDto.setUrl(site.getUrl());
                    siteDto.setStatus("FAILED");
                    siteDto.setLastError("Индексация остановлена пользователем");
                    siteCRUDService.update(siteDto);
                }
            }
        } catch (Exception ex){
            indexingResponse.setResult(false);
            indexingResponse.setError("Индексация не запущена");
            return ResponseEntity.ok(indexingResponse);
        }
        indexingResponse.setResult(true);
        return ResponseEntity.ok(indexingResponse);
    }
    @SneakyThrows
    public void createSites(){
        for (Site site: sites.getSites()) {
            Optional<searchengine.model.Site> siteFind = siteRepository.findByName(site.getName());
            if(siteFind.isPresent()){
                lemmaRepository.deleteBySiteId(siteFind.get());
                List<Page> deletePage = pageRepository.deleteBySiteId(siteFind.get());
                deletePage.forEach(indexRepository::deleteAllByPageId);
            }
            try{
                siteCRUDService.update(IndexingPageService.createSiteDtoIndexing(site));
            } catch (Exception e) {
                siteCRUDService.create(IndexingPageService.createSiteDtoIndexing(site));
            }
        }
    }
    public static Boolean getIndexing() {
        return isIndexing;
    }
    /*public static Document createConnection(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .ignoreContentType(true)
                .timeout(0)
                .followRedirects(false)
                .get();
    }

     */
}
