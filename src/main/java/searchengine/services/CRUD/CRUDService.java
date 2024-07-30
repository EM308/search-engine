package searchengine.services.CRUD;

import org.springframework.http.ResponseEntity;
import java.util.Collection;

public interface CRUDService<T> {
    ResponseEntity<?> getById(Integer id);
    Collection<T> getAll();
    ResponseEntity<T> create(T item);
    ResponseEntity<T> update(T item);
    ResponseEntity<?> delete(Integer id);
}
