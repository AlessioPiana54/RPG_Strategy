# 📌 Tactical RPG — Rpg_Strategy

Gioco di ruolo tattico a turni realizzato con JavaFX, JAXB e Gradle.  
Il giocatore controlla un gruppo di unità su una griglia 2D e combatte contro avversari gestiti da un'IA,
alternando movimenti e abilità in un ciclo di turno strutturato.

---

## 🚀 Come eseguire il progetto

### Prerequisiti
- Java 25 (LTS)
- Gradle (wrapper incluso — nessuna installazione necessaria)
- Connessione internet al primo avvio (scarica JavaFX e JAXB da Maven Central)

### Istruzioni

```bash
git clone <url-del-repository>
cd RPG_Strategy
```

### Build del progetto
```bash
./gradlew build
```

### Esecuzione
```bash
./gradlew run          # Linux / macOS / Windows (Git Bash)
.\gradlew.bat run      # Windows (PowerShell / Prompt dei comandi)
```

> La build produce anche un fat-jar con dipendenze incluse:
> `build/libs/rpg-strategy-1.0-SNAPSHOT-all.jar`

---

## 🎮 Funzionalità principali

- **Mappa a griglia** con tre tipi di terreno: PLAIN, FOREST (percorribile), MOUNTAIN (ostacolo)
- **Tre classi di unità** con statistiche bilanciate:
  - **Guerriero** — 120 HP, gittata 3, attacco corpo a corpo
  - **Mago** — 70 HP, gittata 2, attacco a distanza + cura
  - **Arciere** — 90 HP, gittata 3, attacco a distanza
- **Ciclo di azione per unità** nel turno: `READY → MOVED → EXHAUSTED`
- **IA nemica** con pathfinding BFS verso il bersaglio più vicino
- **Salvataggio / caricamento** partita in XML (`~/.tacticarpg/saves/`)
- **Selezione mappe** predefinite al lancio (foresta e pianura)

---

## 🧪 Test

```bash
./gradlew test
```

Suite JUnit 5 che copre:

| Classe di test | Cosa verifica |
|---|---|
| `BattleEngineTest` | movimento, abilità, eventi del motore di combattimento |
| `DefaultGameControllerTest` | flusso di gioco, selezione unità, undo |
| `TurnLogicTest` | logica ActionState e ciclo di turno |
| `SimpleAIStrategyTest` | pathfinding e attacchi dell'IA nemica |
| `TurnManagerTest` | ordine di attivazione delle unità |
| `XmlGameRepositoryTest` | round-trip di serializzazione XML |

---

## 📁 Struttura del progetto

```
src/main/java/it/unicam/cs/mpgc/rpg125627/
├── model/          # Dominio: Unit, GameState, GridMap, Ability, …
├── controller/     # Logica: DefaultGameController, BattleEngine, SimpleAIStrategy, …
├── persistence/    # I/O: XmlGameRepository, XmlMapRepository, SaveManager, …
└── view/           # UI JavaFX: MapView, ActionBar, GameViewController, …

src/main/resources/maps/
├── map_forest.xml  # Mappa 8×8 con bosco e montagne
└── map_plains.xml  # Mappa 6×6 tutto pianura

src/test/java/…     # Suite JUnit 5
```

---

## 🤖 Uso di strumenti di AI

Durante lo sviluppo del progetto sono stati utilizzati strumenti di AI come supporto in fasi specifiche del lavoro:

- **Scrittura dei test JUnit 5** — generazione della struttura iniziale dei casi di test, scelta degli scenari da coprire e dei valori di boundary; ogni test è stato poi letto, compreso e validato manualmente prima di essere incluso nella suite.
- **Scrittura dei Javadoc** — bozza automatica dei commenti su classi e metodi pubblici; il testo è stato revisionato e adattato per rispecchiare fedelmente il comportamento reale del codice.
- **Ricerca e analisi di bug** — l'AI è stata consultata per individuare la causa di comportamenti inattesi (es. desincronia degli eventi UI, errori di serializzazione JAXB); le soluzioni proposte sono state valutate e integrate solo dopo verifica manuale.
- **Miglioramenti grafici** — progettazione e implementazione di funzionalità visive avanzate: simboli Unicode per terreni e classi unità, barre HP sulla griglia, numeri di danno fluttuanti con animazione, effetto pulsante sulla selezione, forme geometriche classe-specifiche (cerchio/stella/triangolo) con ombra, e centralizzazione di tutti gli stili in un foglio CSS esterno; ogni scelta è stata rivista e adattata al contesto del progetto.


---

## ⚠️ Note

- Il fat-jar (`*-all.jar`) include JavaFX: può essere eseguito con `java -jar` su qualunque JDK 25 senza configurazione aggiuntiva.
- I salvataggi sono compatibili tra piattaforme purché si usi la stessa versione del progetto.

---

## 📄 Licenza

MIT — Copyright 2026 AlessioPiana54
