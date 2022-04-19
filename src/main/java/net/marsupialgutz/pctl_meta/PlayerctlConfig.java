package net.marsupialgutz.pctl_meta;

import com.oroarmor.config.Config;
import com.oroarmor.config.ConfigItem;
import com.oroarmor.config.ConfigItemGroup;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;

public class PlayerctlConfig extends Config {
    public static final ConfigItemGroup main = new ConfigGroup();
    public static final List<ConfigItemGroup> cfgs = of(main);

    public PlayerctlConfig() {
        super(cfgs, new File(FabricLoader.getInstance().getConfigDir().toFile(), "playerctl_config.json"), "playerctl_config");
    }

    public static class ConfigGroup extends ConfigItemGroup {
        public static final ConfigItem<Boolean> showStatus = new ConfigItem<>("show_status", true, "Show status");

        public ConfigGroup() {
            super(of(showStatus), "group");
        }
    }
}
