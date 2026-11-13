<div align="center">

# BCLib

**The shared library behind BetterNether and BetterEnd: world generation, block and content helpers for Fabric mods.**

[![Modrinth downloads](https://img.shields.io/modrinth/dt/bclib?logo=modrinth&label=Modrinth&color=1bd96a&style=for-the-badge)](https://modrinth.com/mod/bclib)
[![CurseForge downloads](https://img.shields.io/curseforge/dt/495191?logo=curseforge&label=CurseForge&color=f16436&style=for-the-badge)](https://www.curseforge.com/minecraft/mc-mods/bclib)
[![Fabric for Minecraft](https://img.shields.io/badge/Fabric-1.20.1_%7C_1.21.6--8_%7C_26.x-62b47a?style=for-the-badge)](https://modrinth.com/mod/bclib/versions)

</div>

**BCLib** is a library mod for [Fabric](https://fabricmc.net/) that powers the Team BetterX mods
[BetterNether](https://github.com/quiqueck/BetterNether) and [BetterEnd](https://github.com/quiqueck/BetterEnd).
Players only need to install it when a mod asks for it; on its own it adds no content to the game. From Minecraft
1.21.6 on, BCLib builds on [WorldWeaver](https://github.com/quiqueck/WorldWeaver), which is gradually taking over its
world generation features.

## Download

The latest Fabric release for each supported Minecraft version (the badges update automatically):

| Minecraft | Modrinth | CurseForge |
|:--|:--|:--|
| **26.3** | [![Modrinth 26.3](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.modrinth.com%2Fv2%2Fproject%2Fbclib%2Fversion%3Fgame_versions%3D%255B%252226.3%2522%255D%26loaders%3D%255B%2522fabric%2522%255D&query=%24%5B0%5D.version_number&label=Modrinth&color=1bd96a&logo=modrinth&logoColor=white)](https://modrinth.com/mod/bclib/versions?g=26.3&l=fabric) | [![CurseForge 26.3](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.modrinth.com%2Fv2%2Fproject%2Fbclib%2Fversion%3Fgame_versions%3D%255B%252226.3%2522%255D%26loaders%3D%255B%2522fabric%2522%255D&query=%24%5B0%5D.version_number&label=CurseForge&color=f16436&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/bclib/files/all?version=26.3&gameVersionTypeId=4) |
| **26.2** | [![Modrinth 26.2](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.modrinth.com%2Fv2%2Fproject%2Fbclib%2Fversion%3Fgame_versions%3D%255B%252226.2%2522%255D%26loaders%3D%255B%2522fabric%2522%255D&query=%24%5B0%5D.version_number&label=Modrinth&color=1bd96a&logo=modrinth&logoColor=white)](https://modrinth.com/mod/bclib/versions?g=26.2&l=fabric) | [![CurseForge 26.2](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.modrinth.com%2Fv2%2Fproject%2Fbclib%2Fversion%3Fgame_versions%3D%255B%252226.2%2522%255D%26loaders%3D%255B%2522fabric%2522%255D&query=%24%5B0%5D.version_number&label=CurseForge&color=f16436&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/bclib/files/all?version=26.2&gameVersionTypeId=4) |
| **26.1 – 26.1.2** | [![Modrinth 26.1 – 26.1.2](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.modrinth.com%2Fv2%2Fproject%2Fbclib%2Fversion%3Fgame_versions%3D%255B%252226.1%2522%252C%252226.1.1%2522%252C%252226.1.2%2522%255D%26loaders%3D%255B%2522fabric%2522%255D&query=%24%5B0%5D.version_number&label=Modrinth&color=1bd96a&logo=modrinth&logoColor=white)](https://modrinth.com/mod/bclib/versions?g=26.1.2&l=fabric) | [![CurseForge 26.1 – 26.1.2](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.modrinth.com%2Fv2%2Fproject%2Fbclib%2Fversion%3Fgame_versions%3D%255B%252226.1%2522%252C%252226.1.1%2522%252C%252226.1.2%2522%255D%26loaders%3D%255B%2522fabric%2522%255D&query=%24%5B0%5D.version_number&label=CurseForge&color=f16436&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/bclib/files/all?version=26.1.2&gameVersionTypeId=4) |
| **1.21.6 – 1.21.8** | [![Modrinth 1.21.6 – 1.21.8](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.modrinth.com%2Fv2%2Fproject%2Fbclib%2Fversion%3Fgame_versions%3D%255B%25221.21.6%2522%252C%25221.21.7%2522%252C%25221.21.8%2522%255D%26loaders%3D%255B%2522fabric%2522%255D&query=%24%5B0%5D.version_number&label=Modrinth&color=1bd96a&logo=modrinth&logoColor=white)](https://modrinth.com/mod/bclib/versions?g=1.21.8&l=fabric) | [![CurseForge 1.21.6 – 1.21.8](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.modrinth.com%2Fv2%2Fproject%2Fbclib%2Fversion%3Fgame_versions%3D%255B%25221.21.6%2522%252C%25221.21.7%2522%252C%25221.21.8%2522%255D%26loaders%3D%255B%2522fabric%2522%255D&query=%24%5B0%5D.version_number&label=CurseForge&color=f16436&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/bclib/files/all?version=1.21.8&gameVersionTypeId=4) |
| **1.20 – 1.20.1** | [![Modrinth 1.20 – 1.20.1](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.modrinth.com%2Fv2%2Fproject%2Fbclib%2Fversion%3Fgame_versions%3D%255B%25221.20%2522%252C%25221.20.1%2522%255D%26loaders%3D%255B%2522fabric%2522%255D&query=%24%5B0%5D.version_number&label=Modrinth&color=1bd96a&logo=modrinth&logoColor=white)](https://modrinth.com/mod/bclib/versions?g=1.20.1&l=fabric) | [![CurseForge 1.20 – 1.20.1](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.modrinth.com%2Fv2%2Fproject%2Fbclib%2Fversion%3Fgame_versions%3D%255B%25221.20%2522%252C%25221.20.1%2522%255D%26loaders%3D%255B%2522fabric%2522%255D&query=%24%5B0%5D.version_number&label=CurseForge&color=f16436&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/bclib/files/all?version=1.20.1&gameVersionTypeId=4) |

**Required:** [Fabric API](https://modrinth.com/mod/fabric-api) and [WorldWeaver](https://modrinth.com/mod/worldweaver)
(1.21.6 and newer).

## Add BCLib to your project

Add our maven repository and the dependency to your `build.gradle`:

```groovy
repositories {
    maven { url 'https://maven.ambertation.de/releases' }
}

dependencies {
    // Minecraft 26.x (Loom 1.17+ no longer has the mod* configurations)
    api "org.betterx:bclib:${project.bclib_version}"
    // Minecraft 1.21.x and older:
    // modImplementation "org.betterx:bclib:${project.bclib_version}"
}
```

Then declare the dependency in your `fabric.mod.json`. BCLib uses semantic versioning, so matching the minor version
keeps your mod from loading with an incompatible BCLib:

<!-- mod-task:deps:start -->

```json
"depends": {
  "bclib": "26.302.x"
},
"breaks": {
  "bclib": "<26.302.2"
}
```

In this example `26.302.2` is the BCLib version you build against.

<!-- mod-task:deps:end -->

## Features

World generation, biomes, structures, material sets and block models have moved to
[WorldWeaver](https://github.com/quiqueck/WorldWeaver). BCLib keeps the content-level building blocks that BetterNether
and BetterEnd share:

### Blocks and items

* Base classes for plants (with age, double, underwater, up/down, wall-attached), vines, stalactites, terrain blocks,
  saplings that grow features, furnaces, glass, pressure plates and anvils with levels.
* Furniture: chairs, taburets and bar stools you can sit on.
* Elytra API for custom elytra items.
* Music discs, fuel, compostables, and shovel/tool block transformations (`ComposterAPI`, `FuelComponents`,
  `ShovelAPI`, `ToolBlockTransformers`).

### Recipes

* Anvil (hammer) recipes and alloying recipes, with JEI and EMI categories.
* `BCLRecipeBuilder` to register recipes from code.

### APIs

* **Bonemeal API** – custom spreadable blocks and plants that grow from bonemeal, with spreaders for End Stone,
  Netherrack, nylium-like and water-grass blocks.
* **World fixers** (`DataFixerAPI`, `MigrationProfile`) – migrate worlds between mod versions when blocks or items are
  renamed or removed. Players are asked to make a backup first.
* **Data exchange** – client/server handshake and data sync.
* **Advancements** – builders for advancements and their datagen.
* **Spawning** – `SpawnRuleBuilder` for entity spawn rules.
* **Mod integration** – access other mods' classes and objects at runtime.
* **Tags** – pre-built block tags (`BCLBlockTags`).
* **Datagen** – providers for block loot tables, loot drops and advancements.

### Libraries

* **SDF library** – signed distance functions with primitives, operators and materials, used to generate features.
* **Noise** – Open Simplex noise.
* **Splines** – simple spline helper that can be turned into an SDF.
* **Helpers** – `BlocksHelper`, `MHelper`, `WeightedList`, `ColorExtractor`, `NbtHelper` and more.

### Client

* Custom fog density per biome.
* A world-fix progress and confirmation screen.

## Building from source

You need JDK 25 for the 26.x branches (JDK 21 for 1.21.x, JDK 17 for 1.20.1).

```bash
git clone https://github.com/quiqueck/BCLib.git
cd BCLib
./gradlew build
```

The mod jar is written to `build/libs`. Each Minecraft version has its own release branch: `release/26.3`, `release/26.2`, `release/26.1`, `release/1.21.6` and `release/1.20` (1.20.1).

## Credits and license

BCLib was created by paulevs and is maintained by
[Quiqueck](https://github.com/quiqueck) and Team BetterX. The original repository is archived; all development happens here.
The source code is licensed under the [MIT License](LICENSE).

**Assets (2026-10-30).** Assets (textures, images, models, music and sounds) are not covered by the MIT License.
They are governed by [LICENSE.ASSETS](LICENSE.ASSETS), which also lists their authors: unless stated otherwise, they
are All Rights Reserved, and a listed subset is licensed under CC BY-NC-SA 4.0 (attribution: Team BetterX).
As long as they stay unmodified and bundled with the official mod release, you may use them in modpacks and on
servers (commercial and non-commercial ones). You may also show them in videos, streams and screenshots.

## AI Usage

The ideas and the design of our APIs are human-made: we come up with the concepts, guide every design decision and implement the code. We use agentic AI as a tool for the following tasks:

- tooling and automation,
- code review,
- proofreading and writing documentation (code comments and this README),
- boilerplate work, including migrating the codebase to new Minecraft versions,
- many of our automated tests.

Everything the AI generates is reviewed and, where needed, edited by us before it is released. We also playtest every release ourselves.
