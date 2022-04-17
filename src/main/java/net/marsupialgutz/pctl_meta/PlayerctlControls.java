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
    public static String result, lastImg;
    public static boolean playing, showStatus = true;
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
                            if (playing) {
                                mc.player.sendMessage(Text.of("Already playing!"), true);
                                return 1;
                            }
                            final String[] cmd = {"playerctl", "play"};
                            rt.exec(cmd);
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                        playing = true;
                        return 1;
                    }
                }
                return 0;
            })));
            dispatcher.register(literal("pctl").then(literal("pause").executes(context -> {
                if (!(result == null)) {
                    if (!(result.equals("No players found"))) {
                        try {
                            if (!playing) {
                                mc.player.sendMessage(Text.of("Already paused!"), true);
                                return 1;
                            }
                            final String[] cmd = {"playerctl", "pause"};
                            rt.exec(cmd);
                            playing = false;
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
                            playing = !playing;
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
                            res = "https://listimg.pinclipart.com/picdir/s/330-3300806_red-slash-png-clipart-images-gallery-for-free.png";
                            InputStream input = new URL(res).openStream();
                            var nativeImage = NativeImage.read(input);
                            nativeImageBackedTexture = new NativeImageBackedTexture(nativeImage);
                        }
                        if (!(Objects.equals(lastImg, res))) {
                            InputStream input = new URL(res).openStream();
                            var nativeImage = NativeImage.read(input);
                            nativeImageBackedTexture = new NativeImageBackedTexture(nativeImage);
                            lastImg = res;
                        }
                        cmd = new String[]{"playerctl", "metadata", "-f", "'{{ artist }} - {{ title }}'"};
                        proc = rt.exec(cmd);
                        stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        while ((s = stdInput.readLine()) != null) result = s.substring(1, s.length() - 1);
                        if (result.equals("No players found")) result = "No players found";
                        cmd = new String[]{"playerctl", "status"};
                        proc = rt.exec(cmd);
                        stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        while ((s = stdInput.readLine()) != null) playing = s.equals("Playing");
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 0, 1000);
        }
    }
}
