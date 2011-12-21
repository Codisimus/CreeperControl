package com.codisimus.plugins.creepercontrol.listeners;

import org.bukkit.entity.Creeper;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

/**
 * Listens for Creepers Exploding events
 * 
 * @author Codisimus
 */
public class EntityEventListener extends EntityListener {
    
    /**
     * Prevents Blocks from being destroyed by EntityExplode(Creeper explosion)
     * 
     * @param event The EntityExplodeEvent that occurred
     */
    @Override
    public void onEntityExplode (EntityExplodeEvent event) {
        if (event.getEntity() instanceof Creeper)
            event.blockList().clear();
    }
}
