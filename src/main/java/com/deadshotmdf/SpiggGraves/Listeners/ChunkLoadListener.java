package com.deadshotmdf.SpiggGraves.Listeners;

import com.deadshotmdf.SpiggGraves.Utils.GraveUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkLoadListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent ev) {
        GraveUtils.removeEntity(null, ev.getChunk().getEntities(), false, true);
    }

}
