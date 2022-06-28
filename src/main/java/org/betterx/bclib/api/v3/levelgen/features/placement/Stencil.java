package org.betterx.bclib.api.v3.levelgen.features.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Stencil extends PlacementModifier {
    public static final Codec<Stencil> CODEC;
    private static final Boolean[] BN_STENCIL;
    private final List<Boolean> stencil;
    private static final Stencil DEFAULT;
    private static final Stencil DEFAULT4;
    private final int selectOneIn;

    private static List<Boolean> convert(Boolean[] s) {
        return Arrays.stream(s).toList();
    }

    public Stencil(Boolean[] stencil, int selectOneIn) {
        this(convert(stencil), selectOneIn);
    }

    public Stencil(List<Boolean> stencil, int selectOneIn) {
        this.stencil = stencil;
        this.selectOneIn = selectOneIn;
    }

    public static Stencil all() {
        return DEFAULT;
    }

    public static Stencil oneIn4() {
        return DEFAULT4;
    }

    @Override
    public Stream<BlockPos> getPositions(
            PlacementContext placementContext,
            RandomSource randomSource,
            BlockPos blockPos
    ) {
        List<BlockPos> pos = new ArrayList<>(16 * 16);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (stencil.get(x << 4 | y)) {
                    pos.add(blockPos.offset(x, 0, y));
                }
            }
        }

        return pos.stream();
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.STENCIL;
    }

    static {
        BN_STENCIL = new Boolean[]{
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                true,
                true,
                false,
                false,
                true,
                true,
                true,
                false,
                false,
                false,
                true,
                true,
                false,
                false,
                false,
                true,
                false,
                false,
                true,
                true,
                true,
                false,
                false,
                true,
                true,
                true,
                true,
                false,
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                true,
                true,
                false,
                false,
                true,
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                false,
                true,
                true,
                false,
                true,
                true,
                false,
                false,
                false,
                true,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                true,
                true,
                true,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                true,
                false,
                true,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                true,
                false,
                true,
                true,
                false,
                false,
                true,
                false,
                false,
                false,
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                true,
                false,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                true,
                true,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                true,
                true,
                false,
                false
        };

        DEFAULT = new Stencil(BN_STENCIL, 1);
        DEFAULT4 = new Stencil(BN_STENCIL, 4);
        CODEC = RecordCodecBuilder.create((instance) -> instance
                .group(
                        ExtraCodecs.nonEmptyList(Codec.BOOL.listOf())
                                   .fieldOf("structures")
                                   .orElse(convert(BN_STENCIL))
                                   .forGetter((Stencil a) -> a.stencil),
                        Codec.INT
                                .fieldOf("one_in")
                                .orElse(1)
                                .forGetter((Stencil a) -> a.selectOneIn)
                )
                .apply(instance, Stencil::new)
        );
    }
}
