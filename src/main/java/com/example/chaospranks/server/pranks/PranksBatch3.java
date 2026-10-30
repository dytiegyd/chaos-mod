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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class PranksBatch3 {

    private static final Random RANDOM = new Random();

    /**
     * Главный маршрутизатор выполнения пранков с 41 по 62
     */
    public static void execute(int prankId, List<ServerPlayer> players) {
        if (players.isEmpty()) return;

        switch (prankId) {
            case 41 -> executePrank41_NextbotChips(players);
            case 42 -> executePrank42_GodzillaChicken(players);
            case 43 -> executePrank43_GigachadCreeper(players);
            case 44 -> executePrank44_GopnikVillagers(players);
            case 45 -> executePrank45_EvilDoppelganger(players);
            case 46 -> executePrank46_MimicChest(players);
            case 47 -> executePrank47_HelicopterCow(players);
            case 48 -> executePrank48_RagdollMode(players);
            case 49 -> executePrank49_AirSwimming(players);
            case 50 -> executePrank50_BalloonPlayer(players);
            case 51 -> executePrank51_HouseEscape(players);
            case 52 -> executePrank52_TrampolineBlocks(players);
            case 53 -> executePrank53_TraitorItems(players);
            case 54 -> executePrank54_IceSlideToHell(players);
            case 55 -> executePrank55_ClownSteps(players);
            case 56 -> executePrank56_FakeDonation(players);
            case 57 -> executePrank57_CourtTrial(players);
            case 58 -> executePrank58_PoliceChase(players);
            case 59 -> executePrank59_PopupAd(players);
            case 60 -> executePrank60_MagicKick(players);
            case 61 -> executePrank61_MitosisOnDamage(players);
            case 62 -> executePrank62_SlendermanHorror(players);
            default -> ChaosPranksMod.LOGGER.warn("[ChaosPranks] Unknown prank ID for Batch3: " + prankId);
        }
    }

    // =========================================================================
    // 41. ПАЧКА ЧИПСОВ-УБИЙЦА (NEXTBOT)
    // 2D-сущность несется сквозь блоки с громким звуком и подбрасывает на 20 блоков
    // =========================================================================
    public static void executePrank41_NextbotChips(List<ServerPlayer> players) {
        ServerPlayer target = players.get(RANDOM.nextInt(players.size()));
        ServerLevel level = target.serverLevel();

        Zombie nextbot = EntityType.ZOMBIE.create(level);
        if (nextbot == null) return;

        nextbot.moveTo(target.getX() + 15.0, target.getY(), target.getZ() + 15.0, 0, 0);
        nextbot.setCustomName(net.minecraft.network.chat.Component.literal("§e§lLAYS С СЫРОМ [NEXTBOT]"));
        nextbot.setCustomNameVisible(true);
        nextbot.setInvisible(false);
        nextbot.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 5, false, false, false));
        nextbot.addTag("NextbotEntity");
        level.addFreshEntity(nextbot);

        PrankManager.getInstance().broadcastMessage("§e§l[!] NEXTBOT: Пачка чипсов бежит за вами со звуком мема (15 сек)!");

        for (int tick = 1; tick <= 300; tick++) {
            PrankManager.getInstance().scheduleTask("Nextbot_Tick_" + tick, tick, () -> {
                if (nextbot.isAlive()) {
                    // Траектория наведения на игрока
                    Vec3 toTarget = target.position().subtract(nextbot.position()).normalize().scale(0.85);
                    nextbot.setDeltaMovement(toTarget.x, 0.1, toTarget.z);

                    if (nextbot.tickCount % 15 == 0) {
                        level.playSound(null, nextbot.getX(), nextbot.getY(), nextbot.getZ(), SoundEvents.DONKEY_ANGRY, SoundSource.HOSTILE, 2.5f, 0.5f);
                    }

                    // Подбрасывание при сближении
                    if (nextbot.distanceTo(target) < 2.0) {
                        target.setDeltaMovement(0, 2.3, 0); // 20 блоков вверх
                        target.hurtMarked = true;
                        target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0, false, false, false));
                        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0f, 1.8f);
                    }
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Nextbot_Despawn", 300, () -> {
            if (nextbot.isAlive()) nextbot.discard();
        });
    }

    // =========================================================================
    // 42. КУРИЦА-ГОДЗИЛЛА
    // 15-метровая курица, трясет камеру шагами, орет басом и несет зажженный ТНТ
    // =========================================================================
    public static void executePrank42_GodzillaChicken(List<ServerPlayer> players) {
        ServerPlayer anchor = players.get(0);
        ServerLevel level = anchor.serverLevel();

        Chicken godzilla = EntityType.CHICKEN.create(level);
        if (godzilla == null) return;

        godzilla.moveTo(anchor.getX() + 6.0, anchor.getY(), anchor.getZ() + 6.0, 0, 0);
        godzilla.setCustomName(net.minecraft.network.chat.Component.literal("§c§lКУРИЦА-ГОДЗИЛЛА"));
        godzilla.setCustomNameVisible(true);
        level.addFreshEntity(godzilla);

        // Клиентский масштаб курицы
        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                42, 400, godzilla.getId(), 0, 8.0f, 0f, "GODZILLA_CHICKEN"
        ));

        PrankManager.getInstance().broadcastMessage("§c§l[!] КУРИЦА-ГОДЗИЛЛА: Огромная курица несется на вас и откладывает ТНТ (20 сек)!");

        for (int tick = 1; tick <= 400; tick++) {
            PrankManager.getInstance().scheduleTask("Godzilla_Tick_" + tick, tick, () -> {
                if (godzilla.isAlive()) {
                    if (godzilla.tickCount % 20 == 0) {
                        level.playSound(null, godzilla.getX(), godzilla.getY(), godzilla.getZ(), SoundEvents.CHICKEN_HURT, SoundSource.HOSTILE, 3.0f, 0.35f);
                        level.playSound(null, godzilla.getX(), godzilla.getY(), godzilla.getZ(), SoundEvents.ZOMBIE_STEP, SoundSource.HOSTILE, 2.5f, 0.4f);
                    }

                    // Каждые 40 тиков (2 сек) спавнит ТНТ вместо яйца
                    if (godzilla.tickCount % 40 == 0) {
                        PrimedTnt tnt = new PrimedTnt(level, godzilla.getX(), godzilla.getY(), godzilla.getZ(), null);
                        tnt.setFuse(40);
                        level.addFreshEntity(tnt);
                        level.playSound(null, godzilla.getX(), godzilla.getY(), godzilla.getZ(), SoundEvents.CHICKEN_EGG, SoundSource.HOSTILE, 2.0f, 0.5f);
                    }
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Godzilla_Despawn", 400, () -> {
            if (godzilla.isAlive()) {
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, godzilla.getX(), godzilla.getY() + 2.0, godzilla.getZ(), 4, 1.0, 1.0, 1.0, 0.1);
                godzilla.discard();
            }
        });
    }

    // =========================================================================
    // 43. КРИПЕР-ГИГАЧАД
    // Не взрывается, а подбегает, хватает игрока и делает суплекс об землю
    // =========================================================================
    public static void executePrank43_GigachadCreeper(List<ServerPlayer> players) {
        ServerPlayer victim = players.get(RANDOM.nextInt(players.size()));
        ServerLevel level = victim.serverLevel();

        Creeper gigachad = EntityType.CREEPER.create(level);
        if (gigachad == null) return;

        gigachad.moveTo(victim.getX() + 5.0, victim.getY(), victim.getZ() + 5.0, 0, 0);
        gigachad.setCustomName(net.minecraft.network.chat.Component.literal("§2§lКРИПЕР-ГИГАЧАД"));
        gigachad.setCustomNameVisible(true);
        gigachad.setTarget(victim);
        gigachad.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2, false, false, false));
        level.addFreshEntity(gigachad);

        PrankManager.getInstance().broadcastMessage("§2§l[!] КРИПЕР-ГИГАЧАД: Он не взрывается... он идет делать вам суплекс!");

        for (int tick = 1; tick <= 300; tick++) {
            PrankManager.getInstance().scheduleTask("Gigachad_Tick_" + tick, tick, () -> {
                if (gigachad.isAlive() && victim.isAlive()) {
                    if (gigachad.distanceTo(victim) < 2.0) {
                        // Суплекс: подброс и удар об землю
                        victim.setDeltaMovement(0, 0.9, 0);
                        victim.hurtMarked = true;
                        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.5f, 0.8f);
                        victim.hurt(victim.damageSources().mobAttack(gigachad), 5.0f);
                    }
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Gigachad_Despawn", 300, () -> {
            if (gigachad.isAlive()) gigachad.discard();
        });
    }

    // =========================================================================
    // 44. БАНДА ГОПНИКОВ-ЖИТЕЛЕЙ
    // 5 жителей в присяде несутся на игрока, пинают его с низким звуком "Хммм"
    // =========================================================================
    public static void executePrank44_GopnikVillagers(List<ServerPlayer> players) {
        ServerPlayer target = players.get(0);
        ServerLevel level = target.serverLevel();
        List<Villager> gopniks = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Villager v = EntityType.VILLAGER.create(level);
            if (v != null) {
                double ox = (RANDOM.nextDouble() - 0.5) * 6.0;
                double oz = (RANDOM.nextDouble() - 0.5) * 6.0;
                v.moveTo(target.getX() + ox, target.getY(), target.getZ() + oz, 0, 0);
                v.setCustomName(net.minecraft.network.chat.Component.literal("§7Гопник #" + (i + 1)));
                v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 3, false, false, false));
                level.addFreshEntity(v);
                gopniks.add(v);
            }
        }

        PrankManager.getInstance().broadcastMessage("§7§l[!] БАНДА ГОПНИКОВ-ЖИТЕЛЕЙ: 'Эй, изумруды есть? А если найдем?' (15 сек)!");

        for (int tick = 1; tick <= 300; tick++) {
            PrankManager.getInstance().scheduleTask("Gopnik_Tick_" + tick, tick, () -> {
                for (Villager v : gopniks) {
                    if (v.isAlive()) {
                        Vec3 toPlayer = target.position().subtract(v.position()).normalize().scale(0.4);
                        v.setDeltaMovement(toPlayer);

                        if (v.distanceTo(target) < 1.6) {
                            target.setDeltaMovement(toPlayer.scale(1.8).add(0, 0.25, 0));
                            target.hurtMarked = true;
                            level.playSound(null, v.getX(), v.getY(), v.getZ(), SoundEvents.VILLAGER_NO, SoundSource.HOSTILE, 1.5f, 0.45f);
                        }
                    }
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Gopniks_Despawn", 300, () -> {
            for (Villager v : gopniks) {
                if (v.isAlive()) v.discard();
            }
        });
    }

    // =========================================================================
    // 45. ЗЛОЙ ДВОЙНИК
    // Спавнится моб, копирующий скин, ник задом наперед и экипировку игрока
    // =========================================================================
    public static void executePrank45_EvilDoppelganger(List<ServerPlayer> players) {
        ServerPlayer target = players.get(RANDOM.nextInt(players.size()));
        ServerLevel level = target.serverLevel();

        Zombie clone = EntityType.ZOMBIE.create(level);
        if (clone == null) return;

        clone.moveTo(target.getX() + 3.0, target.getY(), target.getZ() + 3.0, 0, 0);

        String reversedName = new StringBuilder(target.getScoreboardName()).reverse().toString();
        clone.setCustomName(net.minecraft.network.chat.Component.literal("§4" + reversedName));
        clone.setCustomNameVisible(true);

        // Копируем экипировку
        clone.setItemSlot(EquipmentSlot.HEAD, target.getItemBySlot(EquipmentSlot.HEAD).copy());
        clone.setItemSlot(EquipmentSlot.CHEST, target.getItemBySlot(EquipmentSlot.CHEST).copy());
        clone.setItemSlot(EquipmentSlot.LEGS, target.getItemBySlot(EquipmentSlot.LEGS).copy());
        clone.setItemSlot(EquipmentSlot.FEET, target.getItemBySlot(EquipmentSlot.FEET).copy());
        clone.setItemSlot(EquipmentSlot.MAINHAND, target.getMainHandItem().copy());

        clone.setTarget(target);
        clone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1, false, false, false));
        level.addFreshEntity(clone);

        level.playSound(null, clone.getX(), clone.getY(), clone.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.2f, 1.5f);
        PrankManager.getInstance().broadcastMessage("§4§l[!] ЗЛОЙ ДВОЙНИК: " + reversedName + " пришел уничтожить оригинал (20 сек)!");

        PrankManager.getInstance().scheduleTask("Doppelganger_Despawn", 400, () -> {
            if (clone.isAlive()) {
                level.sendParticles(ParticleTypes.SMOKE, clone.getX(), clone.getY() + 1.0, clone.getZ(), 15, 0.4, 0.6, 0.4, 0.05);
                clone.discard();
            }
        });
    }

    // =========================================================================
    // 46. ОЖИВШИЙ СУНДУК-МИМИК
    // Сундук прыгает на голову игрока, закрывает обзор челюстями и кусает
    // =========================================================================
    public static void executePrank46_MimicChest(List<ServerPlayer> players) {
        ServerPlayer victim = players.get(RANDOM.nextInt(players.size()));

        PacketHandler.sendToPlayer(victim, new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                46, 300, victim.getId(), 0, 1.0f, 0f, "MIMIC_OVERLAY"
        ));

        PrankManager.getInstance().broadcastMessage("§6§l[!] СУНДУК-МИМИК прыгнул на голову §f" + victim.getScoreboardName() + "§6! Друзья, сбейте его ударом!");

        for (int tick = 1; tick <= 300; tick++) {
            PrankManager.getInstance().scheduleTask("Mimic_Bite_" + tick, tick, () -> {
                if (victim.isAlive() && tick % 25 == 0) {
                    victim.hurt(victim.damageSources().generic(), 1.0f);
                    victim.playNotifySound(SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 1.5f, 1.4f);
                }
            });
        }
    }

    // =========================================================================
    // 47. КОРОВА-ВЕРТОЛЕТ
    // Корова взлетает, вращаясь со звуком пропеллера, и сбрасывает взрывное молоко
    // =========================================================================
    public static void executePrank47_HelicopterCow(List<ServerPlayer> players) {
        ServerPlayer target = players.get(0);
        ServerLevel level = target.serverLevel();

        Cow cow = EntityType.COW.create(level);
        if (cow == null) return;

        cow.moveTo(target.getX(), target.getY() + 12.0, target.getZ(), 0, 0);
        cow.setNoGravity(true);
        level.addFreshEntity(cow);

        PrankManager.getInstance().broadcastMessage("§f§l[!] КОРОВА-ВЕРТОЛЕТ: В небе крутится корова и сбрасывает бомбы из молока (15 сек)!");

        for (int tick = 1; tick <= 300; tick++) {
            final int t = tick;
            PrankManager.getInstance().scheduleTask("HeliCow_Tick_" + tick, tick, () -> {
                if (cow.isAlive()) {
                    cow.setYRot(cow.getYRot() + 25.0f); // Бешеное вращение

                    if (t % 15 == 0) {
                        level.playSound(null, cow.getX(), cow.getY(), cow.getZ(), SoundEvents.MINECART_RIDING, SoundSource.NEUTRAL, 2.0f, 1.8f);
                    }

                    // Сброс взрывного молока каждые 30 тиков
                    if (t % 30 == 0) {
                        ItemEntity milk = new ItemEntity(level, cow.getX(), cow.getY() - 1.0, cow.getZ(), new ItemStack(Items.MILK_BUCKET));
                        milk.setDeltaMovement(0, -0.8, 0);
                        level.addFreshEntity(milk);

                        PrankManager.getInstance().scheduleTask("Milk_Boom_" + t, 20, () -> {
                            level.explode(null, milk.getX(), milk.getY(), milk.getZ(), 1.5f, Level.ExplosionInteraction.NONE);
                            level.sendParticles(ParticleTypes.SPLASH, milk.getX(), milk.getY(), milk.getZ(), 25, 0.5, 0.5, 0.5, 0.1);
                        });
                    }
                }
            });
        }

        PrankManager.getInstance().scheduleTask("HeliCow_Despawn", 300, () -> {
            if (cow.isAlive()) cow.discard();
        });
    }

    // =========================================================================
    // 48. РЕЖИМ РЭГДОЛЛА
    // На 8 сек моделька игрока ложится горизонтально, отключается управление
    // =========================================================================
    public static void executePrank48_RagdollMode(List<ServerPlayer> players) {
        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                48, 160, 0, 0, 1.0f, 0f, "RAGDOLL"
        ));

        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.WOOL_FALL, SoundSource.PLAYERS, 1.5f, 0.7f);
            Vec3 roll = new Vec3((RANDOM.nextDouble() - 0.5) * 1.5, 0.3, (RANDOM.nextDouble() - 0.5) * 1.5);
            player.setDeltaMovement(roll);
            player.hurtMarked = true;
        }

        PrankManager.getInstance().broadcastMessage("§7§l[!] РЕЖИМ РЭГДОЛЛА: Отказ опорно-двигательного аппарата на 8 секунд!");
    }

    // =========================================================================
    // 49. ПЛАВАНЬЕ ПО ВОЗДУХУ
    // На 20 сек игроки переходят в позу плавания и плывут по воздуху
    // =========================================================================
    public static void executePrank49_AirSwimming(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 400, 0, false, false, false));
            player.playNotifySound(SoundEvents.PLAYER_SWIM, SoundSource.PLAYERS, 1.2f, 1.0f);
        }

        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                49, 400, 0, 0, 1.0f, 0f, "AIR_SWIM"
        ));

        PrankManager.getInstance().broadcastMessage("§9§l[!] ПЛАВАНЬЕ ПО ВОЗДУХУ: Гравитация выключена, гребите руками (20 сек)!");

        // Плавный спуск по окончанию
        PrankManager.getInstance().scheduleTask("AirSwim_End", 400, () -> {
            for (ServerPlayer player : players) {
                player.removeEffect(MobEffects.LEVITATION);
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 80, 0, false, false, false));
            }
        });
    }

    // =========================================================================
    // 50. ВОЗДУШНЫЙ ШАРИК
    // Игрок раздувается в шар и медленно всплывает, при ударе летает зигзагами
    // =========================================================================
    public static void executePrank50_BalloonPlayer(List<ServerPlayer> players) {
        ServerPlayer balloon = players.get(RANDOM.nextInt(players.size()));

        balloon.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 300, 1, false, false, false));
        balloon.playNotifySound(SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 2.0f, 1.8f);

        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                50, 300, balloon.getId(), 0, 3.0f, 0f, "BALLOON"
        ));

        PrankManager.getInstance().broadcastMessage("§d§l[!] ВОЗДУШНЫЙ ШАРИК: " + balloon.getScoreboardName() + " раздулся в шар и улетает в стратосферу!");

        // Мягкое приземление
        PrankManager.getInstance().scheduleTask("Balloon_End", 300, () -> {
            balloon.removeEffect(MobEffects.LEVITATION);
            balloon.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, false, false));
        });
    }

    // =========================================================================
    // 51. ПОБЕГ ДОМА (ОЖИВШИЕ БЛОКИ)
    // 15 блоков пола/стен превращаются в падающие блоки, разбегаются на 10 сек и возвращаются
    // =========================================================================
    public static void executePrank51_HouseEscape(List<ServerPlayer> players) {
        ServerPlayer anchor = players.get(0);
        ServerLevel level = anchor.serverLevel();
        BlockPos center = anchor.blockPosition();

        Map<BlockPos, BlockState> originalBlocks = new HashMap<>();
        List<FallingBlockEntity> runningBlocks = new ArrayList<>();

        for (int x = -3; x <= 3; x += 2) {
            for (int z = -3; z <= 3; z += 2) {
                BlockPos targetPos = center.offset(x, -1, z);
                BlockState state = level.getBlockState(targetPos);

                if (!state.isAir() && state.getBlock() != Blocks.BEDROCK) {
                    originalBlocks.put(targetPos, state);
                    level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);

                    FallingBlockEntity fb = FallingBlockEntity.fall(level, targetPos.above(), state);
                    fb.setDeltaMovement((RANDOM.nextDouble() - 0.5) * 0.8, 0.4, (RANDOM.nextDouble() - 0.5) * 0.8);
                    runningBlocks.add(fb);
                }
            }
        }

        PrankManager.getInstance().broadcastMessage("§6§l[!] ПОБЕГ ДОМА: Ваши блоки ожили и разбегаются в разные стороны (10 сек)!");

        // Точный откат всех блоков на свои координаты ровно через 10 секунд (200 тиков)
        PrankManager.getInstance().scheduleTask("HouseEscape_Rollback", 200, () -> {
            for (FallingBlockEntity fb : runningBlocks) {
                if (fb.isAlive()) fb.discard();
            }
            for (Map.Entry<BlockPos, BlockState> entry : originalBlocks.entrySet()) {
                level.setBlock(entry.getKey(), entry.getValue(), 3);
            }
            level.playSound(null, center.getX(), center.getY(), center.getZ(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.2f, 1.2f);
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] Ожившие блоки вернулись на свои места.");
        });
    }

    // =========================================================================
    // 52. БЛОКИ-БАТУТЫ
    // На 20 сек любое касание земли или стен отпружинивает игрока с множителем 1.8
    // =========================================================================
    public static void executePrank52_TrampolineBlocks(List<ServerPlayer> players) {
        PrankManager.getInstance().broadcastMessage("§a§l[!] БЛОКИ-БАТУТЫ: Все поверхности стали супер-батутами на 20 секунд!");

        for (int tick = 1; tick <= 400; tick++) {
            PrankManager.getInstance().scheduleTask("Trampoline_Tick_" + tick, tick, () -> {
                for (ServerPlayer player : players) {
                    if (player.onGround()) {
                        Vec3 m = player.getDeltaMovement();
                        player.setDeltaMovement(m.x * 1.3, 0.95, m.z * 1.3);
                        player.hurtMarked = true;
                        player.playNotifySound(SoundEvents.SLIME_BLOCK_FALL, SoundSource.PLAYERS, 1.2f, 1.3f);
                    }
                }
            });
        }
    }

    // =========================================================================
    // 53. ПРЕДМЕТЫ-ПРЕДАТЕЛИ
    // Выброшенные предметы оживают, летят в сторону игроков и кусают их
    // =========================================================================
    public static void executePrank53_TraitorItems(List<ServerPlayer> players) {
        ServerPlayer target = players.get(0);
        ServerLevel level = target.serverLevel();
        List<ItemEntity> traitors = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            ItemEntity item = new ItemEntity(level, target.getX() + (RANDOM.nextDouble() - 0.5) * 6, target.getY() + 1.0, target.getZ() + (RANDOM.nextDouble() - 0.5) * 6, new ItemStack(Items.IRON_SWORD));
            level.addFreshEntity(item);
            traitors.add(item);
        }

        PrankManager.getInstance().broadcastMessage("§4§l[!] ПРЕДМЕТЫ-ПРЕДАТЕЛИ: Лежащий лут ожил и атакует игроков (15 сек)!");

        for (int tick = 1; tick <= 300; tick++) {
            PrankManager.getInstance().scheduleTask("Traitor_Tick_" + tick, tick, () -> {
                for (ItemEntity item : traitors) {
                    if (item.isAlive()) {
                        Vec3 toPlayer = target.position().subtract(item.position()).normalize().scale(0.35);
                        item.setDeltaMovement(toPlayer);

                        if (item.distanceTo(target) < 1.2) {
                            target.hurt(target.damageSources().generic(), 0.5f);
                            target.playNotifySound(SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 0.8f, 1.5f);
                        }
                    }
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Traitor_Cleanup", 300, () -> {
            for (ItemEntity item : traitors) {
                if (item.isAlive()) item.discard();
            }
        });
    }

    // =========================================================================
    // 54. ЛЕДЯНАЯ ГОРКА В АД
    // Игрока сажает в лодку, перед ним строится трасса из синего льда (с откатом)
    // =========================================================================
    public static void executePrank54_IceSlideToHell(List<ServerPlayer> players) {
        ServerPlayer player = players.get(0);
        ServerLevel level = player.serverLevel();
        BlockPos start = player.blockPosition();

        Map<BlockPos, BlockState> iceBackup = new HashMap<>();

        // Строим спуск из синего льда на 20 блоков вперед и вниз
        Vec3 look = player.getLookAngle();
        int dx = (int) Math.signum(look.x);
        int dz = (int) Math.signum(look.z);
        if (dx == 0 && dz == 0) dx = 1;

        for (int step = 0; step < 20; step++) {
            BlockPos icePos = start.offset(dx * step, -step / 2, dz * step).below();
            iceBackup.put(icePos, level.getBlockState(icePos));
            level.setBlock(icePos, Blocks.BLUE_ICE.defaultBlockState(), 3);
        }

        // Спавним лодку и сажаем игрока
        Boat boat = EntityType.BOAT.create(level);
        if (boat != null) {
            boat.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0);
            level.addFreshEntity(boat);
            player.startRiding(boat, true);
            boat.setDeltaMovement(dx * 1.5, -0.4, dz * 1.5);
        }

        PrankManager.getInstance().broadcastMessage("§b§l[!] ЛЕДЯНАЯ ГОРКА: Поехали на лодке по синему льду с ветерком (12 сек)!");

        // Откат льда ровно через 12 секунд (240 тиков)
        PrankManager.getInstance().scheduleTask("IceSlide_Rollback", 240, () -> {
            if (boat != null && boat.isAlive()) {
                player.stopRiding();
                boat.discard();
            }
            for (Map.Entry<BlockPos, BlockState> entry : iceBackup.entrySet()) {
                level.setBlock(entry.getKey(), entry.getValue(), 3);
            }
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] Ледяная горка растаяла, исходный рельеф восстановлен.");
        });
    }

    // =========================================================================
    // 55. КЛОУНСКИЕ ШАГИ
    // На 30 сек каждый шаг игрока издает громкий резиновый писк на 50 блоков
    // =========================================================================
    public static void executePrank55_ClownSteps(List<ServerPlayer> players) {
        PrankManager.getInstance().broadcastMessage("§e§l[!] КЛОУНСКИЕ ШАГИ: Каждый ваш шаг пищит как резиновый утенок на 50 блоков (30 сек)!");

        for (int tick = 1; tick <= 600; tick++) {
            PrankManager.getInstance().scheduleTask("ClownSteps_Tick_" + tick, tick, () -> {
                for (ServerPlayer player : players) {
                    if (player.walkDist - player.prevWalkDist > 0.05) {
                        if (player.tickCount % 5 == 0) {
                            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SLIME_SQUISH_SMALL, SoundSource.PLAYERS, 2.0f, 1.9f);
                        }
                    }
                }
            });
        }
    }

    // =========================================================================
    // 56. ФЕЙКОВЫЙ ДОНАТ
    // Оверлей доната со звуком, спавн золотых самородков, расталкивающих игроков
    // =========================================================================
    public static void executePrank56_FakeDonation(List<ServerPlayer> players) {
        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                56, 160, 0, 0, 1.0f, 0f, "DONATION_ALERT"
        ));

        for (ServerPlayer player : players) {
            ServerLevel level = player.serverLevel();
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 2.0f, 1.2f);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.get(), SoundSource.PLAYERS, 2.0f, 1.8f);

            for (int i = 0; i < 20; i++) {
                ItemEntity gold = new ItemEntity(level, player.getX(), player.getY() + 1.5, player.getZ(), new ItemStack(Items.GOLD_NUGGET));
                gold.setDeltaMovement((RANDOM.nextDouble() - 0.5) * 0.8, 0.4, (RANDOM.nextDouble() - 0.5) * 0.8);
                level.addFreshEntity(gold);

                PrankManager.getInstance().scheduleTask("Gold_Despawn_" + gold.getId(), 160, gold::discard);
            }
        }

        PrankManager.getInstance().broadcastMessage("§6§l[!] ДОНАТ: 'Аноним задонатил 1 000 000 рублей: Удачи выжить!'");
    }

    // =========================================================================
    // 57. СУДЕБНОЕ ЗАСЕДАНИЕ
    // Структура зала суда в воздухе: судья бьет молотком, открывая люки под клеткой
    // =========================================================================
    public static void executePrank57_CourtTrial(List<ServerPlayer> players) {
        if (players.size() < 2) return;

        ServerPlayer judge = players.get(0);
        ServerLevel level = judge.serverLevel();
        BlockPos basePos = judge.blockPosition().above(12);

        Map<BlockPos, BlockState> courtStructure = new HashMap<>();

        // Строим мини-зал суда
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos floorPos = basePos.offset(x, 0, z);
                courtStructure.put(floorPos, level.getBlockState(floorPos));

                if (x >= 0) {
                    // Пол под клеткой подсудимых из дубовых люков
                    level.setBlock(floorPos, Blocks.OAK_TRAPDOOR.defaultBlockState(), 3);
                } else {
                    // Кафедра судьи
                    level.setBlock(floorPos, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), 3);
                }
            }
        }

        // Телепортируем судью на кафедру
        judge.teleportTo(level, basePos.getX() - 1.5, basePos.getY() + 1, basePos.getZ() + 0.5, -90, 0);

        // Телепортируем остальных подсудимых на люки
        for (int i = 1; i < players.size(); i++) {
            ServerPlayer prisoner = players.get(i);
            prisoner.teleportTo(level, basePos.getX() + 1.5, basePos.getY() + 1, basePos.getZ() + 0.5, 90, 0);
        }

        PrankManager.getInstance().broadcastMessage("§4§l[!] СУДЕБНОЕ ЗАСЕДАНИЕ: Судья §e" + judge.getScoreboardName() + "§4 решает судьбу подсудимых (20 сек)!");

        // Через 7 секунд судья выносит приговор: стук молотка и открытие люков
        PrankManager.getInstance().scheduleTask("Court_Verdict", 140, () -> {
            level.playSound(null, basePos.getX(), basePos.getY(), basePos.getZ(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 2.0f, 1.2f);
            PrankManager.getInstance().broadcastMessage("§c§l[!] ПРИГОВОР ВЫНЕСЕН: ВИНОВНЫ! Люки открываются!");

            for (int x = 0; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos p = basePos.offset(x, 0, z);
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                }
            }

            // Плавное падение, чтобы не разбились
            for (int i = 1; i < players.size(); i++) {
                players.get(i).addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0, false, false, false));
            }
        });

        // Откат всей структуры через 20 секунд (400 тиков)
        PrankManager.getInstance().scheduleTask("Court_Rollback", 400, () -> {
            for (Map.Entry<BlockPos, BlockState> entry : courtStructure.entrySet()) {
                level.setBlock(entry.getKey(), entry.getValue(), 3);
            }
            judge.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 80, 0, false, false, false));
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] Зал суда закрыт, исходные блоки восстановлены.");
        });
    }

    // =========================================================================
    // 58. ПОГОНЯ ДПС
    // 2 Голема с красно-синей мигалкой и звуком сирены бегут со Скоростью 3
    // =========================================================================
    public static void executePrank58_PoliceChase(List<ServerPlayer> players) {
        ServerPlayer suspect = players.get(0);
        ServerLevel level = suspect.serverLevel();
        List<IronGolem> police = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            IronGolem golem = EntityType.IRON_GOLEM.create(level);
            if (golem != null) {
                golem.moveTo(suspect.getX() + (i == 0 ? 8 : -8), suspect.getY(), suspect.getZ(), 0, 0);
                golem.setCustomName(net.minecraft.network.chat.Component.literal("§9§lЭКИПАЖ ДПС #" + (i + 1)));
                golem.setCustomNameVisible(true);
                golem.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 360, 2, false, false, false));
                level.addFreshEntity(golem);
                police.add(golem);
            }
        }

        PrankManager.getInstance().broadcastMessage("§9§l[!] ДПС: 'Водитель, примите вправо и заглушите двигатель!' Погоня 18 секунд!");

        for (int tick = 1; tick <= 360; tick++) {
            final int t = tick;
            PrankManager.getInstance().scheduleTask("Police_Tick_" + tick, tick, () -> {
                for (IronGolem g : police) {
                    if (g.isAlive()) {
                        // Красно-синяя мигалка
                        level.sendParticles(t % 10 < 5 ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME, g.getX(), g.getY() + 2.5, g.getZ(), 5, 0.2, 0.1, 0.2, 0.05);

                        if (t % 20 == 0) {
                            level.playSound(null, g.getX(), g.getY(), g.getZ(), SoundEvents.RAID_HORN.get(), SoundSource.HOSTILE, 2.0f, 1.6f);
                        }

                        if (g.distanceTo(suspect) < 2.5) {
                            suspect.setDeltaMovement(suspect.getDeltaMovement().add(0, 0.4, 0));
                            suspect.hurtMarked = true;
                        }
                    }
                }
            });
        }

        PrankManager.getInstance().scheduleTask("Police_Despawn", 360, () -> {
            for (IronGolem g : police) {
                if (g.isAlive()) g.discard();
            }
        });
    }

    // =========================================================================
    // 59. ВСПЛЫВАЮЩАЯ РЕКЛАМА
    // В 1.5 блоках перед лицом висит баннер, пока игрок не нанесет 3 удара
    // =========================================================================
    public static void executePrank59_PopupAd(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            ServerLevel level = player.serverLevel();
            ArmorStand adStand = EntityType.ARMOR_STAND.create(level);
            if (adStand != null) {
                Vec3 look = player.getLookAngle();
                adStand.moveTo(player.getX() + look.x * 1.5, player.getY() + 0.5, player.getZ() + look.z * 1.5, player.getYRot(), 0);
                adStand.setCustomName(net.minecraft.network.chat.Component.literal("§c§l[РЕКЛАМА] КУПИ АЛМАЗЫ! [УДАРЬ 3 РАЗА ДЛЯ ЗАКРЫТИЯ]"));
                adStand.setCustomNameVisible(true);
                adStand.setInvisible(true);
                adStand.setNoGravity(true);
                level.addFreshEntity(adStand);

                player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.5f, 0.5f);

                // Авто-удаление через 15 секунд
                PrankManager.getInstance().scheduleTask("Ad_AutoRemove_" + adStand.getId(), 300, () -> {
                    if (adStand.isAlive()) adStand.discard();
                });
            }
        }

        PrankManager.getInstance().broadcastMessage("§c§l[!] ВСПЛЫВАЮЩАЯ РЕКЛАМА: Закройте рекламный баннер перед лицом!");
    }

    // =========================================================================
    // 60. МАГИЧЕСКИЙ ПЕНДЕЛЬ
    // С неба спускается сапог и пинает игрока на 50 блоков вперед
    // =========================================================================
    public static void executePrank60_MagicKick(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            Vec3 look = player.getLookAngle();
            Vec3 kick = look.scale(4.5).add(0, 0.8, 0);

            player.playNotifySound(SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 2.5f, 0.7f);
            player.playNotifySound(SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.8f);

            player.setDeltaMovement(kick);
            player.hurtMarked = true;
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, false, false));

            if (player.serverLevel() != null) {
                player.serverLevel().sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.3, 0.3, 0.3, 0.1);
            }
        }

        PrankManager.getInstance().broadcastMessage("§6§l[!] МАГИЧЕСКИЙ ПЕНДЕЛЬ: Гигантский сапог с неба отправил всех в полет на 50 блоков!");
    }

    // =========================================================================
    // 61. КЛОНИРОВАНИЕ ПРИ УДАРЕ (МИТОЗ)
    // На 20 сек любой моб при получении урона спавнит свою точную копию
    // =========================================================================
    public static void executePrank61_MitosisOnDamage(List<ServerPlayer> players) {
        PrankManager.getInstance().broadcastMessage("§a§l[!] МИТОЗ: На 20 секунд любой ударенный моб делится пополам!");

        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.SLIME_SPLQUISH, SoundSource.PLAYERS, 1.5f, 1.0f);
        }
    }

    // =========================================================================
    // 62. ХОРРОР-РЕЖИМ (SLENDERMAN)
    // Кромешная тьма, луч света, помехи на экране и высокий силуэт за деревьями
    // =========================================================================
    public static void executePrank62_SlendermanHorror(List<ServerPlayer> players) {
        ServerPlayer target = players.get(0);
        ServerLevel level = target.serverLevel();

        // Накладываем слепоту и тьму
        for (ServerPlayer player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 400, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 400, 1, false, false, true));
            player.playNotifySound(SoundEvents.AMBIENT_CAVE.get(), SoundSource.AMBIENT, 2.0f, 0.6f);
        }

        // Спавним Слендермена (высокий Эндермен с белым черепом)
        EnderMan slenderman = EntityType.ENDERMAN.create(level);
        if (slenderman != null) {
            slenderman.moveTo(target.getX() + 14.0, target.getY(), target.getZ() + 14.0, 0, 0);
            slenderman.setCustomName(net.minecraft.network.chat.Component.literal("§8§lSLENDERMAN"));
            slenderman.setCustomNameVisible(true);
            level.addFreshEntity(slenderman);

            PacketHandler.sendToAll(new ClientPrankPacket(
                    ClientPrankPacket.ActionType.START,
                    62, 400, slenderman.getId(), 0, 1.0f, 0f, "SLENDERMAN_VHS"
            ));

            // Телепортация за спину каждые 80 тиков (4 секунды)
            for (int tp = 1; tp <= 4; tp++) {
                PrankManager.getInstance().scheduleTask("Slender_TP_" + tp, tp * 80, () -> {
                    if (slenderman.isAlive()) {
                        Vec3 behind = target.position().subtract(target.getLookAngle().scale(4.0));
                        slenderman.teleportTo(behind.x, behind.y, behind.z);
                        level.playSound(null, behind.x, behind.y, behind.z, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 2.5f, 0.8f);
                    }
                });
            }

            PrankManager.getInstance().scheduleTask("Slenderman_Despawn", 400, () -> {
                if (slenderman.isAlive()) slenderman.discard();
            });
        }

        PrankManager.getInstance().broadcastMessage("§0§l[!] ХОРРОР: Кромешная тьма... Он смотрит на вас из-за деревьев (20 сек)!");
    }
}