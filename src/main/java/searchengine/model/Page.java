package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "page")
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false, columnDefinition = "INT", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site siteId;
    @Column(name = "path", nullable = false, columnDefinition = "TEXT(50)")
    private String path;
    @Column(name = "code", nullable = false)
    private Integer code;
    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;
}
