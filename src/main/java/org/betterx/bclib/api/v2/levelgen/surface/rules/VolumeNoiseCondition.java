package org.betterx.bclib.api.v2.levelgen.surface.rules;

import org.betterx.bclib.mixin.common.SurfaceRulesContextAccessor;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules.Condition;
import net.minecraft.world.level.levelgen.SurfaceRules.ConditionSource;
import net.minecraft.world.level.levelgen.SurfaceRules.Context;
import net.minecraft.world.level.levelgen.SurfaceRules.LazyCondition;

public abstract class VolumeNoiseCondition implements NoiseCondition {
    public abstract KeyDispatchDataCodec<? extends ConditionSource> codec();

    @Override
    public final Condition apply(Context context2) {
        final VolumeNoiseCondition self = this;

        class Generator extends LazyCondition {
            Generator() {
                super(context2);
            }

            @Override
            protected long getContextLastUpdate() {
                final SurfaceRulesContextAccessor ctx = SurfaceRulesContextAccessor.class.cast(this.context);
                return ctx.getLastUpdateY() + ctx.getLastUpdateXZ();
            }

            @Override
            protected boolean compute() {
                final SurfaceRulesContextAccessor context = SurfaceRulesContextAccessor.class.cast(this.context);
                if (context == null) return false;
                return self.test(context);
            }
        }

        return new Generator();
    }

    public abstract boolean test(SurfaceRulesContextAccessor context);
}
