package me.gianmattia.GravityReal;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUtility implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        //If the command is sent by console e.g.
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is runnable only by players");
            return true;
        }

        //If the command doesn't have any arguments (is one and stop e.g /gravity, /setspawnlobby)
        if (args.length == 0) {
            if (command.getName().equalsIgnoreCase("coords")) {
                player.sendMessage(ChatColor.YELLOW + "Coordinates: ");
                player.sendMessage(ChatColor.YELLOW + " world: " + ChatColor.GRAY + player.getWorld().getName());
                player.sendMessage(ChatColor.YELLOW + " x: " + ChatColor.GRAY + player.getLocation().getX());
                player.sendMessage(ChatColor.YELLOW + " y: " + ChatColor.GRAY + player.getLocation().getY());
                player.sendMessage(ChatColor.YELLOW + " z: " + ChatColor.GRAY + player.getLocation().getZ());

                return true;
            }
            else if (command.getName().equalsIgnoreCase("gmc")) {
                player.setGameMode(GameMode.CREATIVE);

                return true;
            }

        }

        return false;
    }
}
