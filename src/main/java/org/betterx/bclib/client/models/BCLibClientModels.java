package org.betterx.bclib.client.models;

import org.betterx.bclib.blocks.BaseFurnaceBlock;
import org.betterx.bclib.blocks.LeveledAnvilBlock;
import org.betterx.bclib.furniture.slots.BarStool;
import org.betterx.bclib.furniture.slots.Chair;
import org.betterx.bclib.furniture.slots.Taburet;
import org.betterx.bclib.trait.block.LeavesBlockTrait;
import org.betterx.bclib.trait.block.NoAmbientOcclusionCubeModelTrait;
import org.betterx.bclib.trait.block.PathBlockTrait;
import org.betterx.bclib.trait.block.SaplingBlockTrait;
import org.betterx.bclib.trait.block.StalactiteBlockTrait;
import org.betterx.bclib.trait.block.TemplateModelTrait;
import org.betterx.bclib.trait.block.TerrainTraits;
import org.betterx.bclib.trait.block.TripleShapePillarModelTrait;
import org.betterx.bclib.trait.block.VineBlockTrait;
import org.betterx.bclib.trait.block.WaterSeedBlockTrait;
import org.betterx.bclib.trait.block.WeightedCrossModelTrait;
import org.betterx.bclib.trait.block.WeightedPillarModelTrait;
import org.betterx.bclib.trait.block.WeightedTemplateModelTrait;
import de.ambertation.wover.client.api.WoverClientTraitEntrypoint;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Registers the client generators behind BCLib's model keys. Runs through the {@code wover.client.traits}
 * entrypoint, so the generators are present in both the client and the datagen launch.
 */
@Environment(EnvType.CLIENT)
public class BCLibClientModels implements WoverClientTraitEntrypoint {
    @Override
    public void registerClientTraits() {
        BaseFurnaceBlock.registerClientModels();
        LeveledAnvilBlock.registerClientModels();
        BarStool.registerClientModels();
        Chair.registerClientModels();
        Taburet.registerClientModels();
        LeavesBlockTrait.registerClientModels();
        NoAmbientOcclusionCubeModelTrait.registerClientModels();
        PathBlockTrait.registerClientModels();
        SaplingBlockTrait.registerClientModels();
        StalactiteBlockTrait.registerClientModels();
        TemplateModelTrait.registerClientModels();
        TerrainTraits.registerClientModels();
        TripleShapePillarModelTrait.registerClientModels();
        VineBlockTrait.registerClientModels();
        WaterSeedBlockTrait.registerClientModels();
        WeightedCrossModelTrait.registerClientModels();
        WeightedPillarModelTrait.registerClientModels();
        WeightedTemplateModelTrait.registerClientModels();
    }
}
