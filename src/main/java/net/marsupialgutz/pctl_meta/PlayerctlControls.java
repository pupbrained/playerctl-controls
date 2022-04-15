package net.marsupialgutz.pctl_meta;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import static net.minecraft.server.command.CommandManager.literal;

public class PlayerctlControls implements ModInitializer {
    public static String result;
    public static boolean playing, showStatus = true;

    @Override
    public void onInitialize() {
        var mc = MinecraftClient.getInstance();
        var rt = Runtime.getRuntime();
        assert mc.player != null;
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("playerctl").then(literal("play").executes(context -> {
                try {
                    if (playing) {
                        mc.player.sendMessage(Text.of("Already playing!"), true);
                        return 1;
                    }
                    String[] cmd = {"playerctl", "play"};
                    rt.exec(cmd);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                playing = true;
                return 1;
            })));
            dispatcher.register(literal("playerctl").then(literal("pause").executes(context -> {
                try {
                    if (!playing) {
                        mc.player.sendMessage(Text.of("Already paused!"), true);
                        return 1;
                    }
                    String[] cmd = {"playerctl", "pause"};
                    rt.exec(cmd);
                    playing = false;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return -1;
            })));
            dispatcher.register(literal("playerctl").then(literal("playpause").executes(context -> {
                try {
                    String[] cmd = {"playerctl", "play-pause"};
                    rt.exec(cmd);
                    playing = !playing;
                    return 1;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })));
            dispatcher.register(literal("playerctl").then(literal("next").executes(context -> {
                try {
                    String[] cmd = {"playerctl", "next"};
                    rt.exec(cmd);
                    return 1;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })));
            dispatcher.register(literal("playerctl").then(literal("previous").executes(context -> {
                try {
                    String[] cmd = {"playerctl", "previous"};
                    rt.exec(cmd);
                    return 1;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })));
            dispatcher.register(literal("playerctl").then(literal("togglestatus").executes(context -> {
                showStatus = !showStatus;
                return 1;
            })));
        });
        if (showStatus) {
            var timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        String[] cmd = {"playerctl", "metadata", "-f", "'{{ artist }} - {{ title }}'"};
                        Process proc = rt.exec(cmd);
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        String s;
                        while ((s = stdInput.readLine()) != null) {
                            result = s;
                        }
                        cmd = new String[]{"playerctl", "status"};
                        proc = rt.exec(cmd);
                        stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        while ((s = stdInput.readLine()) != null) {
                            playing = s.equals("Playing");
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 0, 1000);
        }
    }
}
