package com.deadshotmdf.SpiggGraves.Utils;

import com.deadshotmdf.SpiggGraves.ConfigSettings;
import com.deadshotmdf.SpiggGraves.Objects.Grave;
import com.deadshotmdf.SpiggGraves.SP;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.MythicBukkit;

import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

public class GraveUtils {

    public static UUID getUUID(String s){
        try{
            return UUID.fromString(s);
        }
        catch(Throwable e){
            return null;
        }
    }

    public static Integer getInteger(String s){
        try{
            return Integer.parseInt(s);
        }
        catch(Throwable e){
            return null;
        }
    }

    public static String locationToString(Location loc){
        return loc.getWorld().getUID() + "#" + loc.getBlockX() + "#" + loc.getBlockY() + "#" + loc.getBlockZ();
    }

    public static Location locationFromString(String loc){
        if(loc == null)
            return null;

        String[] parts = loc.split("#");

        if(parts.length != 4)
            return null;

        UUID w = getUUID(parts[0]);
        World world;

        if(w == null || (world = Bukkit.getWorld(w)) == null)
            return null;

        Integer x = getInteger(parts[1]);
        Integer y = getInteger(parts[2]);
        Integer z = getInteger(parts[3]);

        if(x == null || y == null || z == null)
            return null;

        return new Location(world, x, y, z);
    }

    public static Grave graveFromString(String s, List<ItemStack> items){
        if(s == null)
            return null;

        String[] split = s.split("#");

        if(split.length != 4)
            return null;

        UUID uuid = getUUID(split[0]);
        String name = split[1];
        Integer timer = getInteger(split[2]);
        Integer exp = getInteger(split[3]);

        if(uuid == null || name == null || timer == null || exp == null)
            return null;

        return new Grave(null, uuid, name, null, timer, exp, items, null);
    }

    public static ItemStack createGraveCompass(SP main, Location loc, int timer, double price, double distance){
        ItemStack item = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        meta.setDisplayName(ConfigSettings.getCompassDisplayName(loc, distance, timer, price));
        meta.setLore(ConfigSettings.getCompassLore(loc, distance, timer, price));
        meta.setLodestone(loc);
        meta.setLodestoneTracked(false);
        meta.getPersistentDataContainer().set(new NamespacedKey(main, "GraveGigaCompassTracking"), PersistentDataType.STRING, locationToString(loc));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createGUIItem(SP main, Location loc, int timer, double price, double distance, UUID player){
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayName(ConfigSettings.getGuiItemSelectDisplayName(loc, distance, timer, price));
        meta.setLore(ConfigSettings.getGuiItemSelectLore(Bukkit.getPlayer(player).getLocation(), loc, distance, timer, price));
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(player));
        meta.getPersistentDataContainer().set(new NamespacedKey(main, "GraveGigaCompassTracking"), PersistentDataType.STRING, locationToString(loc));
        item.setItemMeta(meta);
        return item;
    }

    public static Location getGraveLocation(SP main, ItemStack item){
        if(item == null || !(item.getType() == Material.COMPASS || item.getType() == Material.PLAYER_HEAD) || !item.hasItemMeta())
            return null;

        return locationFromString(item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(main, "GraveGigaCompassTracking"), PersistentDataType.STRING));
    }

    public static void removeEntity(Location loc, Entity[] ents, boolean load, boolean respawn){
        try(MythicBukkit idk = MythicBukkit.inst()){
            BukkitAPIHelper apiHelper = idk.getAPIHelper();
            Chunk c = null;

            boolean isNotNull = loc != null;
            if (isNotNull) {
                loc = loc.clone().add(0.5,0,0.5);
                c = loc.getChunk();
                ents = c.getEntities();
            }

            if(load && isNotNull && (!c.isLoaded() || !c.isForceLoaded())){
                loc.getChunk().load();
                ents = c.getEntities();
            }

            for(Entity ent : ents){
                if(!(ent instanceof Display) || (isNotNull && !ent.getLocation().equals(loc)) || (isNotNull && !apiHelper.isMythicMob(ent)))
                    continue;

                if(respawn)
                    apiHelper.spawnMythicMob(ConfigSettings.getGraveEntityID(), ent.getLocation());

                apiHelper.getMythicMobInstance(ent).remove();
            }
        }
        catch (Throwable e){}
    }

    public static double calculatePrice(Location loc, Location playerLoc, double distance){
        double price = Math.ceil(distance / ConfigSettings.getBlockPer()) * ConfigSettings.getPricePer();

        if(!loc.getWorld().equals(playerLoc.getWorld()))
            price += ConfigSettings.getExtraPriceForAnotherWorld();

        return price;
    }

    private static final DecimalFormat df = new DecimalFormat("#.00");

    public static String formatDouble(Double value){
        if(value == null)
            return "0";

        StringBuilder builder = new StringBuilder();

        if(isWhole(value))
            return builder.append(((int)value.doubleValue())).toString();

        if(value > -1.0 && value < 1)
            builder.append("0");

        return builder.append(df.format(value)).toString();
    }

    private static boolean isWhole(double value) {
        return value == Math.floor(value) && !Double.isInfinite(value);
    }

    public static boolean isItemValid(ItemStack item){
        return item != null && !item.getType().toString().endsWith("AIR");
    }

}
