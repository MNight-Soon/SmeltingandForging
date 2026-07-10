/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.smeltingandforging.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.mcreator.smeltingandforging.SmeltingandforgingMod;

public class SmeltingandforgingModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(SmeltingandforgingMod.MODID);
	public static final DeferredItem<Item> FORGER;
	static {
		FORGER = block(SmeltingandforgingModBlocks.FORGER);
	}

	// Start of user code block custom items
	// End of user code block custom items
	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
		return block(block, new Item.Properties());
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
	}
}