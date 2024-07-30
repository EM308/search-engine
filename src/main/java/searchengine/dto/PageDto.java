package searchengine.dto;

import lombok.Data;


@Data
public class PageDto{
    private Integer id;
    private String site;
    private String path;
    private Integer code;
    private String content;

}
