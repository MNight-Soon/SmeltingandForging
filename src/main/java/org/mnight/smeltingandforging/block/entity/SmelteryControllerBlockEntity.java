package org.mnight.smeltingandforging.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.mnight.smeltingandforging.recipe.AlloyRecipe;
import org.mnight.smeltingandforging.recipe.AlloyRecipeInput;
import org.mnight.smeltingandforging.recipe.SmelteryRecipe;
import org.mnight.smeltingandforging.registry.ModBlockEntities;
import org.mnight.smeltingandforging.registry.ModRecipes;
import org.mnight.smeltingandforging.util.HeatHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SmelteryControllerBlockEntity extends BlockEntity implements MenuProvider {

    private final ItemStackHandler itemHandler = new ItemStackHandler(13){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final HeatHandler heatHandler = new HeatHandler(2000);

    protected final ContainerData data;
    private int fuelTime = 0;
    private int maxFuelTime = 0;

    private boolean isFormed = false;
    private int checkStructureTimer = 0;

    private int mode = 0;
    private int selectedOutput = 0;

    private final int[] slotProgress = new int[6];
    private final int[] slotMaxProgress = new int[6];

    private int alloyProgress = 0;
    private int alloyMaxProgress = 0;

    public  SmelteryControllerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.SMELTERY_BE.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex){
                    case 0 -> SmelteryControllerBlockEntity.this.heatHandler.getTemperature();
                    case 1 -> SmelteryControllerBlockEntity.this.fuelTime;
                    case 2 -> SmelteryControllerBlockEntity.this.mode;
                    case 3 -> SmelteryControllerBlockEntity.this.selectedOutput;
                    case 4 -> SmelteryControllerBlockEntity.this.alloyProgress;
                    case 5 -> SmelteryControllerBlockEntity.this.alloyMaxProgress;
                    case 6 -> SmelteryControllerBlockEntity.this.maxFuelTime;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> SmelteryControllerBlockEntity.this.heatHandler.setTemperature(pValue);
                    case 1 -> SmelteryControllerBlockEntity.this.fuelTime = pValue;
                    case 2 -> SmelteryControllerBlockEntity.this.mode = pValue;
                    case 3 -> SmelteryControllerBlockEntity.this.selectedOutput = pValue;
                    case 4 -> SmelteryControllerBlockEntity.this.alloyProgress = pValue;
                    case 5 -> SmelteryControllerBlockEntity.this.alloyMaxProgress = pValue;
                    case 6 -> SmelteryControllerBlockEntity.this.maxFuelTime = pValue;
                }
            }

            @Override
            public int getCount() {
                return 7;
            }
        };
    }

    public void setMode(int newMode) {
        this.mode = newMode;
        setChanged();
    }

    public void setSelectedOutput(int selection) {
        this.selectedOutput = selection;
        setChanged();
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.smeltingandforging.smeltery_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player player){
        return null;
    }

    public static void ticks(Level pLevel, BlockPos pPos, BlockState pState, SmelteryControllerBlockEntity pBlockEntity) {
        if (pLevel.isClientSide()) return;

        if (pBlockEntity.checkStructureTimer++ >= 20) {
            pBlockEntity.isFormed = pBlockEntity.checkStructure();
            pBlockEntity.checkStructureTimer = 0;
        }

        if (!pBlockEntity.isFormed) {
            pBlockEntity.heatHandler.coolDown(5);
            return;
        }

        if (pBlockEntity.fuelTime > 0){
            pBlockEntity.fuelTime--;
            pBlockEntity.heatHandler.heatUp(1);
        } else {
            ItemStack fuelStack = pBlockEntity.itemHandler.getStackInSlot(12);
            if (!fuelStack.isEmpty() && pBlockEntity.heatHandler.getTemperature() < pBlockEntity.heatHandler.getMaxTemperature()) {
                int burntime = AbstractFurnaceBlockEntity.getFuel().getOrDefault(fuelStack.getItem(), 0);
                if (burntime > 0) {
                    pBlockEntity.fuelTime = burntime;
                    pBlockEntity.maxFuelTime = burntime;
                    pBlockEntity.itemHandler.extractItem(12,1,false);
                    setChanged(pLevel,pPos, pState);
                }
            } else {
                pBlockEntity.heatHandler.coolDown(1);
            }
        }

        if (pBlockEntity.mode == 0) {
            // Mode 1: Smelting (6 ช่องแยกกัน)
            for (int i = 0; i < 6; i++) {
                pBlockEntity.processSmeltingSlot(i);
            }
            pBlockEntity.alloyProgress = 0; // Reset progress ของโหมดอื่น
        } else if (pBlockEntity.mode == 1) {
            // Mode 2: Alloying (รวม 4 ช่อง -> 1 ช่อง)
            pBlockEntity.processAlloying();
            for(int i=0; i<6; i++) pBlockEntity.slotProgress[i] = 0; // Reset progress ของโหมดปกติ
        }
    }

    private void processSmeltingSlot(int slotIndex){
        ItemStack inputStack = itemHandler.getStackInSlot(slotIndex);
        if (inputStack.isEmpty()){
            slotProgress[slotIndex] = 0;
            return;
        }

        Optional<SmelteryRecipe> recipe = this.level.getRecipeManager()
                .getRecipeFor(ModRecipes.SMELTERY_RECIPE_TYPE.get(), new SingleRecipeInput(inputStack), this.level)
                .map(r -> (SmelteryRecipe) r.value());
        if (recipe.isPresent()){
            SmelteryRecipe r = recipe.get();
            // เช็คอุณหภูมิผ่าน HeatHandler
            if (this.heatHandler.isHotEnough(r.getMinTemperature())){
                ItemStack result = r.getResultItem(this.level.registryAccess());
                ItemStack outputSlotStack = itemHandler.getStackInSlot(slotIndex + 6);

                boolean canInsert = outputSlotStack.isEmpty() ||
                        (outputSlotStack.getItem() == result.getItem() && outputSlotStack.getCount() + result.getCount() <= outputSlotStack.getMaxStackSize());

                if (canInsert){
                    slotProgress[slotIndex]++;
                    slotMaxProgress[slotIndex] = r.getProcessTime();

                    if (slotProgress[slotIndex] >= slotMaxProgress[slotIndex]){
                        itemHandler.extractItem(slotIndex, 1, false);
                        itemHandler.insertItem(slotIndex + 6, result.copy(), false);
                        slotProgress[slotIndex] = 0;
                        setChanged();
                    }
                }
            } else {
                if (slotProgress[slotIndex] > 0) slotProgress[slotIndex]--;
            }
        } else {
            slotProgress[slotIndex] = 0;
        }
    }

    private void processAlloying(){
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            inputs.add(itemHandler.getStackInSlot(i));
        }

        AlloyRecipeInput alloyInput = new AlloyRecipeInput(inputs);

        Optional<AlloyRecipe> recipe = this.level.getRecipeManager()
                .getRecipeFor(ModRecipes.ALLOY_RECIPE_TYPE.get(), alloyInput, this.level)
                .map(r -> (AlloyRecipe) r.value());

        if (recipe.isPresent()){
            AlloyRecipe r = recipe.get();
            if (this.heatHandler.isHotEnough(r.getMinTemperature())){
                ItemStack result = r.getResultItem(this.level.registryAccess());
                ItemStack outputSlotStack = itemHandler.getStackInSlot(6);

                boolean canInsert =  outputSlotStack.isEmpty() ||
                        (outputSlotStack.getItem() == result.getItem() && outputSlotStack.getCount() <= outputSlotStack.getMaxStackSize());

                if (canInsert){
                    this.alloyProgress++;
                    this.alloyMaxProgress = r.getProcessTime();

                    if (this.alloyProgress >= this.alloyMaxProgress){
                        craftAlloy(r, result, inputs);
                        this.alloyProgress = 0;
                        setChanged();
                    }
                }
            } else {
                if (this.alloyProgress > 0) this.alloyProgress--;
            }
        } else {
            this.alloyProgress = 0;
        }
    }

    private void craftAlloy(AlloyRecipe recipe, ItemStack result, List<ItemStack> inputs){
        ItemStack outputItem = result.copy();
        saveCompositionData(outputItem, inputs);
        itemHandler.insertItem(6, outputItem, false);
        for(int i=0; i<4; i++) {
            if(!itemHandler.getStackInSlot(i).isEmpty()) {
                itemHandler.extractItem(i, 1, false);
            }
        }
    }

    private void saveCompositionData(ItemStack output, List<ItemStack> inputs){
        CustomData customData = output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        ListTag compositionList = new ListTag();

        for(ItemStack stack : inputs){
            if (!stack.isEmpty()) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                compositionList.add(StringTag.valueOf(itemId.toString()));
            }
        }

        tag.put("Composition", compositionList);
        tag.putInt("PatternID", this.selectedOutput);
        output.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private boolean checkStructure() {
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        pTag.put("inventory", itemHandler.serializeNBT(pRegistries));
        pTag.put("heat", heatHandler.serializeNBT(pRegistries));
        pTag.putIntArray("slotProgress", slotProgress);
        pTag.putInt("mode", mode);
        pTag.putInt("selectedOutput", selectedOutput);
        pTag.putInt("alloyProgress", alloyProgress);
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        itemHandler.deserializeNBT(pRegistries, pTag.getCompound("inventory"));
        if (pTag.contains("heat")) {
            heatHandler.deserializeNBT(pRegistries, pTag.getCompound("heat"));
        }
        int[] loadedProgress = pTag.getIntArray("slotProgress");
        if (loadedProgress.length == 6) {
            System.arraycopy(loadedProgress, 0, slotProgress, 0, 6);
        }
        mode = pTag.getInt("mode");
        selectedOutput = pTag.getInt("selectedOutput");
        alloyProgress = pTag.getInt("alloyProgress");
        fuelTime = pTag.getInt("fuelTime");
        maxFuelTime = pTag.getInt("maxFuelTime");
    }

}
