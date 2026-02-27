package org.mnight.smeltingandforging.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mnight.smeltingandforging.Smeltingandforging;
import org.mnight.smeltingandforging.block.entity.SmelteryControllerBlockEntity;

public record SmelteryActionPayload(BlockPos pos, int actionType, int value) implements CustomPacketPayload {
    public static final Type<SmelteryActionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Smeltingandforging.MOD_ID, "smeltery_action"));

    public static final StreamCodec<ByteBuf, SmelteryActionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SmelteryActionPayload::pos,
            ByteBufCodecs.INT, SmelteryActionPayload::actionType,
            ByteBufCodecs.INT, SmelteryActionPayload::value,
            SmelteryActionPayload::new
    );

    @Override
    public Type<? extends SmelteryActionPayload> type() {
        return TYPE;
    }

    public static void handle(final SmelteryActionPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Level level = player.level();

            if (level.getBlockEntity(payload.pos()) instanceof SmelteryControllerBlockEntity blockEntity) {
                if (payload.actionType() == 0){
                    blockEntity.setMode(payload.value());
                } else if (payload.actionType() == 1){
                    blockEntity.setSelectedOutput(payload.value());
                }
            }
        });
    }
}
