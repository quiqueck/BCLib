package org.betterx.worlds.together.world.event;

public interface Event<T> {
    boolean on(T handler);
}
