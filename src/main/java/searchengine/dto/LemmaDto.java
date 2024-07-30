package searchengine.dto;

import lombok.Data;

@Data
public class LemmaDto {
    private Integer id;
    private String site;
    private String lemma;
    private Integer frequency;
}
