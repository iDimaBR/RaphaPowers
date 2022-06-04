package com.github.idimabr;

import com.github.idimabr.commands.GivePowerCommand;
import com.github.idimabr.listeners.PlayerJoinListener;
import com.github.idimabr.listeners.PowerUseListener;
import com.github.idimabr.storage.SQLStorage;
import com.github.idimabr.storage.SQLManager;
import com.github.idimabr.tasks.VerifyHealthTask;
import com.github.idimabr.utils.ItemBuilder;
import com.github.idimabr.utils.NBTApi;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.stream.Collectors;

public final class RaphaPowers extends JavaPlugin {

    private static RaphaPowers plugin;
    private SQLStorage SQL;
    public ItemStack LIGHTNINGSTRIKE_ITEM;
    public ItemStack FIREBALL_ITEM;
    public ItemStack HEARTH_ITEM;


    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        getConfig().options().copyDefaults(true);
        saveConfig();

        SQL = new SQLStorage(this);
        SQL.createTable();

        if (!Bukkit.getPluginManager().isPluginEnabled("yPoints")) {
            getLogger().warning("yPoints não encontrado!");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        loadItens();
        getCommand("powergive").setExecutor(new GivePowerCommand(this));
        Bukkit.getPluginManager().registerEvents(new PowerUseListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        new VerifyHealthTask(this).runTaskTimerAsynchronously(this, 20L, 20L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        SQLManager.saveAllHealths();
    }

    public static RaphaPowers getPlugin() {
        return plugin;
    }

    public SQLStorage getSQL() {
        return SQL;
    }

    private void loadItens(){
        ConfigurationSection itemSection = getConfig().getConfigurationSection("Item");
        for (String key : itemSection.getKeys(false)) {
            ConfigurationSection item = itemSection.getConfigurationSection(key);

            Material material = Material.getMaterial(item.getString("Material"));
            if(material == null) continue;

            ItemBuilder builder = new ItemBuilder(material);
            builder.setName(item.getString("Name").replace("&","§"));
            builder.setLore(
                    item.getStringList("Lore").stream()
                            .map($ -> $.replace("&","§")).collect(Collectors.toList())
            );
            builder.setDurability((short) item.getInt("Data"));


            ItemStack buildedItem = builder.toItemStack();
            buildedItem = NBTApi.setNBTData(buildedItem, "power", key);
            if(item.isSet("Price")) {
                final int price = item.getInt("Price");
                buildedItem = NBTApi.setNBTData(buildedItem, "price", String.valueOf(price));
            }

            switch(key){
                case "Lightning":
                    LIGHTNINGSTRIKE_ITEM = buildedItem;
                    break;
                case "Fireball":
                    FIREBALL_ITEM = buildedItem;
                    break;
                case "MoreHearth":
                    HEARTH_ITEM = buildedItem;
                    break;
                default:
                    getLogger().warning("Item " + key + " not found for registry...");
            }
        }
    }
}
