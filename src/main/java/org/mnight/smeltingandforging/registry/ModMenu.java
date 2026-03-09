package org.mnight.smeltingandforging.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mnight.smeltingandforging.Smeltingandforging;
import org.mnight.smeltingandforging.inventory.SmelteryMenu;

import java.util.function.Supplier;

public class ModMenu {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, Smeltingandforging.MOD_ID);

    public static final Supplier<MenuType<SmelteryMenu>> SMELTER_MENU = MENUS.register("smeltery_menu",
            () -> IMenuTypeExtension.create(SmelteryMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
