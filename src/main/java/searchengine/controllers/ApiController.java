package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.response.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.response.IndexingResponse;
import searchengine.services.*;
import searchengine.services.Statistics.StatisticsService;
import searchengine.services.Statistics.StatisticsServiceImpl;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;
    private final StatisticsServiceImpl statisticsServiceIpml;
    private final IndexingService indexingService;
    private final IndexingPageService indexingPageService;
    private final SearchService searchService;
    public ApiController(StatisticsService statisticsService, StatisticsServiceImpl statisticsServiceIpml, IndexingService indexingService, IndexingPageService indexingPageService, SearchService searchService) {
        this.statisticsService = statisticsService;
        this.statisticsServiceIpml = statisticsServiceIpml;
        this.indexingService = indexingService;
        this.indexingPageService = indexingPageService;
        this.searchService = searchService;
    }
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing(){
        return indexingService.startIndexing();
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing(){
        return indexingService.stopIndexing();
    }
    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> indexPage(@RequestBody String url){
        return indexingPageService.indexPage(url);
    }
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam String query,
                                                 @RequestParam(required = false) String site,
                                                 @RequestParam(required = false, defaultValue = "0") int offset,
                                                 @RequestParam(required = false, defaultValue = "20") int limit){
        return searchService.search(query, site, offset, limit);
    }
}
