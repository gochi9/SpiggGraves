package com.deadshotmdf.SpiggGraves.Listeners;

import com.deadshotmdf.SpiggGraves.ConfigSettings;
import com.deadshotmdf.SpiggGraves.Managers.GravesManager;
import com.deadshotmdf.SpiggGraves.Utils.GraveUtils;
import com.deadshotmdf.SpiggGraves.SP;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PreventCompassDrop implements Listener {

    private final SP main;
    private final GravesManager gravesManager;

    public PreventCompassDrop(SP main, GravesManager gravesManager) {
        this.main = main;
        this.gravesManager = gravesManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent ev){
        if(ConfigSettings.isCanPlayerGiveOutCompass() || GraveUtils.getGraveLocation(main, ev.getItemDrop().getItemStack()) == null)
            return;
        
        ev.getItemDrop().setPickupDelay(Integer.MAX_VALUE);
        ev.getItemDrop().remove();
    }
    
    @EventHandler
    public void onClick(InventoryClickEvent ev){
        if(ConfigSettings.isCanPlayerGiveOutCompass() || ev.getInventory().getType() == InventoryType.CRAFTING)
            return;

        Location location = GraveUtils.getGraveLocation(main, ev.getCurrentItem());
        if(location == null)
            return;

        ev.setCancelled(true);

        if(gravesManager.getGraveAtLocation(location) == null)
            ev.setCurrentItem(null);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent ev){
        if(ConfigSettings.isCanPlayerGiveOutCompass())
            return;

        ev.getDrops().removeIf(itemStack -> GraveUtils.getGraveLocation(main, itemStack) != null);
    }

}
