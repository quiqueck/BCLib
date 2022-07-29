package org.betterx.bclib.items.boat;

import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BoatItem;

public class BaseBoatItem extends BoatItem implements CustomBoatTypeOverride, ItemModelProvider {
    BoatTypeOverride customType;

    public BaseBoatItem(boolean bl, BoatTypeOverride type, Properties properties) {
        super(bl, Boat.Type.OAK, properties);
        setCustomType(type);
    }

    @Override
    public void setCustomType(BoatTypeOverride type) {
        customType = type;
    }

    @Override
    public BoatTypeOverride bcl_getCustomType() {
        return customType;
    }
}
