package org.betterx.bclib.util;

import net.minecraft.world.level.levelgen.WorldgenRandom;

import java.util.Locale;
import java.util.Random;

public class WeighTree<T> {
    private final float maxWeight;
    private final Node root;

    public WeighTree(WeightedList<T> list) {
        maxWeight = list.getMaxWeight();
        root = getNode(list);
    }

    /**
     * Get eandom value from tree.
     *
     * @param random - {@link Random}.
     * @return {@link T} value.
     */
    public T get(WorldgenRandom random) {
        return root.get(random.nextFloat() * maxWeight);
    }

    private Node getNode(WeightedList<T> list) {
        int size = list.size();
        if (size == 1) {
            return new Leaf(list.get(0));
        } else if (size == 2) {
            T first = list.get(0);
            return new Branch(list.getWeight(0), new Leaf(first), new Leaf(list.get(1)));
        } else {
            int index = size >> 1;
            float separator = list.getWeight(index);
            Node a = getNode(list.subList(0, index + 1));
            Node b = getNode(list.subList(index, size));
            return new Branch(separator, a, b);
        }
    }

    private abstract class Node {
        abstract T get(float value);
    }

    private class Branch extends Node {
        final float separator;
        final Node min;
        final Node max;

        public Branch(float separator, Node min, Node max) {
            this.separator = separator;
            this.min = min;
            this.max = max;
        }

        @Override
        T get(float value) {
            return value < separator ? min.get(value) : max.get(value);
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "[%f, %s, %s]", separator, min.toString(), max.toString());
        }
    }

    private class Leaf extends Node {
        final T biome;

        Leaf(T value) {
            this.biome = value;
        }

        @Override
        T get(float value) {
            return biome;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "[%s]", biome.toString());
        }
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
