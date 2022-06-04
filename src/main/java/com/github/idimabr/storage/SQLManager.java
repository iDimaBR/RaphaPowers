package com.github.idimabr.storage;

import com.github.idimabr.RaphaPowers;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SQLManager {

    public static Map<UUID, Long> TIMERS = Maps.newConcurrentMap();

    public static boolean loadHealth(UUID loadUUID){
        Connection connection = RaphaPowers.getPlugin().getSQL().getConnectionMySQL();
        try(PreparedStatement ps = connection.prepareStatement("SELECT * FROM players WHERE UUID = ?")) {
            ps.setString(1, loadUUID.toString());
            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()){
                    Player player = Bukkit.getPlayer(loadUUID);
                    if(player == null) continue;

                    UUID uuid = UUID.fromString(rs.getString("UUID"));
                    int health = rs.getInt("life");
                    long time = rs.getLong("time");

                    player.setMaxHealth(health);
                    player.setHealth(health);
                    TIMERS.put(uuid, time);
                }
            }
            return true;
        } catch (Exception error) {
            error.printStackTrace();
        }
        return false;
    }

    public static void newHealth(UUID uuid, int health, int time){
        SQLStorage SQL = RaphaPowers.getPlugin().getSQL();
        Connection connection = SQL.getConnectionMySQL();
        String query = "UPDATE players SET life = ?, time = ? WHERE UUID = ?";
        if(!SQL.contains(uuid))
            query = "INSERT INTO players(`UUID`,`life`,`time`) VALUES (?,?,?)";

        long timing = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(time);
        try(PreparedStatement ps = connection.prepareStatement(query)) {
            if(query.contains("UPDATE")){
                ps.setInt(1, health);
                ps.setLong(2, timing);
                ps.setString(3, uuid.toString());
            }else{
                ps.setString(1,uuid.toString());
                ps.setInt(2, health);
                ps.setLong(3, timing);
            }
            TIMERS.put(uuid, timing);
            ps.executeUpdate();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static void saveAllHealths(){
        for(Map.Entry<UUID, Long> entry : TIMERS.entrySet()) {
            final UUID uuid = entry.getKey();
            final Player player = Bukkit.getPlayer(uuid);
            if(player == null) continue;
            final int time = (int) TimeUnit.MILLISECONDS.convert(entry.getValue(), TimeUnit.SECONDS);
            final int health = (int) player.getMaxHealth();

            newHealth(uuid, health, time);
        }
    }
}
