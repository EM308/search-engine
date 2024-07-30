package searchengine.services.Statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
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
    private final SitesList sites;
    @Autowired
    private final PageRepository pageRepository;
    @Autowired
    private final LemmaRepository lemmaRepository;
    @Autowired
    private final SiteRepository siteRepository;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        List<Site> sitesList = sites.getSites();
        total.setSites(sitesList.size());
        total.setIndexing(IndexingService.getIndexing());
        total.setPages((int) pageRepository.count());
        total.setLemmas((int) lemmaRepository.count());

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        for (Site site : sitesList) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            String name = site.getName();
            item.setName(name);
            item.setUrl(site.getUrl());
            Optional<searchengine.model.Site> siteOptional = siteRepository.findByName(name);
            if (siteOptional.isPresent()) {
                searchengine.model.Site siteFind = siteOptional.get();
                item.setPages(pageRepository.findAllBySiteId(siteFind).size());
                item.setLemmas(lemmaRepository.findAllBySiteId(siteFind).size());
                item.setStatus(siteFind.getStatus());
                item.setError(siteFind.getLastError());
                item.setStatusTime(siteFind.getStatusTime().getTime());

                detailed.add(item);
            }
        }
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
