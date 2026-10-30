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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class PranksBatch2 {

    private static final Random RANDOM = new Random();

    /**
     * Главный маршрутизатор выполнения пранков с 21 по 40
     */
    public static void execute(int prankId, List<ServerPlayer> players) {
        if (players.isEmpty()) return;

        switch (prankId) {
            case 21 -> executePrank21_ExplosiveBats(players);
            case 22 -> executePrank22_PhantomNightmare(players);
            case 23 -> executePrank23_LightAllergy(players);
            case 24 -> executePrank24_LivingBomb(players);
            case 25 -> executePrank25_FakeLavaWipe(players);
            case 26 -> executePrank26_StrokeSimulator(players);
            case 27 -> executePrank27_HordeHallucination(players);
            case 28 -> executePrank28_BlackHole(players);
            case 29 -> executePrank29_OrbitalStrike(players);
            case 30 -> executePrank30_HelicopterBoss(players);
            case 31 -> executePrank31_Apocalypse(players);
            case 32 -> executePrank32_AustralianServer(players);
            case 33 -> executePrank33_LilliputAndGulliver(players);
            case 34 -> executePrank34_BigHeadMode(players);
            case 35 -> executePrank35_DrunkenFov(players);
            case 36 -> executePrank36_PixelateSoap(players);
            case 37 -> executePrank37_HollywoodBlockbuster(players);
            case 38 -> executePrank38_HumanTower(players);
            case 39 -> executePrank39_WeddingWaltz(players);
            case 40 -> executePrank40_FleasAndCarriers(players);
            default -> ChaosPranksMod.LOGGER.warn("[ChaosPranks] Unknown prank ID for Batch2: " + prankId);
        }
    }

    // =========================================================================
    // 21. ВЗРЫВНЫЕ ЛЕТУЧИЕ МЫШИ
    // 10 мышей с наведением на игрока, взрывающихся при контакте (без поломки блоков)
    // =========================================================================
    public static void executePrank21_ExplosiveBats(List<ServerPlayer> players) {
        List<Bat> bats = new ArrayList<>();

        for (ServerPlayer player : players) {
            ServerLevel level = player.serverLevel();
            for (int i = 0; i < 4; i++) {
                Bat bat = EntityType.BAT.create(level);
                if (bat != null) {
                    double ox = (RANDOM.nextDouble() - 0.5) * 8.0;
                    double oz = (RANDOM.nextDouble() - 0.5) * 8.0;
                    bat.moveTo(player.getX() + ox, player.getY() + 3.0, player.getZ() + oz, 0, 0);
                    level.addFreshEntity(bat);
                    bats.add(bat);
                }
            }
        }

        PrankManager.getInstance().broadcastMessage("§c§l[!] ВЗРЫВНЫЕ МЫШИ: Стая камикадзе пикирует на вас (12 сек)!");

        for (int tick = 1; tick <= 240; tick++) {
            PrankManager.getInstance().scheduleTask("ExplosiveBats_Tick_" + tick, tick, () -> {
                Iterator<Bat> it = bats.iterator();
                while (it.hasNext()) {
                    Bat bat = it.next();
                    if (!bat.isAlive()) {
                        it.remove();
                        continue;
                    }

                    // Наведение на ближайшего игрока
                    ServerPlayer closest = (ServerPlayer) bat.level().getNearestPlayer(bat, 25.0);
                    if (closest != null) {
                        Vec3 toPlayer = closest.position().add(0, 1.0, 0).subtract(bat.position()).normalize().scale(0.42);
                        bat.setDeltaMovement(toPlayer);

                        // Проверка дистанции детонации
                        if (bat.distanceTo(closest) < 1.4) {
                            bat.level().explode(bat, bat.getX(), bat.getY(), bat.getZ(), 1.6f, Level.ExplosionInteraction.NONE);
                            bat.discard();
                            it.remove();
                        }
                    }
                }
            });
        }

        // Очистка через 12 секунд
        PrankManager.getInstance().scheduleTask("ExplosiveBats_Cleanup", 240, () -> {
            for (Bat bat : bats) {
                if (bat.isAlive()) {
                    bat.discard();
                }
            }
        });
    }

    // =========================================================================
    // 22. ФАНТОМНЫЙ КОШМАР
    // С неба пикируют 5 горящих Фантомов (увеличенных в 3 раза)
    // =========================================================================
    public static void executePrank22_PhantomNightmare(List<ServerPlayer> players) {
        List<Phantom> phantoms = new ArrayList<>();

        for (ServerPlayer player : players) {
            ServerLevel level = player.serverLevel();
            player.playNotifySound(SoundEvents.PHANTOM_SWOOP, SoundSource.HOSTILE, 2.0f, 0.7f);

            for (int i = 0; i < 3; i++) {
                Phantom phantom = EntityType.PHANTOM.create(level);
                if (phantom != null) {
                    double ox = (RANDOM.nextDouble() - 0.5) * 12.0;
                    double oz = (RANDOM.nextDouble() - 0.5) * 12.0;
                    phantom.moveTo(player.getX() + ox, player.getY() + 18.0, player.getZ() + oz, 0, 0);
                    phantom.setRemainingFireTicks(400); // Горящие
                    phantom.setPhantomSize(3);          // Увеличенные в 3 раза
                    phantom.setTarget(player);
                    level.addFreshEntity(phantom);
                    phantoms.add(phantom);
                }
            }
        }

        PrankManager.getInstance().broadcastMessage("§5§l[!] ФАНТОМНЫЙ КОШМАР: Горящие мега-фантомы пикируют из облаков!");

        // Деспавн через 15 секунд (300 тиков)
        PrankManager.getInstance().scheduleTask("PhantomNightmare_Cleanup", 300, () -> {
            for (Phantom p : phantoms) {
                if (p.isAlive()) {
                    ((net.minecraft.server.level.ServerLevel)p.level()).sendParticles(ParticleTypes.LARGE_SMOKE, p.getX(), p.getY() + 0.5, p.getZ(), 15, 0.5, 0.5, 0.5, 0.05);
                    p.discard();
                }
            }
        });
    }

    // =========================================================================
    // 23. АЛЛЕРГИЯ НА СВЕТ
    // На 25 сек на солнечном свету игроки горят как вампиры
    // =========================================================================
    public static void executePrank23_LightAllergy(List<ServerPlayer> players) {
        PrankManager.getInstance().broadcastMessage("§6§l[!] АЛЛЕРГИЯ НА СВЕТ: Прячьтесь в тень! На солнце вы горите 25 секунд!");

        for (int tick = 1; tick <= 500; tick++) {
            PrankManager.getInstance().scheduleTask("LightAllergy_Tick_" + tick, tick, () -> {
                for (ServerPlayer player : players) {
                    ServerLevel level = player.serverLevel();
                    if (player.isAlive() && level.isDay() && level.canSeeSky(player.blockPosition())) {
                        player.setRemainingFireTicks(40);
                        if (player.tickCount % 20 == 0) {
                            player.playNotifySound(SoundEvents.GENERIC_BURN, SoundSource.PLAYERS, 0.8f, 1.2f);
                            level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 6, 0.3, 0.3, 0.3, 0.05);
                        }
                    }
                }
            });
        }

        // По окончанию 25 секунд тушим всех игроков
        PrankManager.getInstance().scheduleTask("LightAllergy_Extinguish", 501, () -> {
            for (ServerPlayer player : players) {
                player.clearFire();
            }
        });
    }

    // =========================================================================
    // 24. ЖИВАЯ БОМБА
    // Моделька игрока мигает и раздувается 4 сек, затем происходит взрыв вокруг
    // =========================================================================
    public static void executePrank24_LivingBomb(List<ServerPlayer> players) {
        ServerPlayer bomber = players.get(RANDOM.nextInt(players.size()));
        ServerLevel level = bomber.serverLevel();

        PrankManager.getInstance().broadcastMessage("§c§l[!] ЖИВАЯ БОМБА: " + bomber.getScoreboardName() + " раздувается и взорвется через 4 сек!");

        // Клиентский оверлей и пульс
        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                24,
                80,
                bomber.getId(), 0, 1.5f, 0f, "LIVING_BOMB"
        ));

        // Звуки нарастающего тиканья
        for (int i = 1; i <= 4; i++) {
            final float pitch = 0.8f + (i * 0.3f);
            PrankManager.getInstance().scheduleTask("LivingBomb_TickSound_" + i, i * 20, () -> {
                level.playSound(null, bomber.getX(), bomber.getY(), bomber.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 2.0f, pitch);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, bomber.getX(), bomber.getY() + 1.0, bomber.getZ(), 20, 0.4, 0.6, 0.4, 0.1);
            });
        }

        // Детонация ровно на 80-м тике (4 секунды)
        PrankManager.getInstance().scheduleTask("LivingBomb_Explode", 80, () -> {
            level.explode(bomber, bomber.getX(), bomber.getY(), bomber.getZ(), 3.5f, Level.ExplosionInteraction.NONE);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, bomber.getX(), bomber.getY() + 1.0, bomber.getZ(), 2, 0.2, 0.2, 0.2, 0.1);

            // Разбрасываем всех друзей вокруг на 15 блоков
            for (ServerPlayer friend : players) {
                if (friend.getId() != bomber.getId() && friend.distanceTo(bomber) < 12.0) {
                    Vec3 knock = friend.position().subtract(bomber.position()).normalize().scale(2.5).add(0, 0.5, 0);
                    friend.setDeltaMovement(knock);
                    friend.hurtMarked = true;
                    friend.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 80, 0, false, false, false));
                }
            }
        });
    }

    // =========================================================================
    // 25. ФЕЙКОВЫЙ ВАЙП В ЛАВЕ
    // Клиентская иллюзия: пол пропадает, рендерится лава и огонь на экране 3 сек
    // =========================================================================
    public static void executePrank25_FakeLavaWipe(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 1.5f, 0.8f);
            player.playNotifySound(SoundEvents.PLAYER_HURT_ON_FIRE, SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                25,
                60, // 3 секунды иллюзии
                0, 0, 1.0f, 0f, "LAVA_ILLUSION"
        ));

        PrankManager.getInstance().broadcastMessage("§c§l[!] ВАЙП! Вы провалились в лавовое озеро... А, стоп, показалось!");
    }

    // =========================================================================
    // 26. СИМУЛЯТОР ИНСУЛЬТА
    // На 12 сек Тошнота, Слепота, Скорость, Прыгучесть, мигание и тряска
    // =========================================================================
    public static void executePrank26_StrokeSimulator(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 240, 2, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 240, 0, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 4, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 240, 3, false, false, true));

            player.playNotifySound(SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 2.0f, 1.4f);
        }

        PrankManager.getInstance().broadcastMessage("§5§l[!] СИМУЛЯТОР ИНСУЛЬТА: Все плывет, ноги бегут сами, земля уходит из-под ног (12 сек)!");

        // Гарантированное снятие через 12 секунд
        PrankManager.getInstance().scheduleTask("StrokeSimulator_Cleanup", 240, () -> {
            for (ServerPlayer player : players) {
                player.removeEffect(MobEffects.CONFUSION);
                player.removeEffect(MobEffects.BLINDNESS);
                player.removeEffect(MobEffects.MOVEMENT_SPEED);
                player.removeEffect(MobEffects.JUMP);
            }
        });
    }

    // =========================================================================
    // 27. ГАЛЛЮЦИНАЦИЯ ОРДЫ
    // 30 шипящих криперов бегут на игрока: 29 фантомных (без урона) и 1 настоящий
    // =========================================================================
    public static void executePrank27_HordeHallucination(List<ServerPlayer> players) {
        List<Creeper> creepers = new ArrayList<>();

        for (ServerPlayer player : players) {
            ServerLevel level = player.serverLevel();
            player.playNotifySound(SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 2.0f, 1.0f);

            for (int i = 0; i < 15; i++) {
                Creeper creeper = EntityType.CREEPER.create(level);
                if (creeper != null) {
                    double angle = (Math.PI * 2 / 15) * i;
                    double dist = 7.0;
                    creeper.moveTo(player.getX() + Math.cos(angle) * dist, player.getY(), player.getZ() + Math.sin(angle) * dist, 0, 0);
                    creeper.setTarget(player);
                    creeper.ignite();

                    // Первый крипер настоящий, остальные — иллюзорные
                    if (i != 0) {
                        creeper.addTag("PhantomCreeper");
                    }
                    level.addFreshEntity(creeper);
                    creepers.add(creeper);
                }
            }
        }

        PrankManager.getInstance().broadcastMessage("§2§l[!] ОРДА КРИПЕРОВ! 30 криперов окружили вас со всех сторон!");

        // Очистка фантомов через 8 секунд
        PrankManager.getInstance().scheduleTask("HordeHallucination_Cleanup", 160, () -> {
            for (Creeper c : creepers) {
                if (c.isAlive() && c.getTags().contains("PhantomCreeper")) {
                    ((net.minecraft.server.level.ServerLevel)c.level()).sendParticles(ParticleTypes.POOF, c.getX(), c.getY() + 0.5, c.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
                    c.discard();
                }
            }
        });
    }

    // =========================================================================
    // 28. ЧЕРНАЯ ДЫРА
    // Сфера частиц в 10 блоках 6 секунд физически затягивает игроков и мобов, затем бабахает
    // =========================================================================
    public static void executePrank28_BlackHole(List<ServerPlayer> players) {
        ServerPlayer anchor = players.get(RANDOM.nextInt(players.size()));
        ServerLevel level = anchor.serverLevel();
        Vec3 singularity = anchor.position().add(8.0, 3.0, 8.0);

        PrankManager.getInstance().broadcastMessage("§0§l[!] ЧЕРНАЯ ДЫРА: Гравитационная аномалия затягивает всё вокруг (6 сек)!");

        for (int tick = 1; tick <= 120; tick++) {
            PrankManager.getInstance().scheduleTask("BlackHole_Tick_" + tick, tick, () -> {
                // Визуальная сфера черной дыры
                level.sendParticles(ParticleTypes.SQUID_INK, singularity.x, singularity.y, singularity.z, 25, 0.5, 0.5, 0.5, 0.05);
                level.sendParticles(ParticleTypes.PORTAL, singularity.x, singularity.y, singularity.z, 30, 2.0, 2.0, 2.0, 0.1);

                if (tick % 25 == 0) {
                    level.playSound(null, singularity.x, singularity.y, singularity.z, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 2.0f, 0.5f);
                }

                // Затягивание всех сущностей в радиусе 18 блоков
                List<Entity> entities = level.getEntities((Entity) null, anchor.getBoundingBox().inflate(18.0));
                for (Entity e : entities) {
                    Vec3 pull = singularity.subtract(e.position()).normalize().scale(0.38);
                    e.setDeltaMovement(e.getDeltaMovement().add(pull));
                    e.hurtMarked = true;
                }
            });
        }

        // Финальный взрыв на 120-м тике (6 сек)
        PrankManager.getInstance().scheduleTask("BlackHole_Collapse", 120, () -> {
            level.explode(null, singularity.x, singularity.y, singularity.z, 4.0f, Level.ExplosionInteraction.NONE);
            level.playSound(null, singularity.x, singularity.y, singularity.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 3.0f, 0.6f);

            // Разлет игроков в стороны
            for (ServerPlayer player : players) {
                Vec3 push = player.position().subtract(singularity).normalize().scale(2.8).add(0, 0.6, 0);
                player.setDeltaMovement(push);
                player.hurtMarked = true;
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, false, false));
            }
        });
    }

    // =========================================================================
    // 29. ОРБИТАЛЬНЫЙ УДАР
    // Красный круг частиц под ногами, через 3 сек — связка из 5 ТНТ и молнии
    // =========================================================================
    public static void executePrank29_OrbitalStrike(List<ServerPlayer> players) {
        Map<ServerPlayer, Vec3> strikeTargets = new HashMap<>();
        for (ServerPlayer p : players) {
            strikeTargets.put(p, p.position());
            p.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0f, 1.8f);
        }

        PrankManager.getInstance().broadcastMessage("§c§l[!] ОРБИТАЛЬНЫЙ УДАР! Красный прицел зафиксирован на вас! Бегите (3 сек)!");

        // 3 секунды отрисовки красного круга на земле
        for (int tick = 1; tick <= 60; tick++) {
            PrankManager.getInstance().scheduleTask("OrbitalAim_Tick_" + tick, tick, () -> {
                for (Map.Entry<ServerPlayer, Vec3> entry : strikeTargets.entrySet()) {
                    Vec3 pos = entry.getValue();
                    ServerLevel level = entry.getKey().serverLevel();

                    for (int deg = 0; deg < 360; deg += 30) {
                        double rad = Math.toRadians(deg);
                        level.sendParticles(ParticleTypes.FLAME, pos.x + Math.cos(rad) * 3.0, pos.y + 0.1, pos.z + Math.sin(rad) * 3.0, 1, 0, 0, 0, 0);
                    }
                }
            });
        }

        // Удар ровно через 3 секунды (60 тиков)
        PrankManager.getInstance().scheduleTask("OrbitalStrike_Impact", 60, () -> {
            for (Map.Entry<ServerPlayer, Vec3> entry : strikeTargets.entrySet()) {
                Vec3 target = entry.getValue();
                ServerLevel level = entry.getKey().serverLevel();

                // 2 молнии
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                if (lightning != null) {
                    lightning.moveTo(target);
                    lightning.setVisualOnly(true);
                    level.addFreshEntity(lightning);
                }

                // 4 падающих зажженных ТНТ
                for (int i = 0; i < 4; i++) {
                    PrimedTnt tnt = new PrimedTnt(level, target.x + (RANDOM.nextDouble() - 0.5) * 2.0, target.y + 10.0, target.z + (RANDOM.nextDouble() - 0.5) * 2.0, null);
                    tnt.setFuse(20); // 1 сек
                    level.addFreshEntity(tnt);
                }
            }
        });
    }

    // =========================================================================
    // 30. БОСС-ВЕРТОЛЕТ
    // Над игроками зависает Визер под звук лопастей, стреляет черепами 10 сек и исчезает
    // =========================================================================
    public static void executePrank30_HelicopterBoss(List<ServerPlayer> players) {
        ServerPlayer target = players.get(0);
        ServerLevel level = target.serverLevel();

        WitherBoss wither = EntityType.WITHER.create(level);
        if (wither == null) return;

        wither.moveTo(target.getX(), target.getY() + 10.0, target.getZ(), 0, 0);
        wither.setCustomName(net.minecraft.network.chat.Component.literal("§c§lБОСС-ВЕРТОЛЕТ МИ-24"));
        wither.setCustomNameVisible(true);
        level.addFreshEntity(wither);

        PrankManager.getInstance().broadcastMessage("§4§l[!] БОСС-ВЕРТОЛЕТ: В воздухе завис визер и ведет огонь (10 сек)!");

        // Имитация звука вертолетных лопастей
        for (int tick = 1; tick <= 200; tick++) {
            PrankManager.getInstance().scheduleTask("HeliSound_Tick_" + tick, tick, () -> {
                if (wither.isAlive() && tick % 8 == 0) {
                    level.playSound(null, wither.getX(), wither.getY(), wither.getZ(), SoundEvents.MINECART_RIDING, SoundSource.HOSTILE, 2.5f, 1.8f);
                    level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, wither.getX(), wither.getY() + 1.0, wither.getZ(), 8, 0.4, 0.2, 0.4, 0.05);
                }
            });
        }

        // Деспавн через 10 секунд (200 тиков)
        PrankManager.getInstance().scheduleTask("Heli_Despawn", 200, () -> {
            if (wither.isAlive()) {
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, wither.getX(), wither.getY(), wither.getZ(), 3, 1.0, 1.0, 1.0, 0.1);
                wither.discard();
            }
        });
    }

    // =========================================================================
    // 31. АПОКАЛИПСИС
    // Запуск сразу 5 случайных пранков одновременно под звук сирены
    // =========================================================================
    public static void executePrank31_Apocalypse(List<ServerPlayer> players) {
        PrankManager.getInstance().broadcastMessage("§4§l[!] АПОКАЛИПСИС! 5 случайных пранков запускаются ОДНОВРЕМЕННО!");

        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.RAID_HORN.get(), SoundSource.PLAYERS, 2.5f, 0.8f);
        }

        // Запуск комбо
        executePrank1_RookSwap(players);
        executePrank9_Tornado(players);
        executePrank11_AnvilRain(players);
        executePrank26_StrokeSimulator(players);
        executePrank32_AustralianServer(players);
    }

    private static void executePrank1_RookSwap(List<ServerPlayer> p) { PranksBatch1.execute(1, p); }
    private static void executePrank9_Tornado(List<ServerPlayer> p) { PranksBatch1.execute(9, p); }
    private static void executePrank11_AnvilRain(List<ServerPlayer> p) { PranksBatch1.execute(11, p); }

    // =========================================================================
    // 32. АВСТРАЛИЙСКИЙ СЕРВЕР
    // Камера переворачивается на 180 градусов (Roll) на 25 секунд
    // =========================================================================
    public static void executePrank32_AustralianServer(List<ServerPlayer> players) {
        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                32,
                500, // 25 секунд
                0, 0, 180.0f, 0f, "AUSTRALIAN_SERVER"
        ));

        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.BAT_TAKEOFF, SoundSource.PLAYERS, 1.0f, 0.5f);
        }

        PrankManager.getInstance().broadcastMessage("§a§l[!] АВСТРАЛИЙСКИЙ СЕРВЕР: Мир перевернулся вверх дном на 25 секунд!");
    }

    // =========================================================================
    // 33. ЛИЛИПУТЫ И ГУЛЛИВЕР (ОСОБО ДЕТАЛЬНО)
    // Все игроки уменьшаются до 0.2 блока, а один вырастает до 5 блоков и топчет друзей
    // =========================================================================
    public static void executePrank33_LilliputAndGulliver(List<ServerPlayer> players) {
        if (players.isEmpty()) return;

        // Выбираем одного Гулливера
        ServerPlayer gulliver = players.get(RANDOM.nextInt(players.size()));

        PrankManager.getInstance().broadcastMessage("§6§l[!] ЛИЛИПУТЫ И ГУЛЛИВЕР: §e" + gulliver.getScoreboardName() + " вырос до 5 блоков! Остальные — крошечные лилипуты (30 сек)!");

        // Пакет для Гулливера (Scale = 4.5f)
        PacketHandler.sendToPlayer(gulliver, new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                33,
                600, // 30 секунд
                gulliver.getId(), 0, 4.5f, 0f, "GULLIVER"
        ));

        // Пакеты для всех Лилипутов (Scale = 0.2f)
        for (ServerPlayer player : players) {
            if (player.getId() != gulliver.getId()) {
                PacketHandler.sendToPlayer(player, new ClientPrankPacket(
                        ClientPrankPacket.ActionType.START,
                        33,
                        600,
                        player.getId(), 0, 0.2f, 0f, "LILLIPUT"
                ));
            }
        }

        // Гулливеру даем Силу и Защиту
        gulliver.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1, false, false, false));
        gulliver.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 2, false, false, false));

        // Лилипутам даем Скорость, чтобы могли убегать
        for (ServerPlayer player : players) {
            if (player.getId() != gulliver.getId()) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 2, false, false, false));
            }
        }

        // Механика наступания: каждый шаг Гулливера наносит 4 урона лилипутам под ногами
        for (int tick = 1; tick <= 600; tick++) {
            PrankManager.getInstance().scheduleTask("Stomp_Tick_" + tick, tick, () -> {
                if (gulliver.isAlive()) {
                    for (ServerPlayer lilliput : players) {
                        if (lilliput.getId() != gulliver.getId() && lilliput.isAlive() && lilliput.level() == gulliver.level()) {
                            double dist = lilliput.distanceTo(gulliver);
                            if (dist < 2.2) {
                                // Гулливер наступил на лилипута
                                lilliput.hurt(lilliput.damageSources().generic(), 4.0f);
                                Vec3 squash = lilliput.position().subtract(gulliver.position()).normalize().scale(1.2).add(0, 0.3, 0);
                                lilliput.setDeltaMovement(squash);
                                lilliput.hurtMarked = true;

                                gulliver.playNotifySound(SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.5f, 0.5f);
                                lilliput.playNotifySound(SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.5f, 1.5f);
                            }
                        }
                    }
                }
            });
        }

        // Сброс через 30 секунд (600 тиков)
        PrankManager.getInstance().scheduleTask("Lilliput_Reset", 600, () -> {
            PacketHandler.sendToAll(new ClientPrankPacket(
                    ClientPrankPacket.ActionType.STOP,
                    33, 0, 0, 0, 1.0f, 0f, "RESET_SCALE"
            ));
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] Действие пранка «Лилипуты и Гулливер» завершено. Размеры возвращены!");
        });
    }

    // =========================================================================
    // 34. BIG HEAD MODE
    // Головы всех игроков и мобов в рендере увеличиваются в 5 раз на 30 сек
    // =========================================================================
    public static void executePrank34_BigHeadMode(List<ServerPlayer> players) {
        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                34,
                600, // 30 секунд
                0, 0, 5.0f, 0f, "BIG_HEAD"
        ));

        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1.2f, 0.7f);
        }

        PrankManager.getInstance().broadcastMessage("§d§l[!] BIG HEAD MODE: Все головы раздулись в 5 раз на 30 секунд!");
    }

    // =========================================================================
    // 35. ПЬЯНЫЙ FOV
    // FOV камеры пульсирует по синусоиде от 30 до 160 в течение 15 секунд
    // =========================================================================
    public static void executePrank35_DrunkenFov(List<ServerPlayer> players) {
        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                35,
                300, // 15 секунд
                0, 0, 1.0f, 0f, "DRUNKEN_FOV"
        ));

        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.7f, 1.4f);
        }

        PrankManager.getInstance().broadcastMessage("§3§l[!] ПЬЯНЫЙ FOV: Камеру штормит от 30 до 160 градусов FOV (15 сек)!");
    }

    // =========================================================================
    // 36. ПИКСЕЛЬНОЕ МЫЛО (144p)
    // Пост-эффект пикселизации экрана на 15 секунд
    // =========================================================================
    public static void executePrank36_PixelateSoap(List<ServerPlayer> players) {
        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                36,
                300, // 15 секунд
                0, 0, 144.0f, 0f, "PIXELATE_144P"
        ));

        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.ITEM_FRAME_BREAK, SoundSource.PLAYERS, 1.0f, 0.6f);
        }

        PrankManager.getInstance().broadcastMessage("§7§l[!] ПИКСЕЛЬНОЕ МЫЛО: Включено разрешение YouTube 144p на 15 секунд!");
    }

    // =========================================================================
    // 37. ГОЛЛИВУДСКИЙ БЛОКБАСТЕР (ОСОБО ДЕТАЛЬНО)
    // Черные кинополосы (21:9), эпичный басовый "BRAAAM" и вспышки при ударах
    // =========================================================================
    public static void executePrank37_HollywoodBlockbuster(List<ServerPlayer> players) {
        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                37,
                500, // 25 секунд
                0, 0, 21.0f, 9.0f, "HOLLYWOOD_BLOCKBUSTER"
        ));

        // Эпичный звук "BRAAAM" в начале
        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.RAID_HORN.get(), SoundSource.PLAYERS, 3.0f, 0.5f);
            player.playNotifySound(SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.2f, 0.6f);
        }

        PrankManager.getInstance().broadcastMessage("§e§l[!] ГОЛЛИВУДСКИЙ БЛОКБАСТЕР: Кинематографичный режим 21:9 и BRAAAM (25 сек)!");

        // Каждые 100 тиков (5 секунд) повторяем басовый удар BRAAAM
        for (int i = 1; i <= 5; i++) {
            PrankManager.getInstance().scheduleTask("Blockbuster_Braaam_" + i, i * 100, () -> {
                for (ServerPlayer player : players) {
                    player.playNotifySound(SoundEvents.RAID_HORN.get(), SoundSource.PLAYERS, 2.5f, 0.55f);
                    player.serverLevel().sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.2, player.getZ(), 1, 0, 0, 0, 0);
                }
            });
        }
    }

    // =========================================================================
    // 38. ЧЕЛОВЕЧЕСКАЯ БАШЕНКА
    // Всех игроков телепортирует и сажает верхом друг на друга в одну колонну на 20 сек
    // =========================================================================
    public static void executePrank38_HumanTower(List<ServerPlayer> players) {
        if (players.size() < 2) return;

        ServerPlayer basePlayer = players.get(0);
        ServerLevel level = basePlayer.serverLevel();
        Vec3 basePos = basePlayer.position();

        // Сборка колонны: P1 -> P0, P2 -> P1, P3 -> P2 ...
        for (int i = 1; i < players.size(); i++) {
            ServerPlayer rider = players.get(i);
            ServerPlayer vehicle = players.get(i - 1);

            rider.teleportTo(level, basePos.x, basePos.y, basePos.z, vehicle.getYRot(), vehicle.getXRot());
            rider.startRiding(vehicle, true);
        }

        for (ServerPlayer p : players) {
            p.playNotifySound(SoundEvents.HORSE_SADDLE, SoundSource.PLAYERS, 1.5f, 1.2f);
        }

        PrankManager.getInstance().broadcastMessage("§b§l[!] ЧЕЛОВЕЧЕСКАЯ БАШЕНКА: Все игроки оседлали друг друга в одну живую колонну (20 сек)!");

        // Безопасное спешивание через 20 секунд (400 тиков)
        PrankManager.getInstance().scheduleTask("HumanTower_Dismount", 400, () -> {
            for (ServerPlayer p : players) {
                p.stopRiding();
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 80, 0, false, false, false));
            }
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] Человеческая башенка успешно разобрана!");
        });
    }

    // =========================================================================
    // 39. СВАДЕБНЫЙ ВАЛЬС
    // Двух игроков притягивает вплотную, спавнит сердечки и бешено крутит в паре
    // =========================================================================
    public static void executePrank39_WeddingWaltz(List<ServerPlayer> players) {
        if (players.size() < 2) return;

        ServerPlayer p1 = players.get(0);
        ServerPlayer p2 = players.get(1);

        Vec3 center = p1.position().add(p2.position()).scale(0.5);

        PrankManager.getInstance().broadcastMessage("§d§l[!] СВАДЕБНЫЙ ВАЛЬС: §f" + p1.getScoreboardName() + " §dи §f" + p2.getScoreboardName() + " §dкружатся в танце любви!");

        for (int tick = 1; tick <= 200; tick++) {
            final int t = tick;
            PrankManager.getInstance().scheduleTask("Waltz_Tick_" + tick, tick, () -> {
                if (p1.isAlive() && p2.isAlive()) {
                    double angle = t * 0.45;
                    double radius = 1.1;

                    double x1 = center.x + Math.cos(angle) * radius;
                    double z1 = center.z + Math.sin(angle) * radius;

                    double x2 = center.x + Math.cos(angle + Math.PI) * radius;
                    double z2 = center.z + Math.sin(angle + Math.PI) * radius;

                    p1.setDeltaMovement((x1 - p1.getX()) * 0.8, 0.15, (z1 - p1.getZ()) * 0.8);
                    p2.setDeltaMovement((x2 - p2.getX()) * 0.8, 0.15, (z2 - p2.getZ()) * 0.8);
                    p1.hurtMarked = true;
                    p2.hurtMarked = true;

                    if (t % 10 == 0) {
                        p1.serverLevel().sendParticles(ParticleTypes.HEART, center.x, center.y + 1.2, center.z, 5, 0.4, 0.4, 0.4, 0.05);
                        p1.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.get(), SoundSource.PLAYERS, 1.0f, 1.2f);
                    }
                }
            });
        }

        // В конце (на 200-м тике) эффектный разлет в стороны
        PrankManager.getInstance().scheduleTask("Waltz_Finale", 200, () -> {
            Vec3 fling1 = p1.position().subtract(center).normalize().scale(2.5).add(0, 0.4, 0);
            Vec3 fling2 = p2.position().subtract(center).normalize().scale(2.5).add(0, 0.4, 0);

            p1.setDeltaMovement(fling1);
            p2.setDeltaMovement(fling2);
            p1.hurtMarked = true;
            p2.hurtMarked = true;

            p1.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, false, false, false));
            p2.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, false, false, false));
        });
    }

    // =========================================================================
    // 40. БЛОХИ И НОСИТЕЛИ (ОСОБО ДЕТАЛЬНО)
    // Разбиение по парам: Блоха (scale 0.15) на шее Носителя перехватывает управление.
    // Если число игроков нечетное — лишний становится "Дикой блохой" (Скорость 5) и кусает всех.
    // =========================================================================
    public static void executePrank40_FleasAndCarriers(List<ServerPlayer> players) {
        if (players.isEmpty()) return;

        List<ServerPlayer> pool = new ArrayList<>(players);
        Collections.shuffle(pool);

        int pairsCount = pool.size() / 2;
        boolean hasWildFlea = pool.size() % 2 != 0;

        for (int i = 0; i < pairsCount; i++) {
            ServerPlayer carrier = pool.get(i * 2);
            ServerPlayer flea = pool.get(i * 2 + 1);

            // Блоха садится на шею носителя
            flea.teleportTo(carrier.serverLevel(), carrier.getX(), carrier.getY() + 1.8, carrier.getZ(), carrier.getYRot(), carrier.getXRot());
            flea.startRiding(carrier, true);

            // Уменьшаем блоху до 0.15f
            PacketHandler.sendToPlayer(flea, new ClientPrankPacket(
                    ClientPrankPacket.ActionType.START,
                    40,
                    500, // 25 секунд
                    carrier.getId(), flea.getId(), 0.15f, 0f, "FLEA"
            ));

            // Носителя уведомляем о перехвате управления
            PacketHandler.sendToPlayer(carrier, new ClientPrankPacket(
                    ClientPrankPacket.ActionType.START,
                    40,
                    500,
                    carrier.getId(), flea.getId(), 1.0f, 0f, "CARRIER"
            ));

            flea.playNotifySound(SoundEvents.SPIDER_AMBIENT, SoundSource.PLAYERS, 1.5f, 1.8f);
            carrier.playNotifySound(SoundEvents.PARROT_IMITATE_SPIDER, SoundSource.PLAYERS, 1.5f, 1.2f);
        }

        // Если нечетное число — лишний игрок становится "Дикой Блохой"
        if (hasWildFlea) {
            ServerPlayer wildFlea = pool.get(pool.size() - 1);
            wildFlea.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 500, 4, false, false, true)); // Скорость 5
            wildFlea.addEffect(new MobEffectInstance(MobEffects.JUMP, 500, 2, false, false, true));

            PacketHandler.sendToPlayer(wildFlea, new ClientPrankPacket(
                    ClientPrankPacket.ActionType.START,
                    40,
                    500,
                    wildFlea.getId(), 0, 0.25f, 0f, "WILD_FLEA"
            ));

            wildFlea.playNotifySound(SoundEvents.SILVERFISH_AMBIENT, SoundSource.PLAYERS, 2.0f, 1.8f);
            PrankManager.getInstance().broadcastMessage("§c§l[!] ДИКАЯ БЛОХА: §e" + wildFlea.getScoreboardName() + " стал бешеной дикой блохой со Скоростью 5!");
        }

        PrankManager.getInstance().broadcastMessage("§2§l[!] БЛОХИ И НОСИТЕЛИ: Игроки разбиты по парам! Блохи сидят на шее и дергают управление носителей (25 сек)!");

        // Тик-обработчик синхронизации и укусов Дикой Блохи
        for (int tick = 1; tick <= 500; tick++) {
            PrankManager.getInstance().scheduleTask("Fleas_Tick_" + tick, tick, () -> {
                if (hasWildFlea) {
                    ServerPlayer wild = pool.get(pool.size() - 1);
                    if (wild.isAlive()) {
                        for (int i = 0; i < pairsCount * 2; i++) {
                            ServerPlayer victim = pool.get(i);
                            if (victim.isAlive() && wild.distanceTo(victim) < 1.8) {
                                // Кусаем носителя или блоху
                                victim.hurt(victim.damageSources().generic(), 1.0f);
                                victim.playNotifySound(SoundEvents.BEE_STING, SoundSource.PLAYERS, 1.2f, 1.5f);
                            }
                        }
                    }
                }
            });
        }

        // Полный откат через 25 секунд (500 тиков)
        PrankManager.getInstance().scheduleTask("Fleas_Cleanup", 500, () -> {
            for (ServerPlayer p : players) {
                p.stopRiding();
                p.removeEffect(MobEffects.MOVEMENT_SPEED);
                p.removeEffect(MobEffects.JUMP);
            }
            PacketHandler.sendToAll(new ClientPrankPacket(
                    ClientPrankPacket.ActionType.STOP,
                    40, 0, 0, 0, 1.0f, 0f, "RESET_FLEA"
            ));
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] Все блохи и носители вернулись в исходное состояние!");
        });
    }
}