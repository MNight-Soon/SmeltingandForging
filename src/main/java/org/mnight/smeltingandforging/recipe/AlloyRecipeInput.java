package org.mnight.smeltingandforging.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public record AlloyRecipeInput(List<ItemStack> input) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return input.get(index);
    }

    @Override
    public int size() {
        return input.size();
    }

    @Override
    public boolean isEmpty() {
        return input.isEmpty();
    }
}
