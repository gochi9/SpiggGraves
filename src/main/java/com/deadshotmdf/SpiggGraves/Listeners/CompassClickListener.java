package com.deadshotmdf.SpiggGraves.Listeners;

import com.deadshotmdf.SpiggGraves.ConfigSettings;
import com.deadshotmdf.SpiggGraves.Utils.GraveUtils;
import com.deadshotmdf.SpiggGraves.Managers.GravesManager;
import com.deadshotmdf.SpiggGraves.SP;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class CompassClickListener implements Listener {

    private final SP main;
    private final GravesManager gravesManager;

    public CompassClickListener(SP main, GravesManager gravesManager) {
        this.main = main;
        this.gravesManager = gravesManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCompassClick(PlayerInteractEvent ev) {
        if(ev.getHand() != EquipmentSlot.HAND)
            return;

        ItemStack item = ev.getItem();

        if(item == null)
            return;

        Location location = GraveUtils.getGraveLocation(main, item);

        if(location == null)
            return;

        Player player = ev.getPlayer();

        if(gravesManager.getGraveAtLocation(location) == null){
            player.getInventory().removeItem(item);
            player.sendMessage(ConfigSettings.getGraveNoLongerExists());
            return;
        }

        long cooldown;
        UUID uuid = player.getUniqueId();
        Location playerLoc = player.getLocation();

        if(!playerLoc.getWorld().equals(location.getWorld()))
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ConfigSettings.getNotInSameWorld(location.getWorld().getName())));
        else if((cooldown = gravesManager.canClickCompass(uuid)) <= 0){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ConfigSettings.getGraveDistanceMessage(playerLoc.distance(location))));
            gravesManager.addCooldown(uuid);
        }
        else
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ConfigSettings.graveDistanceCooldownMessage(cooldown)));
    }

}
