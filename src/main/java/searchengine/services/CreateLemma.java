package searchengine.services;

import lombok.SneakyThrows;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CreateLemma {
    public static HashMap<String, Integer> getLemmaMap(Document page) throws IOException {
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        HashMap<String, Integer> lemmaMap = new HashMap<>();
        String pageText = page.text();
        List<String> wordList = toWords(pageText.toLowerCase());
        for (String word : wordList) {
            String lemma;
            try {
                lemma = getLemma(word, luceneMorph);
            } catch (Exception e){
                continue;
            }
            if (lemmaMap.containsKey(lemma)) {
                lemmaMap.put(lemma, lemmaMap.get(lemma) + 1);
            } else {
                lemmaMap.put(lemma, 1);
            }
        }
        return lemmaMap;
    }
    public static String getLemma(String word, LuceneMorphology luceneMorph) throws Exception {
        List<String> wordMorphInfo = luceneMorph.getMorphInfo(word);
        if (wordMorphInfo.stream().anyMatch((e) -> e.matches(".+СОЮЗ|.*МЕЖД|.*ПРЕДЛ|.*МС.*"))) {
            throw new Exception();
        }
        List<String> wordBaseForms = luceneMorph.getNormalForms(word);

        if (wordBaseForms.size() > 1) {
            wordBaseForms = wordBaseForms.stream().filter(s ->
                    luceneMorph.getMorphInfo(s).stream().anyMatch(wordMorphInfo::contains)).collect(Collectors.toList());
        }
        return wordBaseForms.get(0);
    }
    public static List<String> toWords(String text){
        String regexNotWords = "[0-9.,\"=:+/[-];\\s+]";
        String[] words = text.split(regexNotWords);
        List<String> wordsBase = List.of(words);
        wordsBase = wordsBase.stream().filter(e -> !e.isEmpty()).collect(Collectors.toList());
        return wordsBase;
    }
}
