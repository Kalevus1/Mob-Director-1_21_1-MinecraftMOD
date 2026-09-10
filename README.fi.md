# 🎬 Mob Director

> **Ohjaa mobeja kuin näyttelijöitä – komennoilla.**
> Laita mob hyökkäämään toisen kimppuun, seuraamaan entiteettiä, kävelemään tai liukumaan
> elokuvamaisesti tiettyyn koordinaattiin, katsomaan tiettyyn pisteeseen… ja valitse mobeja
> tähtäimellä. Kuvaa elokuvakohtauksia ja järjestä tapahtumia ilman, että mobien tarvitsee
> toimia oman tekoälynsä varassa.

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

Modin on tehnyt **Kalevi Latva-äijö**. Se muuttaa komennot mobien **ohjauspaneeliksi**:
mobit toimivat kuin näyttelijät sen sijaan, että niiden oma tekoäly päättäisi niiden liikkeistä.
Sen kumppani, **Mob Controller**, antaa lisäksi *astua mobin sisään* ja nähdä pelimaailman
sen silmien kautta.

> 📁 **Tämä repositorio:** `downloads/` sisältää **valmiit pelattavat jar-tiedostot** · `source/`
> sisältää **lähdekoodin** (jos haluat kääntää modin itse).

---

## ✨ Miten se toimii

* Kaikki komennot alkavat **`/mobctl`**-juuresta ja vaativat **OP-tason 2**.
* Kohteet (`<mobs>`, `<kohde>`…) ovat tavallisia **entiteettivalitsimia**: `@e`, `@p`,
  `@e[tag=…]`, `@e[type=…]`… Voit siis käyttää **tageja**, rajoituksia ja NBT-suodattimia
  mobien valitsemiseen.
* Jatkuvat toiminnot (seuraaminen, jatkuva hyökkäys ja liukuminen) tallennetaan
  **ohjauksina**, joita päivitetään **joka tick**. Jokaisella mobilla voi olla yksi aktiivinen
  ohjaus kerrallaan. Uusi komento korvaa aiemman ohjauksen, ja `stop` lopettaa sen.

---

## 🎮 Komennot

Syntaksi: `< >` pakollinen · `[ ]` valinnainen.

| Komento                                               | Mitä se tekee                                                                                                                |
| ----------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| `/mobctl select <tag>`                                | Lisää **tagin tarkalleen siihen mobiin, jota katsot** (raycast, 64 lohkoa). Vain pelaajille.                                 |
| `/mobctl deselect <tag>`                              | Poistaa tagin **kaikilta sen omaavilta** ja lopettaa niiden ohjauksen.                                                       |
| `/mobctl moveto <mobs> <x y z> [nopeus]`              | **Kävelee** (reitinhaulla) kohti kohteen lähellä olevaa sijaintia.                                                           |
| `/mobctl glide <mobs> <x y z> <tickit> [ground\|air]` | **Liukuu** suoraan kohteeseen N tickin aikana. `ground` seuraa maastoa; `air` käyttää annettua Y-koordinaattia sellaisenaan. |
| `/mobctl follow <mobs> <kohde> [nopeus]`              | **Seuraa** liikkuvaa entiteettiä.                                                                                            |
| `/mobctl look <mobs> <x y z>`                         | **Kääntyy katsomaan** annettua pistettä.                                                                                     |
| `/mobctl attack <hyökkääjät> <kohde> [pysyvä]`        | **Hyökkää** kohteeseen; `true` varmistaa hyökkäyksen uudelleen joka tick.                                                    |
| `/mobctl stop <mobs>`                                 | **Lopettaa** ohjauksen, kohteen ja reitin sekä palauttaa mobin normaalin tekoälyn käyttöön.                                  |
| `/mobctl delay <sekunnit> <komento…>`                 | **Ajastaa** komennon suoritettavaksi N sekunnin kuluttua ja näyttää lähtölaskennan (esimerkiksi kameran asettamista varten). |

### Tagit + esimerkki

```text
/mobctl select karhu                              # katso mobia ja lisää sille tagi (tarkka)
/mobctl glide  @e[tag=karhu] -1212 62 2796 200    # liu'uta se kohteeseen 10 sekunnissa
/mobctl stop   @e[tag=karhu]
/mobctl deselect karhu                            # vapauta mob ja poista tagi
```

---

## 📦 Asennus (pelaajat)

1. Lataa **käyttämääsi loaderia vastaava jar-tiedosto** [`downloads/`](downloads/)-
   kansiosta tai **[Releases]**-välilehdeltä → `mobdirector-<loader>-1.5.0.jar`.
2. Siirrä jar-tiedosto `.minecraft/mods`-kansioon. **Fabricissa tarvitset lisäksi
   [Fabric API]n**.
3. Mene maailmaan, jossa **huijaukset ovat käytössä** (OP-taso 2), ja käytä
   `/mobctl …` -komentoja.

[Releases]: ../../releases
[Fabric API]: https://modrinth.com/mod/fabric-api

---

## 🧩 Loaderit

Kaikki versiot ovat **Minecraft 1.21.1**:lle · **Java 21** · käyttävät **virallisia
Mojang-mappauksia**.

| Loader       | Versio                                      | Jar                              |
| ------------ | ------------------------------------------- | -------------------------------- |
| **Fabric**   | loader 0.19.3+ · Fabric API 0.116.13+1.21.1 | `mobdirector-fabric-1.5.0.jar`   |
| **NeoForge** | 21.1.x                                      | `mobdirector-neoforge-1.5.0.jar` |
| **Forge**    | 52.x                                        | `mobdirector-forge-1.5.0.jar`    |

---

## 🛠️ Kääntäminen lähdekoodista (kehittäjät)

Lähdekoodi on kansiossa **`source/`** — kyseessä on **multiloader-monorepo**:
`common/` sisältää kaikki 9 komentoa, kun taas `fabric/`, `neoforge/` ja `forge/` sisältävät
vain kunkin loaderin käynnistyksen. Tarvitset vain **JDK 21**:n.

```bash
# Java 21 (tässä Minecraft Launcherin mukana tuleva Java):
export JAVA_HOME=".../.minecraft/runtime/java-runtime-delta/windows/java-runtime-delta"

cd source
./gradlew build              # kaikki kolme loaderia
./gradlew :fabric:build      # tai vain yksi (:neoforge:build / :forge:build)
```

JAR-tiedostot ilmestyvät kansioihin:

`source/<loader>/build/libs/mobdirector-<loader>-1.5.0.jar`

*(Ensimmäinen NeoForge/Forge-käännös voi kestää muutaman minuutin, koska Minecraftin
luokat puretaan ja valmistellaan käännöstä varten.)*

---

## 🗂️ Repositorion rakenne

```text
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

Kaikki logiikka sijaitsee **kerran** `common/`-kansiossa ja on sama kaikilla kolmella
loaderilla. Ne käyttävät samoja Mojmap-nimiä. Kukin loader vastaa vain siitä, miten modi
julistetaan ja miten sen tapahtumat kytketään peliin.

---

## 📄 Lisenssi ja tekijät

Modin on luonut **Kalevi Latva-äijö**. Lisenssi on **CC0-1.0 (public domain)**:
saat käyttää, muokata ja jakaa modia vapaasti.
