package org.mnight.smeltingandforging.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.mnight.smeltingandforging.registry.ModRecipes;

import java.util.ArrayList;
import java.util.List;

public class AlloyRecipe implements Recipe<AlloyRecipeInput> {

    private final NonNullList<Ingredient> ingredients;
    private final ItemStack output;
    private final int minTemperature;
    private final int processTime;

    public AlloyRecipe(NonNullList<Ingredient> ingredients, ItemStack output, int minTemperature, int processTime) {
        this.ingredients = ingredients;
        this.output = output;
        this.minTemperature = minTemperature;
        this.processTime = processTime;
    }

    public int getMinTemperature() {
        return minTemperature;
    }

    public int getProcessTime() {
        return processTime;
    }

    @Override
    public boolean matches(AlloyRecipeInput pInput, Level pLevel) {
        List<ItemStack> inputs = new ArrayList<>();
        inputs.removeIf(ItemStack::isEmpty);
        if (inputs.size() != ingredients.size()) return false;

        List<Ingredient> recipesIngredients = new ArrayList<>(ingredients);

        for (ItemStack item: inputs) {
            boolean found = false;
            for (int i = 0; i < recipesIngredients.size(); i++) {
                if (recipesIngredients.get(i).test(item)) {
                    recipesIngredients.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble (AlloyRecipeInput pInput, HolderLookup.Provider pRegistry) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistry) {
        return this.output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ALLOY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ALLOY_RECIPE_TYPE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public static class Serializer implements RecipeSerializer<AlloyRecipe> {
        public static final MapCodec<AlloyRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").xmap(
                        ingredients -> NonNullList.of(Ingredient.EMPTY, ingredients.toArray(new Ingredient[0])),
                        ingredients -> ingredients
                ).forGetter(r -> r.ingredients),
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.output),
                Codec.INT.fieldOf("min_temperature").forGetter(r -> r.minTemperature),
                Codec.INT.fieldOf("process_time").forGetter(r -> r.processTime)
        ).apply(inst, AlloyRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AlloyRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)), r -> r.ingredients,
                ItemStack.STREAM_CODEC, r -> r.output,
                ByteBufCodecs.INT, r -> r.minTemperature,
                ByteBufCodecs.INT, r -> r.processTime,
                AlloyRecipe::new
        );

        @Override
        public MapCodec<AlloyRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AlloyRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
