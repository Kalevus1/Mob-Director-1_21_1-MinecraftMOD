# 🎬 Mob Director

> **Direct mobs like actors, with commands.**
> Make one mob attack another, follow an entity, walk or cinematically glide to a coordinate,
> look at a point… and select them with your crosshair. For filming cinematics and running
> events, instead of leaving them to their AI.

<p align="center">
  <a href="README.md"><img alt="Español" src="https://img.shields.io/badge/Español-6b7280?style=for-the-badge"></a>
  <a href="README.en.md"><img alt="English" src="https://img.shields.io/badge/English-2f8f4e?style=for-the-badge"></a>
  <a href="README.fi.md"><img alt="Suomi" src="https://img.shields.io/badge/Suomi-6b7280?style=for-the-badge"></a>
</p>

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-6b8f3a)
![Loaders](https://img.shields.io/badge/Loaders-Fabric%20·%20NeoForge%20·%20Forge-2f8f4e)
![Version](https://img.shields.io/badge/version-1.5.0-2f8f4e)
![Java](https://img.shields.io/badge/Java-21-informational)
![License](https://img.shields.io/badge/license-CC0--1.0-lightgrey)

Mod by **Kalevi Latva-äijö**. It turns commands into a "director's console" for mobs: they act
like actors instead of following their AI. Its companion, **Mob Controller**, also lets you step
*inside* a mob and see through its eyes.

> 📁 **This repo:** `downloads/` has the **ready-to-play jars** · `source/` has the **code**
> (only if you want to build it yourself).

---

## ✨ How it works

- Every command hangs off the **`/mobctl`** root and requires **OP level 2**.
- Targets (`<mobs>`, `<target>`…) are normal **entity selectors**: `@e`, `@p`, `@e[tag=…]`,
  `@e[type=…]`… so you can use **tags**, limits and NBT filters.
- Continuous behaviors (follow, persistent attack, glide) are stored as **directives** reapplied
  **every tick**. Each mob has one active directive; a new command replaces the old one and
  `stop` cancels it.

---

## 🎮 Commands

Syntax: `< >` required · `[ ]` optional.

| Command | What it does |
|---|---|
| `/mobctl select <tag>` | Tags **exactly** the mob you're **looking at** (raycast, 64 blocks). Players only. |
| `/mobctl deselect <tag>` | Removes the tag from **all** that have it and **stops** them. |
| `/mobctl moveto <mobs> <x y z> [speed]` | They **walk** (pathfinding) to a nearby position. |
| `/mobctl glide <mobs> <x y z> <ticks> [ground\|air]` | They **glide** in a straight line (cinematic) to the target over N ticks. `ground` follows the terrain; `air` uses the literal Y. |
| `/mobctl follow <mobs> <target> [speed]` | They **follow** a moving entity. |
| `/mobctl look <mobs> <x y z>` | They **turn** to look at a point. |
| `/mobctl attack <attackers> <target> [persistent]` | They **attack** the target; with `true` they reassert it every tick. |
| `/mobctl stop <mobs>` | **Cancels** the directive, target and path (back to their AI). |
| `/mobctl delay <seconds> <command…>` | **Schedules** a command after N s with a countdown (to set up your camera). |

### Tags + example

```
/mobctl select bear                              # look at the mob and tag it (exact)
/mobctl glide  @e[tag=bear] -1212 62 2796 200    # glide it to the target over 10 s
/mobctl stop   @e[tag=bear]
/mobctl deselect bear                            # release it and free the tag
```

---

## 📦 Installation (players)

1. Download the jar for **your loader** from the **[`downloads/`](downloads/)** folder or the
   **[Releases]** tab → `mobdirector-<loader>-1.5.0.jar`.
2. Put it in `.minecraft/mods`. On **Fabric** also add **[Fabric API]**.
3. Enter a world with **cheats** (OP level 2) and use `/mobctl …`.

[Releases]: ../../releases
[Fabric API]: https://modrinth.com/mod/fabric-api

---

## 🧩 Loaders

Everything for **Minecraft 1.21.1** · **Java 21** · **official Mojang mappings**.

| Loader | Version | Jar |
|---|---|---|
| **Fabric** | loader 0.19.3+ · Fabric API 0.116.13+1.21.1 | `mobdirector-fabric-1.5.0.jar` |
| **NeoForge** | 21.1.x | `mobdirector-neoforge-1.5.0.jar` |
| **Forge** | 52.x | `mobdirector-forge-1.5.0.jar` |

---

## 🛠️ Building from source (developers)

The code lives in **`source/`** — a **multiloader** monorepo: `common/` holds the 9 commands and
`fabric/`, `neoforge/`, `forge/` only each loader's entry point. You just need a **JDK 21**.

```bash
# Java 21:
export JAVA_HOME=".../.minecraft/runtime/java-runtime-delta/windows/java-runtime-delta"

cd source
./gradlew build              # all three loaders
./gradlew :fabric:build      # or just one (:neoforge:build / :forge:build)
```

Jars land in `source/<loader>/build/libs/mobdirector-<loader>-1.5.0.jar`.
(The first NeoForge/Forge build takes a few minutes: they decompile Minecraft.)

---

## 🗂️ Repository layout

```
mob-director/                      · repository
├── downloads/                     · the 3 ready-to-download jars
├── README.md · README.en.md · README.fi.md · index.html
└── source/                        · Gradle project (to build)
    ├── common/src/main/java/com/mobdirector/    · SHARED CODE (loader-agnostic)
    │     MobDirectorCommon · MobCommands · Directives · Scheduler
    ├── fabric/   → MobDirectorFabric   + fabric.mod.json
    ├── neoforge/ → MobDirectorNeoForge + neoforge.mods.toml
    └── forge/    → MobDirectorForge    + mods.toml
```

All the logic lives **once** in `common/` and is identical across the three loaders (they use the
same Mojmap names). Each loader only provides how it declares the mod and hooks its events.

---

## 📄 License and credits

Mod created by **Kalevi Latva-äijö**. Licensed **CC0-1.0** (public domain): use, modify and share
it freely.
