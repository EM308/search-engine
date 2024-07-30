package searchengine.dto;

import lombok.Data;

@Data
public class IndexDto {
    private Integer id;
    private String page;
    private String lemma;
    private Float rank;
}
