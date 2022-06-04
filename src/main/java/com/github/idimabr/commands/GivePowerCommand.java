package com.github.idimabr.commands;

import com.github.idimabr.RaphaPowers;
import com.github.idimabr.utils.NBTApi;
import com.ystoreplugins.ypoints.api.yPointsAPI;
import com.ystoreplugins.ypoints.models.PlayerPoints;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.stream.Collectors;

public class GivePowerCommand implements CommandExecutor {

    private RaphaPowers plugin;

    public GivePowerCommand(RaphaPowers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        if(args.length >= 3){
            final Player target = Bukkit.getPlayer(args[0]);
            if(target == null){
                sender.sendMessage("§cJogador " + args[0] + " não encontrado!");
                return false;
            }

            PlayerPoints targetPoints = yPointsAPI.getAccount(target.getName());

            if(!NumberUtils.isNumber(args[2])){
                sender.sendMessage("§cQuantidade inválida!");
                return false;
            }
            final int amount = Integer.parseInt(args[2]);

            int price = 0;
            switch(args[1]){
                case "strike":
                    ItemStack lightningstrike_item = plugin.LIGHTNINGSTRIKE_ITEM.clone();
                    price = Integer.parseInt(NBTApi.getTag(lightningstrike_item, "price"));
                    if(!yPointsAPI.has(target.getName(), price)){
                        sender.sendMessage("§cO jogador não tem cash suficiente.");
                        return false;
                    }

                    yPointsAPI.withdraw(target.getName(), price, true);
                    for(int i = 0;i < amount;i++)
                        target.getInventory().addItem(lightningstrike_item);
                    break;
                case "fireball":
                    ItemStack fireball_item = plugin.FIREBALL_ITEM.clone();
                    price = Integer.parseInt(NBTApi.getTag(fireball_item, "price"));
                    if(!yPointsAPI.has(target.getName(), price)){
                        sender.sendMessage("§cO jogador não tem cash suficiente.");
                        return false;
                    }

                    yPointsAPI.withdraw(target.getName(), price, true);
                    for(int i = 0;i < amount;i++)
                        target.getInventory().addItem(fireball_item);
                    break;
                case "health":
                    if(args.length < 5){
                        sender.sendMessage("§cUtilize /powergive <player> health <amount> <lifeAmount> <lifeTime in seconds> <price:optional>");
                        return false;
                    }

                    if(!NumberUtils.isNumber(args[3])){
                        sender.sendMessage("§cNúmero de vida inválido!");
                        return false;
                    }
                    final int life = Integer.parseInt(args[3]);

                    if(!isNumberOfMultiple(life)) {
                        sender.sendMessage("§cA quantidade de vida tem que ser um numero PAR!");
                        return false;
                    }
                    final int time = Integer.parseInt(args[4]);

                    if(args.length > 5){
                        if(!NumberUtils.isNumber(args[5])){
                            sender.sendMessage("§cPreço invalido!");
                            return false;
                        }
                        price = Integer.parseInt(args[5]);
                        if(!yPointsAPI.has(target.getName(), price)){
                            sender.sendMessage("§cO jogador não tem cash suficiente.");
                            return false;
                        }

                        yPointsAPI.withdraw(target.getName(), price, true);
                    }

                    ItemStack item = plugin.HEARTH_ITEM.clone();
                    item = NBTApi.setNBTData(item, "health", String.valueOf(life));
                    item = NBTApi.setNBTData(item, "time", String.valueOf(time));
                    item = NBTApi.setNBTData(item, "random", RandomStringUtils.random(20));
                    ItemMeta itemMeta = item.getItemMeta();
                    itemMeta.setLore(
                            itemMeta.getLore().stream()
                                    .map($ ->
                                            $.replace("&","§")
                                                    .replace("%life%", String.valueOf(life))
                                                    .replace("%time%", String.valueOf(time))
                                    ).collect(Collectors.toList())
                    );
                    item.setItemMeta(itemMeta);

                    for(int i = 0;i < amount;i++)
                        target.getInventory().addItem(item);
                    break;
                default:
                    sender.sendMessage("§cUtilize /powergive <player> <strike/fireball/health> <amount>");
                    return false;
            }

            sender.sendMessage("§aItem enviado para o jogador §f" + target.getName());
        }else{
            sender.sendMessage("§cUtilize /powergive <player> <strike/fireball/health> <amount>");
        }
        return false;
    }

    private boolean isNumberOfMultiple(int number){
        return (number % 2) == 0;
    }
}
