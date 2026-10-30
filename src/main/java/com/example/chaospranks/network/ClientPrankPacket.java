package com.example.chaospranks.network;

import com.example.chaospranks.client.ClientPrankEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientPrankPacket {
    public enum ActionType {
        START,
        STOP,
        ROULETTE_TICK,
        TRIGGER_ONE_SHOT
    }

    private final ActionType action;
    private final int prankId;
    private final int durationTicks;
    private final int primaryPlayerId;
    private final int secondaryPlayerId;
    private final float floatParam1;
    private final float floatParam2;
    private final String customMessage;

    public ClientPrankPacket(ActionType action, int prankId, int durationTicks, 
                             int primaryPlayerId, int secondaryPlayerId, 
                             float floatParam1, float floatParam2, String customMessage) {
        this.action = action;
        this.prankId = prankId;
        this.durationTicks = durationTicks;
        this.primaryPlayerId = primaryPlayerId;
        this.secondaryPlayerId = secondaryPlayerId;
        this.floatParam1 = floatParam1;
        this.floatParam2 = floatParam2;
        this.customMessage = customMessage != null ? customMessage : "";
    }

    public static void encode(ClientPrankPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeVarInt(msg.prankId);
        buf.writeVarInt(msg.durationTicks);
        buf.writeVarInt(msg.primaryPlayerId);
        buf.writeVarInt(msg.secondaryPlayerId);
        buf.writeFloat(msg.floatParam1);
        buf.writeFloat(msg.floatParam2);
        buf.writeUtf(msg.customMessage);
    }

    public static ClientPrankPacket decode(FriendlyByteBuf buf) {
        ActionType action = buf.readEnum(ActionType.class);
        int prankId = buf.readVarInt();
        int durationTicks = buf.readVarInt();
        int primaryPlayerId = buf.readVarInt();
        int secondaryPlayerId = buf.readVarInt();
        float floatParam1 = buf.readFloat();
        float floatParam2 = buf.readFloat();
        String customMessage = buf.readUtf();

        return new ClientPrankPacket(action, prankId, durationTicks, primaryPlayerId, secondaryPlayerId, floatParam1, floatParam2, customMessage);
    }

    public static void handle(ClientPrankPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientPrankEvents.getInstance().handlePacket(msg);
            });
        });
        ctx.setPacketHandled(true);
    }

    public ActionType getAction() { return action; }
    public int getPrankId() { return prankId; }
    public int getDurationTicks() { return durationTicks; }
    public int getPrimaryPlayerId() { return primaryPlayerId; }
    public int getSecondaryPlayerId() { return secondaryPlayerId; }
    public float getFloatParam1() { return floatParam1; }
    public float getFloatParam2() { return floatParam2; }
    public String getCustomMessage() { return customMessage; }
}