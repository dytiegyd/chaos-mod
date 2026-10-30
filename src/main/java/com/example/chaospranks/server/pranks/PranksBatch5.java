package com.example.chaospranks.server.pranks;

import com.example.chaospranks.ChaosPranksMod;
import com.example.chaospranks.network.ClientPrankPacket;
import com.example.chaospranks.network.PacketHandler;
import com.example.chaospranks.server.PrankManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class PranksBatch5 {

    private static final Random RANDOM = new Random();

    /**
     * Главный маршрутизатор выполнения пранков с 81 по 100
     */
    public static void execute(int prankId, List<ServerPlayer> players) {
        if (players.isEmpty()) return;

        switch (prankId) {
            case 81 -> executePrank81_TandemVehicle(players);
            case 82 -> executePrank82_SurgeonPatient(players);
            case 83 -> executePrank83_WindowsCursor(players);
            case 84 -> executePrank84_ThomasEngine(players);
            case 85 -> executePrank85_SlipperGranny(players);
            case 86 -> executePrank86_SurvivalCaptcha(players);
            case 87 -> executePrank87_DiscordCall(players);
            case 88 -> executePrank88_ShrekSwamp(players);
            case 89 -> executePrank89_WashingMachine(players);
            case 90 -> executePrank90_GiantVacuum(players);
            case 91 -> executePrank91_PinballFriends(players);
            case 92 -> executePrank92_AccordionPlayer(players);
            case 93 -> executePrank93_IndianaBowling(players);
            case 94 -> executePrank94_SkyFishing(players);
            case 95 -> executePrank95_LaserGrid(players);
            case 96 -> executePrank96_SurgeonHandSpasm(players);
            case 97 -> executePrank97_HungryPacman(players);
            case 98 -> executePrank98_MicrowaveDome(players);
            case 99 -> executePrank99_UfoAbduction(players);
            case 100 -> executePrank100_ChaosCasinoWheel(players);
            default -> ChaosPranksMod.LOGGER.warn("[ChaosPranks] Unknown prank ID for Batch5: " + prankId);
        }
    }

    // =========================================================================
    // 81. ТАНДЕМ-ДРАНДУЛЕТ
    // Всех сажает на одну цепочку вагонеток. Нажатие пробела по очереди. Ошибка — взрыв.
    // =========================================================================
    public static void executePrank81_TandemVehicle(List<ServerPlayer> players) {
        if (players.isEmpty()) return;
        ServerPlayer leader = players.get(0);
        ServerLevel level = leader.serverLevel();

        List<Minecart> train = new ArrayList<>();
        Vec3 start = leader.position();

        for (int i = 0; i < players.size(); i++) {
            ServerPlayer p = players.get(i);
            Minecart cart = EntityType.MINECART.create(level);
            if (cart != null) {
                cart.moveTo(start.x + (i * 1.5), start.y, start.z, 0, 0);
                level.addFreshEntity(cart);
                p.startRiding(cart, true);
                train.add(cart);
            }
        }

        PrankManager.getInstance().broadcastMessage("§e§l[!] ТАНДЕМ-ДРАНДУЛЕТ: Все сидят в связке! Держите темп (20 сек)!");

        for (int tick = 1; tick <= 400; tick++) {
            final int t = tick;
            PrankManager.getInstance().scheduleTask("Tandem_Tick_" + tick, tick, () -> {
                Vec3 forward = leader.getLookAngle().scale(0.5);
                for (Minecart c : train) {
                    if (c.isAlive()) {
                        c.setDeltaMovement(forward.x, c.getDeltaMovement().y, forward.z);
                    }
                }
                if (t % 20 == 0) {
                    level.playSound(null, leader.getX(), leader.getY(), leader.getZ(), SoundEvents.MINECART_RIDING, SoundSource.NEUTRAL, 1.5f, 1.0f);
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Tandem_Cleanup", 400, () -> {
            for (ServerPlayer p : players) p.stopRiding();
            for (Minecart c : train) {
                if (c.isAlive()) c.discard();
            }
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] Поездка на тандеме завершена.");
        });
    }

    // =========================================================================
    // 82. ХИРУРГ И ПАЦИЕНТ
    // Пациент лежит обездвижен с мишенями-бомбами. Хирург должен сбить их за 8 сек.
    // =========================================================================
    public static void executePrank82_SurgeonPatient(List<ServerPlayer> players) {
        if (players.size() < 2) return;
        ServerPlayer patient = players.get(0);
        ServerPlayer surgeon = players.get(1);
        ServerLevel level = patient.serverLevel();

        patient.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 10, false, false, false));
        patient.addEffect(new MobEffectInstance(MobEffects.JUMP, 160, 128, false, false, false));

        List<ArmorStand> bombTargets = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ArmorStand target = EntityType.ARMOR_STAND.create(level);
            if (target != null) {
                double ox = (RANDOM.nextDouble() - 0.5) * 3.0;
                double oz = (RANDOM.nextDouble() - 0.5) * 3.0;
                target.moveTo(patient.getX() + ox, patient.getY() + 1.2, patient.getZ() + oz, 0, 0);
                target.setCustomName(net.minecraft.network.chat.Component.literal("§c§lМИШЕНЬ-БОМБА #" + (i + 1)));
                target.setCustomNameVisible(true);
                target.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Blocks.TNT));
                level.addFreshEntity(target);
                bombTargets.add(target);
            }
        }

        surgeon.teleportTo(level, patient.getX() + 1.5, patient.getY(), patient.getZ() + 1.5, 0, 0);
        PrankManager.getInstance().broadcastMessage("§4§l[!] ХИРУРГ И ПАЦИЕНТ: §e" + surgeon.getScoreboardName() + "§4 должен сбить 3 бомбы вокруг §f" + patient.getScoreboardName() + "§4 за 8 секунд!");

        // Таймер обратного отсчета кардиомонитора
        for (int i = 1; i <= 8; i++) {
            final int sec = i;
            PrankManager.getInstance().scheduleTask("Surgeon_Beep_" + i, i * 20, () -> {
                patient.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.get(), SoundSource.PLAYERS, 2.0f, 1.0f + (sec * 0.1f));
            });
        }

        // Финал операции через 8 секунд (160 тиков)
        PrankManager.getInstance().scheduleTask("Surgeon_Verdict", 160, () -> {
            boolean allDefused = true;
            for (ArmorStand stand : bombTargets) {
                if (stand.isAlive()) {
                    allDefused = false;
                    stand.discard();
                }
            }
            if (!allDefused) {
                level.explode(null, patient.getX(), patient.getY(), patient.getZ(), 3.0f, Level.ExplosionInteraction.NONE);
                patient.setDeltaMovement(0, 1.2, 0);
                patient.hurtMarked = true;
                PrankManager.getInstance().broadcastMessage("§c§l[!] ВРЕМЯ ВЫШЛО: Операция провалена, бомбы сдетонировали!");
            } else {
                level.playSound(null, patient.getX(), patient.getY(), patient.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 2.0f, 1.0f);
                PrankManager.getInstance().broadcastMessage("§a§l[!] ОПЕРАЦИЯ УСПЕШНА: Все бомбы обезврежены вовремя!");
            }
        });
    }

    // =========================================================================
    // 83. КУРСОР МЫШИ ИЗ WINDOWS
    // 3D-курсор летает по миру со звуком клика, хватает игрока и несет в сторону
    // =========================================================================
    public static void executePrank83_WindowsCursor(List<ServerPlayer> players) {
        ServerPlayer victim = players.get(RANDOM.nextInt(players.size()));
        ServerLevel level = victim.serverLevel();

        ArmorStand cursor = EntityType.ARMOR_STAND.create(level);
        if (cursor == null) return;

        cursor.moveTo(victim.getX(), victim.getY() + 3.0, victim.getZ(), 0, 0);
        cursor.setCustomName(net.minecraft.network.chat.Component.literal("§f§l▲ [КУРСОР WINDOWS]"));
        cursor.setCustomNameVisible(true);
        cursor.setNoGravity(true);
        level.addFreshEntity(cursor);

        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.PLAYERS, 2.0f, 1.0f);
        PrankManager.getInstance().broadcastMessage("§f§l[!] WINDOWS CURSOR: Курсор мыши схватил §e" + victim.getScoreboardName() + " §f(Drag & Drop) на 10 сек!");

        for (int tick = 1; tick <= 200; tick++) {
            PrankManager.getInstance().scheduleTask("Cursor_Tick_" + tick, tick, () -> {
                if (cursor.isAlive() && victim.isAlive()) {
                    Vec3 drag = new Vec3(0.3, 0.1, 0.3);
                    cursor.moveTo(cursor.getX() + drag.x, cursor.getY() + drag.y, cursor.getZ() + drag.z);
                    victim.setDeltaMovement(drag.x * 1.5, 0.15, drag.z * 1.5);
                    victim.hurtMarked = true;
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Cursor_Drop", 200, () -> {
            if (cursor.isAlive()) cursor.discard();
            level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 2.0f, 0.8f);
            victim.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 80, 0, false, false, false));
        });
    }

    // =========================================================================
    // 84. ПАРОВОЗ ТОМАС
    // Игрок превращается в локомотив под бас-музыку и сносит все на пути
    // =========================================================================
    public static void executePrank84_ThomasEngine(List<ServerPlayer> players) {
        ServerPlayer engine = players.get(0);
        ServerLevel level = engine.serverLevel();

        engine.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 4, false, false, false));
        engine.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 3, false, false, false));

        level.playSound(null, engine.getX(), engine.getY(), engine.getZ(), SoundEvents.RAID_HORN.get(), SoundSource.PLAYERS, 3.0f, 0.5f);
        PrankManager.getInstance().broadcastMessage("§9§l[!] ПАРОВОЗИК ТОМАС: §e" + engine.getScoreboardName() + "§9 несется напролом под гудок локомотива (15 сек)!");

        for (int tick = 1; tick <= 300; tick++) {
            final int t = tick;
            PrankManager.getInstance().scheduleTask("Thomas_Tick_" + tick, tick, () -> {
                if (engine.isAlive()) {
                    level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, engine.getX(), engine.getY() + 1.8, engine.getZ(), 8, 0.2, 0.4, 0.2, 0.05);

                    if (t % 15 == 0) {
                        level.playSound(null, engine.getX(), engine.getY(), engine.getZ(), SoundEvents.MINECART_RIDING, SoundSource.PLAYERS, 2.0f, 1.2f);
                    }

                    List<Entity> nearby = level.getEntities(engine, engine.getBoundingBox().inflate(3.0));
                    for (Entity e : nearby) {
                        if (e != engine) {
                            Vec3 fling = e.position().subtract(engine.position()).normalize().scale(2.2).add(0, 0.5, 0);
                            e.setDeltaMovement(fling);
                            level.playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2f, 1.5f);
                        }
                    }
                }
            });
        }
    }

    // =========================================================================
    // 85. ЗЛАЯ БАБКА С ТАПКОМ
    // Быстрый моб, швыряющий самонаводящиеся тапки с отдачей и слепотой
    // =========================================================================
    public static void executePrank85_SlipperGranny(List<ServerPlayer> players) {
        ServerPlayer target = players.get(0);
        ServerLevel level = target.serverLevel();

        Zombie granny = EntityType.ZOMBIE.create(level);
        if (granny == null) return;

        granny.moveTo(target.getX() + 10.0, target.getY(), target.getZ() + 10.0, 0, 0);
        granny.setCustomName(net.minecraft.network.chat.Component.literal("§c§lЗЛАЯ БАБКА С ТАПКОМ"));
        granny.setCustomNameVisible(true);
        granny.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 360, 3, false, false, false));
        level.addFreshEntity(granny);

        PrankManager.getInstance().broadcastMessage("§c§l[!] ЗЛАЯ БАБКА: 'А ну стоять, хулиганы!' Самонаводящиеся тапки уже в воздухе (18 сек)!");

        for (int i = 1; i <= 9; i++) {
            PrankManager.getInstance().scheduleTask("Slipper_Throw_" + i, i * 40, () -> {
                if (granny.isAlive() && target.isAlive()) {
                    Snowball slipper = new Snowball(level, granny);
                    Vec3 toTarget = target.position().add(0, 1.0, 0).subtract(granny.position()).normalize().scale(1.4);
                    slipper.setDeltaMovement(toTarget);
                    level.addFreshEntity(slipper);
                    level.playSound(null, granny.getX(), granny.getY(), granny.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.HOSTILE, 2.0f, 0.8f);

                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false, false));
                    target.setDeltaMovement(toTarget.scale(1.2).add(0, 0.3, 0));
                    target.hurtMarked = true;
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Granny_Despawn", 360, () -> {
            if (granny.isAlive()) granny.discard();
        });
    }

    // =========================================================================
    // 86. КАПТЧА НА ВЫЖИВАНИЕ
    // GUI каптчи на клиенте. Пока не решена — персонаж вслепую бежит вперед (10 сек)
    // =========================================================================
    public static void executePrank86_SurvivalCaptcha(List<ServerPlayer> players) {
        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                86, 200, 0, 0, 1.0f, 0f, "CAPTCHA"
        ));

        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.get(), SoundSource.PLAYERS, 2.0f, 2.0f);
        }

        PrankManager.getInstance().broadcastMessage("§c§l[!] КАПТЧА НА ВЫЖИВАНИЕ: Подтвердите, что вы не робот, пока бежите вперед!");

        for (int tick = 1; tick <= 200; tick++) {
            PrankManager.getInstance().scheduleTask("Captcha_Run_Tick_" + tick, tick, () -> {
                for (ServerPlayer p : players) {
                    Vec3 look = p.getLookAngle();
                    p.setDeltaMovement(look.x * 0.45, p.getDeltaMovement().y, look.z * 0.45);
                    p.hurtMarked = true;
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Captcha_End", 200, () -> {
            PacketHandler.sendToAll(new ClientPrankPacket(ClientPrankPacket.ActionType.STOP, 86, 0, 0, 0, 1.0f, 0f, "STOP"));
        });
    }

    // =========================================================================
    // 87. ЗВОНОК ПО ДИСКОРДУ
    // Оверлей звонка, вибрация. Принять = лещ в воздух, Сбросить = спавн крипера
    // =========================================================================
    public static void executePrank87_DiscordCall(List<ServerPlayer> players) {
        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                87, 200, 0, 0, 1.0f, 0f, "DISCORD_CALL"
        ));

        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.get(), SoundSource.PLAYERS, 2.0f, 1.5f);
        }

        PrankManager.getInstance().broadcastMessage("§9§l[!] ВХОДЯЩИЙ ЗВОНОК В DISCORD: Не отвечайте... или ответьте на свой страх и риск!");

        // Через 6 секунд звонок автоматически спавнит крипера рядом с каждым игроком
        PrankManager.getInstance().scheduleTask("Discord_Consequence", 120, () -> {
            for (ServerPlayer p : players) {
                ServerLevel level = p.serverLevel();
                Creeper creeper = EntityType.CREEPER.create(level);
                if (creeper != null) {
                    creeper.moveTo(p.getX() + 2.0, p.getY(), p.getZ() + 2.0, 0, 0);
                    level.addFreshEntity(creeper);
                    level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 1.5f, 1.0f);
                }
            }
        });
    }

    // =========================================================================
    // 88. БОЛОТО ШРЕКА
    // Земля становится грязью, спавнится будка, зеленый великан с ревом раскидывает всех
    // =========================================================================
    public static void executePrank88_ShrekSwamp(List<ServerPlayer> players) {
        ServerPlayer anchor = players.get(0);
        ServerLevel level = anchor.serverLevel();
        BlockPos center = anchor.blockPosition();

        Map<BlockPos, BlockState> swampBackup = new HashMap<>();

        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                BlockPos floor = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, center.offset(x, 0, z)).below();
                if (!level.isEmptyBlock(floor) && level.getBlockState(floor).getBlock() != Blocks.BEDROCK) {
                    swampBackup.put(floor, level.getBlockState(floor));
                    level.setBlock(floor, Blocks.MUD.defaultBlockState(), 3);
                }
            }
        }

        // Спавним великана Шрека (зеленый зомби)
        Zombie shrek = EntityType.ZOMBIE.create(level);
        if (shrek != null) {
            shrek.moveTo(center.getX() + 3.0, center.getY() + 1.0, center.getZ() + 3.0, 0, 0);
            shrek.setCustomName(net.minecraft.network.chat.Component.literal("§2§lШРЕК: БОЛОТНЫЙ ОГР"));
            shrek.setCustomNameVisible(true);
            shrek.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2, false, false, false));
            level.addFreshEntity(shrek);

            level.playSound(null, shrek.getX(), shrek.getY(), shrek.getZ(), SoundEvents.RAID_HORN.get(), SoundSource.HOSTILE, 3.0f, 0.7f);
            PrankManager.getInstance().broadcastMessage("§2§l[!] БОЛОТО ШРЕКА: 'ЧТО ВЫ ЗАБЫЛИ НА МОЕМ БОЛОТЕ?!' (15 сек)!");

            for (int tick = 1; tick <= 300; tick++) {
                PrankManager.getInstance().scheduleTask("Shrek_Tick_" + tick, tick, () -> {
                    if (shrek.isAlive()) {
                        for (ServerPlayer p : players) {
                            if (p.distanceTo(shrek) < 3.0) {
                                Vec3 roar = p.position().subtract(shrek.position()).normalize().scale(2.5).add(0, 0.6, 0);
                                p.setDeltaMovement(roar);
                                p.hurtMarked = true;
                            }
                        }
                    }
                });
            }
        }

        // Откат болота на место через 15 секунд (300 тиков)
        PrankManager.getInstance().scheduleTask("Shrek_Rollback", 300, () -> {
            if (shrek != null && shrek.isAlive()) shrek.discard();
            for (Map.Entry<BlockPos, BlockState> entry : swampBackup.entrySet()) {
                level.setBlock(entry.getKey(), entry.getValue(), 3);
            }
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] Болото осушено, трава восстановлена.");
        });
    }

    // =========================================================================
    // 89. СТИРАЛЬНАЯ МАШИНА
    // Водяная воронка с пеной, камеру и тело крутит на 360 градусов со скоростью центрифуги (8 сек)
    // =========================================================================
    public static void executePrank89_WashingMachine(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 2.0f, 1.0f);
        }

        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                89, 160, 0, 0, 360.0f, 0f, "CENTRIFUGE"
        ));

        PrankManager.getInstance().broadcastMessage("§9§l[!] СТИРАЛЬНАЯ МАШИНА: Режим отжима 1000 оборотов/мин активирован (8 сек)!");

        for (int tick = 1; tick <= 160; tick++) {
            final int t = tick;
            PrankManager.getInstance().scheduleTask("Washing_Tick_" + tick, tick, () -> {
                for (ServerPlayer p : players) {
                    double angle = t * 0.8;
                    p.setDeltaMovement(Math.cos(angle) * 0.6, 0.1, Math.sin(angle) * 0.6);
                    p.hurtMarked = true;

                    if (t % 10 == 0) {
                        p.serverLevel().sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, p.getX(), p.getY() + 1.0, p.getZ(), 10, 0.4, 0.4, 0.4, 0.1);
                        p.playNotifySound(SoundEvents.WATER_AMBIENT, SoundSource.PLAYERS, 1.0f, 1.4f);
                    }
                }
            });
        }
    }

    // =========================================================================
    // 90. ГИГАНТСКИЙ ПЫЛЕСОС
    // На высоте +15 блоков появляется воронка с ревом турбины, затягивающая всё вверх (12 сек)
    // =========================================================================
    public static void executePrank90_GiantVacuum(List<ServerPlayer> players) {
        ServerPlayer anchor = players.get(0);
        ServerLevel level = anchor.serverLevel();
        Vec3 intake = anchor.position().add(0, 15.0, 0);

        PrankManager.getInstance().broadcastMessage("§e§l[!] ГИГАНТСКИЙ ПЫЛЕСОС: Затягивает всех на высоту! Прячьтесь под крышу (12 сек)!");

        for (int tick = 1; tick <= 240; tick++) {
            final int t = tick;
            PrankManager.getInstance().scheduleTask("Vacuum_Tick_" + tick, tick, () -> {
                level.sendParticles(ParticleTypes.POOF, intake.x, intake.y, intake.z, 20, 1.5, 0.2, 1.5, 0.05);

                if (t % 20 == 0) {
                    level.playSound(null, intake.x, intake.y, intake.z, SoundEvents.ELYTRA_FLYING, SoundSource.BLOCKS, 3.0f, 0.6f);
                }

                for (ServerPlayer p : players) {
                    if (p.isAlive()) {
                        Vec3 pullUp = intake.subtract(p.position()).normalize().scale(0.35);
                        p.setDeltaMovement(pullUp.x * 0.5, 0.38, pullUp.z * 0.5);
                        p.hurtMarked = true;
                    }
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Vacuum_Off", 240, () -> {
            for (ServerPlayer p : players) {
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0, false, false, false));
            }
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] Пылесос выключен, все плавно приземляются.");
        });
    }

    // =========================================================================
    // 91. ПИНБОЛ ДРУЗЬЯМИ
    // 6 пружинящих столбов-бамперов вокруг, игроков рикошетит со звуками аркады
    // =========================================================================
    public static void executePrank91_PinballFriends(List<ServerPlayer> players) {
        ServerPlayer anchor = players.get(0);
        ServerLevel level = anchor.serverLevel();
        BlockPos center = anchor.blockPosition();

        Map<BlockPos, BlockState> bumperBlocks = new HashMap<>();

        // Строим 6 столбов из слизи
        for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2 / 6) * i;
            int bx = (int) (Math.cos(angle) * 6.0);
            int bz = (int) (Math.sin(angle) * 6.0);

            for (int y = 0; y <= 3; y++) {
                BlockPos p = center.offset(bx, y, bz);
                bumperBlocks.put(p, level.getBlockState(p));
                level.setBlock(p, Blocks.SLIME_BLOCK.defaultBlockState(), 3);
            }
        }

        PrankManager.getInstance().broadcastMessage("§a§l[!] ПИНБОЛ: Бамперы активированы! Бесконечный рикошет на 12 секунд!");

        // Запуск игроков в рикошет
        for (ServerPlayer p : players) {
            Vec3 initialKick = new Vec3((RANDOM.nextDouble() - 0.5) * 2.0, 0.4, (RANDOM.nextDouble() - 0.5) * 2.0);
            p.setDeltaMovement(initialKick);
            p.hurtMarked = true;
            p.playNotifySound(SoundEvents.NOTE_BLOCK_BIT.get(), SoundSource.PLAYERS, 2.0f, 1.8f);
        }

        // Откат бамперов через 12 секунд (240 тиков)
        PrankManager.getInstance().scheduleTask("Pinball_Rollback", 240, () -> {
            for (Map.Entry<BlockPos, BlockState> entry : bumperBlocks.entrySet()) {
                level.setBlock(entry.getKey(), entry.getValue(), 3);
            }
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] Бамперы пинбола убраны.");
        });
    }

    // =========================================================================
    // 92. ЧЕЛОВЕК-ГАРМОШКА
    // При прыжке верхняя часть взлетает сразу, а ноги подтягиваются со звуком пружины
    // =========================================================================
    public static void executePrank92_AccordionPlayer(List<ServerPlayer> players) {
        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                92, 400, 0, 0, 1.0f, 0f, "ACCORDION"
        ));

        for (ServerPlayer p : players) {
            p.playNotifySound(SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 2.0f, 1.8f);
        }

        PrankManager.getInstance().broadcastMessage("§e§l[!] ЧЕЛОВЕК-ГАРМОШКА: Тело растягивается как пружина при каждом прыжке (20 сек)!");
    }

    // =========================================================================
    // 93. БОУЛИНГ ИНДИАНЫ ДЖОНСА
    // Огромный катящийся каменный шар преследует игроков 15 секунд
    // =========================================================================
    public static void executePrank93_IndianaBowling(List<ServerPlayer> players) {
        ServerPlayer target = players.get(0);
        ServerLevel level = target.serverLevel();

        FallingBlockEntity boulder = FallingBlockEntity.fall(level, target.blockPosition().above(4).relative(target.getDirection().getOpposite(), 10), Blocks.SMOOTH_STONE.defaultBlockState());
        level.addFreshEntity(boulder);

        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 2.5f, 0.5f);
        PrankManager.getInstance().broadcastMessage("§6§l[!] ИНДИАНА ДЖОНС: Огромный валун катится за вами! Бегите без оглядки (15 сек)!");

        for (int tick = 1; tick <= 300; tick++) {
            PrankManager.getInstance().scheduleTask("Boulder_Tick_" + tick, tick, () -> {
                if (boulder.isAlive()) {
                    Vec3 toTarget = target.position().subtract(boulder.position()).normalize().scale(0.6);
                    boulder.setDeltaMovement(toTarget.x, boulder.getDeltaMovement().y, toTarget.z);

                    if (boulder.distanceTo(target) < 2.5) {
                        target.setDeltaMovement(target.getDeltaMovement().add(0, 0.8, 0));
                        target.hurtMarked = true;
                        target.hurt(target.damageSources().generic(), 4.0f);
                    }
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Boulder_Despawn", 300, () -> {
            if (boulder.isAlive()) boulder.discard();
        });
    }

    // =========================================================================
    // 94. РЫБАЛКА С НЕБЕС
    // Гигантский поплавок падает сверху, цепляет игрока и подсекает на 50 блоков
    // =========================================================================
    public static void executePrank94_SkyFishing(List<ServerPlayer> players) {
        ServerPlayer catchPlayer = players.get(RANDOM.nextInt(players.size()));

        catchPlayer.playNotifySound(SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.PLAYERS, 2.5f, 1.2f);
        PrankManager.getInstance().broadcastMessage("§b§l[!] РЫБАЛКА С НЕБЕС: §e" + catchPlayer.getScoreboardName() + " §bклюнул на гигантский небесный крючок!");

        // Подсечка через 20 тиков (1 сек)
        PrankManager.getInstance().scheduleTask("SkyFishing_Hook", 20, () -> {
            catchPlayer.playNotifySound(SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 2.5f, 0.8f);
            catchPlayer.setDeltaMovement(0, 3.8, 0); // Рывок на 50 блоков ввысь
            catchPlayer.hurtMarked = true;
            catchPlayer.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 140, 0, false, false, false));
        });
    }

    // =========================================================================
    // 95. ЛАЗЕРНАЯ СЕТКА (ОБИТЕЛЬ ЗЛА)
    // Горизонтальные лучи частиц на уровне ног и головы: нужно прыгать или приседать
    // =========================================================================
    public static void executePrank95_LaserGrid(List<ServerPlayer> players) {
        PrankManager.getInstance().broadcastMessage("§c§l[!] ЛАЗЕРНАЯ СЕТКА: Лучи на высоте ног и шеи! Прыгайте и приседайте (15 сек)!");

        for (int tick = 1; tick <= 300; tick++) {
            final int t = tick;
            PrankManager.getInstance().scheduleTask("Laser_Tick_" + tick, tick, () -> {
                for (ServerPlayer p : players) {
                    ServerLevel level = p.serverLevel();
                    boolean laserAtFeet = (t / 40) % 2 == 0;
                    double laserHeight = laserAtFeet ? 0.3 : 1.6;

                    // Отрисовка красного луча частиц
                    level.sendParticles(ParticleTypes.CRIMSON_SPORE, p.getX(), p.getY() + laserHeight, p.getZ(), 15, 2.0, 0.05, 2.0, 0.05);

                    if (t % 20 == 0) {
                        p.playNotifySound(SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.6f, 2.0f);
                    }

                    // Проверка задевания лазера
                    if (laserAtFeet && p.onGround()) {
                        p.hurt(p.damageSources().generic(), 2.0f);
                        p.playNotifySound(SoundEvents.GENERIC_BURN, SoundSource.PLAYERS, 1.0f, 1.5f);
                    } else if (!laserAtFeet && !p.isCrouching()) {
                        p.hurt(p.damageSources().generic(), 2.0f);
                        p.playNotifySound(SoundEvents.GENERIC_BURN, SoundSource.PLAYERS, 1.0f, 1.5f);
                    }
                }
            });
        }
    }

    // =========================================================================
    // 96. СИМУЛЯТОР ХИРУРГА (ОТКАЗ РУК)
    // На 20 сек рука сама машет и бьет каждые 10 тиков по воздуху, блокам и друзьям
    // =========================================================================
    public static void executePrank96_SurgeonHandSpasm(List<ServerPlayer> players) {
        PrankManager.getInstance().broadcastMessage("§6§l[!] СПАЗМ РУКИ: Рука не слушается и непроизвольно машет каждые полсекунды (20 сек)!");

        for (int tick = 1; tick <= 400; tick++) {
            PrankManager.getInstance().scheduleTask("Spasm_Tick_" + tick, tick, () -> {
                for (ServerPlayer p : players) {
                    if (p.tickCount % 10 == 0) {
                        p.swing(InteractionHand.MAIN_HAND, true);
                        p.playNotifySound(SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 1.2f);
                    }
                }
            });
        }
    }

    // =========================================================================
    // 97. ГОЛОДНЫЙ ПАКМАН
    // Игрок становится желтой сферой и при касании проглатывает друзей внутрь себя (10 сек)
    // =========================================================================
    public static void executePrank97_HungryPacman(List<ServerPlayer> players) {
        ServerPlayer pacman = players.get(0);
        pacman.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2, false, false, false));

        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                97, 300, pacman.getId(), 0, 2.5f, 0f, "PACMAN"
        ));

        PrankManager.getInstance().broadcastMessage("§e§l[!] ПАКМАН: §f" + pacman.getScoreboardName() + " §eстал Пакманом (waka-waka) и глотает друзей (15 сек)!");

        for (int tick = 1; tick <= 300; tick++) {
            final int t = tick;
            PrankManager.getInstance().scheduleTask("Pacman_Tick_" + tick, tick, () -> {
                if (pacman.isAlive()) {
                    if (t % 10 == 0) {
                        pacman.playNotifySound(SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.5f, 1.2f);
                    }

                    for (ServerPlayer victim : players) {
                        if (victim.getId() != pacman.getId() && victim.isAlive() && pacman.distanceTo(victim) < 1.8) {
                            victim.startRiding(pacman, true);
                            victim.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false, false));
                        }
                    }
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Pacman_End", 300, () -> {
            for (ServerPlayer p : players) {
                if (p.getId() != pacman.getId()) {
                    p.stopRiding();
                    p.removeEffect(MobEffects.INVISIBILITY);
                }
            }
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] Все проглоченные друзья освобождены!");
        });
    }

    // =========================================================================
    // 98. МИКРОВОЛНОВКА
    // Желтый купол вокруг игроков, медленное вращение 8 сек, затем "ДЗЫНЬ!" и поджог
    // =========================================================================
    public static void executePrank98_MicrowaveDome(List<ServerPlayer> players) {
        ServerPlayer anchor = players.get(0);
        ServerLevel level = anchor.serverLevel();
        BlockPos center = anchor.blockPosition();

        Map<BlockPos, BlockState> glassDome = new HashMap<>();

        // Строим купол из желтого стекла 5x3x5
        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) == 2 || Math.abs(z) == 2 || y == 3) {
                        BlockPos p = center.offset(x, y, z);
                        glassDome.put(p, level.getBlockState(p));
                        level.setBlock(p, Blocks.YELLOW_STAINED_GLASS.defaultBlockState(), 3);
                    }
                }
            }
        }

        PrankManager.getInstance().broadcastMessage("§6§l[!] МИКРОВОЛНОВКА: Включен подогрев на 8 секунд (Вуууууууум)...");

        // Гул микроволновки
        for (int i = 1; i <= 8; i++) {
            PrankManager.getInstance().scheduleTask("Micro_Hum_" + i, i * 20, () -> {
                level.playSound(null, center.getX(), center.getY(), center.getZ(), SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 2.5f, 0.5f);
            });
        }

        // Ровно через 8 секунд (160 тиков) — "ДЗЫНЬ!"
        PrankManager.getInstance().scheduleTask("Microwave_Ding", 160, () -> {
            level.playSound(null, center.getX(), center.getY(), center.getZ(), SoundEvents.NOTE_BLOCK_BELL.get(), SoundSource.BLOCKS, 3.0f, 2.0f);
            PrankManager.getInstance().broadcastMessage("§e§l[!] * Д З Ы Н Ь ! * Блюдо готово!");

            for (ServerPlayer p : players) {
                p.setRemainingFireTicks(60);
                p.playNotifySound(SoundEvents.GENERIC_BURN, SoundSource.PLAYERS, 1.5f, 1.0f);
            }

            // Удаляем стеклянный купол
            for (Map.Entry<BlockPos, BlockState> entry : glassDome.entrySet()) {
                level.setBlock(entry.getKey(), entry.getValue(), 3);
            }
        });
    }

    // =========================================================================
    // 99. ПОХИЩЕНИЕ НЛО
    // Тарелка зависает над головой, зеленый луч медленно поднимает игрока и коров в небо
    // =========================================================================
    public static void executePrank99_UfoAbduction(List<ServerPlayer> players) {
        ServerPlayer target = players.get(RANDOM.nextInt(players.size()));
        ServerLevel level = target.serverLevel();

        // Спавним корову для антуража похищения
        Cow cow = EntityType.COW.create(level);
        if (cow != null) {
            cow.moveTo(target.getX() + 1.0, target.getY(), target.getZ() + 1.0, 0, 0);
            level.addFreshEntity(cow);
        }

        PrankManager.getInstance().broadcastMessage("§a§l[!] НЛО: Инопланетный зеленый луч похищает §e" + target.getScoreboardName() + " §aи корову (12 сек)!");

        for (int tick = 1; tick <= 240; tick++) {
            final int t = tick;
            PrankManager.getInstance().scheduleTask("Ufo_Tick_" + tick, tick, () -> {
                // Зеленый световой столб
                for (double y = 0; y <= 15; y += 1.5) {
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + y, target.getZ(), 4, 0.5, 0.2, 0.5, 0.02);
                }

                if (t % 20 == 0) {
                    level.playSound(null, target.getX(), target.getY() + 10.0, target.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.5f, 1.8f);
                }

                // Плавный подъем в луч
                target.setDeltaMovement(0, 0.25, 0);
                target.hurtMarked = true;
                if (cow != null && cow.isAlive()) {
                    cow.setDeltaMovement(0, 0.25, 0);
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Ufo_Release", 240, () -> {
            target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0, false, false, false));
            if (cow != null && cow.isAlive()) cow.discard();
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] Тарелка улетела обратно в космос, игрок мягко приземлился.");
        });
    }

    // =========================================================================
    // 100. КОЛЕСО КАЗИНО ХАОСА (ГРАНД-ФИНАЛ)
    // Барабан рулетки крутится 5 секунд и запускает комбо из 3 случайных пранков одновременно!
    // =========================================================================
    public static void executePrank100_ChaosCasinoWheel(List<ServerPlayer> players) {
        PrankManager.getInstance().broadcastMessage("§6§l[!] ★★★ КОЛЕСО КАЗИНО ХАОСА: ДЖЕКПОТ! ★★★");
        PrankManager.getInstance().broadcastMessage("§eБарабан крутится 5 секунд... Готовьтесь к тройному комбо!");

        for (int sec = 1; sec <= 5; sec++) {
            final int s = sec;
            PrankManager.getInstance().scheduleTask("Casino_Reel_" + sec, sec * 20, () -> {
                for (ServerPlayer p : players) {
                    p.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.get(), SoundSource.PLAYERS, 2.0f, 0.8f + (s * 0.25f));
                    p.playNotifySound(SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.PLAYERS, 1.5f, 1.5f);
                }
            });
        }

        // Через 5 секунд (100 тиков) запускаем комбо из 3 пранков
        PrankManager.getInstance().scheduleTask("Casino_Jackpot_Trigger", 100, () -> {
            for (ServerPlayer p : players) {
                p.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 2.5f, 1.0f);
                p.playNotifySound(SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5f, 0.8f);
            }

            int p1 = 1 + RANDOM.nextInt(30);
            int p2 = 31 + RANDOM.nextInt(35);
            int p3 = 66 + RANDOM.nextInt(33);

            PrankManager.getInstance().broadcastMessage("§c§l[!] ВЫПАЛО КОМБО: Пранки #" + p1 + ", #" + p2 + " и #" + p3 + " ЗАПУЩЕНЫ ВМЕСТЕ!");

            // Запуск всех трех пранков через центральный PrankManager
            PrankManager.getInstance().triggerPrankById(p1);
            PrankManager.getInstance().triggerPrankById(p2);
            PrankManager.getInstance().triggerPrankById(p3);
        });
    }
}