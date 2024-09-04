package com.deadshotmdf.SpiggGraves.Commands;

import com.deadshotmdf.SpiggGraves.ConfigSettings;
import com.deadshotmdf.SpiggGraves.Managers.GravesManager;
import com.deadshotmdf.SpiggGraves.SP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadConfigCommand implements CommandExecutor {

    private final SP main;
    private final GravesManager gravesManager;

    public ReloadConfigCommand(SP main, GravesManager gravesManager) {
        this.main = main;
        this.gravesManager = gravesManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player && !sender.hasPermission("spiffgrave.reload")) {
            sender.sendMessage(ConfigSettings.getNoPermission());
            return true;
        }

        ConfigSettings.reloadConfig(main);
        gravesManager.reloadConfig();
        sender.sendMessage(ConfigSettings.getReloadConfig());
        return true;
    }
}
