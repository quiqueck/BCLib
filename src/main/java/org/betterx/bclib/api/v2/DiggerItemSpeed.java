package org.betterx.bclib.api.v2;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class DiggerItemSpeed {
    public static final List<SpeedModifier> modifiers = new LinkedList<>();

    @FunctionalInterface
    public interface SpeedModifier {
        Optional<Float> calculateSpeed(ItemStack stack, BlockState state, float initialSpeed, float currentSpeed);
    }

    public static void addModifier(SpeedModifier mod) {
        modifiers.add(mod);
    }

    public static Optional<Float> getModifiedSpeed(ItemStack stack, BlockState state, float initialSpeed) {
        float currentSpeed = initialSpeed;
        Optional<Float> speed = Optional.empty();
        for (SpeedModifier mod : modifiers) {
            Optional<Float> res = mod.calculateSpeed(stack, state, initialSpeed, currentSpeed);
            if (res.isPresent()) {
                currentSpeed = res.get();
                speed = res;
            }
        }

        return speed;
    }
}
