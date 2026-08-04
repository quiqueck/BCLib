package org.betterx.bclib.blocks.definitions;

import de.ambertation.wover.block.api.BlockDefinition;
import de.ambertation.wover.block.api.BlockRegistry;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.*;

import org.jetbrains.annotations.NotNull;

public class LeavesBlockDefinition<B extends LeavesBlock, D extends LeavesBlockDefinition<B, D>> extends BlockDefinition<B, D> {
    public interface LeaveBlockFactory<B extends LeavesBlock, D extends LeavesBlockDefinition<B, D>> {
        B createItem(float particleChance, D def);
    }

    protected float particleChance;
    protected ParticleOptions particleOptions;

    protected LeavesBlockDefinition(
            BlockRegistry registry,
            String blockName,
            SoundType soundType,
            LeaveBlockFactory<B, D> blockFactory
    ) {
        super(
                registry,
                blockName,
                (def) -> blockFactory.createItem(def.particleChance, def),
                Blocks.leavesProperties(soundType)
        );
        this.particleChance = 0.01F; // Default particle chance for leaves
    }

    protected LeavesBlockDefinition(
            BlockRegistry registry,
            String blockName,
            LeaveBlockFactory<B, D> blockFactory
    ) {
        this(registry, blockName, SoundType.GRASS, blockFactory);
    }

    public static <D extends LeavesBlockDefinition<TintedParticleLeavesBlock, D>> D ofVanillaTinted(
            BlockRegistry registry,
            String blockName
    ) {
        return ofVanillaTinted(registry, blockName, SoundType.GRASS);
    }

    @SuppressWarnings("unchecked")
    public static <D extends LeavesBlockDefinition<TintedParticleLeavesBlock, D>> D ofVanillaTinted(
            BlockRegistry registry,
            String blockName,
            SoundType soundType
    ) {
        return (D) new LeavesBlockDefinition<TintedParticleLeavesBlock, D>(
                registry, blockName,
                soundType, (chance, def) -> new TintedParticleLeavesBlock(chance, def.getProperties())
        );
    }

    public static <D extends LeavesBlockDefinition<UntintedParticleLeavesBlock, D>> D ofVanillaUntinted(
            BlockRegistry registry,
            String blockName
    ) {
        return ofVanillaUntinted(registry, blockName, SoundType.GRASS);
    }

    public static <D extends LeavesBlockDefinition<UntintedParticleLeavesBlock, D>> D ofVanillaUntinted(
            BlockRegistry registry,
            String blockName,
            SoundType soundType
    ) {
        return new LeavesBlockDefinition<UntintedParticleLeavesBlock, D>(
                registry,
                blockName,
                soundType,
                (chance, def) -> new UntintedParticleLeavesBlock(chance, def.particleOptions, def.getProperties())
        ).particleOptions(0xFF70922D).particleChance(0.1f);
    }

    @SuppressWarnings("unchecked")
    public D particleChance(float particleChance) {
        this.particleChance = particleChance;
        return (D) this;
    }

    @SuppressWarnings("unchecked")
    public float particleChance() {
        return particleChance;
    }

    @SuppressWarnings("unchecked")
    public D particleOptions(int color) {
        this.particleOptions = ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, color);
        return (D) this;
    }

    @SuppressWarnings("unchecked")
    public D particleOptions(ParticleOptions particleOptions) {
        this.particleOptions = particleOptions;
        return (D) this;
    }

    public ParticleOptions particleOptions() {
        return particleOptions;
    }

    @Override
    protected void beforeBuild() {

    }

    @Override
    protected @NotNull B beforeRegister(@NotNull B block) {
        return block;
    }
}
