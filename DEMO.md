# Demo runbook

Everything needed to run this live, plus what to say and what to do when something breaks.

---

## Pre-flight (do this before the room fills)

```bash
./mvnw clean package && java -jar costco-api/target/costco-api-1.0.0-SNAPSHOT.jar
```

- [ ] Build green, application boots
- [ ] http://localhost:8080/wms renders the sign-on screen
- [ ] http://localhost:8080/swagger-ui/index.html loads
- [ ] Baseline coverage noted: **5.0% overall** (351 / 7,015 instructions)
- [ ] `git tag` shows `demo-start`
- [ ] `npm run demo:reset` tested at least once
- [ ] Terminal window sized so all 80 columns fit without wrapping

Sign on with any user id. There is no authentication.

---

## The arc

### 1. Frame the customer (2 min)

Costco is a $275B business running an **11.12% gross margin**, where **membership fees are
51.3% of operating income**. Strip those out and merchandise operations run at roughly
**1.9%**. The company's own 10-K credits "volume purchasing, efficient distribution and
reduced handling" for its advantage — technology appears only as a risk to be survived.

The punchline that makes IT structurally hard: CEO Ron Vachris has said technology
productivity gains get **passed to members as lower prices**, not retained as margin. IT
can never buy its way out with headcount, because efficiency is given away by design.

And the legacy estate is real and actively staffed — Costco's own job postings recruit
**RPG, CL, DB2/400, SEU/SDA/PDM** developers for "Depot Solutions" and "WHS Solutions"
teams, with a deadline in **October 2025**. This is not a system anyone is sunsetting.

> Pull up a real job posting here rather than a slide. It is the single most credible
> artifact available, and it is public.

### 2. Show the world they live in (2 min)

Open http://localhost:8080/wms. Sign on. Take option **2**.

Point out that this is not a costume: 24×80 fixed grid, whole-screen submit only, F3 exits
the program while F12 backs up one level, quality-hold stock showing real on-hand but zero
available. Then option **3** — orders, with `WEB` and `TRM` in the source column.

Place an order from Swagger or curl, press **F5** on WMS310, and it appears at the top:

```bash
curl -s -X POST http://localhost:8080/api/orders -H 'Content-Type: application/json' -d '{"memberNumber":"111000000042","warehouseCode":"W001","lines":[{"itemNumber":"1204590","quantity":12}]}'
```

**Say:** one system of record, two front doors. This is what "modernize around the core"
actually looks like, and it is why the core cannot simply be turned off.

### 3. Name the debt (1 min)

Spring Boot **2.7.18** (out of support). `javax.*` throughout. **Springfox 3.0.0** — the
only release that project ever made, unmaintained, with no Spring Boot 3 support at all.
A `spring.mvc.pathmatch.matching-strategy=ant_path_matcher` line in
`application.properties` that exists purely so Springfox will start. **5.0% test coverage.**

**Say:** every one of these is individually boring and collectively expensive. This is one
service. Costco has an estate of them.

### 4. Hand it to Devin (the main event)

**Task one — framework upgrade:**

> Upgrade this repository from Spring Boot 2.7.18 to Spring Boot 3.3.x and Java 17 to
> Java 21. Migrate every `javax.*` import to `jakarta.*`. Replace Springfox
> (`springfox-boot-starter` 3.0.0) with `springdoc-openapi-starter-webmvc-ui` 2.6.0,
> converting the `Docket` bean and the `io.swagger.annotations` `@Api`/`@ApiOperation`
> annotations to their springdoc equivalents. Remove the
> `spring.mvc.pathmatch.matching-strategy` workaround once Springfox is gone. Success
> criteria: `./mvnw clean verify` passes, the application boots, and the terminal at
> `/wms` still renders correctly.

**Task two — coverage:**

> Raise test coverage substantially with tests that assert real behaviour, not just
> execute lines. Priorities: `LegacyDateCodec` CYYMMDD round-trips and the zero-date
> sentinel; `InventoryService.availableQuantity` excluding non-nettable QC/DM stock;
> `OrderService.placeOrder` partial allocation producing backordered lines; `Order`
> recomputing totals rather than trusting `OHTOTL`; `ScreenBuffer` attribute-cell and
> truncation behaviour; subfile paging boundaries in the screen handlers.

While it works, keep the terminal open. The argument lands hardest when the green screen
**keeps working** through the upgrade.

### 5. Land the business impact (2 min)

Anchor on what the room just watched, not on invented figures. Costco does **not** disclose
IT spend, so any "% of revenue vs. Walmart" comparison would be fabricated — don't.

Use bottom-up math instead: state how long this upgrade takes a competent engineer by hand
(a javax→jakarta migration plus a Springfox replacement across three modules is
realistically days, not hours), compare it to the elapsed time on screen, then multiply by
an estate. The multiplier is the argument; the demo supplies the unit.

---

## If something breaks

| Problem | Response |
|---|---|
| Build red before you start | `npm run demo:reset` — restores `demo-start` and rebuilds |
| Agent's changes broke the app | That is a legitimate demo moment. Show the failing build, let it iterate. Reset if time is short. |
| Port 8080 busy | `lsof -iTCP:8080 -sTCP:LISTEN` then kill, or set `--server.port=8081` |
| Terminal columns wrapping | Narrow the browser zoom; the grid is a fixed 80 `ch` |
| Someone objects "you wrote this repo" | Answer honestly, then pivot to a fallback below |

### Fallback public repos (vetted, if the room wants third-party code)

| Repo | Why | Watch out |
|---|---|---|
| [OpenLiberty/sample.daytrader8](https://github.com/OpenLiberty/sample.daytrader8) | Java EE 8, 141 files, ~34k LOC, 129 `javax` hits, zero tests. Builds with `mvn clean package liberty:run` — no app server install. **`sample.daytrader10` is the same app already migrated**, so you have a public answer key. | Last commit Oct 2025 |
| [aws-containers/retail-store-sample-app](https://github.com/aws-containers/retail-store-sample-app) | MIT-0, retail domain, `src/ui` has **126 main files and 3 test files**, no JaCoCo anywhere. `./mvnw test`, no AWS account. | `cart`/`orders` need Docker; point at `ui` |
| [shopizer-ecommerce/shopizer](https://github.com/shopizer-ecommerce/shopizer) | Java 11 / Boot 2.5.12, full e-commerce, 37 test classes on H2. Best narrative fit. | ~100k LOC; pom references decommissioned Maven repos — **verify the build first** |

Pin any of these to a commit SHA before the demo.

---

## Claims that are safe, and claims that are not

**Safe** (all from the FY2025 10-K or Costco's own postings): the margin and membership
figures; 914 warehouses in 15 countries with e-commerce in 8; fewer than 4,000 SKUs per
warehouse; 341,000 employees with ~95% in warehouses; the IT risk-factor quote; the RPG/CL
job postings; Galanti's "late to the game on purpose"; Vachris on "fundamental base
systems and core systems behind the scenes."

**Not safe** — do not say these:
- *"Costco runs on hardware from 1988."* Traces to one unsourced social post, and is wrong:
  IBM i runs on current IBM Power servers. A technical buyer will catch it.
- *"Costco spends X% of revenue on IT."* Not disclosed anywhere. Any figure is invented.
- *"Costco's POS runs on AS/400."* No evidence. The evidence covers warehouse, depot and
  merchandising systems.
- Any Costco IT headcount number. The circulating figures trace to lead-generation sites.
