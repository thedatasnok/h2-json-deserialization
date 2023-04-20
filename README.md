# h2-json-deserialization

Reproduction of a bug using JSON columns in H2 with Hibernate, in Spring Boot 3.0.x.


I have only tested that this is the case with Spring Boot 3.0.6 and 3.0.5, the problem may also affect other versions.


To reproduce the issue, follow these steps:

1. Clone the repository

2. Run the application using the following command:

  ```bash
  ./gradlew bootRun
  ```

3. Use a browser or curl to access the following URL: http://localhost:8080/models

4. Inspect the stack trace error


The application provides three endpoints:
- `GET /models` - should return a list of models (fails due to hibernate not being able to deserialize)
- `GET /models-jdbc` - same parsing failing using ObjectMapper & JdbcTemplate (approximately at least)
- `GET /models-jdbc-fixed` - hacky solution that removes wrapping quotes and escapes from the JSON string, deserialization works as expected


The issue is caused by Jackson not being able to deserialize the string value of the JSON column, the following portion of the stack trace reports what has gone wrong:

```
Caused by: java.lang.IllegalArgumentException: Could not deserialize string to java type: BasicJavaType(java.util.Map<java.lang.String, java.lang.String>)
	at org.hibernate.type.jackson.JacksonJsonFormatMapper.fromString(JacksonJsonFormatMapper.java:42)
	at org.hibernate.type.descriptor.jdbc.JsonJdbcType$2.getObject(JsonJdbcType.java:101)
	at org.hibernate.type.descriptor.jdbc.JsonJdbcType$2.doExtract(JsonJdbcType.java:84)
	at org.hibernate.type.descriptor.jdbc.BasicExtractor.extract(BasicExtractor.java:44)
	at org.hibernate.sql.results.jdbc.internal.JdbcValuesResultSetImpl.readCurrentRowValues(JdbcValuesResultSetImpl.java:263)
	... 98 more
Caused by: com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot construct instance of `java.util.LinkedHashMap` (although at least one Creator exists): no String-argument constructor/factory method to deserialize from String value ('{"key2":"value2","key1":"value1"}')
 at [Source: (String)""{\"key2\":\"value2\",\"key1\":\"value1\"}""; line: 1, column: 1]
	at com.fasterxml.jackson.databind.exc.MismatchedInputException.from(MismatchedInputException.java:63)
	at com.fasterxml.jackson.databind.DeserializationContext.reportInputMismatch(DeserializationContext.java:1733)
	at com.fasterxml.jackson.databind.DeserializationContext.handleMissingInstantiator(DeserializationContext.java:1358)
	at com.fasterxml.jackson.databind.deser.std.StdDeserializer._deserializeFromString(StdDeserializer.java:311)
	at com.fasterxml.jackson.databind.deser.std.MapDeserializer.deserialize(MapDeserializer.java:454)
	at com.fasterxml.jackson.databind.deser.std.MapDeserializer.deserialize(MapDeserializer.java:32)
	at com.fasterxml.jackson.databind.deser.DefaultDeserializationContext.readRootValue(DefaultDeserializationContext.java:323)
	at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMapper.java:4730)
	at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3677)
	at org.hibernate.type.jackson.JacksonJsonFormatMapper.fromString(JacksonJsonFormatMapper.java:39)
	... 102 more
```
