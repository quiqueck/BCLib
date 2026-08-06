[![](https://jitpack.io/v/quiqueck/BCLib.svg)](https://jitpack.io/#quiqueck/BCLib)

# BCLib

BCLib is a library mod for BetterX team mods, developed for Fabric, MC 1.21

## Importing:

You can easily include BCLib into your own mod by adding the following to your `build.gradle`:

```
repositories {
    ...
    maven { url 'https://maven.ambertation.de/releases' }
}
```

```
dependencies {
    ...
    modImplementation "org.betterx:bclib:${project.bclib_version}"
}
```

You should also add a dependency to `fabirc.mod.json`. BCLib uses Semantic versioning, so adding the dependcy as follows
should respect that and ensure that your mod is not loaded with an incompatible version of BCLib:

```
"depends": {
  ...
  "bclib": "2.0.x"
},
"breaks": {
  "bclib": "<2.0.6"
}
```

In this example `2.0.6` is the BCLIb Version you are building against.

## Features:

### Rendering

* Emissive textures (with _e suffix)
    * Can be applied to Solid and Transparent blocks;
    * Can be changed/added with resourcepacks;
    * Incompatible with Sodium and Canvas (just will be not rendered);
    * Incompatible with Iris shaders (Iris without shaders works fine).
* Procedural block and item models (from paterns or from code);
* Block render interfaces.

### API:

* Simple Mod Integration API:
    * Get mod inner methods, classes and objects on runtime.
* Structure Features API:
    * Sructure Features with automatical registration, Helpers and math stuff.
* World Data API:
    * World fixers for comfortable migration between mod versions when content was removed;
    * Support for Block name changes and Tile Entities (WIP).
* Bonemeal API:
    * Add custom spreadable blocks;
    * Add custom plants grow with weight, biomes and other checks;
    * Custom underwater plants.
* Features API:
    * Features with automatical registration, Helpers and math.
* Biome API:
    * Biome wrapper around MC biomes;
    * Custom biome data storage;
    * Custom fog density.
* Tag API:
    * Pre-builded set of tags;
    * Dynamical tag registration with code;
    * Adding blocks and items into tags at runtime.

### Libs:

* Spline library (simple):
    * Helper to create simple splines as set of points;
    * Some basic operation with splines;
    * Converting splines to SDF.
* Recipe manager:
    * Register recipes from code with configs and ingredients check.
* Noise library:
    * Voronoi noise and Open Simplex Noise.
* Math library:
    * Many basic math functions that are missing in MC.
* SDF library:
    * Implementation of Signed Distance Functions;
    * Different SDF Operations and Primitives;
    * Different materials for SDF Primitives;
    * Block post-processing;
    * Feature generation using SDF.

### Helpers And Utils:

* Custom surface builders.
* Translation helper:
    * Generates translation template.
* Weighted list:
    * A list of objects by weight;
* Weighted Tree:
    * Fast approach for big weight structures;
* Block helper:
    * Some useful functions to operate with blocks;

### Complex Materials

* Utility classes used for mass content generation (wooden blocks, stone blocks, etc.);
* Contains a set of defined blocks, items, recipes and tags;
* Can be modified before mods startup (will add new block type for all instances in all mods);
* All inner blocks and items are Patterned (will have auto-generated models with ability to override them with resource
  packs or mod resources).

### Pre-Defined Blocks and Items:

* Most basic blocks from MC;
* Automatic item & block model generation;

### Configs:

* Custom config system based on Json;
* Hierarchical configs;
* Different entry types;
* Only-changes saves.

### Interfaces:

* BlockModelProvider:
    * Allows block to return custom model and blockstate.
* ItemModelProvider:
    * Allows block to return custom item model.
* ~~CustomColorProvider~~ (removed):
    * Replaced by wover's `ClientBlockTraits.TINT` binding, which drives both the in-world block colour and the
      generated item model's tint. Declare it as a trait instead of implementing an interface.
* RenderLayerProvider:
    * Determine block render layer (Transparent and Translucent).
* PostInitable:
    * Allows block to init something after all mods are loaded.
* CustomItemProvider:
    * Allows block to change its registered item (example - signs, water lilies).

## Building:

* Clone repo
* Run command line in folder: gradlew build
* Mod .jar will be in ./build/libs

## Vanilla audit snapshots:

`gradlew :runMinecraftAudit` regenerates the four audit files for the **vanilla** `minecraft`
namespace and writes them into the shared reference checkout next to the mod repos, at
`../minecraft/src/main/generated`:

| File | What it records |
|---|---|
| `block_properties.json` | one line per block: hardness, resistance, map colour, sound, render layer, ... |
| `block_registrations.json` | flammability and compostability, which live in side registries rather than on the block |
| `item_registrations.json` | the equivalent for items |
| `block_shapes.txt` | outline and collision shape per blockstate, collapsed to one line when every state agrees |

These are a reference for diffing vanilla behaviour between Minecraft versions, so nothing else is
generated during the run: the four providers are bound to a `minecraft` `ModCore` and every other
BCLib provider is suppressed, otherwise BCLib's own tags and advancements would land in the vanilla
checkout. The run fails if that checkout is missing rather than creating an empty one somewhere else.

The switch is the `-Dwover.datagen.minecraft-audit` system property (see `MinecraftAuditDatagen`), so
any datagen run can be flipped over to it; the Gradle task just sets the property and the output
directory. Note that BCLib's and WorldWeaver's own mixins are loaded during the run, so this is
vanilla as BCLib sees it.

The same four files are also generated per mod, for that mod's own namespace, by each mod's normal
`gradlew :runDatagenClient` - those land in the mod's `src/main/generated` and are committed with it.

## Release branches:

`gradlew :mergeToRelease` adds the current branch's state to `release/<branch>` (override with
`-PreleaseBranch=`) as a single commit titled `Release Version <mod_version>`, with `CHANGES.md` as
its body. It is not a merge: the commit's only parent is the previous release commit, so the release
branch carries no reference to the development commits and cannot be used to recover them. Nothing is
pushed and no branch is checked out - only the release ref moves. Defined in
`common-publish-tag.gradle`, so every BetterX repo has it.

Neither `CHANGES.md` nor `mod_version` is regenerated - both routinely carry manual edits, so they are
published verbatim, and asking for `changelog` or `nextVersion` in the same build is refused before
anything runs.

The **first** release on a branch continues from the previous version line, so it reads as a diff
rather than as a full import of every file. That predecessor is named by `previous_release_branch` in
`gradle.properties` (`-PpreviousRelease=` wins; `none` starts the branch parentless):

| branch | parent of the first release commit |
|---|---|
| `release/1.21.6` | `origin/1.21.6` - the already-published tip |
| `release/26.1` | `release/1.21.6` |
| `release/26.2` | `release/26.1` |
| `release/26.3` | `release/26.2` |

Anchoring `release/1.21.6` at `origin/1.21.6` rather than at the local branch matters: the 1.21.6
commits that were never pushed then reach the public branch as *content*, inside one
`Release Version 21.8.x` commit, instead of as individual commits carrying their own messages.

Every release after the first sits on the previous release commit of its own branch. The `26.x`
development branches are never touched, and none of their commits become reachable from a release
branch - only the tree they produced does.

Everything reachable from the predecessor *does* become part of the public branch, which is intended
for `1.21.6` but never for a `26.x` development branch. The task prints how many commits are inherited
and how many of those `origin` has not seen yet, before anything is pushed.
