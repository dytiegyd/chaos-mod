package com.example.chaospranks;

import com.example.chaospranks.client.ClientPrankEvents;
import com.example.chaospranks.network.PacketHandler;
import com.example.chaospranks.server.PrankManager;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ChaosPranksMod.MODID)
public class ChaosPranksMod {
    public static final String MODID = "chaospranks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ChaosPranksMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Регистрация фазы общей инициализации
        modEventBus.addListener(this::commonSetup);

        // Регистрация событий Forge
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(PrankManager.getInstance());

        // Регистрация клиентских слушателей только на стороне клиента
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.register(ClientPrankEvents.getInstance());
        });

        LOGGER.info("[ChaosPranks] Mod constructor initialized.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.registerPackets();
            LOGGER.info("[ChaosPranks] Network packets registered successfully.");
        });
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        PrankManager.getInstance().registerCommands(event.getDispatcher());
        LOGGER.info("[ChaosPranks] Commands /prank registered into CommandDispatcher.");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            PrankManager.getInstance().tick();
        }
    }
}