package com.github.xniter.customenderchest.commands;

import com.github.xniter.customenderchest.CustomEnderChest;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class FileToMysqlCmd {

    private final CustomEnderChest pl;

    public FileToMysqlCmd(CustomEnderChest plugin) {
        this.pl = plugin;
    }

    public void runCmd(final CommandSender sender, final boolean overwrite) {
        if (pl.getConfigHandler().getString("database.typeOfDatabase").equalsIgnoreCase("mysql")) {
            if (pl.getMysqlSetup().getConnection() != null) {
                final File dataFolder = new File("plugins" + System.getProperty("file.separator") + CustomEnderChest.pluginName + System.getProperty("file.separator") + "PlayerData");
                if (dataFolder.exists()) {
                    sender.sendMessage(pl.getConfigHandler().getStringWithColor("chatMessages.flatfileImport-started"));
                    Bukkit.getScheduler().runTaskAsynchronously(pl, new Runnable() {

                        @Override
                        public void run() {
                            File[] fileList = dataFolder.listFiles();
                            CustomEnderChest.log.info("Starting importing data from flatfile to mysql! " + fileList.length + " files found...");
                            int progress = 0;
                            for (File f : fileList) {
                                progress = progress + 1;
                                try {
                                    if (f.getName().endsWith(".yml")) {
                                        UUID playerUUID = UUID.fromString(f.getName().substring(0, 36));
                                        if (!pl.getStorageInterface().hasDataFile(playerUUID)) {
                                            //Import skipping existing database data.
                                            FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(f);
                                            int invSize = ymlFormat.getInt("EnderChestSize");
                                            Inventory inv = Bukkit.createInventory(null, invSize);
                                            ArrayList<ItemStack> items = new ArrayList<ItemStack>();
                                            for (int i = 0; i < inv.getSize(); i++) {
                                                ItemStack item = ymlFormat.getItemStack("EnderChestInventory." + i);
                                                items.add(item);
                                            }
                                            ItemStack[] itemsList = items.toArray(new ItemStack[items.size()]);
                                            inv.setContents(itemsList);
                                            items.clear();
                                            pl.getStorageInterface().saveEnderChest(playerUUID, inv, ymlFormat.getString("PlayerLastName"), invSize);
                                        } else if (overwrite) {
                                            //Import overwriting existing data.
                                            FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(f);
                                            int invSize = ymlFormat.getInt("EnderChestSize");
                                            Inventory inv = Bukkit.createInventory(null, invSize);
                                            ArrayList<ItemStack> items = new ArrayList<ItemStack>();
                                            for (int i = 0; i < inv.getSize(); i++) {
                                                ItemStack item = ymlFormat.getItemStack("EnderChestInventory." + i);
                                                items.add(item);
                                            }
                                            ItemStack[] itemsList = items.toArray(new ItemStack[items.size()]);
                                            inv.setContents(itemsList);
                                            items.clear();
                                            pl.getStorageInterface().saveEnderChest(playerUUID, inv);
                                            Player p = Bukkit.getPlayer(playerUUID);
                                            if (p != null) {
                                                if (p.isOnline()) {
                                                    String enderChestTitle = pl.getEnderChestUtils().getTitle(p);
                                                    Inventory invT = Bukkit.getServer().createInventory(p, pl.getStorageInterface().loadSize(playerUUID), enderChestTitle);
                                                    pl.getStorageInterface().loadEnderChest(p, invT);
                                                    pl.getDataHandler().setData(p.getUniqueId(), invT);
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    CustomEnderChest.log.warning("Failed to import file: " + f.getName() + " .Error: " + e.getMessage());
                                }
                                CustomEnderChest.log.info("Import progress: " + progress + " / " + fileList.length);
                            }
                            CustomEnderChest.log.info("Data import is complete!");
                        }

                    });
                } else {
                    sender.sendMessage(pl.getConfigHandler().getStringWithColor("chatMessages.flatfileImport-datafolder"));
                }
            } else {
                sender.sendMessage(pl.getConfigHandler().getStringWithColor("chatMessages.flatfileImport-connection"));
            }
        } else {
            sender.sendMessage(pl.getConfigHandler().getStringWithColor("chatMessages.flatfileImport-mysql"));
        }
    }

}
