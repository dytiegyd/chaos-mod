package com.example.chaospranks.server;

import com.example.chaospranks.ChaosPranksMod;
import com.example.chaospranks.network.ClientPrankPacket;
import com.example.chaospranks.network.PacketHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

public class PrankManager {
    private static final PrankManager INSTANCE = new PrankManager();

    public static PrankManager getInstance() {
        return INSTANCE;
    }

    // Параметры таймера (по умолчанию 90 сек = 1800 тиков)
    private int intervalTicks = 1800;
    private int currentTimer = 1800;
    private boolean isPaused = false;
    private MinecraftServer server;

    // Звуковая рулетка (3 секунды = 60 тиков интриги)
    private static final int ROULETTE_TICKS = 60;
    private boolean rouletteActive = false;
    private int rouletteRemaining = 0;
    private final Random random = new Random();

    // Очередь задач отката (Rollback Scheduled Tasks)
    private final List<ScheduledTask> scheduledTasks = new ArrayList<>();

    public static class ScheduledTask {
        private int ticksRemaining;
        private final Runnable task;
        private final String taskName;

        public ScheduledTask(String taskName, int delayTicks, Runnable task) {
            taskName = taskName;
            ticksRemaining = delayTicks;
            task = task;
        }

        public boolean tick() {
            ticksRemaining--;
            if (ticksRemaining <= 0) {
                try {
                    task.run();
                } catch (Exception e) {
                    ChaosPranksMod.LOGGER.error("[ChaosPranks] Error executing scheduled rollback task: " + taskName, e);
                }
                return true;
            }
            return false;
        }
    }

    private PrankManager() {}

    public void setServer(MinecraftServer server) {
        server = server;
    }

    public void tick() {
        if (server == null) {
            server = ServerLifecycleHooks.getCurrentServer();
        }

        // 1. Обновление очереди задач отката
        if (!scheduledTasks.isEmpty()) {
            Iterator<ScheduledTask> iterator = scheduledTasks.iterator();
            while (iterator.hasNext()) {
                ScheduledTask task = iterator.next();
                if (task.tick()) {
                    iterator.remove();
                }
            }
        }

        if (isPaused) {
            return;
        }

        // 2. Обработка звуковой рулетки (3 секунды до срабатывания)
        if (rouletteActive) {
            rouletteRemaining--;
            if (rouletteRemaining % 8 == 0 || (rouletteRemaining < 20 && rouletteRemaining % 3 == 0)) {
                broadcastRouletteTick(rouletteRemaining);
            }

            if (rouletteRemaining <= 0) {
                rouletteActive = false;
                triggerRandomPrank();
                currentTimer = intervalTicks;
            }
            return;
        }

        // 3. Главный обратный отсчет
        currentTimer--;

        // За 60 тиков запускаем звуковую интригу
        if (currentTimer <= ROULETTE_TICKS) {
            startRouletteCountdown();
        }
    }

    private void startRouletteCountdown() {
        rouletteActive = true;
        rouletteRemaining = ROULETTE_TICKS;
        broadcastMessage("§c§l[ChaosPranks] §eВНИМАНИЕ! Звуковая рулетка запущена! До пранка: 3 сек...");
        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.ROULETTE_TICK,
                0,
                ROULETTE_TICKS,
                0, 0, 1.0f, 0f, "ROULETTE_START"
        ));
    }

    private void broadcastRouletteTick(int ticksLeft) {
        if (server == null) return;
        float pitch = 1.0f + ((float)(ROULETTE_TICKS - ticksLeft) / (float)ROULETTE_TICKS) * 0.9f;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.get(), SoundSource.PLAYERS, 1.3f, pitch);
        }

        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.ROULETTE_TICK,
                0,
                ticksLeft,
                0, 0, pitch, 0f, ""
        ));
    }

    public void triggerRandomPrank() {
        int prankId = 1 + random.nextInt(100);
        triggerPrankById(prankId);
    }

    public void triggerPrankById(int prankId) {
        if (server == null) {
            server = ServerLifecycleHooks.getCurrentServer();
        }
        if (server == null) {
            ChaosPranksMod.LOGGER.warn("[ChaosPranks] Cannot trigger prank: server is null.");
            return;
        }

        List<ServerPlayer> players = getOnlinePlayers();
        if (players.isEmpty()) {
            ChaosPranksMod.LOGGER.info("[ChaosPranks] No players online to prank.");
            return;
        }

        broadcastMessage("§6§l[ChaosPranks] §cСработал ПРАНК #" + prankId + "! Хаос начался!");

        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.9f, 1.2f);
        }

        dispatchPrankExecution(prankId, players);
    }

    private void dispatchPrankExecution(int prankId, List<ServerPlayer> players) {
        int primaryId = players.get(0).getId();
        int secondaryId = players.size() > 1 ? players.get(1).getId() : primaryId;

        // Отправка универсального сетевого пакета всем клиентам
        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                prankId,
                500, // 25 секунд длительность
                primaryId,
                secondaryId,
                1.0f,
                0f,
                "PRANK_TRIGGERED"
        ));

        ChaosPranksMod.LOGGER.info("[ChaosPranks] Triggered prank #" + prankId + " for " + players.size() + " players.");
    }

    public void scheduleTask(String taskName, int delayTicks, Runnable runnable) {
        scheduledTasks.add(new ScheduledTask(taskName, delayTicks, runnable));
    }

    public void scheduleBlockRollback(ServerLevel level, BlockPos pos, BlockState originalState, int delayTicks) {
        scheduleTask("BlockRollback_" + pos.toShortString(), delayTicks, () -> {
            level.setBlock(pos, originalState, 3);
        });
    }

    public List<ServerPlayer> getOnlinePlayers() {
        if (server != null) {
            return server.getPlayerList().getPlayers();
        }
        return Collections.emptyList();
    }

    public void broadcastMessage(String text) {
        if (server != null) {
            server.getPlayerList().broadcastSystemMessage(Component.literal(text), false);
        }
    }

    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("prank")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("start")
                        .executes(ctx -> {
                            isPaused = false;
                            ctx.getSource().sendSuccess(() -> Component.literal("§a[ChaosPranks] Таймер пранков запущен! Интервал: " + (intervalTicks / 20) + " сек."), true);
                            return 1;
                        }))
                .then(Commands.literal("pause")
                        .executes(ctx -> {
                            isPaused = true;
                            ctx.getSource().sendSuccess(() -> Component.literal("§e[ChaosPranks] Таймер пранков приостановлен."), true);
                            return 1;
                        }))
                .then(Commands.literal("trigger")
                        .then(Commands.argument("id", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> {
                                    int id = IntegerArgumentType.getInteger(ctx, "id");
                                    server = ctx.getSource().getServer();
                                    triggerPrankById(id);
                                    ctx.getSource().sendSuccess(() -> Component.literal("§6[ChaosPranks] Принудительно запущен пранк #" + id), true);
                                    return 1;
                                })))
                .then(Commands.literal("timer")
                        .then(Commands.argument("seconds", IntegerArgumentType.integer(5, 7200))
                                .executes(ctx -> {
                                    int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
                                    intervalTicks = seconds * 20;
                                    currentTimer = intervalTicks;
                                    ctx.getSource().sendSuccess(() -> Component.literal("§a[ChaosPranks] Новый интервал установлен: " + seconds + " секунд."), true);
                                    return 1;
                                })))
        );
    }

    public int getRemainingSeconds() { return currentTimer / 20; }
    public boolean isPaused() { return isPaused; }
    public int getIntervalSeconds() { return intervalTicks / 20; }
    public boolean isRouletteActive() { return rouletteActive; }
}