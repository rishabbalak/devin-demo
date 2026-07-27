# costco-wms

Three-module Maven build. The Java and Spring Boot versions live in the root pom.xml.

- `costco-core` domain model and JPA entities. No web dependencies. Every other
  module depends on this one.
- `costco-terminal` server-rendered 5250 terminal UI. Fixed 24x80 character grid.
- `costco-api` REST tier and OpenAPI. The only bootable module.

## Build and verify
- `./mvnw clean verify` is the acceptance command. Run it before you change anything
  so you have a baseline, and after every meaningful step.
- `java -jar costco-api/target/costco-api-1.0.0-SNAPSHOT.jar` boots on port 8080.
- `GET /wms` serves the terminal. `GET /swagger-ui/index.html` serves the API docs.

## House rules
- The domain model is a system of record. Do not change business logic, domain
  semantics, or the database schema.
- The 5250 grid is 24 rows by 80 columns. Column alignment is a correctness
  property. Do not reflow, restyle, or reformat terminal output.
- CYYMMDD dates are a platform convention. Preserve them.
- Do not delete or weaken a test to make the build pass.
- Do not change the JaCoCo configuration.
