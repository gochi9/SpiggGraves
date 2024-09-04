package com.deadshotmdf.SpiggGraves.Commands;

import com.deadshotmdf.SpiggGraves.ConfigSettings;
import com.deadshotmdf.SpiggGraves.Managers.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SeeGravesGUICommands implements CommandExecutor {

    private final GUIManager guiManager;

    public SeeGravesGUICommands(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Only a player may execute this command");
            return true;
        }

        Player player = (Player) sender;

        if(args.length < 1 || !player.hasPermission("spigggraves.openothers")){
            guiManager.openGUI(player, player.getUniqueId());
            return true;
        }

        guiManager.openGUI(player, Bukkit.getOfflinePlayer(args[0]).getUniqueId());
        return true;
    }
}
