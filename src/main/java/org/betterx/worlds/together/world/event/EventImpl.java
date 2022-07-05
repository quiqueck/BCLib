package org.betterx.worlds.together.world.event;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class EventImpl<T> implements Event<T> {
    final List<T> handlers = new LinkedList<>();

    public final boolean on(T handler) {
        if (!handlers.contains(handler)) {
            handlers.add(handler);
            return true;
        }

        return false;
    }

    public final void emit(Consumer<T> c) {
        handlers.forEach(c);
    }
}
