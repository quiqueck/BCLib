package org.betterx.bclib.util;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class WeightedList<T> {
    private final List<Float> weights = new ArrayList<Float>();
    private final List<T> values = new ArrayList<T>();
    private float maxWeight;

    /**
     * Adds value with specified weight to the list
     *
     * @param value
     * @param weight
     */
    public void add(T value, float weight) {
        maxWeight += weight;
        weights.add(maxWeight);
        values.add(value);
    }

    /**
     * Get  random value.
     *
     * @param random - {@link Random}.
     * @return {@link T} value.
     */
    public T get(RandomSource random) {
        if (maxWeight < 1) {
            return null;
        }
        float weight = random.nextFloat() * maxWeight;
        for (int i = 0; i < weights.size(); i++) {
            if (weight <= weights.get(i)) {
                return values.get(i);
            }
        }
        return null;
    }

    /**
     * Get value by index.
     *
     * @param index - {@code int} index.
     * @return {@link T} value.
     */
    public T get(int index) {
        return values.get(index);
    }

    /**
     * Get value weight. Weight is summed with all previous values weights.
     *
     * @param index - {@code int} index.
     * @return {@code float} weight.
     */
    public float getWeight(int index) {
        return weights.get(index);
    }

    /**
     * Chech if the list is empty.
     *
     * @return {@code true} if list is empty and {@code false} if not.
     */
    public boolean isEmpty() {
        return maxWeight == 0;
    }

    /**
     * Get the list size.
     *
     * @return {@code int} list size.
     */
    public int size() {
        return values.size();
    }

    /**
     * Makes a sublist of this list with same weights. Used only in {@link WeighTree}
     *
     * @param start - {@code int} start index (inclusive).
     * @param end   - {@code int} end index (exclusive).
     * @return {@link WeightedList}.
     */
    protected WeightedList<T> subList(int start, int end) {
        WeightedList<T> list = new WeightedList<T>();
        for (int i = start; i < end; i++) {
            list.weights.add(weights.get(i));
            list.values.add(values.get(i));
        }
        return list;
    }

    /**
     * Check if list contains certain value.
     *
     * @param value - {@link T} value.
     * @return {@code true} if value is in list and {@code false} if not.
     */
    public boolean contains(T value) {
        return values.contains(value);
    }

    /**
     * Applies {@link Consumer} to all values in list.
     *
     * @param function - {@link Consumer}.
     */
    public void forEach(Consumer<T> function) {
        values.forEach(function);
    }

    /**
     * Get the maximum weight of the tree.
     *
     * @return {@code float} maximum weight.
     */
    public float getMaxWeight() {
        return maxWeight;
    }
}
