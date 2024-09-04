package com.deadshotmdf.SpiggGraves;

import com.deadshotmdf.SpiggGraves.Utils.GraveUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ConfigSettings manages the configuration settings for the plugin,
 * providing easy access to various settings values through static methods.
 */
public class ConfigSettings {

    // Constant arrays for string replacements
    private static final String[] searchList = {"{player}", "{remainTimer}", "{amount}", "{exp}"};
    private static final String[] graveItemsReplace = {"{world}", "{remainTimer}", "{x}", "{y}", "{z}", "{price}", "{distance}"};

    // Configuration settings fields
    private static String reloadConfig;
    private static String noPermission;

    private static double blockPer;
    private static double pricePer;
    private static double extraPriceForAnotherWorld;

    private static String notInSameWorld;
    private static String graveNoLongerExists;
    private static String graveDistanceMessage;
    private static String graveDistanceCooldownMessage;
    private static String tooPoor;
    private static String teleported;
    private static String noGraves;
    private static String onDeathMessage;

    private static boolean canPlayerGiveOutCompass;
    private static boolean onlyOwnerCanOpen;
    private static int expRemain;
    private static int graveTimer;
    private static boolean giveGraveCompassOnRespawn;
    private static String graveEntityID;

    private static String graveInventoryName;

    private static double hologramYOffset;
    private static List<String> hologramLines;

    private static String compassDisplayName;
    private static List<String> compassLore;
    private static String guiItemSelectDisplayName;
    private static List<String> guiItemSelectLore;

    /**
     * Loads or reloads the configuration from the file, setting all the values.
     * This method is typically called when the plugin is enabled or the user reloads the config.
     *
     * @param main the main class instance of the plugin
     */
    public static void reloadConfig(SP main) {
        main.reloadConfig();
        main.saveDefaultConfig();
        FileConfiguration config = main.getConfig();

        // Load values from config and translate color codes
        reloadConfig = color(config.getString("reloadConfig"));
        noPermission = color(config.getString("noPermission"));

        blockPer = config.getDouble("blockPer");
        pricePer = config.getDouble("pricePer");
        extraPriceForAnotherWorld = config.getDouble("extraPriceForAnotherWorld");

        notInSameWorld = color(config.getString("notInSameWorld"));
        graveNoLongerExists = color(config.getString("graveNoLongerExists"));
        graveDistanceMessage = color(config.getString("graveDistanceMessage"));
        graveDistanceCooldownMessage = color(config.getString("graveDistanceCooldownMessage"));
        tooPoor = color(config.getString("tooPoor"));
        teleported = color(config.getString("teleported"));
        noGraves = color(config.getString("noGraves"));
        onDeathMessage = color(config.getString("onDeathMessage"));

        canPlayerGiveOutCompass = config.getBoolean("canPlayerGiveOutCompass");
        onlyOwnerCanOpen = config.getBoolean("onlyOwnerCanOpen");
        expRemain = config.getInt("expRemain");
        graveTimer = config.getInt("graveTimer");
        giveGraveCompassOnRespawn = config.getBoolean("giveGraveCompassOnRespawn");
        graveEntityID = config.getString("graveEntityID");

        graveInventoryName = color(config.getString("graveInventoryName"));

        hologramYOffset = config.getDouble("hologramYOffset");
        hologramLines = color(config.getStringList("hologramLines"));

        compassDisplayName = color(config.getString("compassDisplayName"));
        compassLore = color(config.getStringList("compassLore"));
        guiItemSelectDisplayName = color(config.getString("guiItemSelectDisplayName"));
        guiItemSelectLore = color(config.getStringList("guiItemSelectLore"));
    }

    // Getters for various configuration settings with simple replacements in strings where necessary

    public static String getReloadConfig() { return reloadConfig; }
    public static String getNoPermission() { return noPermission; }

    public static double getBlockPer() { return blockPer; }
    public static double getPricePer() { return pricePer; }
    public static double getExtraPriceForAnotherWorld() { return extraPriceForAnotherWorld; }

    public static String getNotInSameWorld(String world) { return notInSameWorld.replace("{world}", world); }
    public static String getGraveNoLongerExists() { return graveNoLongerExists; }
    public static String getGraveDistanceMessage(double blocks) { return graveDistanceMessage.replace("{blocks}", s(blocks)); }
    public static String graveDistanceCooldownMessage(long seconds) { return graveDistanceCooldownMessage.replace("{seconds}", s(seconds/1000.0d)); }
    public static String getTooPoor(double balance, double price) { return tooPoor.replace("{balance}", s(balance)).replace("{price}", s(price)); }
    public static String getTeleported(double balance, double price) { return teleported.replace("{balance}", s(balance)).replace("{price}", s(price)); }
    public static String getNoGraves() { return noGraves; }
    public static String getOnDeathMessage(String world, int x, int y, int z) { return onDeathMessage.replace("{world}", world).replace("{x}", s(x)).replace("{y}", s(y)).replace("{z}", s(z)); }

    public static boolean isCanPlayerGiveOutCompass() { return canPlayerGiveOutCompass; }
    public static boolean isOnlyOwnerCanOpen() { return onlyOwnerCanOpen; }
    public static int getExpRemain() { return expRemain; }
    public static int getGraveTimer() { return graveTimer; }
    public static boolean isGiveGraveCompassOnRespawn() { return giveGraveCompassOnRespawn; }
    public static String getGraveEntityID() { return graveEntityID; }

    public static String getGraveInventoryName(String player) { return graveInventoryName.replace("{player}", player); }
    public static double getHologramYOffset() { return hologramYOffset; }

    public static List<String> getHologramLines(String player, String remain, int amount, int exp) { return replaceHologramLines(hologramLines, player, remain, amount, exp); }
    public static String getCompassDisplayName(Location loc, double distance, int remainTimer, double price) { return replaceGraveItem(compassDisplayName, loc, remainTimer, price, distance); }
    public static List<String> getCompassLore(Location loc, double distance, int remainTimer, double price) { return replaceGraveItemList(compassLore, loc, remainTimer, price, distance); }
    public static String getGuiItemSelectDisplayName(Location loc, double distance, int remainTimer, double price) { return replaceGraveItem(guiItemSelectDisplayName, loc, remainTimer, price, distance); }
    public static List<String> getGuiItemSelectLore(Location playerLoc, Location loc, double distance, int remainTimer, double price) { return appendWorldCheck(replaceGraveItemList(guiItemSelectLore, loc, remainTimer, price, distance), playerLoc, loc); }

    // Helper methods for string manipulations

    /**
     * Translates color codes in a string.
     *
     * @param s the string to translate
     * @return the translated string with color codes applied
     */
    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s != null ? s : "invalid_string");
    }

    /**
     * Translates color codes in a list of strings.
     *
     * @param list the list of strings to translate
     * @return the list with translated strings
     */
    private static List<String> color(List<String> list) {
        return list == null || list.isEmpty() ? new ArrayList<>() : list.stream().map(ConfigSettings::color).collect(Collectors.toList());
    }

    /**
     * Performs replacement in hologram lines with provided values.
     *
     * @param lines the list of hologram lines to modify
     * @param player the player's name
     * @param remain the remaining time
     * @param items the amount of items
     * @param exp the amount of experience
     * @return the list of modified hologram lines
     */
    private static List<String> replaceHologramLines(List<String> lines, String player, String remain, int items, int exp) {
        List<String> updatedLines = new LinkedList<>();
        for (String line : lines)
            updatedLines.add(StringUtils.replaceEach(line, searchList, new String[]{player, remain, s(items), s(exp)}));

        return updatedLines;
    }

    /**
     * Performs replacement in grave-related items (compass, GUI item) for display name with provided values.
     *
     * @param line the line to modify
     * @param loc the location
     * @param timer the remaining timer
     * @param price the price
     * @param distance the distance
     * @return the modified line
     */
    private static String replaceGraveItem(String line, Location loc, int timer, double price, double distance) {
        return StringUtils.replaceEach(line, graveItemsReplace, new String[]{loc.getWorld().getName(), formatTimer(timer), s(loc.getBlockX()), s(loc.getBlockY()), s(loc.getBlockZ()), s(price), s(distance)});
    }

    /**
     * Performs replacement in grave-related items (compass, GUI item) for lore with provided values.
     *
     * @param lines the list of lines to modify
     * @param loc the location
     * @param timer the remaining timer
     * @param price the price
     * @param distance the distance
     * @return the list of modified lines
     */
    private static List<String> replaceGraveItemList(List<String> lines, Location loc, int timer, double price, double distance) {
        List<String> updatedLines = new LinkedList<>();
        for (String line : lines)
            updatedLines.add(replaceGraveItem(line, loc, timer, price, distance));

        return updatedLines;
    }

    /**
     * Adds a check for world difference in GUI item lore.
     *
     * @param lines the lore lines
     * @param playerLoc the player's location
     * @param loc the target location
     * @return the modified list of lore lines
     */
    private static List<String> appendWorldCheck(List<String> lines, Location playerLoc, Location loc) {
        if (!playerLoc.getWorld().equals(loc.getWorld()))
            lines.add(getNotInSameWorld(loc.getWorld().getName()));

        return lines;
    }

    /**
     * Formats a number into a string.
     * Used for displaying various numeric values as strings.
     *
     * @param o the object to format
     * @return the formatted string
     */
    private static String s(Object o) {
        return o instanceof Double ? GraveUtils.formatDouble((Double) o) : String.valueOf(o);
    }

    /**
     * Formats time into a human-readable format of days, hours, minutes, and seconds.
     *
     * @param totalSeconds the total seconds to format
     * @return the formatted time string
     */
    public static String formatTimer(long totalSeconds) {
        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d:%02d:%02d", days, hours, minutes, seconds);
    }
}
