package cool.datasnok.samples.h2jsondeserialization;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<Model, Long> {
  
}
