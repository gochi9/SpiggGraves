package com.deadshotmdf.SpiggGraves.Save;

import com.deadshotmdf.SpiggGraves.Objects.Grave;
import com.deadshotmdf.SpiggGraves.Utils.GraveUtils;
import com.deadshotmdf.SpiggGraves.SP;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SaveFile {

    private File file;
    private FileConfiguration config;

    public SaveFile(SP main){
        this.file = new File(main.getDataFolder(), "data.yml");

        if(!file.exists())
            main.saveResource("data.yml", false);

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveFile(Map<Location, Grave> graves){
        this.config.set("Graves", null);
        saveConfig();

        for(Map.Entry<Location, Grave> entry : graves.entrySet()){
            String key = GraveUtils.locationToString(entry.getKey());
            Grave grave = entry.getValue();
            this.config.set("Graves." + key + ".grave", grave.toString());

            List<ItemStack> items = grave.getItems();

            for(int i = 0; i < items.size(); i++)
                this.config.set("Graves." + key + ".items." + i, items.get(i));
        }

        saveConfig();
    }

    public void loadFile(Map<Location, Grave> graves){
        ConfigurationSection sec = config.getConfigurationSection("Graves");

        if(sec == null)
            return;

        Set<String> keys = sec.getKeys(false);

        if(keys.isEmpty())
            return;

        for(String key : keys){
            Location loc = GraveUtils.locationFromString(key);

            if(loc == null)
                continue;

            List<ItemStack> items = getItems(key);

            if(items.isEmpty())
                continue;

            Grave grave = GraveUtils.graveFromString(config.getString("Graves." + key + ".grave"), items);

            if(grave != null)
                graves.put(loc, grave);
        }
    }

    private List<ItemStack> getItems(String key){
        List<ItemStack> items = new LinkedList<>();
        ConfigurationSection sec = config.getConfigurationSection("Graves." + key + ".items");

        if(sec == null)
            return items;

        Set<String> keys = sec.getKeys(false);

        if(keys.isEmpty())
            return items;

        for(String slot : keys){
            ItemStack item = config.getItemStack("Graves." + key + ".items." + slot);

            if(item != null)
                items.add(item);
        }

        return items;
    }

    public void saveConfig(){
        try {
            this.config.save(this.file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
