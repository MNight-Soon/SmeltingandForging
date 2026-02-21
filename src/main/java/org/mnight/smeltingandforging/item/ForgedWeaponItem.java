package org.mnight.smeltingandforging.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.mnight.smeltingandforging.item.component.WeaponStats;
import org.mnight.smeltingandforging.registry.ModDataComponents;

import java.util.List;

public class ForgedWeaponItem extends Item {
    public ForgedWeaponItem(Properties properties) {
        super(properties.component(ModDataComponents.WEAPON_STATS.get(), WeaponStats.DEFAULT));
    }

    public static void applyStatsToStack(ItemStack stack, WeaponStats stats) {
        stack.set(ModDataComponents.WEAPON_STATS.get(), stats);

        ItemAttributeModifiers.Builder modifierBuilder = ItemAttributeModifiers.builder();

        float finalDamage = stats.baseDamage() * stats.quality();
        float finalSpeed = stats.attackSpeed();

        modifierBuilder.add(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_ID, finalDamage, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
        );

        modifierBuilder.add(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_ID, finalSpeed, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
        );

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifierBuilder.build());
        stack.set(DataComponents.MAX_DAMAGE, stats.maxDurability());
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);

        WeaponStats stats = pStack.getOrDefault(ModDataComponents.WEAPON_STATS.get(), WeaponStats.DEFAULT);

        pTooltipComponents.add(Component.translatable("tooltip.smeltingandforging.quality", String.format("%.2f", stats.quality())));
    }
}
