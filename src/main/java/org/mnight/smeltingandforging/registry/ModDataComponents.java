package org.mnight.smeltingandforging.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mnight.smeltingandforging.Smeltingandforging;
import org.mnight.smeltingandforging.item.component.WeaponStats;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS_TYPES = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, Smeltingandforging.MOD_ID);

    public static final Supplier<DataComponentType<WeaponStats>> WEAPON_STATS = DATA_COMPONENTS_TYPES.register("weapon_stats",
            () -> DataComponentType.<WeaponStats>builder()
                    .persistent(WeaponStats.CODEC)
                    .networkSynchronized(WeaponStats.STREAM_CODEC)
                    .build());

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS_TYPES.register(eventBus);
    }
}
