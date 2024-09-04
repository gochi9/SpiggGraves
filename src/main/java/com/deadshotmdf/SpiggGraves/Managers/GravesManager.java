package com.deadshotmdf.SpiggGraves.Managers;

import com.deadshotmdf.SpiggGraves.*;
import com.deadshotmdf.SpiggGraves.Objects.Grave;
import com.deadshotmdf.SpiggGraves.Save.SaveFile;
import com.deadshotmdf.SpiggGraves.Utils.GraveUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GravesManager implements Runnable{

    private final SP main;
    private final GUIManager guiManager;
    private final ConcurrentHashMap<Location, Grave> graves;
    private final HashMap<UUID, List<Grave>> playerGraves;
    private final HashMap<UUID, Long> clickCooldown;
    private final SaveFile saveFile;

    public GravesManager(SP main, GUIManager guiManager){
        this.main = main;
        this.guiManager = guiManager;
        this.graves = new ConcurrentHashMap<>();
        this.playerGraves = new HashMap<>();
        this.clickCooldown = new HashMap<>();
        this.saveFile = new SaveFile(main);
        this.saveFile.loadFile(graves);
        reloadConfig();
    }

    AtomicInteger atomicInt = new AtomicInteger(0);
    public void run(){
        Set<Location> toRemove = new HashSet<>();

        for(Map.Entry<Location, Grave> entry : graves.entrySet()){
            Grave grave = entry.getValue();

            if(grave.getItemsSize() > 0 && grave.tickTimer() > 0){
                grave.updateHologram();
                continue;
            }

            toRemove.add(entry.getKey());
        }

        if(!toRemove.isEmpty())
            Bukkit.getScheduler().runTask(main, () -> toRemove.forEach(loc -> removeGrave(loc, true)));

        if(atomicInt.getAndIncrement() > 10){
            atomicInt.set(0);
            Bukkit.getScheduler().runTask(main, this::syncRun);
        }
    }

    private void syncRun(){
        for(Grave grave : graves.values()){
            Location loc = grave.getLocation();
            World w = loc.getWorld();
            int x = loc.getBlockX() >> 4, z = loc.getBlockZ() >> 4;
            if(w.isChunkLoaded(x, z) || w.isChunkForceLoaded(x,z))
                loc.getBlock().setType(Material.BARRIER);
        }
    }

    public void addCooldown(UUID uuid){
        clickCooldown.put(uuid, System.currentTimeMillis() + 5000);
    }

    public Grave getGraveAtLocation(Location loc){
        return graves.get(loc);
    }

    public List<Grave> getGraves(UUID uuid){
        return new LinkedList<>(playerGraves.getOrDefault(uuid, new LinkedList<>()));
    }

    public void onDisable(){
        saveFile.saveFile(graves);
        new HashSet<>(graves.keySet()).forEach(loc -> removeGrave(loc, false));
    }

    public void reloadConfig(){
        HashMap<Location, Grave> newGraves = new HashMap<>(graves);
        newGraves.keySet().forEach(loc -> removeGrave(loc, false));
        graves.clear();
        playerGraves.clear();
        newGraves.forEach((k, v) -> placeGrave(v.getPlayerName(), v.getOwner(), v.getItems(), k, null, v.tickTimer(), v.getExp()));
    }

    public void placeGrave(String name, UUID uuid, List<ItemStack> items, Location location, EntityDamageEvent.DamageCause cause, int timer, int exp){
        location = fixLocation(cause, location.getBlock().getLocation());

        if(location.getY() != location.getWorld().getMaxHeight() - 1 && !location.getBlock().getType().toString().endsWith("AIR"))
            location.add(0,1,0);

        Hologram hologram = DHAPI.createHologram(UUID.randomUUID() + "_" + UUID.randomUUID() + "_" + uuid, location.clone().add(0.5, ConfigSettings.getHologramYOffset(), 0.5));
        Grave grave = new Grave(guiManager, uuid, name, hologram, timer, exp, items, location);
        grave.updateHologram();
        graves.put(location, grave);
        playerGraves.computeIfAbsent(uuid, k -> new LinkedList<>()).add(grave);

        location.getBlock().setType(Material.BARRIER);

        try(MythicBukkit idk = MythicBukkit.inst()){
            idk.getAPIHelper().spawnMythicMob(ConfigSettings.getGraveEntityID(), location.clone().add(0.5,0,0.5));
        }
        catch (Throwable e) {}
    }

    public void removeGrave(Location loc, boolean removeInventory){
        Grave grave = graves.remove(loc);

        if(grave == null)
            return;

        GraveUtils.removeEntity(loc, null, true, false);
        int exp = grave.getExp();

        if(exp > 0)
            ((ExperienceOrb)loc.getWorld().spawnEntity(loc, EntityType.EXPERIENCE_ORB)).setExperience(exp);

        loc.getBlock().setType(Material.AIR);
        guiManager.unregisterGUI(grave.getInv());
        Hologram hologram = grave.getHologram();

        if(hologram != null)
            hologram.delete();

        playerGraves.computeIfAbsent(grave.getOwner(), k -> new LinkedList<>()).remove(grave);

        if(removeInventory)
            grave.removeInventory();
    }

    public void giveGraveCompass(Player player, Location loc){
        player.closeInventory();

        if(!loc.getWorld().equals(player.getWorld())){
            player.sendMessage(ConfigSettings.getNotInSameWorld(loc.getWorld().getName()));
            return;
        }

        Grave grave = graves.get(loc);
        if(grave == null){
            player.sendMessage(ConfigSettings.getGraveNoLongerExists());
            return;
        }

        Location playerLoc = player.getLocation();
        double distance = loc.getWorld().equals(playerLoc.getWorld()) ? loc.distance(playerLoc) : 5000;
        player.getInventory().addItem(GraveUtils.createGraveCompass(main, loc, grave.getTimer(), GraveUtils.calculatePrice(loc, playerLoc, distance), distance));
    }

    public long canClickCompass(UUID uuid){
        Long cooldown = clickCooldown.get(uuid);

        if(cooldown == null)
            return 0;

        return cooldown - System.currentTimeMillis();
    }

    public void teleportPlayer(Player player, Location loc){
        Economy economy = main.getEconomy();
        double balance = economy.getBalance(player);
        Location playerLoc = player.getLocation();
        double price = GraveUtils.calculatePrice(loc, player.getLocation(), loc.getWorld().equals(playerLoc.getWorld()) ? loc.distance(playerLoc) : 5000);

        if(price > balance){
            player.sendMessage(ConfigSettings.getTooPoor(balance, price));
            return;
        }

        economy.withdrawPlayer(player, price);
        player.sendMessage(ConfigSettings.getTeleported(economy.getBalance(player), price));
        player.teleport(loc.clone().add(0.5,1,0.5));
    }

    private Location fixLocation(EntityDamageEvent.DamageCause cause, Location loc){
        if(cause == null)
            return loc;

        int y = loc.getBlockY();
        World w = loc.getWorld();
        World.Environment env = w.getEnvironment();

        if(env == World.Environment.THE_END && y < 0)
            loc.setY(0);
        else if(y < -59)
            loc.setY(-59);

        int max = w.getMaxHeight();
        if(y >= max)
            loc.setY(max - 1);

        if (cause == EntityDamageEvent.DamageCause.LAVA) {
            Location clone = loc.clone();
            while (y < max) {
                clone.setY(y++);

                if (clone.getBlock().getType() != Material.LAVA)
                    return clone;
            }
        }

        return loc;
    }

}