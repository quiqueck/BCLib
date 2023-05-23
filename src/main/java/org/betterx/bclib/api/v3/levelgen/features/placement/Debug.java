package org.betterx.bclib.api.v3.levelgen.features.placement;

import org.betterx.bclib.BCLib;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public class Debug extends PlacementModifier {
    public static final Debug INSTANCE = new Debug("Placing at {}");
    public static final Codec<Debug> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    Codec.STRING
                            .fieldOf("caption")
                            .orElse("Placing at {}")
                            .forGetter(cfg -> cfg.caption)
            )
            .apply(instance, Debug::new));
    private final String caption;

    public Debug(String caption) {
        this.caption = caption;
    }

    @Override
    public Stream<BlockPos> getPositions(
            PlacementContext placementContext,
            RandomSource randomSource,
            BlockPos blockPos
    ) {
        BCLib.LOGGER.info(caption, blockPos);
        return Stream.of(blockPos);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.DEBUG;
    }
}
