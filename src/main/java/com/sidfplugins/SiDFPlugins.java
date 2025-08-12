package com.sidfplugins;

import org.bukkit.plugin.java.JavaPlugin;
import com.sidfplugins.managers.PluginManager;
import com.sidfplugins.listeners.PlayerListener;

public class SiDFPlugins extends JavaPlugin {
    
    @Override
    public void onEnable() {
        
        // Initialize managers
        PluginManager.getInstance().initialize();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        
        getLogger().info("SiDFPlugins has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SiDFPlugins has been disabled!");
    }
    
}