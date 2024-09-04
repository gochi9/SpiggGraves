package com.deadshotmdf.SpiggGraves.Listeners;

import com.deadshotmdf.SpiggGraves.ConfigSettings;
import com.deadshotmdf.SpiggGraves.Managers.GUIManager;
import com.deadshotmdf.SpiggGraves.Objects.GUIModel;
import com.deadshotmdf.SpiggGraves.Objects.Grave;
import com.deadshotmdf.SpiggGraves.Managers.GravesManager;
import com.deadshotmdf.SpiggGraves.Objects.GraveGUI;
import com.deadshotmdf.SpiggGraves.Utils.GraveUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.LinkedList;
import java.util.List;

public class PlayerInteractListener implements Listener {

    private final GravesManager gravesManager;
    private final GUIManager guiManager;

    public PlayerInteractListener(GravesManager gravesManager, GUIManager guiManager){
        this.gravesManager = gravesManager;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent ev){
        if(ev.getHand() != EquipmentSlot.HAND)
            return;

        Player player = ev.getPlayer();
        boolean open;

        if(!(((open = ev.getAction() == Action.RIGHT_CLICK_BLOCK) || (ev.getAction() == Action.LEFT_CLICK_BLOCK))))
            return;

        Block block = ev.getClickedBlock();

        if(block == null || block.getType() != Material.BARRIER)
            return;

        Grave grave = gravesManager.getGraveAtLocation(block.getLocation());

        if(grave == null || (ConfigSettings.isOnlyOwnerCanOpen() && !(grave.isOwner(player.getUniqueId()) || player.hasPermission("spiffgrave.accessgrave"))))
            return;

        ev.setCancelled(true);
        ev.setUseInteractedBlock(Event.Result.DENY);
        ev.setUseItemInHand(Event.Result.DENY);

        if(!open){
//            block.setType(Material.AIR);
//            gravesManager.removeGrave(block.getLocation(), true);
            return;
        }

        if(!player.isSneaking()){
            player.openInventory(grave.getInv());
            return;
        }

        Inventory inv = grave.getInv();
        PlayerInventory playerInventory = player.getInventory();

        if(inv.isEmpty())
            return;

        List<ItemStack> drops = new LinkedList<>();
        for (ItemStack item : inv)
            if(item != null)
                drops.addAll(playerInventory.addItem(item).values());

        World w = player.getWorld();
        Location loc = player.getLocation();
        drops.forEach(item -> {
            if(GraveUtils.isItemValid(item))
                w.dropItem(loc, item);
        });

        GUIModel gui = guiManager.getGUI(inv);
        inv.clear();

        if(gui instanceof  GraveGUI)
            ((GraveGUI)gui).checkItems();
    }

}
