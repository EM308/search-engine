package searchengine.services.Statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Site;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.IndexingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private final PageRepository pageRepository;
    @Autowired
    private final LemmaRepository lemmaRepository;
    @Autowired
    private final SiteRepository siteRepository;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        List<Site> sitesList = siteRepository.findAll();
        total.setSites(sitesList.size());
        total.setIndexing(IndexingService.getIndexing());

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        int countPages = 0, countLemmas = 0;
        for (Site site : sitesList) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            String name = site.getName();
            item.setName(name);
            item.setUrl(site.getUrl());
            Optional<Site> siteOptional = siteRepository.findByName(name);
            if (siteOptional.isPresent()) {
                searchengine.model.Site siteFind = siteOptional.get();
                int countPagesBySite = pageRepository.findAllBySiteId(siteFind).size();
                countPages += countPagesBySite;
                item.setPages(countPagesBySite);
                int countLemmasBySite = lemmaRepository.findAllBySiteId(siteFind).size();
                countLemmas += countLemmasBySite;
                item.setLemmas(countLemmasBySite);
                item.setStatus(siteFind.getStatus());
                item.setError(siteFind.getLastError());
                item.setStatusTime(siteFind.getStatusTime().getTime());

                detailed.add(item);
            }
        }
        total.setLemmas(countLemmas);
        total.setPages(countPages);
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
