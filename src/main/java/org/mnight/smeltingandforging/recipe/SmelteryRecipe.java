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
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.mnight.smeltingandforging.registry.ModRecipes;

public class SmelteryRecipe implements Recipe<SingleRecipeInput> {

    // กำหนดส่วนประกอบของสูตร: วัตถุดิบ, ผลลัพธ์, อุณหภูมิที่ต้องการ, เวลาที่ใช้
    private final Ingredient input;
    private final ItemStack output;
    private final int minTemperature; // อุณหภูมิขั้นต่ำในการหลอม
    private final int processTime;    // เวลาที่ใช้ในการหลอม (tick)

    public SmelteryRecipe(Ingredient input, ItemStack output, int minTemperature, int processTime) {
        this.input = input;
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
    public boolean matches(SingleRecipeInput pInput, Level level) {
        // เช็คว่าไอเทมในสล็อตตรงกับ Input ของสูตรหรือไม่ (Logic จะถูกเรียกใช้ใน BlockEntity)
        return this.input.test(pInput.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput pInput, HolderLookup.Provider pRegistries){
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistries) {
        return this.output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SMELTERY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.SMELTERY_RECIPE_TYPE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(this.input);
        return list;
    }

    public static class Serializer implements RecipeSerializer<SmelteryRecipe> {
        public static final MapCodec<SmelteryRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.input),
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.output),
                Codec.INT.fieldOf("min_temperature").forGetter(r -> r.minTemperature),
                Codec.INT.fieldOf("process_time").forGetter(r -> r.processTime)
        ).apply(inst, SmelteryRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SmelteryRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.input,
                ItemStack.STREAM_CODEC, r -> r.output,
                ByteBufCodecs.INT, r -> r.minTemperature,
                ByteBufCodecs.INT, r -> r.processTime,
                SmelteryRecipe::new
        );

        @Override
        public MapCodec<SmelteryRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmelteryRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
