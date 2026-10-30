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
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class PranksBatch1 {

    private static final Random RANDOM = new Random();

    /**
     * Главный маршрутизатор выполнения пранков с 1 по 20
     */
    public static void execute(int prankId, List<ServerPlayer> players) {
        if (players.isEmpty()) return;

        switch (prankId) {
            case 1 -> executePrank1_RookSwap(players);
            case 2 -> executePrank2_ChainReaction(players);
            case 3 -> executePrank3_BloodBond(players);
            case 4 -> executePrank4_NuclearSneeze(players);
            case 5 -> executePrank5_RepellingPoles(players);
            case 6 -> executePrank6_BlindFury(players);
            case 7 -> executePrank7_ControlInversion(players);
            case 8 -> executePrank8_ShotgunRecoil(players);
            case 9 -> executePrank9_Tornado(players);
            case 10 -> executePrank10_SupersonicKick(players);
            case 11 -> executePrank11_AnvilRain(players);
            case 12 -> executePrank12_Minefield(players);
            case 13 -> executePrank13_BouncyTntRain(players);
            case 14 -> executePrank14_BedrockCage(players);
            case 15 -> executePrank15_PiglinSwat(players);
            case 16 -> executePrank16_GhastAirStrike(players);
            case 17 -> executePrank17_SilverfishCarpet(players);
            case 18 -> executePrank18_GiantZombie(players);
            case 19 -> executePrank19_EnderKidnapper(players);
            case 20 -> executePrank20_MagmaCubePress(players);
            default -> ChaosPranksMod.LOGGER.warn("[ChaosPranks] Unknown prank ID for Batch1: " + prankId);
        }
    }

    // =========================================================================
    // 1. РОКИРОВКА НАД БЕЗДНОЙ
    // Мгновенный обмен координатами по кругу между всеми игроками
    // =========================================================================
    public static void executePrank1_RookSwap(List<ServerPlayer> players) {
        int n = players.size();
        if (n < 2) return;

        List<Vec3> positions = new ArrayList<>();
        List<Float> yaws = new ArrayList<>();
        List<Float> pitches = new ArrayList<>();

        for (ServerPlayer player : players) {
            positions.add(player.position());
            yaws.add(player.getYRot());
            pitches.add(player.getXRot());
        }

        // Круговой сдвиг: игрок i получает координаты игрока (i + 1) % n
        for (int i = 0; i < n; i++) {
            ServerPlayer player = players.get(i);
            int nextIndex = (i + 1) % n;
            Vec3 targetPos = positions.get(nextIndex);
            float targetYaw = yaws.get(nextIndex);
            float targetPitch = pitches.get(nextIndex);

            ServerLevel level = player.serverLevel();
            level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 0.5, 0.5, 0.1);

            player.teleportTo(level, targetPos.x, targetPos.y, targetPos.z, targetYaw, targetPitch);
            player.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);

            level.sendParticles(ParticleTypes.REVERSE_PORTAL, targetPos.x, targetPos.y + 1.0, targetPos.z, 30, 0.5, 0.5, 0.5, 0.1);
        }

        PrankManager.getInstance().broadcastMessage("§b[!] Рокировка! Все игроки поменялись местами по кругу!");
    }

    // =========================================================================
    // 2. ЦЕПНАЯ РЕАКЦИЯ
    // Всех телепортирует к случайному другу + спавн ТНТ (таймер 40 тиков)
    // =========================================================================
    public static void executePrank2_ChainReaction(List<ServerPlayer> players) {
        ServerPlayer anchor = players.get(RANDOM.nextInt(players.size()));
        ServerLevel level = anchor.serverLevel();
        Vec3 targetPos = anchor.position();

        for (ServerPlayer player : players) {
            player.teleportTo(level, targetPos.x + (RANDOM.nextDouble() - 0.5) * 2.0, targetPos.y, targetPos.z + (RANDOM.nextDouble() - 0.5) * 2.0, player.getYRot(), player.getXRot());
            player.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        // Спавн зажженного ТНТ в центре кучи с таймером 40 тиков (2 секунды)
        PrimedTnt tnt = new PrimedTnt(level, targetPos.x, targetPos.y + 1.0, targetPos.z, null);
        tnt.setFuse(40);
        level.addFreshEntity(tnt);

        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.2f, 1.0f);
        }

        PrankManager.getInstance().broadcastMessage("§c§l[!] ЦЕПНАЯ РЕАКЦИЯ! Все собраны в точку, ТНТ уже шипит (2 сек)!");
    }

    // =========================================================================
    // 3. КРОВАВАЯ СВЯЗЬ
    // Физическая резинка между игроками на 15 сек: при дистанции > 6 блоков тянет друг к другу
    // =========================================================================
    public static void executePrank3_BloodBond(List<ServerPlayer> players) {
        if (players.size() < 2) return;

        PrankManager.getInstance().broadcastMessage("§4§l[!] КРОВАВАЯ СВЯЗЬ: Вы связаны невидимой резинкой на 15 секунд!");

        // Планируем периодическую задачу на 300 тиков (15 сек)
        for (int tick = 1; tick <= 300; tick++) {
            PrankManager.getInstance().scheduleTask("BloodBond_Tick_" + tick, tick, () -> {
                for (int i = 0; i < players.size(); i++) {
                    ServerPlayer p1 = players.get(i);
                    ServerPlayer p2 = players.get((i + 1) % players.size());

                    if (p1.isAlive() && p2.isAlive() && p1.level() == p2.level()) {
                        double dist = p1.distanceTo(p2);
                        if (dist > 6.0) {
                            Vec3 pullVector = p2.position().subtract(p1.position()).normalize().scale(0.35);
                            p1.setDeltaMovement(p1.getDeltaMovement().add(pullVector));
                            p1.hurtMarked = true;

                            // Красные частицы цепи
                            if (p1.serverLevel() != null) {
                                p1.serverLevel().sendParticles(ParticleTypes.CRIMSON_SPORE, p1.getX(), p1.getY() + 1.0, p1.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
                            }
                        }
                    }
                }
            });
        }
    }

    // =========================================================================
    // 4. ЯДЕРНЫЙ ЧИХ
    // Чих одного игрока сносит всех в радиусе 20 блоков на 30 блоков назад
    // =========================================================================
    public static void executePrank4_NuclearSneeze(List<ServerPlayer> players) {
        ServerPlayer sneezer = players.get(RANDOM.nextInt(players.size()));
        ServerLevel level = sneezer.serverLevel();
        Vec3 sneezePos = sneezer.position();

        // Звук мощного взрыва и чиха
        level.playSound(null, sneezePos.x, sneezePos.y, sneezePos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0f, 1.8f);
        level.playSound(null, sneezePos.x, sneezePos.y, sneezePos.z, SoundEvents.PANDA_SNEEZE, SoundSource.PLAYERS, 2.5f, 0.8f);

        // Ударная волна частиц
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, sneezePos.x, sneezePos.y + 1.0, sneezePos.z, 5, 1.0, 1.0, 1.0, 0.2);

        for (ServerPlayer victim : players) {
            if (victim.getId() != sneezer.getId() && victim.level() == level) {
                double dist = victim.distanceTo(sneezer);
                if (dist <= 20.0) {
                    Vec3 knockback = victim.position().subtract(sneezePos).normalize().scale(3.2).add(0, 0.5, 0);
                    victim.setDeltaMovement(knockback);
                    victim.hurtMarked = true;

                    // Выдаем плавное падение на 4 сек, чтобы игрок не разбился насмерть
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 80, 0, false, false, false));
                }
            }
        }

        PrankManager.getInstance().broadcastMessage("§e§l" + sneezer.getScoreboardName() + " §cапчхикнул ядерной волной! Всех снесло!");
    }

    // =========================================================================
    // 5. ОТТАЛКИВАЮЩИЙ ПОЛЮС
    // На 30 сек при сближении ближе 4 блоков друзей с силой отталкивает
    // =========================================================================
    public static void executePrank5_RepellingPoles(List<ServerPlayer> players) {
        PrankManager.getInstance().broadcastMessage("§b[!] ОТТАЛКИВАЮЩИЙ ПОЛЮС: Включена магнитная полярность на 30 секунд!");

        for (int tick = 1; tick <= 600; tick++) {
            PrankManager.getInstance().scheduleTask("Repelling_Tick_" + tick, tick, () -> {
                for (int i = 0; i < players.size(); i++) {
                    for (int j = i + 1; j < players.size(); j++) {
                        ServerPlayer p1 = players.get(i);
                        ServerPlayer p2 = players.get(j);

                        if (p1.isAlive() && p2.isAlive() && p1.level() == p2.level()) {
                            double dist = p1.distanceTo(p2);
                            if (dist < 4.0 && dist > 0.05) {
                                Vec3 repel = p1.position().subtract(p2.position()).normalize().scale(0.85);
                                p1.setDeltaMovement(p1.getDeltaMovement().add(repel.scale(0.5)));
                                p2.setDeltaMovement(p2.getDeltaMovement().subtract(repel.scale(0.5)));
                                p1.hurtMarked = true;
                                p2.hurtMarked = true;

                                if (tick % 20 == 0) {
                                    p1.playNotifySound(SoundEvents.CONDUIT_DEACTIVATE, SoundSource.PLAYERS, 0.8f, 1.5f);
                                    p2.playNotifySound(SoundEvents.CONDUIT_DEACTIVATE, SoundSource.PLAYERS, 0.8f, 1.5f);
                                    p1.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, p1.getX(), p1.getY() + 1.0, p1.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    // =========================================================================
    // 6. СЛЕПАЯ ЯРОСТЬ
    // Слепота на 10 сек, телепорт в кучу, рандомный поворот камеры и фейерверки
    // =========================================================================
    public static void executePrank6_BlindFury(List<ServerPlayer> players) {
        ServerPlayer anchor = players.get(RANDOM.nextInt(players.size()));
        ServerLevel level = anchor.serverLevel();
        Vec3 center = anchor.position();

        for (ServerPlayer player : players) {
            // Накладываем слепоту на 10 секунд (200 тиков)
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 1, false, false, true));

            // Телепортируем в кучу (радиус до 3 блоков) со случайным разворотом головы
            float randomYaw = RANDOM.nextFloat() * 360.0f;
            float randomPitch = (RANDOM.nextFloat() * 120.0f) - 60.0f;
            double offsetX = (RANDOM.nextDouble() - 0.5) * 3.0;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 3.0;

            player.teleportTo(level, center.x + offsetX, center.y, center.z + offsetZ, randomYaw, randomPitch);

            // Спавн фейерверка
            ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);
            FireworkRocketEntity rocket = new FireworkRocketEntity(level, center.x + offsetX, center.y + 1.0, center.z + offsetZ, fireworkStack);
            level.addFreshEntity(rocket);
        }

        PrankManager.getInstance().broadcastMessage("§8§l[!] СЛЕПАЯ ЯРОСТЬ: Ничего не видно, камера сбита, фейерверки взрываются!");
    }

    // =========================================================================
    // 7. ИНВЕРСИЯ УПРАВЛЕНИЯ
    // На 20 сек на клиенте инвертируются WASD и оси мыши
    // =========================================================================
    public static void executePrank7_ControlInversion(List<ServerPlayer> players) {
        PacketHandler.sendToAll(new ClientPrankPacket(
                ClientPrankPacket.ActionType.START,
                7,
                400, // 20 секунд = 400 тиков
                players.get(0).getId(),
                0, 1.0f, 0f, "INVERT_CONTROLS"
        ));

        for (ServerPlayer player : players) {
            player.playNotifySound(SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 0.7f, 1.2f);
        }

        PrankManager.getInstance().broadcastMessage("§d§l[!] ИНВЕРСИЯ УПРАВЛЕНИЯ: Ваши клавиши и мышь перевернуты на 20 секунд!");
    }

    // =========================================================================
    // 8. ОТДАЧА ДРОБОВИКА
    // На 20 сек любой взмах или удар отшвыривает самого игрока назад на 8 блоков
    // =========================================================================
    public static void executePrank8_ShotgunRecoil(List<ServerPlayer> players) {
        PrankManager.getInstance().broadcastMessage("§6§l[!] ОТДАЧА ДРОБОВИКА: Осторожно с ударами! Отдача на 20 секунд активирована!");

        for (int tick = 1; tick <= 400; tick++) {
            PrankManager.getInstance().scheduleTask("ShotgunRecoil_Tick_" + tick, tick, () -> {
                for (ServerPlayer player : players) {
                    // Проверяем факт взмаха руки игрока
                    if (player.swinging && player.swingTime == 1) {
                        Vec3 look = player.getLookAngle();
                        Vec3 recoil = new Vec3(-look.x * 2.2, 0.45, -look.z * 2.2);
                        player.setDeltaMovement(recoil);
                        player.hurtMarked = true;

                        player.playNotifySound(SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.2f, 1.9f);
                        if (player.serverLevel() != null) {
                            player.serverLevel().sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 1.2, player.getZ(), 10, 0.2, 0.2, 0.2, 0.05);
                        }
                    }
                }
            });
        }
    }

    // =========================================================================
    // 9. ТОРНАДО
    // На 12 сек вихрь частиц закручивает игрока по спирали вверх и разбрасывает мобов
    // =========================================================================
    public static void executePrank9_Tornado(List<ServerPlayer> players) {
        PrankManager.getInstance().broadcastMessage("§f§l[!] ТОРНАДО! Мощный вихрь закручивает всех в небо на 12 секунд!");

        for (int tick = 1; tick <= 240; tick++) {
            final int currentTick = tick;
            PrankManager.getInstance().scheduleTask("Tornado_Tick_" + tick, tick, () -> {
                for (ServerPlayer player : players) {
                    if (player.isAlive()) {
                        double angle = currentTick * 0.3;
                        double radius = 1.2;
                        double cos = Math.cos(angle) * radius;
                        double sin = Math.sin(angle) * radius;

                        // Спиральный подъем
                        player.setDeltaMovement(cos * 0.4, 0.32, sin * 0.4);
                        player.hurtMarked = true;

                        ServerLevel level = player.serverLevel();
                        level.sendParticles(ParticleTypes.CLOUD, player.getX() + cos, player.getY() + (currentTick % 20) * 0.1, player.getZ() + sin, 4, 0.1, 0.1, 0.1, 0.05);

                        // Разбрасываем сущностей вокруг
                        List<Entity> nearby = level.getEntities(player, player.getBoundingBox().inflate(6.0));
                        for (Entity e : nearby) {
                            if (!(e instanceof ServerPlayer)) {
                                Vec3 fling = e.position().subtract(player.position()).normalize().scale(1.2).add(0, 0.4, 0);
                                e.setDeltaMovement(fling);
                            }
                        }
                    }
                }
            });
        }

        // По окончанию 12 секунд даем плавное падение для мягкой посадки
        PrankManager.getInstance().scheduleTask("Tornado_Landing", 241, () -> {
            for (ServerPlayer player : players) {
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0, false, false, false));
            }
        });
    }

    // =========================================================================
    // 10. СВЕРХЗВУКОВОЙ ПИНОК
    // Звук поршня за спиной и запуск вперед по вектору взгляда со сверхзвуковой скоростью
    // =========================================================================
    public static void executePrank10_SupersonicKick(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            Vec3 look = player.getLookAngle();
            Vec3 kick = look.scale(3.8).add(0, 0.3, 0);

            player.playNotifySound(SoundEvents.PISTON_EXTEND, SoundSource.PLAYERS, 2.0f, 0.6f);
            player.playNotifySound(SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.5f);

            player.setDeltaMovement(kick);
            player.hurtMarked = true;

            // Защита от разбивания о стену на 3 секунды
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 4, false, false, false));

            if (player.serverLevel() != null) {
                player.serverLevel().sendParticles(ParticleTypes.SONIC_BOOM, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0, 0, 0, 0);
            }
        }

        PrankManager.getInstance().broadcastMessage("§e§l[!] ПИНОК СВЕРХЗВУКОМ! Все улетели вперед со скоростью света!");
    }

    // =========================================================================
    // 11. ДОЖДЬ ИЗ НАКОВАЛЕН
    // Сетка из 20 падающих наковален на высоте +15 блоков с откатом
    // =========================================================================
    public static void executePrank11_AnvilRain(List<ServerPlayer> players) {
        PrankManager.getInstance().broadcastMessage("§7§l[!] СВИСТ В НЕБЕ: Дождь из наковален! Смотрите наверх!");

        List<BlockPos> spawnedPositions = new ArrayList<>();

        for (ServerPlayer player : players) {
            ServerLevel level = player.serverLevel();
            BlockPos basePos = player.blockPosition();

            for (int i = 0; i < 6; i++) {
                int ox = RANDOM.nextInt(9) - 4;
                int oz = RANDOM.nextInt(9) - 4;
                BlockPos anvilPos = basePos.offset(ox, 15, oz);

                if (level.isEmptyBlock(anvilPos)) {
                    FallingBlockEntity.fall(level, anvilPos, Blocks.DAMAGED_ANVIL.defaultBlockState());
                    spawnedPositions.add(anvilPos);
                }
            }

            player.playNotifySound(SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.5f, 0.5f);
        }

        // Авто-удаление упавших наковален через 12 секунд для предотвращения захламления карты
        PrankManager.getInstance().scheduleTask("AnvilRain_Cleanup", 240, () -> {
            for (ServerPlayer player : players) {
                ServerLevel level = player.serverLevel();
                BlockPos center = player.blockPosition();
                for (int x = -10; x <= 10; x++) {
                    for (int y = -5; y <= 5; y++) {
                        for (int z = -10; z <= 10; z++) {
                            BlockPos checkPos = center.offset(x, y, z);
                            BlockState state = level.getBlockState(checkPos);
                            if (state.is(Blocks.DAMAGED_ANVIL) || state.is(Blocks.ANVIL) || state.is(Blocks.CHIPPED_ANVIL)) {
                                level.setBlock(checkPos, Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
        });
    }

    // =========================================================================
    // 12. МИННОЕ ПОЛЕ
    // Нажимные плиты с ТНТ в радиусе 8 блоков с полным откатом через 20 секунд
    // =========================================================================
    public static void executePrank12_Minefield(List<ServerPlayer> players) {
        PrankManager.getInstance().broadcastMessage("§c§l[!] МИННОЕ ПОЛЕ: Земля заминирована! Осторожно под ногами (20 сек)!");

        Map<BlockPos, BlockState> originalBlocks = new HashMap<>();

        for (ServerPlayer player : players) {
            ServerLevel level = player.serverLevel();
            BlockPos pPos = player.blockPosition();

            for (int x = -6; x <= 6; x += 3) {
                for (int z = -6; z <= 6; z += 3) {
                    BlockPos surface = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, pPos.offset(x, 0, z));
                    BlockPos under = surface.below();

                    if (!level.isEmptyBlock(under) && level.getBlockState(under).getBlock() != Blocks.BEDROCK) {
                        originalBlocks.put(surface, level.getBlockState(surface));
                        originalBlocks.put(under, level.getBlockState(under));

                        level.setBlock(under, Blocks.TNT.defaultBlockState(), 3);
                        level.setBlock(surface, Blocks.STONE_PRESSURE_PLATE.defaultBlockState(), 3);
                    }
                }
            }
        }

        // Полный откат всех блоков мин точно через 20 секунд (400 тиков)
        PrankManager.getInstance().scheduleTask("Minefield_Rollback", 400, () -> {
            for (Map.Entry<BlockPos, BlockState> entry : originalBlocks.entrySet()) {
                ServerLevel level = players.get(0).serverLevel();
                level.setBlock(entry.getKey(), entry.getValue(), 3);
            }
            PrankManager.getInstance().broadcastMessage("§a[ChaosPranks] Минное поле обезврежено, блоки восстановлены.");
        });
    }

    // =========================================================================
    // 13. ПРЫГУЧИЙ ТНТ-ДОЖДЬ
    // 10 зажженных ТНТ падают и пружинят от земли со звуком отскока
    // =========================================================================
    public static void executePrank13_BouncyTntRain(List<ServerPlayer> players) {
        PrankManager.getInstance().broadcastMessage("§c§l[!] ПРЫГУЧИЙ ТНТ-ДОЖДЬ! С неба сыплются динамиты-попрыгунчики!");

        List<PrimedTnt> bouncyTnts = new ArrayList<>();

        for (ServerPlayer player : players) {
            ServerLevel level = player.serverLevel();
            Vec3 pos = player.position();

            for (int i = 0; i < 4; i++) {
                double ox = (RANDOM.nextDouble() - 0.5) * 8.0;
                double oz = (RANDOM.nextDouble() - 0.5) * 8.0;

                PrimedTnt tnt = new PrimedTnt(level, pos.x + ox, pos.y + 12.0, pos.z + oz, null);
                tnt.setFuse(100); // 5 секунд запал
                level.addFreshEntity(tnt);
                bouncyTnts.add(tnt);
            }
        }

        // Пружинящая физика на 100 тиков
        for (int tick = 1; tick <= 100; tick++) {
            PrankManager.getInstance().scheduleTask("BouncyTnt_Tick_" + tick, tick, () -> {
                for (PrimedTnt tnt : bouncyTnts) {
                    if (tnt.isAlive() && tnt.onGround()) {
                        Vec3 motion = tnt.getDeltaMovement();
                        tnt.setDeltaMovement(motion.x * 1.1, 0.75, motion.z * 1.1);
                        tnt.hasImpulse = true;

                        if (((net.minecraft.server.level.ServerLevel)tnt.level()) != null) {
                            ((net.minecraft.server.level.ServerLevel)tnt.level()).playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.SLIME_BLOCK_FALL, SoundSource.BLOCKS, 1.0f, 1.2f);
                            ((net.minecraft.server.level.ServerLevel)tnt.level()).sendParticles(ParticleTypes.ITEM_SLIME, tnt.getX(), tnt.getY(), tnt.getZ(), 8, 0.2, 0.1, 0.2, 0.05);
                        }
                    }
                }
            });
        }
    }

    // =========================================================================
    // 14. БЕДРОК-ПРАНК НА ВЫДЕРЖКУ
    // Коробка из бедрока с крипером, исчезающая за 2 тика до взрыва
    // =========================================================================
    public static void executePrank14_BedrockCage(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            ServerLevel level = player.serverLevel();
            BlockPos pPos = player.blockPosition();

            Map<BlockPos, BlockState> savedBlocks = new HashMap<>();

            // Строим коробку 3x3x3 из бедрока
            for (int x = -1; x <= 1; x++) {
                for (int y = 0; y <= 2; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos targetPos = pPos.offset(x, y, z);
                        if (x == 0 && (y == 0 || y == 1) && z == 0) {
                            // Пространство внутри для игрока и крипера
                            continue;
                        }
                        savedBlocks.put(targetPos, level.getBlockState(targetPos));
                        level.setBlock(targetPos, Blocks.BEDROCK.defaultBlockState(), 3);
                    }
                }
            }

            // Факел на стену, чтобы было видно крипера
            BlockPos torchPos = pPos.above();
            savedBlocks.put(torchPos, level.getBlockState(torchPos));
            level.setBlock(torchPos, Blocks.TORCH.defaultBlockState(), 3);

            // Спавним шипящего крипера прямо в упор к игроку
            Creeper creeper = EntityType.CREEPER.create(level);
            if (creeper != null) {
                creeper.moveTo(player.getX() + 0.3, player.getY(), player.getZ() + 0.3, 0, 0);
                creeper.ignite(); // Начинает шипеть (таймер взрыва 30 тиков)
                level.addFreshEntity(creeper);

                // За 2 тика до детонации (на 28-м тике) мгновенно удаляем крипера и бедрок
                PrankManager.getInstance().scheduleTask("BedrockCage_Poof_" + player.getId(), 28, () -> {
                    creeper.discard();
                    for (Map.Entry<BlockPos, BlockState> entry : savedBlocks.entrySet()) {
                        level.setBlock(entry.getKey(), entry.getValue(), 3);
                    }
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1.5f, 1.5f);
                    level.sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
                });
            }
        }

        PrankManager.getInstance().broadcastMessage("§4§l[!] ПРАНК НА ВЫДЕРЖКУ: Бедрок замурован, крипер шипит!");
    }

    // =========================================================================
    // 15. СВИНОЙ СПЕЦНАЗ
    // Сирена, звук выбитой двери, спавн 4 Брутальных Пиглинов на 15 секунд
    // =========================================================================
    public static void executePrank15_PiglinSwat(List<ServerPlayer> players) {
        List<PiglinBrute> spawnedPiglins = new ArrayList<>();

        for (ServerPlayer player : players) {
            ServerLevel level = player.serverLevel();

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0f, 0.8f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAID_HORN.get(), SoundSource.HOSTILE, 2.0f, 1.2f);

            for (int i = 0; i < 4; i++) {
                PiglinBrute brute = EntityType.PIGLIN_BRUTE.create(level);
                if (brute != null) {
                    double ox = (RANDOM.nextDouble() - 0.5) * 4.0;
                    double oz = (RANDOM.nextDouble() - 0.5) * 4.0;
                    brute.moveTo(player.getX() + ox, player.getY(), player.getZ() + oz, RANDOM.nextFloat() * 360, 0);
                    brute.setImmuneToZombification(true);
                    brute.setTarget(player);
                    level.addFreshEntity(brute);
                    spawnedPiglins.add(brute);
                }
            }
        }

        PrankManager.getInstance().broadcastMessage("§c§l[!] СВИНОЙ СПЕЦНАЗ: Дверь выбита! Брутальные пиглины штурмуют (15 сек)!");

        // Деспавн спецназа ровно через 15 секунд (300 тиков)
        PrankManager.getInstance().scheduleTask("PiglinSwat_Despawn", 300, () -> {
            for (PiglinBrute brute : spawnedPiglins) {
                if (brute.isAlive()) {
                    ((net.minecraft.server.level.ServerLevel)brute.level()).sendParticles(ParticleTypes.LARGE_SMOKE, brute.getX(), brute.getY() + 1.0, brute.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
                    brute.discard();
                }
            }
        });
    }

    // =========================================================================
    // 16. ВОЗДУШНЫЙ НАЛЕТ ГАСТОВ
    // 3 Гаста в небе делают залп и исчезают через 10 секунд
    // =========================================================================
    public static void executePrank16_GhastAirStrike(List<ServerPlayer> players) {
        List<Ghast> ghasts = new ArrayList<>();

        for (ServerPlayer player : players) {
            ServerLevel level = player.serverLevel();
            player.playNotifySound(SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 2.0f, 1.0f);

            for (int i = 0; i < 3; i++) {
                Ghast ghast = EntityType.GHAST.create(level);
                if (ghast != null) {
                    double ox = (RANDOM.nextDouble() - 0.5) * 16.0;
                    double oz = (RANDOM.nextDouble() - 0.5) * 16.0;
                    ghast.moveTo(player.getX() + ox, player.getY() + 14.0, player.getZ() + oz, 0, 0);
                    ghast.setTarget(player);
                    level.addFreshEntity(ghast);
                    ghasts.add(ghast);
                }
            }
        }

        PrankManager.getInstance().broadcastMessage("§f§l[!] ВОЗДУШНЫЙ НАЛЕТ: Гасты в небе дают залп огненными шарами!");

        // Деспавн через 10 секунд (200 тиков)
        PrankManager.getInstance().scheduleTask("Ghast_Despawn", 200, () -> {
            for (Ghast ghast : ghasts) {
                if (ghast.isAlive()) {
                    ((net.minecraft.server.level.ServerLevel)ghast.level()).sendParticles(ParticleTypes.POOF, ghast.getX(), ghast.getY() + 1.0, ghast.getZ(), 20, 1.0, 1.0, 1.0, 0.1);
                    ghast.discard();
                }
            }
        });
    }

    // =========================================================================
    // 17. КОВЕР ИЗ ЧЕШУЙНИЦ
    // Под ногами спавнится 25 чешуйниц на 15 секунд
    // =========================================================================
    public static void executePrank17_SilverfishCarpet(List<ServerPlayer> players) {
        List<Silverfish> silverfishes = new ArrayList<>();

        for (ServerPlayer player : players) {
            ServerLevel level = player.serverLevel();
            player.playNotifySound(SoundEvents.SILVERFISH_AMBIENT, SoundSource.HOSTILE, 1.5f, 1.0f);

            for (int i = 0; i < 25; i++) {
                Silverfish sf = EntityType.SILVERFISH.create(level);
                if (sf != null) {
                    double ox = (RANDOM.nextDouble() - 0.5) * 4.0;
                    double oz = (RANDOM.nextDouble() - 0.5) * 4.0;
                    sf.moveTo(player.getX() + ox, player.getY(), player.getZ() + oz, 0, 0);
                    sf.setTarget(player);
                    level.addFreshEntity(sf);
                    silverfishes.add(sf);
                }
            }
        }

        PrankManager.getInstance().broadcastMessage("§7§l[!] КОВЕР ИЗ ЧЕШУЙНИЦ: Пол зашевелился! 25 чешуйниц под ногами!");

        // Деспавн через 15 секунд (300 тиков)
        PrankManager.getInstance().scheduleTask("Silverfish_Cleanup", 300, () -> {
            for (Silverfish sf : silverfishes) {
                if (sf.isAlive()) {
                    ((net.minecraft.server.level.ServerLevel)sf.level()).sendParticles(ParticleTypes.SMOKE, sf.getX(), sf.getY() + 0.2, sf.getZ(), 3, 0.1, 0.1, 0.1, 0.02);
                    sf.discard();
                }
            }
        });
    }

    // =========================================================================
    // 18. ЗОМБИ-ГИГАНТ
    // Спавнится Giant (12 блоков) с тряской экрана и пинком на 15 блоков (живет 25 сек)
    // =========================================================================
    public static void executePrank18_GiantZombie(List<ServerPlayer> players) {
        ServerPlayer target = players.get(RANDOM.nextInt(players.size()));
        ServerLevel level = target.serverLevel();

        Giant giant = EntityType.GIANT.create(level);
        if (giant == null) return;

        giant.moveTo(target.getX() + 6.0, target.getY(), target.getZ() + 6.0, 0, 0);
        giant.setCustomName(net.minecraft.network.chat.Component.literal("§4§lЗОМБИ-ГИГАНТ"));
        giant.setCustomNameVisible(true);
        level.addFreshEntity(giant);

        PrankManager.getInstance().broadcastMessage("§4§l[!] ЗОМБИ-ГИГАНТ: Земля дрожит под его шагами (25 сек)!");

        // Кастомный тик-обработчик шагов и пинка
        for (int tick = 1; tick <= 500; tick++) {
            PrankManager.getInstance().scheduleTask("Giant_AI_Tick_" + tick, tick, () -> {
                if (giant.isAlive()) {
                    // Звук тяжелых шагов каждые 25 тиков
                    if (giant.tickCount % 25 == 0) {
                        level.playSound(null, giant.getX(), giant.getY(), giant.getZ(), SoundEvents.ZOMBIE_STEP, SoundSource.HOSTILE, 3.0f, 0.5f);
                    }

                    // Пинок игроков в радиусе 4 блоков
                    for (ServerPlayer player : players) {
                        if (player.distanceTo(giant) < 4.5) {
                            Vec3 kickVector = player.position().subtract(giant.position()).normalize().scale(2.2).add(0, 0.8, 0);
                            player.setDeltaMovement(kickVector);
                            player.hurtMarked = true;
                            player.hurt(player.damageSources().mobAttack(giant), 4.0f);
                            player.playNotifySound(SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.5f, 0.7f);
                        }
                    }
                }
            });
        }

        // Деспавн через 25 секунд (500 тиков)
        PrankManager.getInstance().scheduleTask("Giant_Despawn", 500, () -> {
            if (giant.isAlive()) {
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, giant.getX(), giant.getY() + 4.0, giant.getZ(), 5, 2.0, 2.0, 2.0, 0.1);
                giant.discard();
            }
        });
    }

    // =========================================================================
    // 19. ЭНДЕРМЕН-ПОХИТИТЕЛЬ
    // Спавнит Эндермена, сажает игрока верхом, телепортирует 5 раз и сбрасывает
    // =========================================================================
    public static void executePrank19_EnderKidnapper(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            ServerLevel level = player.serverLevel();

            EnderMan enderman = EntityType.ENDERMAN.create(level);
            if (enderman != null) {
                enderman.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0);
                level.addFreshEntity(enderman);

                // Сажаем игрока на эндермена
                player.startRiding(enderman, true);
                player.playNotifySound(SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 1.5f, 0.9f);

                // 5 последовательных телепортов каждые 20 тиков (каждую секунду)
                for (int tp = 1; tp <= 5; tp++) {
                    final int step = tp;
                    PrankManager.getInstance().scheduleTask("EnderKidnap_Step_" + step + "_" + player.getId(), step * 20, () -> {
                        if (enderman.isAlive()) {
                            double ox = (RANDOM.nextDouble() - 0.5) * 20.0;
                            double oz = (RANDOM.nextDouble() - 0.5) * 20.0;
                            BlockPos dest = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, enderman.blockPosition().offset((int)ox, 0, (int)oz));
                            enderman.teleportTo(dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5);
                            level.playSound(null, dest.getX(), dest.getY(), dest.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.2f, 1.0f);
                        }
                    });
                }

                // Ссаживание игрока и исчезновение эндермена через 110 тиков
                PrankManager.getInstance().scheduleTask("EnderKidnap_Finish_" + player.getId(), 110, () -> {
                    player.stopRiding();
                    level.sendParticles(ParticleTypes.PORTAL, enderman.getX(), enderman.getY() + 1.0, enderman.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
                    enderman.discard();
                });
            }
        }

        PrankManager.getInstance().broadcastMessage("§5§l[!] ЭНДЕРМЕН-ПОХИТИТЕЛЬ: Вас схватили и утаскивают телепортами!");
    }

    // =========================================================================
    // 20. ЛАВОВЫЙ КУБ-ПРЕСС
    // Сверху падает Магмовый куб 10-го размера со шлепком
    // =========================================================================
    public static void executePrank20_MagmaCubePress(List<ServerPlayer> players) {
        List<MagmaCube> cubes = new ArrayList<>();

        for (ServerPlayer player : players) {
            ServerLevel level = player.serverLevel();

            MagmaCube cube = EntityType.MAGMA_CUBE.create(level);
            if (cube != null) {
                // Размер 10 (гигантский куб)
                cube.setSize(10, true);
                cube.moveTo(player.getX(), player.getY() + 12.0, player.getZ(), 0, 0);
                cube.setDeltaMovement(0, -1.2, 0);
                cube.setTarget(player);
                level.addFreshEntity(cube);
                cubes.add(cube);

                player.playNotifySound(SoundEvents.MAGMA_CUBE_SQUISH, SoundSource.HOSTILE, 2.0f, 0.6f);
            }
        }

        PrankManager.getInstance().broadcastMessage("§6§l[!] ЛАВОВЫЙ КУБ-ПРЕСС: Гигантский магмовый куб 10-го размера падает на головы!");

        // Деспавн через 15 секунд (300 тиков)
        PrankManager.getInstance().scheduleTask("MagmaCube_Despawn", 300, () -> {
            for (MagmaCube cube : cubes) {
                if (cube.isAlive()) {
                    ((net.minecraft.server.level.ServerLevel)cube.level()).sendParticles(ParticleTypes.FLAME, cube.getX(), cube.getY() + 2.0, cube.getZ(), 30, 2.0, 2.0, 2.0, 0.1);
                    cube.discard();
                }
            }
        });
    }
}