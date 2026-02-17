package org.mnight.smeltingandforging.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mnight.smeltingandforging.Smeltingandforging;
import org.mnight.smeltingandforging.block.SmelteryControllerBlock;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Smeltingandforging.MOD_ID);

    public static final DeferredBlock<Block> SMELTERY_CONTROLLER_BLOCK = BLOCKS.register("smeltery_controller",
            () -> new SmelteryControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4f)
                    .requiresCorrectToolForDrops()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
