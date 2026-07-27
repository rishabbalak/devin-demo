# Demo notes

**The run of show, talk track and Devin prompts live outside this repo**, in
`~/Documents/GitHub/devin-demo-kit/` — `RUN-OF-SHOW.md` and `PROMPTS.md`. They are kept
there deliberately so that `npm run demo:reset` cannot delete them.

This file covers only what is true about *this repository*.

---

## Running it

Requires a JDK (17 or 21). No Docker, no database to install.

```bash
./mvnw clean package
```

```bash
java -jar costco-api/target/costco-api-1.0.0-SNAPSHOT.jar
```

Warm build ~11s; boots in ~2–3s. A cold clone is slower on the first Maven download, so
pre-warm `~/.m2` before a demo.

| What | Where |
|---|---|
| Green-screen terminal | http://localhost:8080/wms |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| H2 console | http://localhost:8080/h2 (JDBC URL `jdbc:h2:mem:wmsdta`) |

Sign on with any user id. **There is no authentication** — the password field is ignored and
exists only because a 5250 session without one would not read as one.

`F3` exits the program, `F12` backs up one level, `F5` refreshes, `F7`/`F8` page a subfile,
`F11` toggles the alternate column set on WMS210.

### Both tiers hit the same files

```bash
curl -s -X POST http://localhost:8080/api/orders -H 'Content-Type: application/json' -d '{"memberNumber":"111000000042","warehouseCode":"W001","lines":[{"itemNumber":"1204590","quantity":12}]}'
```

Take option **3** on the terminal and the order is at the top of the subfile with source
`WEB` — allocated against the same `INVBAL` rows WMS210 displays.

---

## The debt, on purpose

Everything here is real, present, and deliberately left alone.

| Debt | Detail |
|---|---|
| Spring Boot **2.7.18** | Final 2.7.x; out of OSS support |
| **38 `javax.*` occurrences** | Across 11 files — 28 `javax.persistence`, 8 `javax.validation`, 2 `javax.servlet`. Spans all three modules. |
| **Springfox 3.0.0** | The project's only release ever. Unmaintained, **no Spring Boot 3 support at all** — a hard blocker, not an incidental dependency. |
| `ant_path_matcher` workaround | In `application.properties`, forcing pre-Boot-2.6 path matching. Without it Springfox will not start. Dead weight the moment Springfox goes. |
| 20 `io.swagger` annotations | `@Api` / `@ApiOperation` on every controller |
| Coverage at **5.0%** | See below |

**JaCoCo is the one dependency kept current.** The agent instruments JDK classes, so an
older build refuses to run on a newer runtime and would fail the baseline for reasons that
have nothing to do with the application.

### Upgrade target — version pairing matters

Boot **3.5.x** (currently 3.5.16) with springdoc **2.8.x** (currently 2.8.17).

Verified from the published parent POMs: **springdoc 2.6.0 is built against Spring Boot
3.3.0; springdoc 2.8.17 is built against Spring Boot 3.5.13.** Pairing Boot 3.5.x with
springdoc 2.6.0 fails. Pin away from Boot 4.x — springdoc's 2.8 line does not target it.

### Coverage baseline

Measured by JaCoCo at the `demo-start` tag:

| Module | Instruction coverage |
|---|---|
| `costco-core` | **2.2%** (51 / 2,295) |
| `costco-terminal` | 5.8% (229 / 3,966) |
| `costco-api` | 9.4% (71 / 754) |
| **Overall** | **5.0%** (351 / 7,015) |

Reports land at `<module>/target/site/jacoco/index.html` after `./mvnw clean verify`.

**Do the coverage work before the migration**, not after — it gives the migration a
regression net and makes "nothing broke" a demonstrated claim rather than an assertion.

---

## Resetting

```bash
npm run demo:reset
```

Restores the `demo-start` tag and rebuilds. **Destructive**: it discards tracked changes and
deletes untracked files inside this repository. It prints exactly what it will remove and
asks first; `./scripts/reset-demo.sh --force` skips the prompt.

It cannot touch anything outside this repository, which is why the demo kit lives elsewhere.

---

## If the room wants a repo we didn't write

Vetted alternatives, should the "you authored this" objection land:

| Repo | Why | Watch out |
|---|---|---|
| [OpenLiberty/sample.daytrader8](https://github.com/OpenLiberty/sample.daytrader8) | Java EE 8, ~34k LOC, 129 `javax` hits, zero tests. `mvn clean package liberty:run`, no app server install. **`sample.daytrader10` is the same app already migrated** — a public answer key. | Last commit Oct 2025 |
| [aws-containers/retail-store-sample-app](https://github.com/aws-containers/retail-store-sample-app) | MIT-0, retail domain, `src/ui` has 126 main files and 3 test files, no JaCoCo anywhere. | `cart`/`orders` need Docker; point at `ui` |
| [shopizer-ecommerce/shopizer](https://github.com/shopizer-ecommerce/shopizer) | Java 11 / Boot 2.5.12, full e-commerce, 37 test classes on H2. Best narrative fit. | ~100k LOC; pom references decommissioned Maven repos — **verify the build first** |

Pin any of these to a commit SHA before demoing.
