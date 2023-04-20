package cool.datasnok.samples.h2jsondeserialization;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ModelController {
  private final ModelRepository modelRepository;
  private final JdbcTemplate jdbcTemplate;
  private final ObjectMapper objectMapper;

  private static final TypeReference<Map<String, String>> STRING_MAP_TYPE_REF = new TypeReference<>() {};

  @GetMapping("/models")
  public Iterable<Model> getModels() {
    return this.modelRepository.findAll();
  }

  @GetMapping("/models-jdbc")
  public Iterable<Map<String, Object>> getModelsJdbc() {
    return this.jdbcTemplate.query("SELECT * FROM model", (rs, i) -> {
      try {
        return Map.of(
          "id", rs.getLong("id"),
          "name", rs.getString("name"),
          "attributes", this.objectMapper.readValue(rs.getString("attributes"), STRING_MAP_TYPE_REF)
        );
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    });
  }

  @GetMapping("/models-jdbc-fixed")
  public Iterable<Map<String, Object>> getModelsJdbcFixed() {
    return this.jdbcTemplate.query("SELECT * FROM model", (rs, i) -> {
      try {
        var attributes = rs.getString("attributes");
        // removes wrapping quotes and escapes
        var fixedAttributes = attributes.substring(1, attributes.length() - 1).replace("\\", "");

        return Map.of(
          "id", rs.getLong("id"),
          "name", rs.getString("name"),
          "attributes", this.objectMapper.readValue(fixedAttributes, STRING_MAP_TYPE_REF)
        );
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    });
  }

  @PostConstruct
  void init() {
    this.modelRepository.save(new Model(
      "Sample model",
      Map.of("key1", "value1", "key2", "value2")
    ));
  }

}