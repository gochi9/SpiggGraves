package com.deadshotmdf.SpiggGraves.Objects;

import com.deadshotmdf.SpiggGraves.Managers.GravesManager;
import com.deadshotmdf.SpiggGraves.SP;
import com.deadshotmdf.SpiggGraves.Utils.GraveUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GravesCompassGUI implements GUIModel{

    private final SP main;
    private final GravesManager gravesManager;
    private final Inventory inventory;

    public GravesCompassGUI(SP main, GravesManager gravesManager, List<Grave> graveList, Location playerLoc) {
        this.main = main;
        this.gravesManager = gravesManager;
        int listSize = graveList.size();

        int size;
        if(listSize <= 0)
            size = 9;
        else if(listSize >= 54)
            size = 54;
        else
            size = (int) (Math.ceil(listSize / 9.0D) * 9);

        inventory = Bukkit.createInventory(null, size, "");
        int i = 0;
        for(Grave grave : graveList) {
            if(i >= 54)
                break;

            Location loc = grave.getLocation();
            double distance = loc.getWorld().equals(playerLoc.getWorld()) ? loc.distance(playerLoc) : 5000;
            inventory.addItem(GraveUtils.createGUIItem(main, grave.getLocation(), grave.getTimer(), GraveUtils.calculatePrice(grave.getLocation(), playerLoc, distance), distance, grave.getOwner()));
            ++i;
        }
    }

    public void onOpen(InventoryOpenEvent ev){}

    public void onClick(InventoryClickEvent ev){
        ev.setCancelled(true);

        if(ev.getRawSlot() > 53)
            return;

        ItemStack item = ev.getCurrentItem();
        Location loc = GraveUtils.getGraveLocation(main, item);

        if(loc == null)
            return;

        Player player = (Player) ev.getWhoClicked();
        switch (ev.getClick()){
            case LEFT:
                gravesManager.giveGraveCompass(player, loc);
                player.closeInventory();
                break;
            case SHIFT_RIGHT:
                gravesManager.teleportPlayer(player, loc);
                player.closeInventory();
                break;
            case SHIFT_LEFT:
                if(!player.hasPermission("spigggraves.openothers"))
                    break;

                Grave grave = gravesManager.getGraveAtLocation(loc);

                if(grave != null && !grave.getOwner().equals(player.getUniqueId()))
                    player.openInventory(grave.getInv());

                break;
        }

    }

    public Inventory getInventory(){
        return inventory;
    }

}
