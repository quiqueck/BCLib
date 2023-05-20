package org.betterx.bclib.complexmaterials.entry;

import org.betterx.bclib.complexmaterials.ComplexMaterialSet;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class SlotMap<M extends ComplexMaterialSet<? extends ComplexMaterialSet<?>>> implements Iterable<MaterialSlot<M>> {
    private final Map<String, MaterialSlot<M>> map;

    public SlotMap() {
        this.map = new LinkedHashMap<>();
    }

    public static <M extends ComplexMaterialSet<? extends ComplexMaterialSet<?>>> SlotMap<M> of(MaterialSlot<M>... slots) {
        final SlotMap<M> map = new SlotMap<>();
        for (MaterialSlot<M> slot : slots) {
            map.add(slot);
        }
        return map;
    }

    public SlotMap<M> replace(MaterialSlot<M> slot) {
        return add(slot);
    }

    public SlotMap<M> add(MaterialSlot<M> slot) {
        map.put(slot.suffix, slot);
        return this;
    }

    public SlotMap<M> remove(MaterialSlot<M> slot) {
        map.remove(slot.suffix);
        return this;
    }

    public SlotMap<M> remove(String slot) {
        map.remove(slot);
        return this;
    }

    @NotNull
    @Override
    public Iterator<MaterialSlot<M>> iterator() {
        return map.values().iterator();
    }
}
