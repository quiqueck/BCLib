package org.betterx.bclib.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record BCLEntityWrapper<T extends Entity>(EntityType<T> type, boolean canSpawn) {
}
