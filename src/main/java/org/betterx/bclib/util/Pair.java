package org.betterx.bclib.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public class Pair<A, B> {
    public static <A, B> Codec<Pair<A, B>> pairCodec(Codec<A> a, Codec<B> b, String first, String second) {
        return RecordCodecBuilder.create(instance -> instance
                .group(
                        a.fieldOf(first).forGetter(o -> o.first),
                        b.fieldOf(second).forGetter(o -> o.second)
                )
                .apply(instance, Pair::new));
    }

    public final A first;
    public final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "Pair{" + "first=" + first + ", second=" + second + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
