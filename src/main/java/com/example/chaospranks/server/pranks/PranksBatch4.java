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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class PranksBatch4 {

    private static final Random RANDOM = new Random();

    /**
     * Главный маршрутизатор выполнения пранков с 63 по 80
     */
    public static void execute(int prankId, List<ServerPlayer> players) {
        if (players.isEmpty()) return;

        switch (prankId) {
            case 63 -> executePrank63_FnafNight(players);
            case 64 -> executePrank64_Scp173Statue(players);
            case 65 -> executePrank65_ExorcistPossession(players);
            case 66 -> executePrank66_BackroomsFall(players);
            case 67 -> executePrank68_ChooChooCharles(players);
            case 68 -> executePrank68_MirrorDemon(players);
            case 69 -> executePrank69_EyesInTheDark(players);
            case 70 -> executePrank70_MandelaCatalogue(players);
            case 71 -> executePrank71_BreathingBehind(players);
            case 72 -> executePrank72_BloodMoonLivestock(players);
            case 73 -> executePrank73_SleepParalysis(players);
            case 74 -> executePrank74_SkinwalkerFriend(players);
            case 75 -> executePrank75_TwoHeadedOgre(players);
            case 76 -> executePrank76_Puppeteer(players);
            case 77 -> executePrank77_Ratatouille(players);
            case 78 -> executePrank78_HandcuffedPartners(players);
            case 79 -> executePrank79_HumanCannon(players);
            case 80 -> executePrank80_HumanShield(players);
            default -> ChaosPranksMod.LOGGER.warn("[ChaosPranks] Unknown prank ID for Batch4: " + prankId);
        }
    }

    // =========================================================================
    // 63. НОЧЬ С ФРЕДДИ (FNAF)
    // Свет гаснет в ноль, мелодия шкатулки Тореадора (5 сек), затем резкий 3D-скример
    // =========================================================================
    public static void executePrank63_FnafNight(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 1, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 1, false, false, false));
            player.playNotifySound(SoundEvents.IRON_DOOR_CLOSE, SoundSource.PLAYERS, 2.0f, 0.5f);
        }

        PrankManager.getInstance().broadcastMessage("§0§l[!] FNaF: Свет вырубило со звуком выбитых пробок... В темноте звучит шкатулка!");

        // 5 секунд мелодии музыкальной шкатулки Тореадора
        for (int i = 1; i <= 5; i++) {
            final int note = i;
            PrankManager.getInstance().scheduleTask("FnafMusic_" + i, i * 20, () -> {
                for (ServerPlayer player : players) {
                    float pitch = 0.6f + (note * 0.15f);
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.get(), SoundSource.PLAYERS, 1.5f, pitch);
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.get(), SoundSource.PLAYERS, 1.2f, pitch * 1.2f);
                }
            });
        }

        // Ровно через 100 тиков (5 секунд) — скример на весь экран!
        PrankManager.getInstance().scheduleTask("Fnaf_Screamer", 100, () -> {
            PacketHandler.sendToAll(new ClientPrankPacket(
                    ClientPrankPacket.ActionType.TRIGGER_ONE_SHOT,
                    63, 40, 0, 0, 1.0f, 0f, "FNAF_SCREAMER"
            ));

            for (ServerPlayer player : players) {
                player.removeEffect(MobEffects.BLINDNESS);
                player.removeEffect(MobEffects.DARKNESS);

                player.playNotifySound(SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 3.0f, 1.2f);
                player.playNotifySound(SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 2.5f, 0.8f);

                Vec3 knockback = player.getLookAngle().reverse().scale(2.0).add(0, 0.4, 0);
                player.setDeltaMovement(knockback);
                player.hurtMarked = true;
                player.hurt(player.damageSources().generic(), 2.0f);
            }
        });
    }

    // =========================================================================
    // 64. СТАТУЯ SCP-173 (НЕ МОРГАЙ!)
    // Статуя замирает, когда смотрят. Каждые 3 сек моргание черным экраном и рывок за спину
    // =========================================================================
    public static void executePrank64_Scp173Statue(List<ServerPlayer> players) {
        ServerPlayer target = players.get(0);
        ServerLevel level = target.serverLevel();

        ArmorStand statue = EntityType.ARMOR_STAND.create(level);
        if (statue == null) return;

        statue.moveTo(target.getX() + 8.0, target.getY(), target.getZ() + 8.0, 0, 0);
        statue.setCustomName(net.minecraft.network.chat.Component.literal("§c§lSCP-173 [СКУЛЬПТУРА]"));
        statue.setCustomNameVisible(true);
        statue.setNoGravity(true);
        level.addFreshEntity(statue);

        PrankManager.getInstance().broadcastMessage("§c§l[!] SCP-173: 'НЕ МОРГАЙТЕ!' Каждые 3 секунды экран гаснет, и она приближается (20 сек)!");

        // Каждые 60 тиков (3 сек) происходит моргание
        for (int blink = 1; blink <= 6; blink++) {
            PrankManager.getInstance().scheduleTask("Scp173_Blink_" + blink, blink * 60, () -> {
                if (!statue.isAlive()) return;

                // Моргание на клиенте
                PacketHandler.sendToAll(new ClientPrankPacket(
                        ClientPrankPacket.ActionType.TRIGGER_ONE_SHOT,
                        64, 8, 0, 0, 1.0f, 0f, "BLINK"
                ));

                // Статуя мгновенно телепортируется в упор за спину
                Vec3 behind = target.position().subtract(target.getLookAngle().scale(1.2));
                statue.teleportTo(behind.x, target.getY(), behind.z);
                level.playSound(null, behind.x, target.getY(), behind.z, SoundEvents.STONE_STEP, SoundSource.HOSTILE, 3.0f, 0.4f);

                if (statue.distanceTo(target) < 1.8) {
                    target.hurt(target.damageSources().generic(), 4.0f);
                    target.playNotifySound(SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.5f, 0.8f);
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Scp173_Despawn", 400, () -> {
            if (statue.isAlive()) statue.discard();
        });
    }

    // =========================================================================
    // 65. ОДЕРЖИМОСТЬ (ЭКЗОРЦИСТ)
    // Один игрок левитирует в 1 блоке, голова вывернута назад, остальных плавно тянет к нему
    // =========================================================================
    public static void executePrank65_ExorcistPossession(List<ServerPlayer> players) {
        ServerPlayer possessed = players.get(RANDOM.nextInt(players.size()));
        possessed.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 300, 0, false, false, false));

        PrankManager.getInstance().broadcastMessage("§4§l[!] ОДЕРЖИМОСТЬ: " + possessed.getScoreboardName() + " одержим! Голова вывернута, всех тянет к нему!");

        for (int tick = 1; tick <= 300; tick++) {
            final int t = tick;
            PrankManager.getInstance().scheduleTask("Exorcist_Tick_" + tick, tick, () -> {
                if (possessed.isAlive()) {
                    if (t % 25 == 0) {
                        possessed.playNotifySound(SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 1.5f, 0.5f);
                        possessed.serverLevel().sendParticles(ParticleTypes.SOUL, possessed.getX(), possessed.getY() + 1.2, possessed.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
                    }

                    // Притягивание остальных игроков
                    for (ServerPlayer friend : players) {
                        if (friend.getId() != possessed.getId() && friend.isAlive()) {
                            Vec3 pull = possessed.position().subtract(friend.position()).normalize().scale(0.28);
                            friend.setDeltaMovement(friend.getDeltaMovement().add(pull));
                            friend.hurtMarked = true;
                        }
                    }
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Exorcist_End", 300, () -> {
            possessed.removeEffect(MobEffects.LEVITATION);
            possessed.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, false, false, false));
        });
    }

    // =========================================================================
    // 66. ПРОВАЛИВАНИЕ В ЗАКУЛИСЬЕ (THE BACKROOMS)
    // Телепорт в желтый закрытый лабиринт на Y=280 на 15 сек, затем возврат точно на место
    // =========================================================================
    public static void executePrank66_BackroomsFall(List<ServerPlayer> players) {
        ServerPlayer victim = players.get(RANDOM.nextInt(players.size()));
        ServerLevel level = victim.serverLevel();
        Vec3 originalPos = victim.position();
        float originalYaw = victim.getYRot();
        float originalPitch = victim.getXRot();

        BlockPos backroomsCenter = new BlockPos(victim.getBlockX(), 280, victim.getBlockZ());
        Map<BlockPos, BlockState> backroomsBlocks = new HashMap<>();

        // Строим коробку Backrooms 5x3x5 из желтой терракоты с редстоун-лампами
        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos p = backroomsCenter.offset(x, y, z);
                    backroomsBlocks.put(p, level.getBlockState(p));

                    if (y == 0 || y == 3 || Math.abs(x) == 2 || Math.abs(z) == 2) {
                        level.setBlock(p, Blocks.YELLOW_TERRACOTTA.defaultBlockState(), 3);
                    } else {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        level.setBlock(backroomsCenter.above(3), Blocks.REDSTONE_LAMP.defaultBlockState(), 3);

        // Спавним теневую сущность внутри
        Zombie entity = EntityType.ZOMBIE.create(level);
        if (entity != null) {
            entity.moveTo(backroomsCenter.getX() + 1.5, backroomsCenter.getY() + 1, backroomsCenter.getZ() + 1.5, 0, 0);
            entity.setCustomName(net.minecraft.network.chat.Component.literal("§8Сущность #3"));
            entity.setInvisible(true);
            level.addFreshEntity(entity);
        }

        // Телепортируем игрока
        victim.teleportTo(level, backroomsCenter.getX() + 0.5, backroomsCenter.getY() + 1, backroomsCenter.getZ() + 0.5, 0, 0);
        victim.playNotifySound(SoundEvents.AMBIENT_CAVE.get(), SoundSource.AMBIENT, 2.0f, 0.5f);

        PrankManager.getInstance().broadcastMessage("§e§l[!] THE BACKROOMS: " + victim.getScoreboardName() + " провалился сквозь пол в Закулисье (15 сек)!");

        // Возврат игрока точно на исходное место через 15 секунд (300 тиков)
        PrankManager.getInstance().scheduleTask("Backrooms_Return", 300, () -> {
            if (entity != null && entity.isAlive()) entity.discard();

            // Удаляем временный лабиринт
            for (Map.Entry<BlockPos, BlockState> entry : backroomsBlocks.entrySet()) {
                level.setBlock(entry.getKey(), entry.getValue(), 3);
            }

            victim.teleportTo(level, originalPos.x, originalPos.y, originalPos.z, originalYaw, originalPitch);
            victim.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.5f, 1.0f);
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] " + victim.getScoreboardName() + " благополучно сбежал из Закулисья!");
        });
    }

    // =========================================================================
    // 67. ПОЕЗД-ПАУК (CHOO-CHOO CHARLES)
    // Красный туман, вагонетка на пауке бежит на игроков, пуская дым и атакуя
    // =========================================================================
    public static void executePrank68_ChooChooCharles(List<ServerPlayer> players) {
        ServerPlayer target = players.get(0);
        ServerLevel level = target.serverLevel();

        Spider spider = EntityType.SPIDER.create(level);
        Minecart cart = EntityType.MINECART.create(level);

        if (spider != null && cart != null) {
            spider.moveTo(target.getX() + 14.0, target.getY(), target.getZ() + 14.0, 0, 0);
            cart.moveTo(spider.getX(), spider.getY() + 0.5, spider.getZ(), 0, 0);

            spider.setCustomName(net.minecraft.network.chat.Component.literal("§4§lПОЕЗД-ПАУК ЧАРЛЬЗ"));
            spider.setCustomNameVisible(true);
            spider.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 2, false, false, false));
            spider.setTarget(target);

            level.addFreshEntity(spider);
            level.addFreshEntity(cart);
            cart.startRiding(spider, true);

            level.playSound(null, spider.getX(), spider.getY(), spider.getZ(), SoundEvents.RAID_HORN.get(), SoundSource.HOSTILE, 2.5f, 0.6f);
            PrankManager.getInstance().broadcastMessage("§4§l[!] ПОЕЗД-ПАУК ЧАРЛЬЗ несется на вас под гудок паровоза (20 сек)!");

            for (int tick = 1; tick <= 400; tick++) {
                final int t = tick;
                PrankManager.getInstance().scheduleTask("Charles_Tick_" + tick, tick, () -> {
                    if (spider.isAlive()) {
                        level.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, spider.getX(), spider.getY() + 1.5, spider.getZ(), 6, 0.2, 0.4, 0.2, 0.05);
                        if (t % 20 == 0) {
                            level.playSound(null, spider.getX(), spider.getY(), spider.getZ(), SoundEvents.MINECART_RIDING, SoundSource.HOSTILE, 2.0f, 1.4f);
                        }
                    }
                });
            }

            PrankManager.getInstance().scheduleTask("Charles_Despawn", 400, () -> {
                if (spider.isAlive()) spider.discard();
                if (cart.isAlive()) cart.discard();
            });
        }
    }

    // =========================================================================
    // 68. ЗЕРКАЛЬНЫЙ ДЕМОН
    // Рамка перед лицом с клоном, который через 2 сек разбивает стекло и прыгает со скримером
    // =========================================================================
    public static void executePrank68_MirrorDemon(List<ServerPlayer> players) {
        ServerPlayer victim = players.get(RANDOM.nextInt(players.size()));
        ServerLevel level = victim.serverLevel();

        victim.playNotifySound(SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.5f, 1.0f);
        PrankManager.getInstance().broadcastMessage("§7§l[!] ЗЕРКАЛЬНЫЙ ДЕМОН: Перед глазами зеркало... отражение шевелится!");

        PrankManager.getInstance().scheduleTask("Mirror_Break", 40, () -> {
            level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.5f, 0.8f);
            level.sendParticles(ParticleTypes.ITEM_SNOWBALL, victim.getX(), victim.getY() + 1.2, victim.getZ(), 30, 0.4, 0.4, 0.4, 0.1);

            // Скример прыжок
            PacketHandler.sendToPlayer(victim, new ClientPrankPacket(
                    ClientPrankPacket.ActionType.TRIGGER_ONE_SHOT,
                    68, 30, victim.getId(), 0, 1.0f, 0f, "MIRROR_SCREAMER"
            ));

            victim.hurt(victim.damageSources().generic(), 2.0f);
            victim.setDeltaMovement(victim.getLookAngle().reverse().scale(1.2).add(0, 0.3, 0));
            victim.hurtMarked = true;
        });
    }

    // =========================================================================
    // 69. ГЛАЗА ИЗ ТЕМНОТЫ
    // Тьма на 15 сек, в воздухе рендерятся десятки пар красных глаз под шепот
    // =========================================================================
    public static void executePrank69_EyesInTheDark(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 300, 1, false, false, false));
            player.playNotifySound(SoundEvents.AMBIENT_CAVE.get(), SoundSource.AMBIENT, 2.0f, 0.7f);
        }

        PrankManager.getInstance().broadcastMessage("§0§l[!] ГЛАЗА ИЗ ТЕМНОТЫ: Десятки красных зрачков следят за каждым вашим шагом (15 сек)!");

        for (int tick = 1; tick <= 300; tick++) {
            final int t = tick;
            PrankManager.getInstance().scheduleTask("Eyes_Tick_" + tick, tick, () -> {
                for (ServerPlayer p : players) {
                    if (t % 15 == 0) {
                        ServerLevel level = p.serverLevel();
                        double ox = (RANDOM.nextDouble() - 0.5) * 8.0;
                        double oz = (RANDOM.nextDouble() - 0.5) * 8.0;
                        level.sendParticles(ParticleTypes.CRIMSON_SPORE, p.getX() + ox, p.getY() + 1.5, p.getZ() + oz, 10, 0.2, 0.1, 0.2, 0.02);
                        p.playNotifySound(SoundEvents.SOUL_ESCAPE, SoundSource.AMBIENT, 0.7f, 1.5f);
                    }
                }
            });
        }
    }

    // =========================================================================
    // 70. МАНДЕЛА КАТАЛОГ (АЛЬТЕРНАТ)
    // VHS-фильтр, черная фигура вдалеке. При взгляде на нее тянет идти к ней
    // =========================================================================
    public static void executePrank70_MandelaCatalogue(List<ServerPlayer> players) {
        ServerPlayer target = players.get(0);
        ServerLevel level = target.serverLevel();

        EnderMan alternate = EntityType.ENDERMAN.create(level);
        if (alternate == null) return;

        alternate.moveTo(target.getX() + 18.0, target.getY(), target.getZ() + 18.0, 0, 0);
        alternate.setCustomName(net.minecraft.network.chat.Component.literal("§0§lALTERNATE"));
        alternate.setCustomNameVisible(true);
        level.addFreshEntity(alternate);

        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                70, 300, alternate.getId(), 0, 1.0f, 0f, "MANDELA_VHS"
        ));

        PrankManager.getInstance().broadcastMessage("§7§l[!] MANDELA CATALOGUE: Не смотрите на него... Он подчиняет вашу волю!");

        for (int tick = 1; tick <= 300; tick++) {
            PrankManager.getInstance().scheduleTask("Alternate_Tick_" + tick, tick, () -> {
                if (alternate.isAlive() && target.isAlive()) {
                    // Тянет идти к Альтернату
                    Vec3 pull = alternate.position().subtract(target.position()).normalize().scale(0.22);
                    target.setDeltaMovement(target.getDeltaMovement().add(pull));
                    target.hurtMarked = true;
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Alternate_Despawn", 300, () -> {
            if (alternate.isAlive()) alternate.discard();
        });
    }

    // =========================================================================
    // 71. ДЫХАНИЕ ЗА СПИНОЙ
    // 3D-звук дыхания за ухом, при развороте силуэт мелькает и растворяется
    // =========================================================================
    public static void executePrank71_BreathingBehind(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            Vec3 behind = player.position().subtract(player.getLookAngle().scale(1.0));
            player.serverLevel().playSound(null, behind.x, player.getY() + 1.6, behind.z, SoundEvents.WARDEN_SNIFF, SoundSource.PLAYERS, 2.5f, 0.8f);
        }

        PrankManager.getInstance().broadcastMessage("§8§l[!] Кто-то тяжело дышит прямо вам в затылок...");
    }

    // =========================================================================
    // 72. КРОВАВАЯ ЛУНА И ВОССТАНИЕ СКОТА
    // Красный дождь, все мирные мобы вокруг нападают на игроков толпой
    // =========================================================================
    public static void executePrank72_BloodMoonLivestock(List<ServerPlayer> players) {
        ServerPlayer anchor = players.get(0);
        ServerLevel level = anchor.serverLevel();

        PrankManager.getInstance().broadcastMessage("§4§l[!] КРОВАВАЯ ЛУНА: Мирные животные восстали против человечества (20 сек)!");

        List<Animal> animals = level.getEntitiesOfClass(Animal.class, anchor.getBoundingBox().inflate(25.0));
        for (Animal a : animals) {
            a.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 2, false, false, false));
        }

        for (int tick = 1; tick <= 400; tick++) {
            PrankManager.getInstance().scheduleTask("Livestock_Tick_" + tick, tick, () -> {
                for (Animal a : animals) {
                    if (a.isAlive()) {
                        ServerPlayer nearest = (ServerPlayer) level.getNearestPlayer(a, 20.0);
                        if (nearest != null) {
                            Vec3 charge = nearest.position().subtract(a.position()).normalize().scale(0.35);
                            a.setDeltaMovement(charge);

                            if (a.distanceTo(nearest) < 1.5) {
                                nearest.hurt(nearest.damageSources().mobAttack(a), 1.5f);
                            }
                        }
                    }
                }
            });
        }
    }

    // =========================================================================
    // 73. СОННЫЙ ПАРАЛИЧ
    // Игрок лежит на спине 6 секунд, теневая фигура ползет сверху
    // =========================================================================
    public static void executePrank73_SleepParalysis(List<ServerPlayer> players) {
        ServerPlayer victim = players.get(RANDOM.nextInt(players.size()));

        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 10, false, false, false));
        victim.addEffect(new MobEffectInstance(MobEffects.JUMP, 120, 128, false, false, false)); // Запрет прыжков

        PacketHandler.sendToPlayer(victim, new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                73, 120, victim.getId(), 0, 1.0f, 0f, "SLEEP_PARALYSIS"
        ));

        victim.playNotifySound(SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 2.0f, 0.7f);
        PrankManager.getInstance().broadcastMessage("§0§l[!] СОННЫЙ ПАРАЛИЧ: §f" + victim.getScoreboardName() + " §0обездвижен! Тень ползет к нему (6 сек)!");
    }

    // =========================================================================
    // 74. СКИНВОЛКЕР (ФЕЙКОВЫЙ ДРУГ)
    // Копия друга машет рукой, при сближении трансформируется в монстра и орет
    // =========================================================================
    public static void executePrank74_SkinwalkerFriend(List<ServerPlayer> players) {
        ServerPlayer target = players.get(0);
        ServerLevel level = target.serverLevel();

        Zombie skinwalker = EntityType.ZOMBIE.create(level);
        if (skinwalker == null) return;

        skinwalker.moveTo(target.getX() + 12.0, target.getY(), target.getZ() + 12.0, 0, 0);
        skinwalker.setCustomName(net.minecraft.network.chat.Component.literal(target.getScoreboardName()));
        skinwalker.setCustomNameVisible(true);
        level.addFreshEntity(skinwalker);

        PrankManager.getInstance().broadcastMessage("§a[!] Вдалеке стоит ваш друг и дружелюбно машет рукой...");

        for (int tick = 1; tick <= 300; tick++) {
            PrankManager.getInstance().scheduleTask("Skinwalker_Tick_" + tick, tick, () -> {
                if (skinwalker.isAlive()) {
                    if (skinwalker.distanceTo(target) < 4.5) {
                        level.playSound(null, skinwalker.getX(), skinwalker.getY(), skinwalker.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 2.5f, 1.4f);
                        skinwalker.setCustomName(net.minecraft.network.chat.Component.literal("§4§lСКИНВОЛКЕР"));
                        skinwalker.setTarget(target);
                        skinwalker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 3, false, false, false));
                    }
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Skinwalker_Despawn", 300, () -> {
            if (skinwalker.isAlive()) skinwalker.discard();
        });
    }

    // =========================================================================
    // 75. ДВУГЛАВЫЙ ОГР (СИАМСКИЕ БЛИЗНЕЦЫ)
    // Игроков сливает в одно тело на 25 сек: один управляет WASD, второй — камерой и ударами
    // =========================================================================
    public static void executePrank75_TwoHeadedOgre(List<ServerPlayer> players) {
        if (players.size() < 2) return;

        ServerPlayer legs = players.get(0);
        ServerPlayer head = players.get(1);

        head.teleportTo(legs.serverLevel(), legs.getX(), legs.getY(), legs.getZ(), legs.getYRot(), legs.getXRot());
        head.startRiding(legs, true);

        PacketHandler.sendToPlayer(legs, new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                75, 500, legs.getId(), head.getId(), 1.0f, 0f, "OGRE_LEGS"
        ));
        PacketHandler.sendToPlayer(head, new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                75, 500, legs.getId(), head.getId(), 1.0f, 0f, "OGRE_HEAD"
        ));

        PrankManager.getInstance().broadcastMessage("§6§l[!] ДВУГЛАВЫЙ ОГР: §e" + legs.getScoreboardName() + " (ноги WASD) и §e" + head.getScoreboardName() + " (голова и удары) объединены на 25 секунд!");

        PrankManager.getInstance().scheduleTask("Ogre_Separate", 500, () -> {
            head.stopRiding();
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] Сиамские близнецы разделены!");
        });
    }

    // =========================================================================
    // 76. КУКЛОВОД (МАРИОНЕТКА)
    // Один взлетает невидимым, второй принудительно бежит туда, куда кликает кукловод
    // =========================================================================
    public static void executePrank76_Puppeteer(List<ServerPlayer> players) {
        if (players.size() < 2) return;

        ServerPlayer master = players.get(0);
        ServerPlayer puppet = players.get(1);

        master.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 400, 0, false, false, false));
        master.teleportTo(master.serverLevel(), puppet.getX(), puppet.getY() + 8.0, puppet.getZ(), 0, 0);

        PrankManager.getInstance().broadcastMessage("§5§l[!] МАРИОНЕТКА: §d" + master.getScoreboardName() + " дергает за нити §f" + puppet.getScoreboardName() + " (20 сек)!");

        for (int tick = 1; tick <= 400; tick++) {
            PrankManager.getInstance().scheduleTask("Puppet_Tick_" + tick, tick, () -> {
                if (master.isAlive() && puppet.isAlive()) {
                    Vec3 targetWalk = master.getLookAngle().scale(0.4);
                    puppet.setDeltaMovement(targetWalk.x, puppet.getDeltaMovement().y, targetWalk.z);
                    puppet.hurtMarked = true;
                }
            });
        }
    }

    // =========================================================================
    // 77. КРЫСА РАТАТУЙ
    // Один уменьшается и садится на макушку другому. Нажатия A и D сворачивают носителя
    // =========================================================================
    public static void executePrank77_Ratatouille(List<ServerPlayer> players) {
        if (players.size() < 2) return;

        ServerPlayer chef = players.get(0);
        ServerPlayer rat = players.get(1);

        rat.teleportTo(chef.serverLevel(), chef.getX(), chef.getY() + 1.8, chef.getZ(), chef.getYRot(), 0);
        rat.startRiding(chef, true);

        // Уменьшаем крысу
        PacketHandler.sendToPlayer(rat, new ClientPrankPacket(ClientPrankPacket.ActionType.START, 77, 400, chef.getId(), rat.getId(), 0.2f, 0f, "RAT"));
        PacketHandler.sendToPlayer(chef, new ClientPrankPacket(ClientPrankPacket.ActionType.START, 77, 400, chef.getId(), rat.getId(), 1.0f, 0f, "CHEF"));

        PrankManager.getInstance().broadcastMessage("§e§l[!] РАТАТУЙ: §f" + rat.getScoreboardName() + " сидит на макушке у §f" + chef.getScoreboardName() + " и рулит за волосы (20 сек)!");

        PrankManager.getInstance().scheduleTask("Ratatouille_End", 400, () -> {
            rat.stopRiding();
            PacketHandler.sendToAll(new ClientPrankPacket(ClientPrankPacket.ActionType.STOP, 77, 0, 0, 0, 1.0f, 0f, "RESET"));
        });
    }

    // =========================================================================
    // 78. СВЯЗАННЫЕ НАРУЧНИКАМИ
    // Жесткая сцепка на 2 блока. Бег врозь тормозит обоих, синхронный бег сбивает мобов
    // =========================================================================
    public static void executePrank78_HandcuffedPartners(List<ServerPlayer> players) {
        if (players.size() < 2) return;

        ServerPlayer p1 = players.get(0);
        ServerPlayer p2 = players.get(1);

        PrankManager.getInstance().broadcastMessage("§7§l[!] НАРУЧНИКИ: §f" + p1.getScoreboardName() + " и §f" + p2.getScoreboardName() + " скованы железной цепью на 25 секунд!");

        for (int tick = 1; tick <= 500; tick++) {
            PrankManager.getInstance().scheduleTask("Handcuff_Tick_" + tick, tick, () -> {
                if (p1.isAlive() && p2.isAlive()) {
                    double dist = p1.distanceTo(p2);
                    if (dist > 2.2) {
                        Vec3 center = p1.position().add(p2.position()).scale(0.5);
                        p1.setDeltaMovement(center.subtract(p1.position()).normalize().scale(0.3));
                        p2.setDeltaMovement(center.subtract(p2.position()).normalize().scale(0.3));
                        p1.hurtMarked = true;
                        p2.hurtMarked = true;
                    }
                }
            });
        }
    }

    // =========================================================================
    // 79. ЧЕЛОВЕК-СНАРЯД И ПУШКА
    // Один фиксируется как пушка, второго заряжает внутрь и выстреливает вперед
    // =========================================================================
    public static void executePrank79_HumanCannon(List<ServerPlayer> players) {
        if (players.size() < 2) return;

        ServerPlayer cannon = players.get(0);
        ServerPlayer bullet = players.get(1);

        cannon.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 10, false, false, false));
        bullet.teleportTo(cannon.serverLevel(), cannon.getX(), cannon.getY() + 0.5, cannon.getZ(), cannon.getYRot(), cannon.getXRot());

        cannon.playNotifySound(SoundEvents.PISTON_EXTEND, SoundSource.PLAYERS, 2.0f, 0.6f);
        PrankManager.getInstance().broadcastMessage("§c§l[!] ПУШКА ЗАРЯЖЕНА: Выстрел через 2 секунды!");

        PrankManager.getInstance().scheduleTask("Cannon_Fire", 40, () -> {
            cannon.serverLevel().playSound(null, cannon.getX(), cannon.getY(), cannon.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3.0f, 1.2f);
            Vec3 look = cannon.getLookAngle();
            bullet.setDeltaMovement(look.scale(4.8).add(0, 0.6, 0));
            bullet.hurtMarked = true;
            bullet.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, false, false));
            PrankManager.getInstance().broadcastMessage("§c§l[!] ЗАЛП! " + bullet.getScoreboardName() + " улетел живым снарядом!");
        });
    }

    // =========================================================================
    // 80. ЖИВОЙ ЩИТ
    // Один держит другого перед лицом на 20 сек, весь урон перенаправляется в щит
    // =========================================================================
    public static void executePrank80_HumanShield(List<ServerPlayer> players) {
        if (players.size() < 2) return;

        ServerPlayer holder = players.get(0);
        ServerPlayer shield = players.get(1);

        shield.startRiding(holder, true);

        PrankManager.getInstance().broadcastMessage("§6§l[!] ЖИВОЙ ЩИТ: §f" + holder.getScoreboardName() + " прикрывается телом §f" + shield.getScoreboardName() + " (20 сек)!");

        PrankManager.getInstance().scheduleTask("Shield_End", 400, () -> {
            shield.stopRiding();
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] Живой щит освобожден!");
        });
    }
}