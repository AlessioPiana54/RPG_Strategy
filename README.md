# Tactical RPG — Rpg_Strategy

Un gioco di ruolo tattico a turni realizzato con JavaFX, JAXB e Gradle.

## Descrizione

Gioco di combattimento su griglia 2D turn-based in cui il giocatore controlla un gruppo di unità (Guerriero, Mago, Arciere) contro avversari gestiti da un'IA. Ogni unità ha un ciclo di azione per turno: **READY → MOVED → EXHAUSTED**, dopodichè il turno passa al nemico e poi si ricomincia.

### Funzionalità principali

- **Mappa a griglia** con tre tipi di terreno: PLAIN, FOREST (percorribile), MOUNTAIN (ostacolo)
- **Tre classi di unità** con statistiche bilanciate:
  - **Guerriero** — 120 HP, gittata movimento 3, attacco corpo a corpo
  - **Mago** — 80 HP, gittata movimento 2, attacco a distanza + cura
  - **Arciere** — 90 HP, gittata movimento 3, attacco a distanza
- **Ciclo di turno** con stato d'azione per unità:
  - `READY` — può muoversi e agire
  - `MOVED` — si è già mossa, può ancora agire
  - `EXHAUSTED` — ha completato il turno
- **IA nemica** con pathfinding BFS verso il bersaglio più vicino
- **Salvataggio/caricamento** in XML via JAXB (`~/.tacticarpg/saves/`)
- **Selezione mappe** predefinite al lancio (foresta e pianura)
- **Feedback visivo**:
  - Unità EXHAUSTED: opacità ridotta (0.4)
  - Unità MOVED: bordo tratteggiato azzurro
  - Turno nemico: controlli disabilitati + etichetta "Turno nemico..."

## Requisiti

- Java 25 (JDK)
- Gradle Wrapper incluso (`gradlew` / `gradlew.bat`)
- Connessione internet al primo avvio (scarica JavaFX e JAXB da Maven Central)

## Avvio

```bash
./gradlew run          # Linux / macOS
gradlew.bat run        # Windows
```

## Build completa

```bash
./gradlew build
```

Produce:
- `build/libs/Rpg_Strategy.jar` — jar standard
- `build/libs/Rpg_Strategy-all.jar` — fat jar con dipendenze incluse

## Test

```bash
./gradlew test
```

Suite JUnit 5 che copre:
- `BattleEngineTest` — movimento, abilità, eventi
- `DefaultGameControllerTest` — flusso di gioco, selezione, undo
- `TurnLogicTest` — logica ActionState e ciclo di turno
- `SimpleAIStrategyTest` — pathfinding e attacchi IA
- `TurnManagerTest` — ordine di attivazione unità
- `XmlGameRepositoryTest` — round-trip serializzazione XML

## Struttura del progetto

```
src/main/java/it/unicam/cs/mpgc/rpg125627/
├── model/          # Dominio: Unit, GameState, GridMap, Ability, ActionState, …
├── controller/     # Logica: DefaultGameController, BattleEngine, SimpleAIStrategy, …
├── persistence/    # I/O: XmlGameRepository, MapLoader, SaveManager, …
└── view/           # UI JavaFX: MapView, ActionBar, GameViewController, …

src/main/resources/maps/
├── map_forest.xml  # Mappa 8×8 con bosco e montagne
└── map_plains.xml  # Mappa 6×6 tutto pianura

src/test/java/…     # Test JUnit 5
```

## Licenza

MIT — Copyright 2026 AlessioPiana54
