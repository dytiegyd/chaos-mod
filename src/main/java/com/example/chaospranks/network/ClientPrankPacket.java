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
        this.action = action;
        this.prankId = prankId;
        this.duration = duration;
        this.entityId1 = entityId1;
        this.entityId2 = entityId2;
        this.scale = scale;
        this.value = value;
        this.message = message;
    }

    public ClientPrankPacket(FriendlyByteBuf buffer) {
        this.action = buffer.readEnum(ActionType.class);
        this.prankId = buffer.readInt();
        this.duration = buffer.readInt();
        this.entityId1 = buffer.readInt();
        this.entityId2 = buffer.readInt();
        this.scale = buffer.readFloat();
        this.value = buffer.readFloat();
        this.message = buffer.readUtf();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.action);
        buffer.writeInt(this.prankId);
        buffer.writeInt(this.duration);
        buffer.writeInt(this.entityId1);
        buffer.writeInt(this.entityId2);
        buffer.writeFloat(this.scale);
        buffer.writeFloat(this.value);
        buffer.writeUtf(this.message);
    }

    // Добавленные методы, которые искала нейросеть:
    public ActionType getAction() { return this.action; }
    public int getPrankId() { return this.prankId; }
    public int getDurationTicks() { return this.duration; }
    public float getFloatParam1() { return this.scale; }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            com.example.chaospranks.client.ClientPrankEvents.handlePacket(this);
        });
        ctx.get().setPacketHandled(true);
    }
}
