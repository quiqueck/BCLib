package ru.bclib.mixin.common;

import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.bclib.interfaces.FrozableRegistry;

@Mixin(MappedRegistry.class)
public class MappedRegistryMixin<T> implements FrozableRegistry {
	@Shadow
	private boolean frozen;
	
	@Override
	public void setFrozeState(boolean frozen) {
		this.frozen = frozen;
	}
	
	@Override
	public boolean getFrozeState() {
		return this.frozen;
	}
}
