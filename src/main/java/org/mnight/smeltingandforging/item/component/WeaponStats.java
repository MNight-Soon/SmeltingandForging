package org.mnight.smeltingandforging.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WeaponStats(float baseDamage, float attackSpeed, int maxDurability, float quality) {

    public static final WeaponStats DEFAULT = new WeaponStats(1.0f, 1.0f, 100, 1.0f);

    public static final Codec<WeaponStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("base_damage").forGetter(WeaponStats::baseDamage),
            Codec.FLOAT.fieldOf("attack_speed").forGetter(WeaponStats::attackSpeed),
            Codec.INT.fieldOf("max_durability").forGetter(WeaponStats::maxDurability),
            Codec.FLOAT.fieldOf("quality").forGetter(WeaponStats::quality)
    ).apply(instance, WeaponStats::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponStats> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, WeaponStats::baseDamage,
            ByteBufCodecs.FLOAT, WeaponStats::attackSpeed,
            ByteBufCodecs.INT, WeaponStats::maxDurability,
            ByteBufCodecs.FLOAT, WeaponStats::quality,
            WeaponStats::new
    );
}
