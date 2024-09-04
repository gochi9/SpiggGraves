package com.deadshotmdf.SpiggGraves.Listeners;

import com.deadshotmdf.SpiggGraves.Managers.GUIManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class GUIListener implements Listener {

    private final GUIManager guiManager;

    public GUIListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent ev) {
        guiManager.onOpen(ev);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent ev) {
        guiManager.onClick(ev);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent ev) {
        guiManager.onClose(ev);
    }

}
