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
@Table(name = "indexes")
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_id", nullable = false, columnDefinition = "INT", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Page pageId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lemma_id", nullable = false, columnDefinition = "INT", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lemma lemmaId;
    @Column(name = "ranks", nullable = false, columnDefinition = "FLOAT")
    private Float rank;
}
