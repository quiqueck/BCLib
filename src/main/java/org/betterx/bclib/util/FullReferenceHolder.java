package org.betterx.bclib.util;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

public class FullReferenceHolder<T> implements Holder<T> {
    private Set<TagKey<T>> tags = Set.of();
    @Nullable
    private ResourceKey<T> key;
    @Nullable
    private T value;

    private ResourceKey<Registry<T>> owner;

    private FullReferenceHolder(
            ResourceKey<Registry<T>> owner,
            @Nullable ResourceKey<T> resourceKey,
            @Nullable T object
    ) {
        this.owner = owner;
        this.key = resourceKey;
        this.value = object;
    }

    public static <T> FullReferenceHolder<T> create(
            ResourceKey<Registry<T>> owner,
            ResourceKey<T> resourceKey,
            @Nullable T object
    ) {
        return new FullReferenceHolder(owner, resourceKey, object);
    }

    public static <T> FullReferenceHolder<T> create(
            ResourceKey<Registry<T>> owner,
            ResourceLocation id,
            @Nullable T object
    ) {
        return new FullReferenceHolder(owner, ResourceKey.create(owner, id), object);
    }


    public ResourceKey<T> key() {
        if (this.key == null) {
            throw new IllegalStateException("Trying to access unbound value '" + this.value + "' from registry " + this.owner);
        } else {
            return this.key;
        }
    }

    @Override
    public T value() {
        if (this.value == null) {
            throw new IllegalStateException("Trying to access unbound value '" + this.key + "' from registry " + this.owner);
        } else {
            return this.value;
        }
    }

    @Override
    public boolean is(ResourceLocation resourceLocation) {
        return this.key().location().equals(resourceLocation);
    }

    @Override
    public boolean is(ResourceKey<T> resourceKey) {
        return this.key() == resourceKey;
    }

    @Override
    public boolean is(TagKey<T> tagKey) {
        return this.tags.contains(tagKey);
    }

    @Override
    public Stream<TagKey<T>> tags() {
        return this.tags.stream();
    }

    @Override
    public boolean is(Predicate<ResourceKey<T>> predicate) {
        return predicate.test(this.key());
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> holderOwner) {
        return true;
    }

    @Override
    public Either<ResourceKey<T>, T> unwrap() {
        return Either.left(this.key());
    }

    @Override
    public Optional<ResourceKey<T>> unwrapKey() {
        return Optional.of(this.key());
    }

    @Override
    public Kind kind() {
        return Holder.Kind.REFERENCE;
    }

    @Override
    public boolean isBound() {
        return this.key != null && this.value != null;
    }

    @Override
    public String toString() {
        return "FullReference{" + this.key + "=" + this.value + "}";
    }
}
