package net.marsupialgutz.pctl_meta;

import com.oroarmor.config.Config;
import com.oroarmor.config.ConfigItemGroup;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback.EVENT;
import static net.minecraft.server.command.CommandManager.literal;

public class PlayerctlControls implements ModInitializer {
    public static final Config CONFIG = new PlayerctlConfig();
    public static String result, lastImg, playing;
    public static boolean showStatus, statCheck = true;
    public static NativeImageBackedTexture nativeImageBackedTexture;

    @Override
    public void onInitialize() {
        initCfg();
        readCfg();
        final var mc = MinecraftClient.getInstance();
        final var rt = Runtime.getRuntime();
        assert mc.player != null;
        EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("pctl").then(literal("play").executes(context -> {
                if (!(result == null)) {
                    if (!(result.equals("No players found"))) {
                        try {
                            if (playing.equals("Stopped"))
                                return 0;
                            if (playing.equals("Playing")) {
                                mc.player.sendMessage(Text.of("Already playing!"), true);
                                return 1;
                            }
                            final String[] cmd = { "playerctl", "play" };
                            rt.exec(cmd);
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                        return 1;
                    }
                }
                return 0;
            })));
            dispatcher.register(literal("pctl").then(literal("pause").executes(context -> {
                if (!(result == null)) {
                    if (!(result.equals("No players found"))) {
                        try {
                            if (playing.equals("Stopped"))
                                return 0;
                            if (playing.equals("Paused")) {
                                mc.player.sendMessage(Text.of("Already paused!"), true);
                                return 1;
                            }
                            final String[] cmd = { "playerctl", "pause" };
                            rt.exec(cmd);
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                        return -1;
                    }
                }
                return 0;
            })));
            dispatcher.register(literal("pctl").then(literal("playpause").executes(context -> {
                if (!(result == null)) {
                    if (!(result.equals("No players found"))) {
                        try {
                            rt.exec("playerctl play-pause");
                            if (playing.equals("Stopped"))
                                return 0;
                            return 1;
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                return 0;
            })));
            dispatcher.register(literal("pctl").then(literal("next").executes(context -> {
                if (!(result == null)) {
                    if (!(result.equals("No players found"))) {
                        try {
                            rt.exec("playerctl next");
                            return 1;
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                return 0;
            })));
            dispatcher.register(literal("pctl").then(literal("previous").executes(context -> {
                if (!(result == null)) {
                    if (!(result.equals("No players found"))) {
                        try {
                            rt.exec("playerctl previous");
                            return 1;
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                return 0;
            })));
            dispatcher.register(literal("pctl").then(literal("volup").executes(context -> {
                if (!(result == null)) {
                    if (!(result.equals("No players found"))) {
                        try {
                            rt.exec("playerctl volume 0.1+");
                            return 1;
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                return 0;
            })));
            dispatcher.register(literal("pctl").then(literal("voldown").executes(context -> {
                if (!(result == null)) {
                    if (!(result.equals("No players found"))) {
                        try {
                            rt.exec("playerctl volume 0.1-");
                            return 1;
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                return 0;
            })));
        });
        if (showStatus) {
            final var timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        readCfg();
                        final String[] cmd = { "playerctl", "metadata", "-f", "'{{ mpris:artUrl }}'" };
                        final Process proc = rt.exec(cmd);
                        final BufferedReader stdInput = new BufferedReader(
                                new InputStreamReader(proc.getInputStream()));
                        String s;
                        String res = null;
                        while ((s = stdInput.readLine()) != null) {
                            res = s.substring(1, s.length() - 1);
                        }
                        if (res == null) {
                            statCheck = false;
                            playing = "OFF";
                        } else {
                            statCheck = true;
                            if (!(Objects.equals(lastImg, res))) {
                                final InputStream input = new URL(res).openStream();
                                final var nativeImage = NativeImage.read(input);
                                nativeImageBackedTexture = new NativeImageBackedTexture(nativeImage);
                                lastImg = res;
                            }
                        }
                    } catch (final IOException e) {
                        lastImg = null;
                    }
                }
            }, 0, 1000);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        String s2;
                        final String[] cmd2 = { "playerctl", "metadata", "-f", "'{{ artist }} - {{ title }}'" };
                        final Process proc2 = rt.exec(cmd2);
                        final BufferedReader stdInput2 = new BufferedReader(
                                new InputStreamReader(proc2.getInputStream()));
                        while ((s2 = stdInput2.readLine()) != null) {
                            result = s2.substring(1, s2.length() - 1);
                        }
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 0, 1000);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        String s3;
                        final String[] cmd3 = { "playerctl", "status" };
                        final Process proc3 = rt.exec(cmd3);
                        final BufferedReader stdInput3 = new BufferedReader(
                                new InputStreamReader(proc3.getInputStream()));
                        while ((s3 = stdInput3.readLine()) != null) {
                            switch (s3) {
                                case "Playing" -> playing = "Playing";
                                case "Paused" -> playing = "Paused";
                                case "Stopped" -> playing = "Stopped";
                                default -> playing = "OFF";
                            }
                        }
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 0, 1000);
        }
    }
    private void initCfg() {
        CONFIG.readConfigFromFile();
        CONFIG.saveConfigToFile();
        ServerLifecycleEvents.SERVER_STOPPED.register(instance -> CONFIG.saveConfigToFile());
    }

    private void readCfg() {
        List<ConfigItemGroup> cfgs = CONFIG.getConfigs();
        showStatus = cfgs.get(0).toJson().get("show_status").getAsBoolean();
    }
}
