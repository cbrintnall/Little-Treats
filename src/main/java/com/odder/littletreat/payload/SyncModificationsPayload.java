package com.odder.littletreat.payload;

import com.odder.littletreat.LittleTreat;
import com.odder.littletreat.client.ClientState;
import com.odder.littletreat.codec.ActiveModificationDefinition;
import com.odder.littletreat.init.Attachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record SyncModificationsPayload(
        List<ActiveModificationDefinition> modifications
) implements CustomPacketPayload {
    public static final Type<SyncModificationsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LittleTreat.MODID, "sync_mods"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncModificationsPayload> STREAM_CODEC =
            StreamCodec.composite(
                ActiveModificationDefinition.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncModificationsPayload::modifications,
                    SyncModificationsPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void syncToClient(ServerPlayer player) {
        var active = player.getData(Attachments.ACTIVE_MODIFICATIONS);
        PacketDistributor.sendToPlayer(player, new SyncModificationsPayload(List.copyOf(active)));
    }

    public static void handle(SyncModificationsPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientState.INSTANCE.setActive(payload.modifications));
    }
}
