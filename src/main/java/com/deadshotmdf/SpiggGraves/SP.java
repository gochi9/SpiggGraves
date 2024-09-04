package com.deadshotmdf.SpiggGraves;

import com.deadshotmdf.SpiggGraves.Commands.ReloadConfigCommand;
import com.deadshotmdf.SpiggGraves.Commands.SeeGravesGUICommands;
import com.deadshotmdf.SpiggGraves.Listeners.*;
import com.deadshotmdf.SpiggGraves.Managers.GUIManager;
import com.deadshotmdf.SpiggGraves.Managers.GravesManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class SP extends JavaPlugin {

    private static Economy econ;
    private GravesManager gravesManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ConfigSettings.reloadConfig(this);
        this.guiManager = new GUIManager(this);
        this.gravesManager = new GravesManager(this, guiManager);
        this.guiManager.setGravesManager(this.gravesManager);

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerDeathListener(this, gravesManager), this);
        pm.registerEvents(new PlayerInteractListener(gravesManager, guiManager), this);
        pm.registerEvents(new PreventCompassDrop(this, gravesManager), this);
        pm.registerEvents(new CompassClickListener(this, gravesManager), this);
        pm.registerEvents(new GUIListener(guiManager), this);
        pm.registerEvents(new ChunkLoadListener(), this);

        this.getCommand("spigggravesreload").setExecutor(new ReloadConfigCommand(this, gravesManager));
        this.getCommand("sgraves").setExecutor(new SeeGravesGUICommands(guiManager));

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, gravesManager, 20, 20);
    }

    @Override
    public void onDisable() {
        gravesManager.onDisable();
        Bukkit.getOnlinePlayers().forEach(HumanEntity::closeInventory);
    }

    public Economy getEconomy() {
        return econ;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;

        econ = rsp.getProvider();
        return econ != null;
    }

}
