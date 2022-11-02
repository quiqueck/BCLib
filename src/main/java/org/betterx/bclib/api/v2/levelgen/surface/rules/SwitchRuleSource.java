package org.betterx.bclib.api.v2.levelgen.surface.rules;

import org.betterx.bclib.interfaces.NumericProvider;
import org.betterx.bclib.mixin.common.SurfaceRulesContextAccessor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules.Context;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import net.minecraft.world.level.levelgen.SurfaceRules.SurfaceRule;

import java.util.List;
import org.jetbrains.annotations.Nullable;

//
public record SwitchRuleSource(NumericProvider selector, List<RuleSource> collection) implements RuleSource {
    public static final Codec<SwitchRuleSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                                                                                                              NumericProvider.CODEC.fieldOf("selector").forGetter(SwitchRuleSource::selector),
                                                                                                              RuleSource.CODEC.listOf().fieldOf("collection").forGetter(SwitchRuleSource::collection)
                                                                                                      )
                                                                                                      .apply(
                                                                                                              instance,
                                                                                                              SwitchRuleSource::new
                                                                                                      ));

    private static final KeyDispatchDataCodec<? extends RuleSource> KEY_CODEC = KeyDispatchDataCodec.of(SwitchRuleSource.CODEC);

    @Override
    public KeyDispatchDataCodec<? extends RuleSource> codec() {
        return KEY_CODEC;
    }

    @Override
    public SurfaceRule apply(Context context) {

        return new SurfaceRule() {
            @Nullable
            @Override
            public BlockState tryApply(int x, int y, int z) {
                final SurfaceRulesContextAccessor ctx = SurfaceRulesContextAccessor.class.cast(context);
                int nr = Math.max(0, selector.getNumber(ctx)) % collection.size();

                return collection.get(nr).apply(context).tryApply(x, y, z);
            }
        };
    }

    static {

    }
}
