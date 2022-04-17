package net.marsupialgutz.pctl_meta;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static net.minecraft.server.command.CommandManager.literal;

public class PlayerctlControls implements ModInitializer {
    public static String result, lastImg, playing;
    public static boolean showStatus = true, statCheck = true;
    public static NativeImageBackedTexture nativeImageBackedTexture;

    @Override
    public void onInitialize() {
        final var mc = MinecraftClient.getInstance();
        final var rt = Runtime.getRuntime();
        assert mc.player != null;
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
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
                            final String[] cmd = {"playerctl", "play"};
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
                            final String[] cmd = {"playerctl", "pause"};
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
            dispatcher.register(literal("pctl").then(literal("togglestatus").executes(context -> {
                if (!(result == null)) {
                    if (!(result.equals("No players found"))) {
                        showStatus = !showStatus;
                        return 1;
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
                        String[] cmd = {"playerctl", "metadata", "-f", "'{{ mpris:artUrl }}'"};
                        Process proc = rt.exec(cmd);
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        String s;
                        String res = null;
                        while ((s = stdInput.readLine()) != null) {
                            res = s.substring(1, s.length() - 1);
                        }
                        if (res == null) {
                            res = "https://possums.xyz/imgs/no-song.png";
                            statCheck = false;
                            playing = "OFF";
                        } else {
                            statCheck = true;
                        }
                        if (!(Objects.equals(lastImg, res))) {
                            InputStream input = new URL(res).openStream();
                            var nativeImage = NativeImage.read(input);
                            nativeImageBackedTexture = new NativeImageBackedTexture(nativeImage);
                            lastImg = res;
                        }
                    } catch (final IOException e) {
                        run();
                    }
                }
            }, 0, 1000);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        String s2;
                        String[] cmd2 = {"playerctl", "metadata", "-f", "'{{ artist }} - {{ title }}'"};
                        Process proc2 = rt.exec(cmd2);
                        BufferedReader stdInput2 = new BufferedReader(new InputStreamReader(proc2.getInputStream()));
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
                        String[] cmd3 = {"playerctl", "status"};
                        Process proc3 = rt.exec(cmd3);
                        BufferedReader stdInput3 = new BufferedReader(new InputStreamReader(proc3.getInputStream()));
                        while ((s3 = stdInput3.readLine()) != null) {
                            switch (s3) {
                                case "Playing" -> playing = "Playing";
                                case "Paused" -> playing = "Paused";
                                case "Stopped" -> playing = "Stopped";
                                default -> playing = "OFF";
                            }
                        }
                        System.out.println("playing: " + playing);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 0, 1000);
        }
    }
}
