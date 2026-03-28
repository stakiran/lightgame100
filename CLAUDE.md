# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Minecraft Fabric mod (1.21.4) — a minigame where players compete to light up underground air blocks within a 100-block world border. Written in Java 21.

## Build & Run

```bash
./gradlew build          # Build → build/libs/lightgame100-*.jar
./gradlew vscode         # Generate VSCode project files
```

Debug: VSCode Run and Debug → "Minecraft Client"

No test suite exists.

## Architecture

Four classes in `src/main/java/com/stakiran/lightgame/`:

- **LightGameMod** — Entrypoint (`onInitialize`). Registers commands and tick events.
- **LightGameCommand** — Brigadier command tree for `/lightgame {setup,start,stop,score}`. Validates game state, delegates to GameManager.
- **GameManager** — Core game logic. Manages phase state machine (IDLE → PREVIEW → GAME → ENDED), world border setup, air block snapshot (y < 64), score calculation via block light levels, and tick-based timers (30s preview, 10min game). All state is static.
- **ScoreboardManager** — Sidebar scoreboard display during the game phase. Shows time remaining, lit count, total, and percentage.

Game flow: `setup` sets world border and center position → `start` snapshots all air blocks below y=64, enters 30s spectator preview → transitions to 10min survival game phase → `stop` (manual or timeout) shows final score.

Score = count of snapshotted air blocks that now have block light ≥ 1.

## Key Details

- All game state is static fields on GameManager — single game instance only.
- Versions are defined in `gradle.properties` (Minecraft, Yarn mappings, Fabric loader/API).
- Mod metadata in `src/main/resources/fabric.mod.json`.
- Language: UI messages are in Japanese.
