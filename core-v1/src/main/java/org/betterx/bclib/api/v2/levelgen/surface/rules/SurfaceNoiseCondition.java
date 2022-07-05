package org.betterx.bclib.api.v2.levelgen.surface.rules;

import org.betterx.bclib.mixin.common.SurfaceRulesContextAccessor;

import net.minecraft.world.level.levelgen.SurfaceRules.Condition;
import net.minecraft.world.level.levelgen.SurfaceRules.Context;
import net.minecraft.world.level.levelgen.SurfaceRules.LazyXZCondition;

public abstract class SurfaceNoiseCondition implements NoiseCondition {
    @Override
    public final Condition apply(Context context2) {
        final SurfaceNoiseCondition self = this;

        class Generator extends LazyXZCondition {
            Generator() {
                super(context2);
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
