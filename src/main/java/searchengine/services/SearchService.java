package searchengine.services;

import liquibase.repackaged.org.apache.commons.lang3.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.dto.response.SearchData;
import searchengine.dto.response.SearchResponse;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final int LARGE_PERCENTAGE_MATCHES = 80;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;

    public ResponseEntity<SearchResponse> search(String query, String site, int offset, int limit) throws IOException {
        if(query.isEmpty()){
            return errorResponse("Задан пустой поисковый запрос");
        }
        Document doc = Jsoup.parse(query);
        HashMap<String, Integer> lemmaInQueryMap = CreateLemma.getLemmaMap(doc);
        List<Lemma> requiredLemmaInQuery = getLemmasWithoutFrequent(lemmaInQueryMap.keySet());
        List<Page> pageList;
        try {
            pageList = createListPage(requiredLemmaInQuery, site);
        } catch (Exception e){
            return errorResponse("Указанная страница не найдена");
        }
        List<Map.Entry<Float, Page>> listPageEntryRelevance= getPageSortRelevance(pageList, requiredLemmaInQuery);
        List<SearchData> searchDataList = new ArrayList<>();
        int size = Math.min(offset + limit, listPageEntryRelevance.size());
        for (int i = offset; i < size; i++) {
            float key = listPageEntryRelevance.get(i).getKey();
            Page value = listPageEntryRelevance.get(i).getValue();
            Document document = Jsoup.parse(value.getContent());

            Set<String> lemmaInPageForm = lemmaSnippet(document, lemmaInQueryMap.keySet());
            String snippet = getSnippet(lemmaInPageForm, document.text());
            SearchData searchData = new SearchData();
            searchData.setSite(value.getSiteId().getUrl());
            searchData.setSiteName(value.getSiteId().getName());
            searchData.setUri(value.getPath());
            searchData.setTitle(document.title());
            searchData.setSnippet(snippet);
            searchData.setRelevance(key);

            searchDataList.add(searchData);
        }

        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setResult(true);
        searchResponse.setCount(listPageEntryRelevance.size());
        searchResponse.setData(searchDataList);
        return ResponseEntity.ok(searchResponse);
    }
    private Set<String> lemmaSnippet(Document page, Set<String> wordQuery) throws IOException {
        Set<String> lemmaSet = new HashSet<>();
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        List<String> listWordsPage = CreateLemma.toWords(page.text());
        for (String word : listWordsPage) {
            try {
                if(wordQuery.contains(CreateLemma.getLemma(word.toLowerCase(), luceneMorph))){
                    lemmaSet.add(word);
                }
            } catch (Exception e){
            }
        }
        return lemmaSet;
    }
    private String getSnippet(Set<String> lemmaPageSet, String textPage){
        textPage = textPage.replaceAll("</?.?.>", "");
        for (String snippet : lemmaPageSet){
            textPage = textPage.replaceAll(snippet + " ", "<b>" + snippet + "</b> ");
            textPage = textPage.replaceAll(snippet + ",", "<b>" + snippet + "</b>,");
            textPage = textPage.replaceAll(snippet + "\\.", "<b>" + snippet + "</b>.");
            textPage = textPage.replaceAll(snippet + ":", "<b>" + snippet + "</b>:");
        }
        textPage = textPage.replaceAll("</b> ?<b>", " ");

        int countWords = StringUtils.countMatches(textPage, "<b>");
        return createSnippetFromText(textPage, countWords);
    }
    private String createSnippetFromText(String textPage, int countWords){
        StringBuilder snippet = new StringBuilder();

        boolean LotOfWords = countWords > 5;
        boolean HaveLongWords = textPage.matches(".*<b>.{8,}</b>.*");

        while (textPage.contains("<b>")){
            int lenghtSkip = 120 / countWords;
            if(LotOfWords && HaveLongWords &&
                    textPage.indexOf("</b>") - textPage.indexOf("<b>") < 11) {
                textPage = textPage.substring(textPage.indexOf("</b>") + 2);
                countWords = countWords - 1;
            }
            else {
                int indexStartB = textPage.indexOf("<b>") - lenghtSkip;
                int indexFinishB = textPage.indexOf("</b>") + lenghtSkip;
                int start = Math.max(indexStartB, 0);
                start = Math.min(start, textPage.indexOf(" ", start - 10) < 0
                        ? start : textPage.indexOf(" ", start - 10));
                int finish = Math.min(indexFinishB, textPage.length());
                finish = Math.max(finish, textPage.indexOf(" ", indexFinishB) < 0
                        ? finish : textPage.indexOf(" ", indexFinishB));
                String stringForSnippet = textPage.substring(start, finish);
                int countOpenTag = StringUtils.countMatches(stringForSnippet, "<b>");
                if(countOpenTag > 1) {
                    countWords = countWords - countOpenTag + 1;
                    finish = textPage.indexOf("<b>", textPage.indexOf("</b>")) - 1;
                    stringForSnippet = textPage.substring(start, finish);
                    snippet.append(stringForSnippet);
                } else{
                    snippet.append(stringForSnippet).append(" ... ");
                }
                textPage = textPage.substring(finish);
            }
            if(snippet.length() >= 330){
                return snippet.toString();
            }
        }
        return snippet.toString();
    }


    private List<Lemma> getLemmasWithoutFrequent(Set<String> lemmsInQuery){
        int pageCount = (int) pageRepository.count();
        int highFrequency = pageCount * LARGE_PERCENTAGE_MATCHES / 100;
        List<Lemma> lemmaInQueryList = new ArrayList<>();
        for (String lemma : lemmsInQuery) {
            Optional<Lemma> lemmaOptional = lemmaRepository.findByLemma(lemma);
            if(lemmaOptional.isPresent()){
                if(lemmaOptional.get().getFrequency() < highFrequency){
                    lemmaInQueryList.add(lemmaOptional.get());
                }
            }
        }
        lemmaInQueryList.sort(Comparator.comparing(Lemma::getFrequency));
        return lemmaInQueryList;
    }
    public static ResponseEntity<SearchResponse> errorResponse(String error){
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setResult(false);
        searchResponse.setError(error);
        return new ResponseEntity<>(searchResponse, HttpStatus.NOT_FOUND);
    }
    private List<Map.Entry<Float, Page>> getPageSortRelevance(List<Page> pageList, List<Lemma> requiredLemma){
        float relevanceAbsolute = (float) 0;
        Map<Float, Page> pageMapRelevance = new HashMap<>();
        for (Page page: pageList){
            List<Index> indexList = indexRepository.findAllByPageId(page);
            for (Index index: indexList){
                if (requiredLemma.contains(index.getLemmaId())) {
                    relevanceAbsolute = relevanceAbsolute + index.getRank();
                }
            }
            pageMapRelevance.put(relevanceAbsolute, page);
        }
        Set<Float> keys = pageMapRelevance.keySet();
        Float relevanceRelative = Collections.max(keys);
        Map<Float, Page> newPageMap = new HashMap<>();
        for (Map.Entry<Float, Page> entry : pageMapRelevance.entrySet()) {
            float key = entry.getKey();
            Page value = entry.getValue();
            newPageMap.put(key / relevanceRelative, value);
        }
        Comparator<Map.Entry<Float, Page>> pageComparator = Comparator.comparing(Map.Entry::getKey);
        Comparator<Map.Entry<Float, Page>> pageComparatorReversed = pageComparator.reversed();
        List<Map.Entry<Float, Page>> sortedList = newPageMap.entrySet()
                .stream()
                .sorted(pageComparatorReversed).toList();
        return sortedList;
    }

    private List<Page> createListPage(List<Lemma> lemmaInQueryList, String site) throws Exception {
        List<Page> pageList = new ArrayList<>();
        List<Index> indexList = indexRepository.findAllByLemmaId(lemmaInQueryList.get(0));
        for (Index index: indexList){
            if (site == null || index.getPageId().getSiteId().getUrl().equals(site)) {
                pageList.add(index.getPageId());
            }
        }
        for (int i = 1; i < lemmaInQueryList.size(); i++){
            List<Page> newPageSet = new ArrayList<>();
            List<Index> indexListFind = indexRepository.findAllByLemmaId(lemmaInQueryList.get(i));
            for(Index index: indexListFind) {
                if (pageList.contains(index.getPageId())){
                    newPageSet.add(index.getPageId());
                }
            }
            pageList = newPageSet;
        }
        if(pageList.isEmpty()){
            throw new Exception();
        }
        return  pageList;
    }
}

