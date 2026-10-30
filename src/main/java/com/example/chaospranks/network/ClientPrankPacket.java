package com.example.chaospranks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class ClientPrankPacket {
    public enum ActionType { START, STOP, TRIGGER_ONE_SHOT, ROULETTE_TICK }

    public final ActionType action;
    public final int prankId;
    public final int duration;
    public final int entityId1;
    public final int entityId2;
    public final float scale;
    public final float value;
    public final String message;

    public ClientPrankPacket(ActionType action, int prankId, int duration, int entityId1, int entityId2, float scale, float value, String message) {
        action = action;
        prankId = prankId;
        duration = duration;
        entityId1 = entityId1;
        entityId2 = entityId2;
        scale = scale;
        value = value;
        message = message;
    }

    public ClientPrankPacket(FriendlyByteBuf buffer) {
        action = buffer.readEnum(ActionType.class);
        prankId = buffer.readInt();
        duration = buffer.readInt();
        entityId1 = buffer.readInt();
        entityId2 = buffer.readInt();
        scale = buffer.readFloat();
        value = buffer.readFloat();
        message = buffer.readUtf();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeEnum(action);
        buffer.writeInt(prankId);
        buffer.writeInt(duration);
        buffer.writeInt(entityId1);
        buffer.writeInt(entityId2);
        buffer.writeFloat(scale);
        buffer.writeFloat(value);
        buffer.writeUtf(message);
    }

    // Добавленные методы, которые искала нейросеть:
    public ActionType getAction() { return action; }
    public int getPrankId() { return prankId; }
    public int getDurationTicks() { return duration; }
    public float getFloatParam1() { return scale; }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            com.example.chaospranks.client.ClientPrankEvents.handlePacket(this);
        });
        ctx.get().setPacketHandled(true);
    }
}
