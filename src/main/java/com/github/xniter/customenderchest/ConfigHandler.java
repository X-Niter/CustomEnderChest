package com.github.xniter.customenderchest;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigHandler {

    private final CustomEnderChest enderchest;

    public ConfigHandler(CustomEnderChest enderchest) {
        this.enderchest = enderchest;
        loadConfig();
    }

    public void loadConfig() {
        File pluginFolder = new File("plugins" + System.getProperty("file.separator") + CustomEnderChest.pluginName);
        if (!pluginFolder.exists()) {
            pluginFolder.mkdir();
        }
        File configFile = new File("plugins" + System.getProperty("file.separator") + CustomEnderChest.pluginName + System.getProperty("file.separator") + "config.yml");
        if (!configFile.exists()) {
            CustomEnderChest.log.info("No config file found! Creating new one...");
            enderchest.saveDefaultConfig();
        }
        try {
            CustomEnderChest.log.info("Loading the config file...");
            enderchest.getConfig().load(configFile);
            CustomEnderChest.log.info("Config loaded successfully!");
        } catch (Exception e) {
            CustomEnderChest.log.severe("Could not load the config file! You need to regenerate the config! Error: " + e.getMessage());
        }
    }

    public String getStringWithColor(String key) {
        if (!enderchest.getConfig().contains(key)) {
            enderchest.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + CustomEnderChest.pluginName + " folder! (Try generating a new one by deleting the current)");
            return "errorCouldNotLocateInConfigYml:" + key;
        } else {
            String configValue = enderchest.getConfig().getString(key);
            if (configValue != null) {
                return configValue.replaceAll("&", "§");
            } else {
                // Handle the case where configValue is null
                enderchest.getLogger().severe("Value for " + key + " in the config.yml is null!");
                return "errorConfigValueIsNull:" + key;
            }
        }
    }


    public String getString(String key) {
        if (!enderchest.getConfig().contains(key)) {
            enderchest.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + CustomEnderChest.pluginName + " folder! (Try generating a new one by deleting the current)");
            return "errorCouldNotLocateInConfigYml:" + key;
        } else {
            return enderchest.getConfig().getString(key);
        }
    }

    public List<String> getStringList(String key) {
        if (!enderchest.getConfig().contains(key)) {
            enderchest.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + CustomEnderChest.pluginName + " folder! (Try generating a new one by deleting the current)");
            return null;
        } else {
            return enderchest.getConfig().getStringList(key);
        }
    }

    public boolean getBoolean(String key) {
        if (!enderchest.getConfig().contains(key)) {
            enderchest.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + CustomEnderChest.pluginName + " folder! (Try generating a new one by deleting the current)");
            return false;
        } else {
            return enderchest.getConfig().getBoolean(key);
        }
    }

    public double getDouble(String key) {
        if (!enderchest.getConfig().contains(key)) {
            enderchest.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + CustomEnderChest.pluginName + " folder! (Try generating a new one by deleting the current)");
            return 0.0;
        } else {
            return enderchest.getConfig().getDouble(key);
        }
    }

    public int getInteger(String key) {
        if (!enderchest.getConfig().contains(key)) {
            enderchest.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + CustomEnderChest.pluginName + " folder! (Try generating a new one by deleting the current)");
            return 0;
        } else {
            return enderchest.getConfig().getInt(key);
        }
    }

    //TODO remove this \/
    //Send chat messages from config
    public void printMessage(Player p, String messageKey) {
        if (enderchest.getConfig().contains(messageKey)) {
            List<String> message = new ArrayList<String>();
            String configMsg = enderchest.getConfig().getString(messageKey);

            if (configMsg.matches("")) return;

            message.add(configMsg);

            if (p != null) {
                //Message format
                p.sendMessage(getString("chatMessages.prefix").replaceAll("&", "§") + message.get(0).replaceAll("&", "§"));
            }

        } else {
            enderchest.getLogger().severe("Could not locate '" + messageKey + "' in the config.yml inside of the CustomEnderChest folder!");

            p.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + ">> " + ChatColor.RED + "Could not locate '" + messageKey + "' in the config.yml inside of the CustomEnderChest folder!");
        }

    }

}
