package org.mnight.smeltingandforging.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.mnight.smeltingandforging.recipe.SmelteryRecipe;
import org.mnight.smeltingandforging.registry.ModBlockEntities;
import org.mnight.smeltingandforging.registry.ModRecipes;
import org.mnight.smeltingandforging.util.HeatHandler;

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
    private int maxFualTime = 0;

    private boolean isFormed = false;
    private int checkStructureTimer = 0;

    private final int[] slotProgress = new int[6];
    private final int[] slotMaxProgress = new int[6];

    public  SmelteryControllerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.SMELTERY_BE.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex){
                    case 0 -> SmelteryControllerBlockEntity.this.heatHandler.getTemperature();
                    case 1 -> SmelteryControllerBlockEntity.this.fuelTime;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> SmelteryControllerBlockEntity.this.heatHandler.setTemperature(pValue);
                    case 1 -> SmelteryControllerBlockEntity.this.fuelTime = pValue;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
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

    public static void ticks(Level pLevel, BlockPos pPos, BlockState pBlockState, SmelteryControllerBlockEntity pBlockEntity) {
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
                    pBlockEntity.maxFualTime = burntime;
                    pBlockEntity.itemHandler.extractItem(12,1,false);
                }
            } else {
                pBlockEntity.heatHandler.coolDown(1);
            }
        }

        for (int i = 0; i < 6; i++){
            pBlockEntity.processSlot(i);
        }
    }

    private void processSlot(int slotIndex){
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
                        craftItem(slotIndex, r, result);
                        slotProgress[slotIndex] = 0;
                    }
                }
            } else {
                if (slotProgress[slotIndex] > 0) slotProgress[slotIndex]--;
            }
        } else {
            slotProgress[slotIndex] = 0;
        }
    }

    private void craftItem(int slotIndex, SmelteryRecipe recipe, ItemStack result){
        itemHandler.extractItem(slotIndex, 1, false);
        itemHandler.insertItem(slotIndex + 6, result.copy(), false);
    }

    private boolean checkStructure() {
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        pTag.put("inventory", itemHandler.serializeNBT(pRegistries));
        //Save HeatHandler
        pTag.put("heat", heatHandler.serializeNBT(pRegistries));
        pTag.putIntArray("slotProgress", slotProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        itemHandler.deserializeNBT(pRegistries, pTag.getCompound("inventory"));
        // Load HeatHandler
        if (pTag.contains("heat")) {
            heatHandler.deserializeNBT(pRegistries, pTag.getCompound("heat"));
        }
        int[] loadedProgress = pTag.getIntArray("slotProgress");
        if (loadedProgress.length == 6){
            System.arraycopy(loadedProgress, 0, slotProgress, 0, 6);
        }
    }

}
