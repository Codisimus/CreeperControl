package com.codisimus.plugins.creepercontrol;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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
    boolean controlFireballs;
    boolean controlOther;
    int controlAboveYAxis;
    static Properties p;
    static Logger logger;

    @Override
    public void onEnable () {
        //Metrics hook
        try { new Metrics(this).start(); } catch (IOException e) {}

        this.getServer().getPluginManager().registerEvents(this, this);
        logger = getLogger();
        loadSettings();

        Properties version = new Properties();
        try {
            version.load(this.getResource("version.properties"));
        }
        catch (Exception ex) {
        }
        logger.info("CreeperControl " + this.getDescription().getVersion() + " (Build " + version.getProperty("Build") + ") is enabled!");

        logger.info("[CreeperControl] Controlling "
                + (controlCreepers ? "Creepers " : "")
                + (controlTNT ? "TNT " : "")
                + (controlFireballs ? "Fireballs " : "")
                + (controlOther ? "Other " : "")
                + (controlAboveYAxis > 0 ? ("above y-axis " + controlAboveYAxis) : ""));
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
            if (!file.isDirectory()) {
                file.mkdir();
            }
            file = new File(file.getPath() + "/config.properties");
            if (!file.exists()) {
                this.saveResource("config.properties", true);
            }

            //Load config file
            p = new Properties();
            fis = new FileInputStream(file);
            p.load(fis);

            controlCreepers = loadBool("ControlCreepers", true);
            controlTNT = loadBool("ControlTNT", false);
            controlFireballs = loadBool("ControlFireballs", false);
            controlOther = loadBool("ControlOther", false);
            controlAboveYAxis = loadInt("ControlAboveYAxis", 0);
        } catch (Exception missingProp) {
            logger.severe("Failed to load CreeperControl " + this.getDescription().getVersion());
            missingProp.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Loads the given key and prints an error if the key is missing
     *
     * @param key The key to be loaded
     * @return The String value of the loaded key
     */
    private static String loadString(String key, String defaultString) {
        if (p.containsKey(key)) {
            return p.getProperty(key);
        } else {
            logger.severe("Missing value for " + key);
            logger.severe("Please regenerate the config.properties file (delete the old file to allow a new one to be created)");
            logger.severe("DO NOT POST A TICKET FOR THIS MESSAGE, IT WILL JUST BE IGNORED");
            return defaultString;
        }
    }

    /**
     * Loads the given key and prints an error if the key is not an Integer
     *
     * @param key The key to be loaded
     * @return The Integer value of the loaded key
     */
    private static int loadInt(String key, int defaultValue) {
        String string = loadString(key, null);
        try {
            return Integer.parseInt(string);
        } catch (Exception e) {
            logger.severe("The setting for " + key + " must be a valid integer");
            logger.severe("DO NOT POST A TICKET FOR THIS MESSAGE, IT WILL JUST BE IGNORED");
            return defaultValue;
        }
    }

    /**
     * Loads the given key and prints an error if the key is not a boolean
     *
     * @param key The key to be loaded
     * @return The boolean value of the loaded key
     */
    private static boolean loadBool(String key, boolean defaultValue) {
        String string = loadString(key, null);
        try {
            return Boolean.parseBoolean(string);
        } catch (Exception e) {
            logger.severe("The setting for " + key + " must be 'true' or 'false' ");
            logger.severe("DO NOT POST A TICKET FOR THIS MESSAGE, IT WILL JUST BE IGNORED");
            return defaultValue;
        }
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onExplosion(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        List<Block> blockList = event.blockList();
        boolean clear;
        switch (entity.getType()) {
        case CREEPER:
            clear = controlCreepers;
            break;
        case PRIMED_TNT:
            clear = controlTNT;
            break;
        case FIREBALL:
            clear = controlFireballs;
            break;
        default:
            clear = controlOther;
            break;
        }
        if (clear) {
            clearBlocks(blockList);
        }
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            return;
        }

        //Protect non-living Entities such as Paintings
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            return;
        }

        if (event.getDamager() == null) {
            event.setCancelled(controlOther);
        }
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            return;
        }

        //Protect non-living Entities such as Paintings
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            return;
        }

        switch (event.getDamager().getType()) {
        case CREEPER:
            event.setCancelled(controlCreepers);
            break;
        case PRIMED_TNT:
            event.setCancelled(controlTNT);
            break;
        case FIREBALL:
            event.setCancelled(controlFireballs);
            break;
        default:
            event.setCancelled(controlOther);
            break;
        }
    }

    private void clearBlocks(List<Block> blockList) {
        Iterator<Block> itr = blockList.iterator();
        while (itr.hasNext()) {
            if (itr.next().getY() > controlAboveYAxis) {
                itr.remove();
            }
        }
    }
}
