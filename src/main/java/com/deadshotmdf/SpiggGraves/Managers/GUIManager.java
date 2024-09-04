package com.deadshotmdf.SpiggGraves.Managers;

import com.deadshotmdf.SpiggGraves.ConfigSettings;
import com.deadshotmdf.SpiggGraves.Objects.GUIModel;
import com.deadshotmdf.SpiggGraves.Objects.GraveGUI;
import com.deadshotmdf.SpiggGraves.Objects.GravesCompassGUI;
import com.deadshotmdf.SpiggGraves.Objects.Grave;
import com.deadshotmdf.SpiggGraves.SP;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GUIManager {

    private final SP main;
    private GravesManager gravesManager;
    private final HashMap<Inventory, GUIModel> guis;

    public GUIManager(SP main) {
        this.main = main;
        guis = new HashMap<>();
    }

    public void registerGraveGUI(Inventory inv, Grave grave){
        this.guis.put(inv, new GraveGUI(main, grave));
    }

    public void openGUI(Player toOpen, UUID target) {
        if(gravesManager == null)
            return;

        List<Grave> graves = gravesManager.getGraves(target);

        if(graves == null || graves.isEmpty()){
            toOpen.sendMessage(ConfigSettings.getNoGraves());
            return;
        }

        GravesCompassGUI gui = new GravesCompassGUI(main, gravesManager, graves, toOpen.getLocation());
        Inventory inv = gui.getInventory();
        guis.put(inv, gui);
        toOpen.openInventory(inv);
    }

    public GUIModel getGUI(Inventory inv){
        return guis.get(inv);
    }

    public void onOpen(InventoryOpenEvent ev){
        GUIModel gui = guis.get(ev.getInventory());

        if(gui != null)
            gui.onOpen(ev);
    }

    public void onClick(InventoryClickEvent ev){
        GUIModel gui = guis.get(ev.getInventory());

        if(gui != null)
            gui.onClick(ev);
    }

    public void onClose(InventoryCloseEvent ev){
        Inventory inv = ev.getInventory();
        GUIModel gui = guis.get(inv);

        if(gui instanceof GravesCompassGUI)
            unregisterGUI(inv);
    }

    public void unregisterGUI(Inventory inv){
        guis.remove(inv);
    }

    public void setGravesManager(GravesManager gravesManager){
        this.gravesManager = gravesManager;
    }

}
