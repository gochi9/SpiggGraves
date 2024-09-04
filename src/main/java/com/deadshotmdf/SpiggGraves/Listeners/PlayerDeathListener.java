package com.deadshotmdf.SpiggGraves.Listeners;

import com.deadshotmdf.SpiggGraves.ConfigSettings;
import com.deadshotmdf.SpiggGraves.Managers.GravesManager;
import com.deadshotmdf.SpiggGraves.Objects.Grave;
import com.deadshotmdf.SpiggGraves.SP;
import com.deadshotmdf.SpiggGraves.Utils.GraveUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PlayerDeathListener implements Listener {

    private final SP main;
    private final GravesManager gravesManager;

    public PlayerDeathListener(SP main, GravesManager gravesManager) {
        this.main = main;
        this.gravesManager = gravesManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent ev) {
        Player player = ev.getEntity();
        EntityDamageEvent e = ev.getEntity().getLastDamageCause();

        if(ev.getDrops().isEmpty())
            return;

        Location playerLoc = player.getLocation();
        gravesManager.placeGrave(player.getName(), player.getUniqueId(), new LinkedList<>(ev.getDrops()), playerLoc, e == null ? null : e.getCause(), ConfigSettings.getGraveTimer(), ev.getDroppedExp() / ConfigSettings.getExpRemain());
        ev.getDrops().clear();
        ev.setDroppedExp(0);
        player.sendMessage(ConfigSettings.getOnDeathMessage(playerLoc.getWorld().getName(), playerLoc.getBlockX(), playerLoc.getBlockY(), playerLoc.getBlockZ()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent ev){
        if(!ConfigSettings.isGiveGraveCompassOnRespawn())
            return;

        Player player = ev.getPlayer();
        List<Grave> graves = gravesManager.getGraves(player.getUniqueId());

        if(graves == null || graves.isEmpty())
            return;

        Grave grave = graves.get(graves.size() - 1);

        if(grave == null)
            return;

        Location loc = grave.getLocation();
        Location playerLoc = player.getLocation();
        double distance = loc.getWorld().equals(playerLoc.getWorld()) ? loc.distance(playerLoc) : 5000;
        player.getInventory().addItem(GraveUtils.createGraveCompass(main, loc, grave.getTimer(), GraveUtils.calculatePrice(loc, playerLoc, distance), distance));
    }

    private ItemStack[] extraItems(PlayerInventory inventory, Collection<ItemStack> items){
        ItemStack[] extra = new ItemStack[5];
        Iterator<ItemStack> iterator = items.iterator();

        while(iterator.hasNext()){
            ItemStack item = iterator.next();

            if(!GraveUtils.isItemValid(item))
                continue;

            ItemStack helmet = inventory.getHelmet(), chestplate = inventory.getChestplate(), leggings = inventory.getLeggings(), boots = inventory.getBoots(), offHand = inventory.getItemInOffHand();

            if(GraveUtils.isItemValid(helmet) && helmet.equals(item)){
                extra[0] = item;
                iterator.remove();
            }

            if(GraveUtils.isItemValid(chestplate) && chestplate.equals(item)){
                extra[1] = item;
                iterator.remove();
            }

            if(GraveUtils.isItemValid(leggings) && leggings.equals(item)){
                extra[2] = item;
                iterator.remove();
            }

            if(GraveUtils.isItemValid(boots) && boots.equals(item)){
                extra[3] = item;
                iterator.remove();
            }

            if(GraveUtils.isItemValid(offHand) && offHand.equals(item)){
                extra[4] = item;
                iterator.remove();
            }
        }

        return extra;
    }

}
