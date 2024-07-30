package searchengine.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IndexingResponse {
    private Boolean result;
    private String error;
}
