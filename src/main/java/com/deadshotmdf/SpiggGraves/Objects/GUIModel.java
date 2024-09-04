package com.deadshotmdf.SpiggGraves.Objects;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public interface GUIModel {

    public void onOpen(InventoryOpenEvent ev);
    public void onClick(InventoryClickEvent ev);

}
