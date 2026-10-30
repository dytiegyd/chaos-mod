package com.example.chaospranks.client;

import com.example.chaospranks.network.ClientPrankPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientPrankEvents {
    private static final ClientPrankEvents INSTANCE = new ClientPrankEvents();

    public static ClientPrankEvents getInstance() {
        return INSTANCE;
    }

    private static int activePrankId = 0;
    private static int prankRemainingTicks = 0;

    // Камера и угол поворота (Пранк 32)
    private static boolean cameraInverted = false;
    private float cameraRollAngle = 0.0f;

    // Пульсирующий FOV (Пранк 35)
    private static boolean drunkenFov = false;
    private static float fovWaveTime = 0.0f;

    // Масштабирование голов и тел (Пранк 33, 34)
    private static boolean bigHeadMode = false;
    private static float playerScaleMultiplier = 1.0f;

    // Инверсия управления и перехват инпута (Пранк 7, 40)
    private static boolean controlsInverted = false;
    private static boolean hijackedByFlea = false;

    // Клиентские оверлеи (Кинополосы 21:9, Скримеры, Каптча, Звонок Дискорд)
    private static boolean blockbusterBars = false;
    private static boolean screamerActive = false;
    private static int screamerTicks = 0;
    private static boolean captchaActive = false;
    private static boolean discordCallActive = false;

    private ClientPrankEvents() {}

    public static void handlePacket(ClientPrankPacket packet) {
        Minecraft mc = Minecraft.getInstance();

        switch (packet.getAction()) {
            case START -> {
                activePrankId = packet.getPrankId();
                prankRemainingTicks = packet.getDurationTicks();
                activateClientPrank(packet);
            }
            case STOP -> {
                resetAllClientEffects();
            }
            case ROULETTE_TICK -> {
                if (mc.player != null) {
                    mc.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.get(), 0.6f, packet.getFloatParam1());
                }
            }
            case TRIGGER_ONE_SHOT -> {
                if (packet.getPrankId() == 63) {
                    screamerActive = true;
                    screamerTicks = 40;
                }
            }
        }
    }

    private static void activateClientPrank(ClientPrankPacket packet) {
        int id = packet.getPrankId();

        if (id == 7) {
            controlsInverted = true;
        } else if (id == 32) {
            cameraInverted = true;
        } else if (id == 33) {
            playerScaleMultiplier = packet.getFloatParam1() > 0 ? packet.getFloatParam1() : 0.2f;
        } else if (id == 34) {
            bigHeadMode = true;
        } else if (id == 35) {
            drunkenFov = true;
            fovWaveTime = 0.0f;
        } else if (id == 37) {
            blockbusterBars = true;
        } else if (id == 40) {
            hijackedByFlea = true;
        } else if (id == 63) {
            screamerActive = true;
            screamerTicks = 40;
        } else if (id == 86) {
            captchaActive = true;
        } else if (id == 87) {
            discordCallActive = true;
        }
    }

    public static void resetAllClientEffects() {
        activePrankId = 0;
        prankRemainingTicks = 0;
        cameraInverted = false;
        cameraRollAngle = 0.0f;
        drunkenFov = false;
        bigHeadMode = false;
        playerScaleMultiplier = 1.0f;
        controlsInverted = false;
        hijackedByFlea = false;
        blockbusterBars = false;
        screamerActive = false;
        captchaActive = false;
        discordCallActive = false;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (prankRemainingTicks > 0) {
            prankRemainingTicks--;
            if (prankRemainingTicks <= 0) {
                resetAllClientEffects();
            }
        }

        if (drunkenFov) {
            fovWaveTime += 0.08f;
        }

        if (screamerActive && screamerTicks > 0) {
            screamerTicks--;
            if (screamerTicks <= 0) {
                screamerActive = false;
            }
        }
    }

    /**
     * Пранк 32 (Австралийский сервер): Плавный переворот камеры на 180°
     */
    @SubscribeEvent
    public void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (cameraInverted) {
            cameraRollAngle = Mth.lerp(0.15f, cameraRollAngle, 180.0f);
            event.setRoll(event.getRoll() + cameraRollAngle);
        } else if (cameraRollAngle > 0.1f) {
            cameraRollAngle = Mth.lerp(0.2f, cameraRollAngle, 0.0f);
            event.setRoll(event.getRoll() + cameraRollAngle);
        }
    }

    /**
     * Пранк 35 (Пьяный FOV): Синусоидальная пульсация FOV
     */
    @SubscribeEvent
    public void onComputeFov(ViewportEvent.ComputeFov event) {
        if (drunkenFov) {
            double wave = Math.sin(fovWaveTime);
            double fovModifier = 70.0 + (wave * 55.0);
            event.setFOV(fovModifier);
        }
    }

    /**
     * Пранк 7 (Инверсия управления) и Пранк 40 (Блохи и Носители): Перехват инпута
     */
    @SubscribeEvent
    public void onMovementInput(MovementInputUpdateEvent event) {
        if (controlsInverted) {
            event.getInput().forwardImpulse = -event.getInput().forwardImpulse;
            event.getInput().leftImpulse = -event.getInput().leftImpulse;
        }

        if (hijackedByFlea) {
            if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.random.nextFloat() < 0.25f) {
                event.getInput().jumping = true;
            }
        }
    }

    /**
     * Пранк 33 (Лилипуты/Гулливер) и Пранк 34 (Big Head Mode): Масштабирование модели
     */
    @SubscribeEvent
    public void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        PoseStack poseStack = event.getPoseStack();
        LivingEntity entity = event.getEntity();

        poseStack.pushPose();

        if (playerScaleMultiplier != 1.0f && entity instanceof LocalPlayer) {
            poseStack.scale(playerScaleMultiplier, playerScaleMultiplier, playerScaleMultiplier);
        }

        if (bigHeadMode) {
            EntityModel<?> model = event.getRenderer().getModel();
            if (model instanceof HeadedModel headedModel) {
                ModelPart head = headedModel.getHead();
                head.xScale = 4.5f;
                head.yScale = 4.5f;
                head.zScale = 4.5f;
            }
        }
    }

    @SubscribeEvent
    public void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        PoseStack poseStack = event.getPoseStack();

        if (bigHeadMode) {
            EntityModel<?> model = event.getRenderer().getModel();
            if (model instanceof HeadedModel headedModel) {
                ModelPart head = headedModel.getHead();
                head.xScale = 1.0f;
                head.yScale = 1.0f;
                head.zScale = 1.0f;
            }
        }

        poseStack.popPose();
    }

    /**
     * Отрисовка оверлеев: Скримеры, Кино-полосы 21:9, Каптча, Звонок Дискорд
     */
    @SubscribeEvent
    public void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // Кино-полосы 21:9 (Пранк 37)
        if (blockbusterBars) {
            int barHeight = height / 7;
            graphics.fill(0, 0, width, barHeight, 0xFF000000);
            graphics.fill(0, height - barHeight, width, height, 0xFF000000);
        }

        // FNaF Скример (Пранк 63)
        if (screamerActive) {
            graphics.fill(0, 0, width, height, 0xDDFF0000);
            String screamerText = "§4§l[!] SCREAMER TRIGGERED [!]";
            int textWidth = mc.font.width(screamerText);
            graphics.drawString(mc.font, screamerText, (width - textWidth) / 2, height / 2 - 20, 0xFFFFFFFF, true);
        }

        // Каптча (Пранк 86)
        if (captchaActive) {
            int boxW = 220;
            int boxH = 100;
            int left = (width - boxW) / 2;
            int top = (height - boxH) / 2;

            graphics.fill(left, top, left + boxW, top + boxH, 0xEE1E1E1E);
            graphics.drawString(mc.font, "§c§lКАПТЧА НА ВЫЖИВАНИЕ", left + 15, top + 10, 0xFFFFFF);
            graphics.drawString(mc.font, "Нажмите зеленую кнопку!", left + 15, top + 30, 0xAAAAAA);
            graphics.fill(left + 20, top + 55, left + 90, top + 80, 0xFF2E7D32);
            graphics.drawString(mc.font, "[ Я не бот ]", left + 25, top + 63, 0xFFFFFF);
        }

        // Входящий звонок Discord (Пранк 87)
        if (discordCallActive) {
            int callW = 200;
            int callH = 70;
            int callX = width - callW - 20;
            int callY = 20;

            graphics.fill(callX, callY, callX + callW, callY + callH, 0xEE5865F2);
            graphics.drawString(mc.font, "§fВходящий звонок...", callX + 15, callY + 12, 0xFFFFFF);
            graphics.drawString(mc.font, "§7Chaos Friend #1", callX + 15, callY + 28, 0xDDDDDD);
            graphics.fill(callX + 15, callY + 45, callX + 75, callY + 62, 0xFF43B581);
            graphics.drawString(mc.font, "Принять", callX + 22, callY + 48, 0xFFFFFF);
            graphics.fill(callX + 85, callY + 45, callX + 145, callY + 62, 0xFFF04747);
            graphics.drawString(mc.font, "Сбросить", callX + 90, callY + 48, 0xFFFFFF);
        }
    }
}