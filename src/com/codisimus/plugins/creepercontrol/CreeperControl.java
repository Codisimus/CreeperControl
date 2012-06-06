package com.codisimus.plugins.creepercontrol;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Prevents Creepers from doing land damage
 * 
 * @author Codisimus
 */
public class CreeperControl extends JavaPlugin implements Listener {
    boolean controlCreepers;
    boolean controlTNT;
    boolean controlGhasts;
    boolean controlOther;
    int controlAboveYAxis;
    Properties p;

    @Override
    public void onDisable () {}

    @Override
    public void onEnable () {
        this.getServer().getPluginManager().registerEvents(this, this);
        loadSettings();
        System.out.println("CreeperControl "+this.getDescription().getVersion()+" is enabled!");
        System.out.println("[CreeperControl] Controlling "+(controlCreepers ? "Creepers " : "")+(controlTNT ? "TNT " : "")
                +(controlGhasts ? "Ghasts " : "")+(controlOther ? "Other " : "")+(controlAboveYAxis > 0 ? ("above y-axis "+controlAboveYAxis) : ""));
    }
    
    /**
     * Loads settings from the config.properties file
     * 
     */
    public void loadSettings() {
        FileInputStream fis = null;
        try {
            //Copy the file from the jar if it is missing
            File file = this.getDataFolder();
            if (!file.isDirectory())
                file.mkdir();
            file = new File(file.getPath()+"/config.properties");
            if (!file.exists())
                this.saveResource("config.properties", true);
            
            //Load config file
            p = new Properties();
            fis = new FileInputStream(file);
            p.load(fis);
            
            controlCreepers = Boolean.parseBoolean(loadValue("ControlCreepers"));
            controlTNT = Boolean.parseBoolean(loadValue("ControlTNT"));
            controlGhasts = Boolean.parseBoolean(loadValue("ControlGhasts"));
            controlOther = Boolean.parseBoolean(loadValue("ControlOther"));
            controlAboveYAxis = Integer.parseInt(loadValue("ControlAboveYAxis"));
        }
        catch (Exception missingProp) {
            System.err.println("Failed to load CreeperControl "+this.getDescription().getVersion());
            missingProp.printStackTrace();
        }
        finally {
            try {
                fis.close();
            }
            catch (Exception e) {
            }
        }
    }

    /**
     * Loads the given key and prints an error if the key is missing
     *
     * @param key The key to be loaded
     * @return The String value of the loaded key
     */
    private String loadValue(String key) {
        //Print an error if the key is not found
        if (!p.containsKey(key)) {
            System.err.println("[CreeperControl] Missing value for "+key+" in config file");
            System.err.println("[CreeperControl] Please regenerate config file");
        }
        
        return p.getProperty(key);
    }
    
    @EventHandler
    public void onCreeperBoom(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        List<Block> blockList = event.blockList();
        if (entity instanceof Creeper) {
            if (controlCreepers)
                clearBlocks(blockList);
        }
        else if (entity instanceof TNTPrimed) {
            if (controlTNT)
                clearBlocks(blockList);
        }
        else if (entity instanceof Ghast) {
            if (controlGhasts)
                clearBlocks(blockList);
        }
        else if (controlOther)
            clearBlocks(blockList);
    }
    
    private void clearBlocks(List<Block> blockList) {
        Iterator<Block> itr = blockList.iterator();
        while (itr.hasNext())
            if (itr.next().getY() > controlAboveYAxis)
                itr.remove();
    }
}