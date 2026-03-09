package org.mnight.smeltingandforging.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.mnight.smeltingandforging.block.entity.SmelteryControllerBlockEntity;
import org.mnight.smeltingandforging.registry.ModBlocks;
import org.mnight.smeltingandforging.registry.ModMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class SmelteryMenu extends  AbstractContainerMenu {
    public final SmelteryControllerBlockEntity blockEntity;
    private final ContainerData data;

    public SmelteryMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()),new SimpleContainerData(19));
    }

    public SmelteryMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenu.SMELTER_MENU.get(), pContainerId);
        this.blockEntity = (SmelteryControllerBlockEntity) entity;
        this.data = data;

        for (int i = 0; i < 6; i++) {
            this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(), i, 35 + (i * 18),24){
                @Override public boolean isActive() { return getMode() == 0; }
            });

            this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(), i + 6, 35 + (i * 18), 54) {
                @Override public boolean isActive() { return getMode() == 0; }
            });
        }

        for(int i =0; i < 4; i++){
            this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(), i, 53 + (i * 18), 35){
                @Override public boolean isActive() { return getMode() == 1; }
            });
        }

        this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(),6 , 80, 60){
            @Override public boolean isActive() { return getMode() == 1; }
        });

        this.addSlot(new SlotItemHandler(this.blockEntity.getItemHandler(), 12, 152, 60));

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addDataSlots(data);
    }

    public int getMode() { return data.get(2); }
    public int getSelectedOutput() { return data.get(3); }
    public int getTemp() { return data.get(0); }
    public int getFuel() { return data.get(1); }
    public int getMaxFuel() { return data.get(6); }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index){
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer){
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                pPlayer, ModBlocks.SMELTERY_CONTROLLER_BLOCK.get());
    }

    private void addPlayerInventory(Inventory playerInventory){
        for (int i = 0; i < 3; i++) {
            for (int l = 0; l < 9; l++) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory){
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
