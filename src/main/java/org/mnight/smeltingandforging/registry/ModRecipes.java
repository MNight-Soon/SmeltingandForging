package org.mnight.smeltingandforging.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mnight.smeltingandforging.Smeltingandforging;
import org.mnight.smeltingandforging.recipe.AlloyRecipe;
import org.mnight.smeltingandforging.recipe.SmelteryRecipe;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Smeltingandforging.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Smeltingandforging.MOD_ID);

    public static final Supplier<RecipeType<SmelteryRecipe>> SMELTERY_RECIPE_TYPE = RECIPE_TYPES.register("smeltery", () -> new RecipeType<SmelteryRecipe>() {
        @Override
        public String toString() {return "smeltingandforging:smeltery";}
    });
    public static final Supplier<RecipeSerializer<SmelteryRecipe>> SMELTERY_SERIALIZER = RECIPE_SERIALIZER.register("smeltery", () -> new SmelteryRecipe.Serializer());

    public static final Supplier<RecipeType<AlloyRecipe>> ALLOY_RECIPE_TYPE = RECIPE_TYPES.register("alloy", () -> new RecipeType<AlloyRecipe>() {
        @Override
        public String toString() {return "smeltingandforging:alloy";}
    });
    public static final Supplier<RecipeSerializer<AlloyRecipe>> ALLOY_SERIALIZER = RECIPE_SERIALIZER.register("alloy", () -> new AlloyRecipe.Serializer());

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZER.register(eventBus);
    }
}
