<p align="center">
  <img src="docs/rpg-strategy.webp" alt="Tactical RPG — Rpg_Strategy" width="400">
</p>

# 📌 Tactical RPG — Rpg_Strategy

![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-UI-1E88E5?style=for-the-badge&logo=openjdk&logoColor=white)
![JAXB](https://img.shields.io/badge/JAXB-XML-6A1B9A?style=for-the-badge)
![Gradle](https://img.shields.io/badge/Gradle-Build-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit-5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

Turn-based tactical role-playing game built with JavaFX, JAXB and Gradle.  
The player controls a group of units on a 2D grid and fights against opponents driven by an AI,
alternating movement and abilities in a structured turn cycle.

---

## 🚀 How to run the project

### Prerequisites
- Java 25 (LTS)
- Gradle (wrapper included — no installation required)
- Internet connection on first launch (downloads JavaFX and JAXB from Maven Central)

### Instructions

```bash
git clone <repository-url>
cd RPG_Strategy
```

### Build the project
```bash
./gradlew build
```

### Run
```bash
./gradlew run          # Linux / macOS / Windows (Git Bash)
.\gradlew.bat run      # Windows (PowerShell / Command Prompt)
```

> The build also produces a fat-jar with all dependencies included:
> `build/libs/rpg-strategy-1.0-SNAPSHOT-all.jar`

---

## 🎮 Main features

- **Grid map** with three terrain types: PLAIN, FOREST (walkable), MOUNTAIN (obstacle)
- **Three unit classes** with balanced stats:
  - **Warrior** — 120 HP, range 3, melee attack
  - **Mage** — 70 HP, range 2, ranged attack + healing
  - **Archer** — 90 HP, range 3, ranged attack
- **Per-unit action cycle** within the turn: `READY → MOVED → EXHAUSTED`
- **Enemy AI** with BFS pathfinding towards the nearest target
- **Save / load** games in XML (`~/.tacticarpg/saves/`)
- **Built-in map selection** at launch (forest and plains)

---

## 🧪 Tests

```bash
./gradlew test
```

JUnit 5 suite covering:

| Test class | What it verifies |
|---|---|
| `BattleEngineTest` | movement, abilities, combat engine events |
| `DefaultGameControllerTest` | game flow, unit selection, undo |
| `TurnLogicTest` | ActionState logic and turn cycle |
| `SimpleAIStrategyTest` | enemy AI pathfinding and attacks |
| `TurnManagerTest` | unit activation order |
| `XmlGameRepositoryTest` | XML serialization round-trip |

---

## 📁 Project structure

```
src/main/java/it/unicam/cs/mpgc/rpg125627/
├── model/          # Domain: Unit, GameState, GridMap, Ability, …
├── controller/     # Logic: DefaultGameController, BattleEngine, SimpleAIStrategy, …
├── persistence/    # I/O: XmlGameRepository, XmlMapRepository, SaveManager, …
└── view/           # JavaFX UI: MapView, ActionBar, GameViewController, …

src/main/resources/maps/
├── map_forest.xml  # 8×8 map with woods and mountains
└── map_plains.xml  # 6×6 map, all plains

src/test/java/…     # JUnit 5 suite
```

---

## 🤖 Use of AI tools

During the development of the project, AI tools were used as support in specific phases of the work:

- **Writing JUnit 5 tests** — generation of the initial structure of the test cases, choice of the scenarios to cover and of the boundary values; each test was then read, understood and manually validated before being included in the suite.
- **Writing Javadoc** — automatic drafting of comments on public classes and methods; the text was reviewed and adapted to faithfully reflect the actual behavior of the code.
- **Bug research and analysis** — the AI was consulted to identify the cause of unexpected behavior (e.g. UI event desynchronization, JAXB serialization errors); the proposed solutions were evaluated and integrated only after manual verification.
- **Graphical improvements** — design and implementation of advanced visual features: Unicode symbols for terrains and unit classes, HP bars on the grid, animated floating damage numbers, pulsing effect on selection, class-specific geometric shapes (circle/star/triangle) with shadows, and centralization of all styles in an external CSS stylesheet; every choice was reviewed and adapted to the project context.


---

## ⚠️ Notes

- The fat-jar (`*-all.jar`) includes JavaFX: it can be run with `java -jar` on any JDK 25 with no additional configuration.
- Saves are cross-platform compatible as long as the same project version is used.

---

## 📄 License

MIT — Copyright 2026 AlessioPiana54
