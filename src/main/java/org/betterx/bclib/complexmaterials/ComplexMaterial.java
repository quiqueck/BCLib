package org.betterx.bclib.complexmaterials;

import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.ItemEntry;
import org.betterx.bclib.complexmaterials.entry.MaterialSlot;
import org.betterx.bclib.complexmaterials.entry.RecipeEntry;
import org.betterx.bclib.registry.BlockRegistry;
import org.betterx.bclib.registry.ItemRegistry;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

public abstract class ComplexMaterial {
    private static final Map<ResourceLocation, List<RecipeEntry>> RECIPE_ENTRIES = Maps.newHashMap();
    private static final Map<ResourceLocation, List<BlockEntry>> BLOCK_ENTRIES = Maps.newHashMap();
    private static final Map<ResourceLocation, List<ItemEntry>> ITEM_ENTRIES = Maps.newHashMap();
    private static final List<ComplexMaterial> MATERIALS = Lists.newArrayList();

    private final List<RecipeEntry> defaultRecipeEntries = Lists.newArrayList();
    private final List<BlockEntry> defaultBlockEntries = Lists.newArrayList();
    private final List<ItemEntry> defaultItemEntries = Lists.newArrayList();

    private final Map<String, TagKey<Block>> blockTags = Maps.newHashMap();
    private final Map<String, TagKey<Item>> itemTags = Maps.newHashMap();
    private final Map<String, Block> blocks = Maps.newHashMap();
    private final Map<String, Item> items = Maps.newHashMap();

    protected final String baseName;
    protected final String modID;
    protected final String receipGroupPrefix;

    public ComplexMaterial(String modID, String baseName, String receipGroupPrefix) {
        this.baseName = baseName;
        this.modID = modID;
        this.receipGroupPrefix = receipGroupPrefix;
        MATERIALS.add(this);
    }

    /**
     * Initialize and registers all content inside material, return material itself.
     *
     * @param blocksRegistry {@link BlockRegistry} instance to add blocks in;
     * @param itemsRegistry  {@link ItemRegistry} instance to add items in;
     * @return {@link ComplexMaterial}.
     */
    public ComplexMaterial init(BlockRegistry blocksRegistry, ItemRegistry itemsRegistry) {
        initTags();

        final BlockBehaviour.Properties blockSettings = getBlockSettings();
        final Item.Properties itemSettings = getItemSettings(itemsRegistry);
        initDefault(blockSettings, itemSettings);

        getBlockEntries().forEach(entry -> {
            Block block = entry.init(this, blockSettings, blocksRegistry);
            blocks.put(entry.getSuffix(), block);
        });

        getItemEntries().forEach(entry -> {
            Item item = entry.init(this, itemSettings, itemsRegistry);
            items.put(entry.getSuffix(), item);
        });

        initDefaultRecipes();
        getRecipeEntries().forEach(entry -> {
            entry.init(this);
        });

        initFlammable(FlammableBlockRegistry.getDefaultInstance());
        return this;
    }

    /**
     * Init default content for {@link ComplexMaterial} - blocks and items.
     *
     * @param blockSettings {@link BlockBehaviour.Properties} default block settings for this material;
     * @param itemSettings  {@link Item.Properties} default item settings for this material.
     */

    protected abstract void initDefault(BlockBehaviour.Properties blockSettings, Item.Properties itemSettings);

    /**
     * Init custom tags for this {@link ComplexMaterial}, not required.
     */
    protected void initTags() {
    }

    /**
     * Init default recipes for this {@link ComplexMaterial}, not required.
     */
    protected void initDefaultRecipes() {
    }

    /**
     * Allows to add blocks into Fabric {@link FlammableBlockRegistry} for this {@link ComplexMaterial}, not required.
     */
    protected void initFlammable(FlammableBlockRegistry registry) {
    }

    /**
     * Adds custom block tag for this {@link ComplexMaterial}, tag can be created with {@link TagManager} or you can use one of already created tags.
     *
     * @param tag {@link TagKey} for {@link Block}
     */
    protected void addBlockTag(TagKey<Block> tag) {
        String key = tag.location().getPath().replace(getBaseName() + "_", "");
        blockTags.put(key, tag);
    }

    /**
     * Adds custom item tag for this {@link ComplexMaterial}, tag can be created with {@link TagManager} or you can use one of already created tags.
     *
     * @param tag {@link TagKey} for {@link Item}
     */
    protected void addItemTag(TagKey<Item> tag) {
        String key = tag.location().getPath().replace(getBaseName() + "_", "");
        itemTags.put(key, tag);
    }

    /**
     * Get custom {@link Block} {@link TagKey} from this {@link ComplexMaterial}.
     *
     * @param key {@link String} tag name (path of its {@link ResourceLocation}), for inner tags created inside material its tag suffix.
     * @return {@link TagKey} for {@link Block} or {@code null} if nothing is stored.
     */
    @Nullable
    public TagKey<Block> getBlockTag(String key) {
        return blockTags.get(key);
    }

    /**
     * Get custom {@link Item} {@link TagKey} from this {@link ComplexMaterial}.
     *
     * @param key {@link String} tag name (path of its {@link ResourceLocation}), for inner tags created inside material its tag suffix.
     * @return {@link TagKey} for {@link Item} or {@code null} if nothing is stored.
     */
    @Nullable
    public TagKey<Item> getItemTag(String key) {
        return itemTags.get(key);
    }

    /**
     * Get initiated {@link Block} from this {@link ComplexMaterial}.
     *
     * @param key {@link String} block name suffix (example: "mod:custom_log" will have a "log" suffix if "custom" is a base name of this material)
     * @return {@link Block} or {@code null} if nothing is stored.
     */
    @Nullable
    public Block getBlock(String key) {
        return blocks.get(key);
    }

    /**
     * Get initiated {@link Block} from this {@link ComplexMaterial}.
     *
     * @param key          {@link String} block name suffix (example: "mod:custom_log" will have a "log" suffix if "custom" is a base name of this material)
     * @param runIfPresent {@link Consumer} to run if block is present.
     * @return {@link Block} or {@code null} if nothing is stored.
     */
    @Nullable
    public Block ifBlockPresent(String key, Consumer<Block> runIfPresent) {
        final Block block = blocks.get(key);
        if (block != null) {
            runIfPresent.accept(block);
        }
        return block;
    }

    /**
     * Get initiated {@link Block} from this {@link ComplexMaterial}.
     *
     * @param slot         {@link MaterialSlot} The Block Entry
     * @param runIfPresent {@link Consumer} to run if block is present.
     * @param <M>          The {@link ComplexMaterial} this slot is usable for Type
     * @return {@link Block} or {@code null} if nothing is stored.
     */
    @Nullable
    public <M extends ComplexMaterial> Block ifBlockPresent(MaterialSlot<M> slot, Consumer<Block> runIfPresent) {
        final Block block = blocks.get(slot.suffix);
        if (block != null) {
            runIfPresent.accept(block);
        }
        return block;
    }

    /**
     * Get initiated {@link Block} from this {@link ComplexMaterial}.
     *
     * @param key {@link MaterialSlot} The Block Entry
     * @param <M> The {@link ComplexMaterial} this slot is usable for Type
     * @return {@link Block} or {@code null} if nothing is stored.
     */
    @Nullable
    public <M extends ComplexMaterial> Block getBlock(MaterialSlot<M> key) {
        return blocks.get(key.suffix);
    }

    /**
     * Get initiated {@link Block} from this {@link ComplexMaterial}.
     *
     * @param key {@link MaterialSlot} The Block Entry
     * @param <M> The {@link ComplexMaterial} this slot is usable for Type
     * @return {@link Block} or {@code null} if nothing is stored.
     */
    @Nullable
    public <M extends ComplexMaterial> BlockItem getBlockItem(MaterialSlot<M> key) {
        return getBlockItem(key.suffix);
    }

    /**
     * Get initiated {@link Block} from this {@link ComplexMaterial}.
     *
     * @param key {@link MaterialSlot} The Block Entry
     * @return {@link Block} or {@code null} if nothing is stored.
     */
    @Nullable
    public BlockItem getBlockItem(String key) {
        final Block bl = blocks.get(key);
        return bl != null && bl.asItem() instanceof BlockItem bi ? bi : null;
    }

    /**
     * Get initiated {@link Item} from this {@link ComplexMaterial}.
     *
     * @param key {@link String} block name suffix (example: "mod:custom_apple" will have a "apple" suffix if "custom" is a base name of this material)
     * @return {@link Item} or {@code null} if nothing is stored.
     */
    @Nullable
    public Item getItem(String key) {
        return items.get(key);
    }

    /**
     * Get initiated {@link Item} from this {@link ComplexMaterial}.
     *
     * @param slot {@link MaterialSlot} The Item Entry
     * @param <M>  The {@link ComplexMaterial} this slot is usable for Type
     * @return {@link Item} or {@code null} if nothing is stored.
     */
    @Nullable
    public <M extends ComplexMaterial> Item getItem(MaterialSlot<M> slot) {
        return items.get(slot.suffix);
    }

    /**
     * Get initiated {@link Item} from this {@link ComplexMaterial}.
     *
     * @param key          {@link String} item name suffix (example: "mod:custom_apple" will have a "apple" suffix if "custom" is a base name of this material)
     * @param runIfPresent {@link Consumer} to run if item is present.
     * @return {@link Item} or {@code null} if nothing is stored.
     */
    @Nullable
    public Item ifItemPresent(String key, Consumer<Item> runIfPresent) {
        final Item item = items.get(key);
        if (item != null) {
            runIfPresent.accept(item);
        }
        return item;
    }

    /**
     * Get initiated {@link Item} from this {@link ComplexMaterial}.
     *
     * @param slot         {@link MaterialSlot} The Item Entry
     * @param runIfPresent {@link Consumer} to run if item is present.
     * @param <M>          The {@link ComplexMaterial} this slot is usable for Type
     * @return {@link Item} or {@code null} if nothing is stored.
     */
    @Nullable
    public <M extends ComplexMaterial> Item ifItemPresent(MaterialSlot<M> slot, Consumer<Item> runIfPresent) {
        final Item item = items.get(slot.suffix);
        if (item != null) {
            runIfPresent.accept(item);
        }
        return item;
    }


    /**
     * Get default block settings for this material.
     *
     * @return {@link BlockBehaviour.Properties}
     */
    protected abstract BlockBehaviour.Properties getBlockSettings();

    /**
     * Get default item settings for this material.
     *
     * @return {@link Item.Properties}
     */
    protected Item.Properties getItemSettings(ItemRegistry registry) {
        return registry.makeItemSettings();
    }

    private Collection<BlockEntry> getBlockEntries() {
        List<BlockEntry> result = Lists.newArrayList(defaultBlockEntries);
        List<BlockEntry> entries = BLOCK_ENTRIES.get(this.getMaterialID());
        if (entries != null) {
            result.addAll(entries);
        }
        return result;
    }

    private Collection<ItemEntry> getItemEntries() {
        List<ItemEntry> result = Lists.newArrayList(defaultItemEntries);
        List<ItemEntry> entries = ITEM_ENTRIES.get(this.getMaterialID());
        if (entries != null) {
            result.addAll(entries);
        }
        return result;
    }

    private Collection<RecipeEntry> getRecipeEntries() {
        List<RecipeEntry> result = Lists.newArrayList(defaultRecipeEntries);
        List<RecipeEntry> entries = RECIPE_ENTRIES.get(this.getMaterialID());
        if (entries != null) {
            result.addAll(entries);
        }
        return result;
    }

    /**
     * Get base name of this {@link ComplexMaterial}.
     *
     * @return {@link String} name
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * Get mod ID for this {@link ComplexMaterial}.
     *
     * @return {@link String} mod ID.
     */
    public String getModID() {
        return modID;
    }

    /**
     * Get a unique {@link ResourceLocation} for each material class.
     * For example WoodenComplexMaterial will have a "bclib:Wooden_Complex_Material" {@link ResourceLocation}.
     * This is used to add custom entries before mods init using Fabric "preLaunch" entry point.
     *
     * @return {@link ResourceLocation} for this material
     * @see <a href="https://fabricmc.net/wiki/documentation:entrypoint">Fabric Documentation: Entrypoint</a>
     */
    public abstract ResourceLocation getMaterialID();

    /**
     * Get all initiated block from this {@link ComplexMaterial}.
     *
     * @return {@link Collection} of {@link Block}.
     */
    public Collection<Block> getBlocks() {
        return blocks.values();
    }

    /**
     * Get all initiated items from this {@link ComplexMaterial}.
     *
     * @return {@link Collection} of {@link Item}.
     */
    public Collection<Item> getItems() {
        return items.values();
    }

    /**
     * Adds a default {@link BlockEntry} to this {@link ComplexMaterial}. Used to initiate blocks later.
     *
     * @param entry {@link BlockEntry}
     */
    protected void addBlockEntry(BlockEntry entry) {
        defaultBlockEntries.add(entry);
    }

    /**
     * Replaces or Adds a default {@link BlockEntry} to this {@link ComplexMaterial}. Used to initiate blocks later.
     * <p>
     * If this {@link ComplexMaterial} does already contain an entry for the {@link ResourceLocation}, the entry will
     * be removed first.
     *
     * @param entry {@link BlockEntry}
     */
    protected void replaceOrAddBlockEntry(BlockEntry entry) {
        int pos = defaultBlockEntries.indexOf(entry);
        if (pos >= 0) defaultBlockEntries.remove(entry);

        addBlockEntry(entry);
    }

    /**
     * Adds a default {@link ItemEntry} to this {@link ComplexMaterial}. Used to initiate items later.
     *
     * @param entry {@link ItemEntry}
     */
    protected void addItemEntry(ItemEntry entry) {
        defaultItemEntries.add(entry);
    }

    /**
     * Adds a default {@link RecipeEntry} to this {@link ComplexMaterial}. Used to initiate items later.
     *
     * @param entry {@link RecipeEntry}
     */
    protected void addRecipeEntry(RecipeEntry entry) {
        defaultRecipeEntries.add(entry);
    }

    /**
     * Adds a custom {@link BlockEntry} for specified {@link ComplexMaterial} using its {@link ResourceLocation}.
     * Used to add custom entry for all instances of {@link ComplexMaterial}.
     * Should be called only using Fabric "preLaunch" entry point.
     *
     * @param materialName {@link ResourceLocation} id of {@link ComplexMaterial};
     * @param entry        {@link BlockEntry}.
     * @see <a href="https://fabricmc.net/wiki/documentation:entrypoint">Fabric Documentation: Entrypoint</a>
     */
    public static void addBlockEntry(ResourceLocation materialName, BlockEntry entry) {
        List<BlockEntry> entries = BLOCK_ENTRIES.get(materialName);
        if (entries == null) {
            entries = Lists.newArrayList();
            BLOCK_ENTRIES.put(materialName, entries);
        }
        entries.add(entry);
    }

    /**
     * Adds a custom {@link ItemEntry} for specified {@link ComplexMaterial} using its {@link ResourceLocation}.
     * Used to add custom entry for all instances of {@link ComplexMaterial}.
     * Should be called only using Fabric "preLaunch" entry point.
     *
     * @param materialName {@link ResourceLocation} id of {@link ComplexMaterial};
     * @param entry        {@link ItemEntry}.
     * @see <a href="https://fabricmc.net/wiki/documentation:entrypoint">Fabric Documentation: Entrypoint</a>
     */
    public static void addItemEntry(ResourceLocation materialName, ItemEntry entry) {
        List<ItemEntry> entries = ITEM_ENTRIES.get(materialName);
        if (entries == null) {
            entries = Lists.newArrayList();
            ITEM_ENTRIES.put(materialName, entries);
        }
        entries.add(entry);
    }

    /**
     * Adds a custom {@link RecipeEntry} for specified {@link ComplexMaterial} using its {@link ResourceLocation}.
     * Used to add custom entry for all instances of {@link ComplexMaterial}.
     * Should be called only using Fabric "preLaunch" entry point.
     *
     * @param materialName {@link ResourceLocation} id of {@link ComplexMaterial};
     * @param entry        {@link RecipeEntry}.
     * @see <a href="https://fabricmc.net/wiki/documentation:entrypoint">Fabric Documentation: Entrypoint</a>
     */
    public static void addRecipeEntry(ResourceLocation materialName, RecipeEntry entry) {
        List<RecipeEntry> entries = RECIPE_ENTRIES.get(materialName);
        if (entries == null) {
            entries = Lists.newArrayList();
            RECIPE_ENTRIES.put(materialName, entries);
        }
        entries.add(entry);
    }

    /**
     * Get all instances of all materials.
     *
     * @return {@link Collection} of {@link ComplexMaterial}.
     */
    public static Collection<ComplexMaterial> getAllMaterials() {
        return MATERIALS;
    }
}
