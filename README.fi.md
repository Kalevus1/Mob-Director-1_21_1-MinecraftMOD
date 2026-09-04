# 🎬 Mob Director

> **Ohjaa mobeja näyttelijöinä komennoilla.**
> Laita mob hyökkäämään toisen kimppuun, seuraamaan entiteettiä, kävelemään tai liukumaan
> elokuvamaisesti koordinaattiin, katsomaan pistettä… ja valitse ne tähtäimellä.
> Elokuvakohtausten kuvaamiseen ja tapahtumiin, sen sijaan että ne jätettäisiin tekoälyyn.

<p align="center">
  <a href="README.md"><img alt="Español" src="https://img.shields.io/badge/Español-6b7280?style=for-the-badge"></a>
  <a href="README.en.md"><img alt="English" src="https://img.shields.io/badge/English-6b7280?style=for-the-badge"></a>
  <a href="README.fi.md"><img alt="Suomi" src="https://img.shields.io/badge/Suomi-2f8f4e?style=for-the-badge"></a>
</p>

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-6b8f3a)
![Loaderit](https://img.shields.io/badge/Loaderit-Fabric%20·%20NeoForge%20·%20Forge-2f8f4e)
![Versio](https://img.shields.io/badge/versio-1.5.0-2f8f4e)
![Java](https://img.shields.io/badge/Java-21-informational)
![Lisenssi](https://img.shields.io/badge/lisenssi-CC0--1.0-lightgrey)

Modin tekijä **Kalevi Latva-äijö**. Se muuttaa komennot mobien "ohjauspaneeliksi": ne toimivat
kuin näyttelijät tekoälynsä sijaan. Sen kumppani, **Mob Controller**, antaa lisäksi *astua mobin
sisään* ja nähdä sen silmien läpi.

> 📁 **Tämä repo:** `downloads/` sisältää **valmiit jarit pelaamiseen** · `source/` sisältää
> **koodin** (vain jos haluat kääntää sen itse).

---

## ✨ Miten toimii

- Kaikki komennot ovat juuren **`/mobctl`** alla ja vaativat **OP-tason 2**.
- Kohteet (`<mobs>`, `<kohde>`…) ovat tavallisia **entiteettivalitsimia**: `@e`, `@p`,
  `@e[tag=…]`, `@e[type=…]`… joten voit käyttää **merkintöjä**, rajoja ja NBT-suodattimia.
- Jatkuvat käytökset (seuraa, jatkuva hyökkäys, liuku) tallennetaan **direktiiveinä**, jotka
  toteutetaan uudelleen **joka tick**. Kullakin mobilla on yksi aktiivinen direktiivi; uusi
  komento korvaa vanhan ja `stop` peruu sen.

---

## 🎮 Komennot

Syntaksi: `< >` pakollinen · `[ ]` valinnainen.

| Komento | Mitä tekee |
|---|---|
| `/mobctl select <merkintä>` | Merkitsee **tarkalleen** mobin jota **katsot** (raycast, 64 lohkoa). Vain pelaajat. |
| `/mobctl deselect <merkintä>` | Poistaa merkinnän **kaikilta** joilla se on ja **pysäyttää** ne. |
| `/mobctl moveto <mobs> <x y z> [nopeus]` | **Kävelevät** (reitinhaku) lähellä olevaan sijaintiin. |
| `/mobctl glide <mobs> <x y z> <tickit> [ground\|air]` | **Liukuvat** suoraan (elokuvamaisesti) kohteeseen N tickissä. `ground` seuraa maastoa; `air` käyttää kirjaimellista Y:tä. |
| `/mobctl follow <mobs> <kohde> [nopeus]` | **Seuraavat** liikkuvaa entiteettiä. |
| `/mobctl look <mobs> <x y z>` | **Kääntyvät** katsomaan pistettä. |
| `/mobctl attack <hyökkääjät> <kohde> [pysyvä]` | **Hyökkäävät** kohteeseen; `true`:lla vahvistavat sen joka tick. |
| `/mobctl stop <mobs>` | **Peruu** direktiivin, kohteen ja reitin (takaisin tekoälyyn). |
| `/mobctl delay <sekunnit> <komento…>` | **Ajastaa** komennon N s päähän lähtölaskennalla (kameran asettamiseen). |

### Merkinnät + esimerkki

```
/mobctl select karhu                             # katso mobia ja merkitse se (tarkka)
/mobctl glide  @e[tag=karhu] -1212 62 2796 200   # liu'uta se kohteeseen 10 s aikana
/mobctl stop   @e[tag=karhu]
/mobctl deselect karhu                           # vapauta se ja merkintä
```

---

## 📦 Asennus (pelaajat)

1. Lataa **loaderisi** jar **[`downloads/`](downloads/)**-kansiosta tai **[Releases]**-
   välilehdeltä → `mobdirector-<loader>-1.5.0.jar`.
2. Aseta se kansioon `.minecraft/mods`. **Fabricissa** lisää myös **[Fabric API]**.
3. Mene maailmaan, jossa **huijaukset** ovat päällä (OP-taso 2), ja käytä `/mobctl …`.

[Releases]: ../../releases
[Fabric API]: https://modrinth.com/mod/fabric-api

---

## 🧩 Loaderit

Kaikki **Minecraft 1.21.1** · **Java 21** · **viralliset Mojang-mappaukset**.

| Loader | Versio | Jar |
|---|---|---|
| **Fabric** | loader 0.19.3+ · Fabric API 0.116.13+1.21.1 | `mobdirector-fabric-1.5.0.jar` |
| **NeoForge** | 21.1.x | `mobdirector-neoforge-1.5.0.jar` |
| **Forge** | 52.x | `mobdirector-forge-1.5.0.jar` |

---

## 🛠️ Kääntäminen lähdekoodista (kehittäjät)

Koodi on kansiossa **`source/`** — **multiloader**-monorepo: `common/` sisältää 9 komentoa ja
`fabric/`, `neoforge/`, `forge/` vain kunkin loaderin käynnistyksen. Tarvitset vain **JDK 21**:n.

```bash
# Java 21:
export JAVA_HOME=".../.minecraft/runtime/java-runtime-delta/windows/java-runtime-delta"

cd source
./gradlew build              # kaikki kolme loaderia
./gradlew :fabric:build      # tai vain yksi (:neoforge:build / :forge:build)
```

Jarit ilmestyvät kansioon `source/<loader>/build/libs/mobdirector-<loader>-1.5.0.jar`.
(Ensimmäinen NeoForge/Forge-käännös kestää muutaman minuutin: ne purkavat Minecraftin.)

---

## 🗂️ Repositorion rakenne

```
mob-director/                      · repositorio
├── downloads/                     · 3 valmista ladattavaa jaria
├── README.md · README.en.md · README.fi.md · index.html
└── source/                        · Gradle-projekti (kääntämiseen)
    ├── common/src/main/java/com/mobdirector/    · JAETTU KOODI (loader-riippumaton)
    │     MobDirectorCommon · MobCommands · Directives · Scheduler
    ├── fabric/   → MobDirectorFabric   + fabric.mod.json
    ├── neoforge/ → MobDirectorNeoForge + neoforge.mods.toml
    └── forge/    → MobDirectorForge    + mods.toml
```

Kaikki logiikka on **kerran** kansiossa `common/` ja identtinen kolmella loaderilla (samat
Mojmap-nimet). Jokainen loader tuo vain tavan, jolla se julistaa modin ja kytkee tapahtumansa.

---

## 📄 Lisenssi ja tekijät

Modin loi **Kalevi Latva-äijö**. Lisenssi **CC0-1.0** (public domain): käytä, muokkaa ja jaa
vapaasti.
