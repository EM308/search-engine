package searchengine.dto;

import lombok.Data;

import java.util.Date;
@Data
public class SiteDto {
    private Integer id;
    private String status;
    private Date statusTime;
    private String lastError;
    private String url;
    private String name;
}
