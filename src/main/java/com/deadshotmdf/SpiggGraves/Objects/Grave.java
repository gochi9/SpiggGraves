package com.deadshotmdf.SpiggGraves.Objects;

import com.deadshotmdf.SpiggGraves.ConfigSettings;
import com.deadshotmdf.SpiggGraves.Managers.GUIManager;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Grave {

    private final Inventory inv;
    private final UUID owner;
    private final String playerName;
    private final Hologram hologram;
    private int timer;
    private final int exp;
    private final Location location;
    private int items;

    public Grave(GUIManager guiManager, UUID owner, String playerName, Hologram hologram, int timer, int exp, List<ItemStack> items, Location location) {
        this.owner = owner;
        this.playerName = playerName;
        this.hologram = hologram;
        this.timer = timer;
        this.exp = exp;
        this.location = location;

        int rows = (int) Math.ceil(items.size() / 9.0D);

        if(rows <= 0)
            rows = 1;
        if(rows > 6)
            rows = 6;

        this.inv = Bukkit.createInventory(null, rows * 9, ConfigSettings.getGraveInventoryName(playerName));
        for(int i = 0; i < items.size(); i++)
            inv.setItem(i, items.get(i));

        refreshItemsSize();

        if(guiManager != null)
            guiManager.registerGraveGUI(inv, this);
    }

    public UUID getOwner(){
        return owner;
    }

    public boolean isOwner(UUID uuid){
        return owner.equals(uuid);
    }

    public String getPlayerName(){
        return playerName;
    }

    public Hologram getHologram(){
        return hologram;
    }

    public int getExp(){
        return exp;
    }

    public Inventory getInv(){
        return inv;
    }

    public Location getLocation(){
        return location;
    }

    public void updateHologram(){
        List<String> lines = ConfigSettings.getHologramLines(playerName, ConfigSettings.formatTimer(timer), getItemsSize(), exp);
        HologramPage page = hologram.getPage(0);

        if(page.getLines().size() < lines.size()){
            lines.forEach(line -> DHAPI.addHologramLine(hologram, line));
            return;
        }

        for(int i = 0; i < lines.size(); i++)
            DHAPI.setHologramLine(hologram, i, lines.get(i));
    }

    public int tickTimer(){
        return timer--;
    }

    public int getTimer(){
        return timer;
    }

    public List<ItemStack> getItems(){
        List<ItemStack> items = new LinkedList<>();
        for(ItemStack item : inv)
            if(item != null)
                items.add(item);

        return items;
    }

    public void refreshItemsSize(){
        items = 0;
        for(ItemStack item : inv)
            if(item != null)
                ++items;
    }

    public int getItemsSize(){
        return items;
    }

    public void removeInventory(){
        new HashSet<>(inv.getViewers()).forEach(HumanEntity::closeInventory);
        World w = location.getWorld();

        for(ItemStack item : inv)
            if(item != null)
                w.dropItemNaturally(location, item);

        inv.clear();
    }

    @Override
    public String toString(){
        return owner.toString() + "#" + playerName + "#" + timer + "#" + exp;
    }

}
