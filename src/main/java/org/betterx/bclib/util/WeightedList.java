package org.betterx.bclib.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public class WeightedList<T> {


    private final List<Float> weights = new ArrayList<Float>();
    private final List<T> values = new ArrayList<T>();
    private float maxWeight;

    public static <T> Codec<Pair<Float, T>> pairCodec(Codec<T> elementCodec, String fieldName) {
        return Pair.pairCodec(Codec.FLOAT, elementCodec, "weight", fieldName);
    }

    public static <T> Codec<WeightedList<T>> listCodec(Codec<T> elementCodec, String fieldName, String elementName) {
        return RecordCodecBuilder.create(instance -> instance
                .group(
                        pairCodec(elementCodec, elementName).listOf()
                                                            .fieldOf(fieldName)
                                                            .forGetter(WeightedList::pairs)
                )
                .apply(instance, WeightedList::new)
        );
    }

    private List<Pair<Float, T>> pairs() {
        List<Pair<Float, T>> pairs = new ArrayList<>(weights.size());
        for (int i = 0; i < weights.size(); i++) {
            pairs.add(new Pair<>(weights.get(i), values.get(i)));
        }
        return pairs;
    }

    private WeightedList(List<Pair<Float, T>> pairs) {
        maxWeight = 0;
        for (var pair : pairs) {
            maxWeight += pair.first;
            weights.add(pair.first);
            values.add(pair.second);
        }
    }

    public WeightedList() {
    }

    public <R> WeightedList<R> map(Function<T, R> map) {
        List<Pair<Float, R>> pairs = new ArrayList<>(weights.size());
        for (int i = 0; i < weights.size(); i++) {
            pairs.add(new Pair<>(weights.get(i), map.apply(values.get(i))));
        }
        return new WeightedList<>(pairs);
    }

    public void addAll(WeightedList<T> other) {
        weights.addAll(other.weights);
        values.addAll(other.values);
        maxWeight += other.maxWeight;
    }

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
        if (maxWeight <= 0) {
            return null;
        }
        float weight = random.nextFloat() * maxWeight;
        for (int i = 0; i < weights.size(); i++) {
            if (weight <= weights.get(i)) {
                return values.get(i);
            }
            weight -= weights.get(i);
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
