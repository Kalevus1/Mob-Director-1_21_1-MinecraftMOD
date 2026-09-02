# Mob Director

**Mob Director** es un mod que convierte los comandos de Minecraft en un "panel de
dirección" para los mobs: te permite hacer que un mob **ataque** a otro, que **siga** a
una entidad, que **camine** o se **deslice de forma cinemática** a una coordenada, que
**mire** a un punto, y seleccionar mobs concretos con la **mira**.

Está pensado para **grabar cinemáticas / documentales** y para **montar eventos y
minijuegos**, donde necesitas que los mobs actúen como actores en vez de por su IA.

Es un **mod multiloader**: el mismo mod está disponible para **Fabric**, **NeoForge** y
**Forge**, compartiendo el mismo código.

---

## Versiones y loaders

Todo para **Minecraft 1.21.1** y **Java 21**. Elige el jar según tu loader:

| Loader | Versión objetivo | Jar a instalar | Necesita además |
|---|---|---|---|
| **Fabric** | Loader 0.19.3+ | `mobdirector-fabric-1.5.0.jar` | **Fabric API** |
| **NeoForge** | 21.1.x | `mobdirector-neoforge-1.5.0.jar` | — |
| **Forge** | 52.x | `mobdirector-forge-1.5.0.jar` | — |

> ⚠️ **Solo 1.21.1.** No cargará en otras versiones de Minecraft sin recompilar.
> Los mappings usados son los **oficiales de Mojang** (los mismos en los tres loaders).

---

## Instalación

1. Instala el **loader** que uses (Fabric, NeoForge o Forge) para **1.21.1**.
2. Consigue el jar de **tu loader** (tabla de arriba): descárgalo de la pestaña
   [**Releases**](../../releases) del repositorio, o compílalo tú (ver
   [Compilar](#compilar-desde-el-código)).
3. Cópialo en tu carpeta `mods`:
   - **Fabric** → añade también la **Fabric API** para 1.21.1 (Modrinth/CurseForge).
   - **NeoForge / Forge** → no necesitan nada más.
4. Arranca Minecraft con el perfil del loader.

Para usar los comandos necesitas **nivel de operador 2** (activa los trucos en tu mundo,
o hazte OP en un servidor).

---

## Cómo funciona

- Todos los comandos cuelgan de la raíz **`/mobctl`**.
- Los blancos (`<mobs>`, `<objetivo>`…) son **selectores de entidad normales** de Minecraft:
  `@e`, `@p`, `@e[tag=...]`, `@e[type=...]`, etc. Así puedes usar **etiquetas**, límites,
  distancia y filtros NBT.
- Los comportamientos continuos (seguir, ataque persistente, deslizarse) se guardan como
  **"directivas"** que el mod reaplica **cada tick del servidor**. Cada mob tiene como
  máximo **una directiva activa**: un comando nuevo reemplaza al anterior, y `stop` la cancela.

---

## Comandos

Todos requieren **OP nivel 2**. Sintaxis: `[ ]` = opcional, `< >` = obligatorio.

### Selección

| Comando | Qué hace |
|---|---|
| `/mobctl select <etiqueta>` | Etiqueta **exactamente** el mob que estás **mirando** (raycast, 64 bloques). Sin ambigüedad entre, p. ej., un adulto y su cría. **Solo jugadores.** |
| `/mobctl deselect <etiqueta>` | Quita esa etiqueta de **todos** los mobs que la tengan y **detiene** su comportamiento, para liberar la etiqueta y usarla con otro. |

### Movimiento

| Comando | Qué hace |
|---|---|
| `/mobctl moveto <mobs> <x y z> [velocidad]` | Los mobs **caminan** (pathfinding) hacia la posición. Velocidad por defecto `1.0`. Solo alcanza destinos **cercanos** (límite del pathfinding). |
| `/mobctl glide <mobs> <x y z> <ticks> [ground\|air]` | Los mobs se **deslizan de forma cinemática** hacia la posición en `N` ticks (20 ticks = 1 s), en **línea recta** y mirando hacia el destino. `ground` (por defecto) los mantiene **encima del terreno**; `air` usa la **Y literal** (mobs voladores / tomas aéreas). Ignora distancia y colisiones. |
| `/mobctl follow <mobs> <objetivo> [velocidad]` | Los mobs **siguen** continuamente a una entidad, aunque se mueva. Usa pathfinding. |
| `/mobctl look <mobs> <x y z>` | Los mobs **giran** para mirar hacia un punto (orientación puntual). |

### Combate

| Comando | Qué hace |
|---|---|
| `/mobctl attack <atacantes> <objetivo> [persistente]` | Los atacantes fijan como **objetivo** al objetivo y lo atacan. Con `true` **reafirman** el objetivo cada tick (no lo pierden). Los atacantes se marcan como persistentes para que no despawneen. |

### Control

| Comando | Qué hace |
|---|---|
| `/mobctl stop <mobs>` | Cancela la directiva activa, olvida el objetivo y para la ruta. Devuelve al mob a su IA normal. |
| `/mobctl delay <segundos> <comando...>` | Programa **cualquier** comando para ejecutarse tras N segundos, con **cuenta atrás** en la barra de acción ("Grabando en 3…"). Ideal para colocar la cámara antes de que el mob se mueva. El comando interior se escribe con o sin la `/` inicial. |

---

## Etiquetas (tags)

La forma más potente de dirigir mobs es por **etiquetas**. Dos maneras de ponerlas:

**A) Con la mira (exacto):**
```
/mobctl select oso
```

**B) Con el comando vanilla `tag` (por tipo/filtro):**
```
tag @e[type=zombie] add guardias
tag @e[type=polar_bear,nbt={Age:0}] add oso     # nbt={Age:0} = adulto; negativo = cría
```

Y luego los diriges por esa etiqueta:
```
/mobctl attack @e[tag=guardias] @e[tag=oso,limit=1] true
/mobctl glide  @e[tag=oso] -1212 62 2796 200
/mobctl stop   @e[tag=guardias]
```

---

## Flujos de ejemplo

### Dirigir un mob concreto: mirar → etiquetar → dirigir → soltar
```
/mobctl select oso                                   # miras al mob y lo etiquetas
/mobctl glide  @e[tag=oso] -1212 62 2796 200         # lo deslizas al destino en 10 s
/mobctl stop   @e[tag=oso]
/mobctl deselect oso                                 # lo sueltas y liberas la etiqueta
```

### Grabar con la cámara lista
```
/mobctl select oso
/mobctl delay 3 mobctl glide @e[tag=oso] -1212 62 2796 200
# durante la cuenta atrás cambias a tu cámara/espectador y el oso arranca solo
```

---

## Notas y limitaciones

- **`moveto` / `follow`** usan el pathfinding del juego: si el destino está lejos o no
  hay camino posible, el mob no se moverá. Para distancias largas o exactas usa **`glide`**.
- **`glide`** mueve por interpolación en línea recta con la IA desactivada durante el
  trayecto (`setNoAi`), sin velocidad de física, para evitar zigzag y tambaleo. Al terminar
  se restauran su gravedad y su IA. En modo `ground` la altura sigue el terreno columna a
  columna (puede notarse un leve escalonado en pendientes pronunciadas).
- **`attack`**: la mayoría de mobs hostiles perseguirán y golpearán; algunos mobs pacíficos
  no tienen IA de ataque cuerpo a cuerpo, así que no harán daño aunque se les fije el objetivo.
- **`select`** necesita el punto de mira, así que **solo lo puede ejecutar un jugador**.

---

## ¿Qué es Gradle y para qué se usa?

**Gradle** es la herramienta que **construye** (compila y empaqueta) el mod. En vez de
compilar los `.java` a mano, describes el proyecto en unos archivos y Gradle se encarga
de todo el trabajo pesado.

**Qué hace por ti:**
- **Descarga las dependencias** automáticamente: Minecraft, el loader (Fabric/NeoForge/Forge),
  la Fabric API, los mappings de Mojang… No tienes que buscar ni copiar nada a mano.
- **Compila** el código Java contra la versión correcta de Minecraft.
- **Empaqueta** el resultado en el `.jar` instalable, con sus metadatos.

**No necesitas instalar Gradle.** El proyecto trae el **Gradle Wrapper**
(`gradlew` / `gradlew.bat`): un pequeño lanzador que descarga y usa la versión exacta de
Gradle que necesita el proyecto. Por eso siempre se ejecuta `./gradlew ...`, nunca `gradle` a secas.

**Archivos de Gradle en este proyecto:**

| Archivo | Para qué sirve |
|---|---|
| `settings.gradle` | Lista los subproyectos del monorepo (`fabric`, `neoforge`, `forge`) y de dónde bajar los plugins. |
| `gradle.properties` | Las **versiones** en un solo sitio (Minecraft, loaders, versión del mod). |
| `build.gradle` (raíz) | Configuración compartida por los tres loaders. |
| `fabric/`, `neoforge/`, `forge/` → `build.gradle` | Cómo construir **cada** loader (su plugin y sus dependencias). |
| `gradlew`, `gradlew.bat`, `gradle/wrapper/` | El **wrapper**: no se toca, y **sí** debe subirse a GitHub para que otros compilen. |

---

## Compilar desde el código

Necesitas un **JDK 21** (nada más — Gradle baja el resto).

```bash
git clone https://github.com/<usuario>/mob-director.git
cd mob-director

# Compilar los TRES loaders de una vez:
./gradlew build                 # en Windows: gradlew.bat build

# O solo uno:
./gradlew :fabric:build
./gradlew :neoforge:build
./gradlew :forge:build
```

Los `.jar` aparecen en la carpeta `build/libs/` de **cada** loader:
```
fabric/build/libs/mobdirector-fabric-1.5.0.jar
neoforge/build/libs/mobdirector-neoforge-1.5.0.jar
forge/build/libs/mobdirector-forge-1.5.0.jar
```
(Usa el `.jar` normal, no el `-sources.jar`.)

> ¿No tienes un JDK 21? El propio **launcher de Minecraft** ya incluye uno
> (`.minecraft/runtime/java-runtime-delta`); puedes apuntar `JAVA_HOME` a esa ruta.
> La primera compilación de NeoForge/Forge tarda varios minutos (descompilan Minecraft);
> las siguientes van mucho más rápido.

---

## Estructura del proyecto (monorepo)

La lógica se escribe **una sola vez** en `common/` y los tres loaders la comparten. Cada
carpeta de loader solo añade su "arranque" (cómo engancha sus eventos).

```
mob-director/
├── settings.gradle · build.gradle · gradle.properties   · configuración raíz
├── gradlew · gradlew.bat · gradle/wrapper/               · Gradle Wrapper
│
├── common/                                               · CÓDIGO COMPARTIDO (loader-agnóstico)
│   └── src/main/java/com/mobdirector/
│       ├── MobDirectorCommon.java   · constantes + puente de eventos
│       ├── MobCommands.java         · árbol de comandos /mobctl (Brigadier)
│       ├── Directives.java          · directivas por tick (follow / attack / glide)
│       └── Scheduler.java           · cola de comandos programados (/mobctl delay)
│
├── fabric/                                               · arranque + metadatos Fabric
│   ├── build.gradle
│   └── src/main/java/com/mobdirector/fabric/MobDirectorFabric.java
│   └── src/main/resources/fabric.mod.json
│
├── neoforge/                                             · arranque + metadatos NeoForge
│   ├── build.gradle
│   └── src/main/java/com/mobdirector/neoforge/MobDirectorNeoForge.java
│   └── src/main/resources/META-INF/neoforge.mods.toml
│
└── forge/                                                · arranque + metadatos Forge
    ├── build.gradle
    └── src/main/java/com/mobdirector/forge/MobDirectorForge.java
    └── src/main/resources/META-INF/mods.toml
```

**Qué es específico de cada loader** (lo único que cambia entre los tres):
- Cómo se declara el mod (`ModInitializer` en Fabric; `@Mod` en NeoForge/Forge).
- Cómo se enganchan los eventos de *registrar comandos* y *tick del servidor*.
- El archivo de metadatos (`fabric.mod.json` / `neoforge.mods.toml` / `mods.toml`).

Todo lo demás (los 9 comandos y su lógica) vive en `common/` y es idéntico, porque usa
clases de Minecraft con los **mismos nombres** (mappings de Mojang) en los tres loaders.

## Licencia

CC0-1.0.
