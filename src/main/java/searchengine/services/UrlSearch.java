package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.dto.PageDto;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.services.CRUD.PageCRUDService;

import java.util.ArrayList;
import java.util.concurrent.RecursiveTask;


@RequiredArgsConstructor
public class UrlSearch extends RecursiveTask<ArrayList<UrlSearch>> {
    private ArrayList<UrlSearch> urlSearches = new ArrayList<>();
    private Site url;
    private ArrayList<UrlSearch> returnList = new ArrayList<>();
    private final PageCRUDService pageCRUDService;
    private final PageRepository pageRepository;
    private final LemmaService lemmaService;
    private Elements div;
    UrlSearch(Site url, Elements div, PageCRUDService pageCRUDService, PageRepository pageRepository, LemmaService lemmaService) {
        this.url = url;
        this.div = div;
        this.lemmaService = lemmaService;
        this.pageCRUDService = pageCRUDService;
        this.pageRepository = pageRepository;
    }
    @Override
    @SneakyThrows
    protected ArrayList<UrlSearch> compute() {
        for (Element d : div) {
            String urlNow = d.attr("href");
            if (IndexingService.getIndexing()
                    && !urlNow.contains("#")
                    && urlNow.startsWith("/")
                    && (urlNow.contains(".html") || !urlNow.contains("."))) {
                try {
                    pageDtoCreate(urlNow);
                } catch (Exception e){
                 continue;
                }
            }
           if(!IndexingService.getIndexing()){
               throw new Exception("Индексация остановлена пользователем");
            }
        }
       for (UrlSearch urlSearch2 : urlSearches) {
            returnList.addAll(urlSearch2.join());
        }
        return returnList;
    }
    private void newPageSelect(Document newPage, int statusCode, PageDto pageDto){
        Elements div = newPage.select("a");
        if(statusCode < 400 && !div.isEmpty()) {
            Runnable runnable = () -> lemmaService.indexing(pageDto);
            new Thread(runnable).start();
            UrlSearch urlSearch = new UrlSearch(url, div, pageCRUDService, pageRepository, lemmaService);
            urlSearch.fork();
            urlSearches.add(urlSearch);
        }
    }
    private void pageDtoCreate(String urlNow) throws Exception {
        int statusCode;
        PageDto pageDto = new PageDto();
        pageDto.setPath(urlNow);
        pageDto.setSite(url.getName());
        Document newPage;
        synchronized (pageCRUDService) {
            if(pageRepository.findByPath(urlNow).isEmpty()) {
                newPage = Jsoup.connect(url.getUrl() + urlNow)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .ignoreContentType(true)
                        .timeout(0)
                        .followRedirects(false)
                        .get();
                statusCode = newPage.connection().execute().statusCode();
                pageDto.setCode(statusCode);
                pageDto.setContent(String.valueOf(newPage));
                pageCRUDService.createWithSite(pageDto, url);
            } else {
                throw new Exception();
            }
        }
        newPageSelect(newPage, statusCode, pageDto);
    }

}
