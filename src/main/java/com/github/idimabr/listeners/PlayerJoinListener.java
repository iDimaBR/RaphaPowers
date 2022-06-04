package com.github.idimabr.listeners;

import com.github.idimabr.RaphaPowers;
import com.github.idimabr.storage.SQLManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private RaphaPowers plugin;

    public PlayerJoinListener(RaphaPowers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        final Player player = e.getPlayer();
        player.setMaxHealth(20);
        SQLManager.loadHealth(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        final Player player = e.getPlayer();
        SQLManager.TIMERS.remove(player.getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e){
        final Player player = e.getPlayer();
        SQLManager.TIMERS.remove(player.getUniqueId());
    }
}
