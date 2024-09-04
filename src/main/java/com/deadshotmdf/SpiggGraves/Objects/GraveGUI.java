package com.deadshotmdf.SpiggGraves.Objects;

import com.deadshotmdf.SpiggGraves.SP;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;

public class GraveGUI implements GUIModel{

    private final SP main;
    private final Grave grave;
    private boolean locked;

    public GraveGUI(SP main, Grave grave){
        this.main = main;
        this.grave = grave;
        this.locked = false;
    }

    @Override
    public void onOpen(InventoryOpenEvent ev){
        if(this.locked)
            ev.setCancelled(true);
    }

    @Override
    public void onClick(InventoryClickEvent ev) {
        InventoryView view = ev.getView();
        Inventory clicked = ev.getClickedInventory();

        if(clicked == null)
            return;

        boolean isTop = clicked == view.getTopInventory(), isNull = isNull(ev.getWhoClicked().getItemOnCursor());
        ev.setCancelled((isTop && !isNull) || (!isTop && ev.isShiftClick()));

        if(!ev.isCancelled())
            Bukkit.getScheduler().runTask(main, this::checkItems);
    }

    private static boolean isNull(ItemStack item){
        return item == null || item.getType() == Material.AIR;
    }

    //Equip armor. A bit important, don't forget about it in the final build plsssss
    public void equipStuff(){

    }

    public void checkItems(){
        grave.refreshItemsSize();

        if(grave.getItemsSize() > 0) {
            grave.refreshItemsSize();
            return;
        }

        this.locked = true;
        new HashSet<>(grave.getInv().getViewers()).forEach(HumanEntity::closeInventory);
    }
}
