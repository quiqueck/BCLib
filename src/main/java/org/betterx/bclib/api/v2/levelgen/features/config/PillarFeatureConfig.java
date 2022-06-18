package org.betterx.bclib.api.v2.levelgen.features.config;

import org.betterx.bclib.blocks.BCLBlockProperties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class PillarFeatureConfig implements FeatureConfiguration {
    @FunctionalInterface
    public interface StateTransform {
        int apply(int height, int maxHeight, BlockState inputState, BlockPos pos, RandomSource rnd);
    }

    public enum KnownTransformers implements StringRepresentable {
        SIZE_DECREASE(
                "size_decrease",
                BCLBlockProperties.SIZE,
                (height, maxHeight, state, pos, rnd) -> Math.max(0, Math.min(7, maxHeight - height))
        ),
        SIZE_INCREASE(
                "size_increase",
                BCLBlockProperties.SIZE,
                (height, maxHeight, state, pos, rnd) -> Math.max(0, Math.min(7, height))
        );


        public static final StringRepresentable.EnumCodec<KnownTransformers> CODEC = StringRepresentable
                .fromEnum(KnownTransformers::values);


        public final String name;
        public final IntegerProperty property;
        public final StateTransform transform;

        KnownTransformers(String name, IntegerProperty property, StateTransform transform) {
            this.name = name;
            this.property = property;
            this.transform = transform;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final Codec<PillarFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    IntProvider.CODEC.fieldOf("height").forGetter(o -> o.height),
                    Direction.CODEC.fieldOf("direction").orElse(Direction.UP).forGetter(o -> o.direction),
                    BlockPredicate.CODEC.fieldOf("allowed_placement").forGetter(o -> o.allowedPlacement),
                    BlockStateProvider.CODEC.fieldOf("state").forGetter(o -> o.stateProvider),
                    KnownTransformers.CODEC.fieldOf("transform").forGetter(o -> o.transformer)
            )
            .apply(instance, PillarFeatureConfig::new));

    public final IntProvider height;
    public final BlockStateProvider stateProvider;

    public final KnownTransformers transformer;
    public final Direction direction;
    public final BlockPredicate allowedPlacement;


    public PillarFeatureConfig(
            IntProvider height,
            Direction direction,
            BlockPredicate allowedPlacement,
            BlockStateProvider stateProvider,
            KnownTransformers transformer
    ) {
        this.height = height;
        this.stateProvider = stateProvider;
        this.transformer = transformer;
        this.direction = direction;
        this.allowedPlacement = allowedPlacement;
    }

    public BlockState transform(int currentHeight, int maxHeight, BlockPos pos, RandomSource rnd) {
        BlockState state = stateProvider.getState(rnd, pos);
        return state.setValue(
                transformer.property,
                transformer.transform.apply(currentHeight, maxHeight, state, pos, rnd)
        );
    }
}
