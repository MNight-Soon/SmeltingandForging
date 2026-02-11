package org.mnight.smeltingandforging.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mnight.smeltingandforging.Smeltingandforging;
import org.mnight.smeltingandforging.block.entity.SmelteryControllerBlockEntity;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Smeltingandforging.MOD_ID);

    public static final Supplier<BlockEntityType<SmelteryControllerBlockEntity>> SMELTERY_BE = BLOCK_ENTITIES.register("smeltery_be",
            () -> BlockEntityType.Builder.of(SmelteryControllerBlockEntity::new, ModBlocks.SMELTERY_CONTROLLER_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
