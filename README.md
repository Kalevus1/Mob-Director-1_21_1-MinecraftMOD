# 🎬 Mob Director

> **Dirige a los mobs como actores, con comandos.**
> Haz que un mob ataque a otro, siga a una entidad, camine o se deslice de forma cinemática a
> una coordenada, mire a un punto… y selecciónalos con la mira. Para grabar cinemáticas y montar
> eventos, sin dejarlos a su IA.

<p align="center">
  <a href="README.md"><img alt="Español" src="https://img.shields.io/badge/Español-2f8f4e?style=for-the-badge"></a>
  <a href="README.en.md"><img alt="English" src="https://img.shields.io/badge/English-6b7280?style=for-the-badge"></a>
  <a href="README.fi.md"><img alt="Suomi" src="https://img.shields.io/badge/Suomi-6b7280?style=for-the-badge"></a>
</p>

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-6b8f3a)
![Loaders](https://img.shields.io/badge/Loaders-Fabric%20·%20NeoForge%20·%20Forge-2f8f4e)
![Versión](https://img.shields.io/badge/versión-1.5.0-2f8f4e)
![Java](https://img.shields.io/badge/Java-21-informational)
![Licencia](https://img.shields.io/badge/licencia-CC0--1.0-lightgrey)

Mod por **Kalevi Latva-äijö**. Convierte los comandos en un "panel de dirección" para mobs:
actúan como actores en vez de por su IA. Su complemento, **Mob Controller**, te deja además
*meterte dentro* de un mob y verlo por sus ojos.

> 📁 **Este repositorio:** `downloads/` tiene los **jars listos para jugar** · `source/` tiene
> el **código** (solo si quieres compilarlo tú).

---

## ✨ Cómo funciona

- Todos los comandos cuelgan de la raíz **`/mobctl`** y requieren **OP nivel 2**.
- Los blancos (`<mobs>`, `<objetivo>`…) son **selectores de entidad** normales: `@e`, `@p`,
  `@e[tag=…]`, `@e[type=…]`… así usas **etiquetas**, límites y filtros NBT.
- Los comportamientos continuos (seguir, ataque persistente, deslizarse) se guardan como
  **directivas** que se reaplican **cada tick**. Cada mob tiene una directiva activa; un comando
  nuevo reemplaza al anterior y `stop` la cancela.

---

## 🎮 Comandos

Sintaxis: `< >` obligatorio · `[ ]` opcional.

| Comando | Qué hace |
|---|---|
| `/mobctl select <etiqueta>` | Etiqueta **exacto** el mob que **miras** (raycast, 64 bloques). Solo jugadores. |
| `/mobctl deselect <etiqueta>` | Quita la etiqueta de **todos** los que la tengan y los **detiene**. |
| `/mobctl moveto <mobs> <x y z> [vel]` | **Caminan** (pathfinding) hacia una posición cercana. |
| `/mobctl glide <mobs> <x y z> <ticks> [ground\|air]` | Se **deslizan** en línea recta (cinemático) al destino en N ticks. `ground` sigue el terreno; `air` usa la Y literal. |
| `/mobctl follow <mobs> <objetivo> [vel]` | **Siguen** a una entidad en movimiento. |
| `/mobctl look <mobs> <x y z>` | **Giran** a mirar un punto. |
| `/mobctl attack <atacantes> <objetivo> [persistente]` | **Atacan** al objetivo; con `true` lo reafirman cada tick. |
| `/mobctl stop <mobs>` | **Cancelan** directiva, objetivo y ruta (vuelven a su IA). |
| `/mobctl delay <segundos> <comando…>` | **Programa** un comando tras N s con cuenta atrás (para colocar la cámara). |

### Etiquetas + ejemplo

```
/mobctl select oso                              # miras al mob y lo etiquetas (exacto)
/mobctl glide  @e[tag=oso] -1212 62 2796 200    # lo deslizas al destino en 10 s
/mobctl stop   @e[tag=oso]
/mobctl deselect oso                            # lo sueltas y liberas la etiqueta
```

---

## 📦 Instalación (jugadores)

1. Descarga el jar de **tu loader** desde la carpeta **[`downloads/`](downloads/)** o desde la
   pestaña **[Releases]** → `mobdirector-<loader>-1.5.0.jar`.
2. Colócalo en `.minecraft/mods`. En **Fabric** añade también **[Fabric API]**.
3. Entra a un mundo con **trucos** (OP nivel 2) y usa `/mobctl …`.

[Releases]: ../../releases
[Fabric API]: https://modrinth.com/mod/fabric-api

---

## 🧩 Loaders

Todo para **Minecraft 1.21.1** · **Java 21** · mappings **oficiales de Mojang**.

| Loader | Versión | Jar |
|---|---|---|
| **Fabric** | loader 0.19.3+ · Fabric API 0.116.13+1.21.1 | `mobdirector-fabric-1.5.0.jar` |
| **NeoForge** | 21.1.x | `mobdirector-neoforge-1.5.0.jar` |
| **Forge** | 52.x | `mobdirector-forge-1.5.0.jar` |

---

## 🛠️ Compilar desde el código (desarrolladores)

El código está en **`source/`** — un monorepo **multiloader**: `common/` tiene los 9 comandos y
`fabric/`, `neoforge/`, `forge/` solo el arranque de cada loader. Solo necesitas un **JDK 21**.

```bash
# Java 21 (aquí, del launcher de Minecraft):
export JAVA_HOME=".../.minecraft/runtime/java-runtime-delta/windows/java-runtime-delta"

cd source
./gradlew build              # los 3 loaders
./gradlew :fabric:build      # o solo uno (:neoforge:build / :forge:build)
```

Los jars salen en `source/<loader>/build/libs/mobdirector-<loader>-1.5.0.jar`.
(La primera compilación de NeoForge/Forge tarda varios minutos: descompilan Minecraft.)

---

## 🗂️ Estructura del repositorio

```
mob-director/                      · repositorio
├── downloads/                     · los 3 jars listos para descargar
├── README.md · README.en.md · README.fi.md · index.html
└── source/                        · proyecto Gradle (para compilar)
    ├── common/src/main/java/com/mobdirector/    · CÓDIGO COMPARTIDO (loader-agnóstico)
    │     MobDirectorCommon · MobCommands · Directives · Scheduler
    ├── fabric/   → MobDirectorFabric   + fabric.mod.json
    ├── neoforge/ → MobDirectorNeoForge + neoforge.mods.toml
    └── forge/    → MobDirectorForge    + mods.toml
```

Toda la lógica vive **una vez** en `common/` y es idéntica en los tres loaders (usan los mismos
nombres Mojmap). Cada loader solo aporta cómo declara el mod y engancha sus eventos.

---

## 📄 Licencia y créditos

Mod creado por **Kalevi Latva-äijö**. Licencia **CC0-1.0** (dominio público): úsalo, modifícalo
y compártelo libremente.
