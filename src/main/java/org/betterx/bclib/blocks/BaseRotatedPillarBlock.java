package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.RuntimeBlockModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public abstract class BaseRotatedPillarBlock extends RotatedPillarBlock implements RuntimeBlockModelProvider, DropSelfLootProvider<BaseRotatedPillarBlock> {
    protected BaseRotatedPillarBlock(Properties settings) {
        super(settings);
    }

    protected BaseRotatedPillarBlock(Block block) {
        this(Properties.ofFullCopy(block));
    }


    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation blockId) {
        return getBlockModel(blockId, defaultBlockState());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
        Optional<String> pattern = createBlockPattern(blockId);
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public UnbakedModel getModelVariant(
            ModelResourceLocation stateId,
            BlockState blockState,
            Map<ResourceLocation, UnbakedModel> modelCache
    ) {
        ModelResourceLocation modelId = RuntimeBlockModelProvider.remapModelResourceLocation(stateId, blockState);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        return ModelsHelper.createRotatedModel(modelId.id(), blockState.getValue(AXIS));
    }

    protected Optional<String> createBlockPattern(ResourceLocation blockId) {
        return PatternsHelper.createBlockPillar(blockId);
    }

    public static class Wood extends BaseRotatedPillarBlock implements BehaviourWood {
        protected final boolean flammable;

        public Wood(Properties settings, boolean flammable) {
            super(flammable ? settings.ignitedByLava() : settings);
            this.flammable = flammable;
        }

        public Wood(Block block, boolean flammable) {
            this(Properties.ofFullCopy(block), flammable);
        }
    }

    public static class Stone extends BaseRotatedPillarBlock implements BehaviourStone {
        public Stone(Properties settings) {
            super(settings);
        }

        public Stone(Block block) {
            super(block);
        }
    }

    public static class Metal extends BaseRotatedPillarBlock implements BehaviourMetal {
        public Metal(Properties settings) {
            super(settings);
        }

        public Metal(Block block) {
            super(block);
        }
    }

    public static BaseRotatedPillarBlock from(Block source, boolean flammable) {
        return BehaviourHelper.from(
                source,
                (s) -> new Wood(s, flammable),
                Stone::new,
                Metal::new
        );
    }
}
