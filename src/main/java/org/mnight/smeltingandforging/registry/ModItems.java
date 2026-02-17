package org.mnight.smeltingandforging.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mnight.smeltingandforging.Smeltingandforging;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Smeltingandforging.MOD_ID);
    public static final DeferredItem<Item> SMELTERY_CONTROLLER_ITEM = ITEMS.register("smeltery_controller",
            () -> new BlockItem(ModBlocks.SMELTERY_CONTROLLER_BLOCK.get(), new Item.Properties()));

    public static void register(IEventBus eventbus) {
        ITEMS.register(eventbus);
    }
}
