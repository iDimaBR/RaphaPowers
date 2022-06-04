package com.github.idimabr.listeners;

import com.github.idimabr.RaphaPowers;
import com.github.idimabr.storage.SQLManager;
import com.github.idimabr.utils.NBTApi;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import java.util.stream.Collectors;

public class PowerUseListener implements Listener {

    private RaphaPowers plugin;

    public PowerUseListener(RaphaPowers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        final ItemStack item = e.getItem();
        final Player player = e.getPlayer();

        if(!e.getAction().toString().contains("RIGHT")) return;
        if(!item.hasItemMeta()) return;
        if(!item.getItemMeta().hasDisplayName()) return;
        if(!NBTApi.hasTag(item, "power")) return;

        final FileConfiguration config = plugin.getConfig();
        final String itemKey = NBTApi.getTag(item, "power");

        switch(itemKey){
            case "Lightning":
                player.getWorld().strikeLightningEffect(player.getLocation());
                final int radius = config.getInt("Item.Lightning.Radius");
                final int damage = config.getInt("Item.Lightning.Damage");

                for (Entity entity : player.getNearbyEntities(radius, radius, radius).stream().filter($ -> $.getType().equals(EntityType.PLAYER)).collect(Collectors.toList())) {
                    Player target = (Player) entity;
                    target.getWorld().strikeLightningEffect(target.getLocation());
                    target.damage(damage);
                }
                break;
            case "Fireball":
                final double speed = config.getDouble("Item.Fireball.Damage");
                shootFireball(player, speed, player.getLocation());
                break;
            case "MoreHearth":
                double playerHealth = player.getMaxHealth();
                int newHealth = Integer.parseInt(NBTApi.getTag(item, "health")) + 20;
                int time = Integer.parseInt(NBTApi.getTag(item, "time"));

                if(playerHealth > newHealth){
                    plugin.getConfig().getStringList("Messages.Health.MoreThanHealth").stream().map($ -> $
                            .replace("&","§")
                            .replace("%life%",newHealth+"")
                            .replace("%oldlife%",playerHealth+"")
                            .replace("%time%",time+"")
                    ).forEach(player::sendMessage);
                    return;
                }

                player.setMaxHealth(newHealth);
                player.setHealth(newHealth);
                plugin.getConfig().getStringList("Messages.Health.Activate").stream().map($ -> $
                        .replace("&","§")
                        .replace("%life%",newHealth+"")
                        .replace("%oldlife%",playerHealth+"")
                        .replace("%time%",time+"")
                ).forEach(player::sendMessage);
                SQLManager.newHealth(player.getUniqueId(), newHealth, time);
                break;
        }
        consumeItem(player, player.getItemInHand());
    }

    private void shootFireball(Player player, Double speed, Location shootLocation) {
        Vector directionVector = shootLocation.getDirection().normalize();
        double startShift = 2;
        Vector shootShiftVector = new Vector(directionVector.getX() * startShift, directionVector.getY() * startShift, directionVector.getZ() * startShift);
        shootLocation = shootLocation.add(shootShiftVector.getX(), shootShiftVector.getY() + 0.6, shootShiftVector.getZ());

        Fireball fireball = shootLocation.getWorld().spawn(shootLocation, Fireball.class);
        fireball.setMetadata("launcher", new FixedMetadataValue(plugin, player.getName()));
        fireball.setVelocity(directionVector.multiply(speed));
        fireball.setIsIncendiary(false);
    }

    private boolean consumeItem(Player player, ItemStack hand){
        int itemAmount = hand.getAmount();
        ItemStack item = hand.clone();
        if(itemAmount > 1){
            item.setAmount(item.getAmount() - 1);
            player.setItemInHand(item);
            return true;
        }
        if(itemAmount == 1){
            player.setItemInHand(null);
            return true;
        }
        return false;
    }
}
