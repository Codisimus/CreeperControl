package com.codisimus.plugins.creepercontrol;

import com.codisimus.plugins.creepercontrol.listeners.EntityEventListener;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loads Plugin
 * 
 * @author Codisimus
 */
public class CreeperControl extends JavaPlugin {

    @Override
    public void onDisable () {
    }

    /**
     * Calls methods to load this Plugin when it is enabled
     *
     */
    @Override
    public void onEnable () {
        getServer().getPluginManager().registerEvent(Type.ENTITY_EXPLODE, new EntityEventListener(), Priority.Normal, this);
        
        System.out.println("CreeperControl "+this.getDescription().getVersion()+" is enabled!");
    }
}