package org.mnight.smeltingandforging.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mnight.smeltingandforging.Smeltingandforging;
import org.mnight.smeltingandforging.inventory.SmelteryMenu;
import org.mnight.smeltingandforging.network.SmelteryActionPayload;

public class SmelteryScreen extends AbstractContainerScreen<SmelteryMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Smeltingandforging.MOD_ID, "textures/gui/smeltery_gui.png");

    public SmelteryScreen(SmelteryMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        int mode = menu.getMode();
        if (mode == 0){
            guiGraphics.blit(TEXTURE, x + 10, y - 28,176,0, 28, 32);
            guiGraphics.blit(TEXTURE, x + 38, y - 28,204,0, 28, 28);
        } else {
            guiGraphics.blit(TEXTURE, x + 10, y - 28, 176, 32, 28, 28);
            guiGraphics.blit(TEXTURE, x + 38, y - 28, 204, 32, 28, 32);
        }

        if (menu.getFuel() > 0 && menu.getFuel() > 0) {
            int fuelHeight = (menu.getFuel()* 14) / menu.getMaxFuel();
            guiGraphics.blit(TEXTURE, x + 153, y + 43 + (14 - fuelHeight), 176, 64 + (14 - fuelHeight), 14, fuelHeight);
        }

        int tempHeight = (menu.getTemp()* 50)/ 2000;
        guiGraphics.blit(TEXTURE, x + 12, y + 68 - tempHeight, 190, 114 - tempHeight, 16, tempHeight);

        if (mode == 1){
            guiGraphics.drawString(this.font, "<", x + 50, y + 15, 0x404040, false);
            guiGraphics.drawString(this.font, ">", x + 110, y + 15, 0x404040, false);

            guiGraphics.drawString(this.font, "x0", x + 130, y + 38, 0x404040, false);

            ItemStack displayItem = new ItemStack(Items.IRON_SWORD);
            if (menu.getSelectedOutput() == 1) displayItem = new ItemStack(Items.IRON_AXE);

            guiGraphics.renderItem(displayItem, x + 80, y + 10);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics,  mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        if (mouseX >= x + 10 && mouseX < x + 38 && mouseY >= y - 28 && mouseY < y){
            PacketDistributor.sendToServer(new SmelteryActionPayload(menu.blockEntity.getBlockPos(), 0, 0));
            return true;
        }

        if (mouseX >= x + 38 && mouseX < x + 66 && mouseY >= y - 28 && mouseY < y){
            PacketDistributor.sendToServer(new SmelteryActionPayload(menu.blockEntity.getBlockPos(), 0, 1));
            return true;
        }

        if (menu.getMode() == 1){
            if (mouseX >= x + 48 && mouseX < x + 60 && mouseY >= y + 12 && mouseY < y + 25) {
                int newVal = Math.max(0, menu.getSelectedOutput() - 1);
                PacketDistributor.sendToServer(new SmelteryActionPayload(menu.blockEntity.getBlockPos(), 1, newVal));
                return true;
            }
            if (mouseX >= x + 108 && mouseX < x + 120 && mouseY >= y + 12 && mouseY < y + 25) {
                int newVal = menu.getSelectedOutput() + 1; // อนาคตสามารถใส่ค่า Max ได้
                PacketDistributor.sendToServer(new SmelteryActionPayload(menu.blockEntity.getBlockPos(), 1, newVal));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
