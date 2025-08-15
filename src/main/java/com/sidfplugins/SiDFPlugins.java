package com.sidfplugins;

import java.io.File;

import org.bukkit.command.PluginCommand; // 导入 PluginCommand
import org.bukkit.plugin.java.JavaPlugin;

import com.sidfplugins.commands.SidfCommand;
import com.sidfplugins.listeners.PlayerListener;
import com.sidfplugins.managers.PluginManager;
import com.sidfplugins.managers.StrongholdManager;

public class SiDFPlugins extends JavaPlugin {
    
    private static SiDFPlugins instance;
    
    public static SiDFPlugins getInstance() {
        return instance;
    }
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 设置据点数据文件路径
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        StrongholdManager.getInstance().setDataFile(new File(dataFolder, "strongholds.json"));
        
        // Initialize managers
        PluginManager.getInstance().initialize();
        StrongholdManager.getInstance().loadStrongholds(); // 加载据点数据
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        
        // Register commands safely
        PluginCommand sidfCommand = this.getCommand("sidf");
        if (sidfCommand != null) {
            sidfCommand.setExecutor(new SidfCommand());
        } else {
            getLogger().severe("命令 'sidf' 未在 plugin.yml 中正确注册，请检查！");
        }
        
        getLogger().info("SiDFPlugins Enabled 已加载");
    }

    @Override
    public void onDisable() {
        StrongholdManager.getInstance().saveStrongholds(); // 保存据点数据
        getLogger().info("SiDFPlugins Disabled 已卸载");
    }
    
}