[![](https://jitpack.io/v/quiqueck/BCLib.svg)](https://jitpack.io/#quiqueck/BCLib)

# BCLib

BCLib is a library mod for BetterX team mods, developed for Fabric, MC 1.19

## Importing:

You can easily include BCLib into your own mod by adding the following to your `build.gradle`:

```
repositories {
    ...
    maven { url 'https://jitpack.io' } 
}
```

```
dependencies {
    ...
    modImplementation "com.github.quiqueck:BCLib:${project.bclib_version}"
}
```

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
* CustomColorProvider:
    * Make available to add block and item color provider.
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
