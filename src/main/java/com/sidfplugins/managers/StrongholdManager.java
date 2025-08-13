package com.sidfplugins.managers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sidfplugins.models.Stronghold;
import com.sidfplugins.models.Stronghold.SerializedStronghold;

public class StrongholdManager {
    private static StrongholdManager instance;
    private final Map<String, Stronghold> strongholds = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File dataFile;

    private StrongholdManager() {}

    public static StrongholdManager getInstance() {
        if (instance == null) {
            instance = new StrongholdManager();
        }
        return instance;
    }

    public void setDataFile(File file) {
        this.dataFile = file;
    }

    public void createOrUpdateStronghold(String id, Location center, double radius) {
        Stronghold stronghold = new Stronghold(id, center, radius);
        strongholds.put(id, stronghold);
    }

    public void removeStronghold(String id) {
        strongholds.remove(id);
    }

    public Stronghold getStronghold(String id) {
        return strongholds.get(id);
    }

    public Map<String, Stronghold> getAllStrongholds() {
        return strongholds;
    }

    public void loadStrongholds() {
        if (dataFile == null || !dataFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(dataFile)) {
            List<SerializedStronghold> serializedList = gson.fromJson(
                reader, 
                new TypeToken<List<SerializedStronghold>>(){}.getType()
            );
            
            if (serializedList != null) {
                strongholds.clear();
                for (SerializedStronghold serialized : serializedList) {
                    // 获取世界对象
                    World world = Bukkit.getWorld(serialized.world);
                    if (world != null) {
                        Location location = new Location(
                            world,
                            serialized.x,
                            serialized.y,
                            serialized.z
                        );
                        
                        Stronghold stronghold = new Stronghold(
                            serialized.id,
                            location,
                            serialized.radius
                        );
                        
                        strongholds.put(serialized.id, stronghold);
                    }
                }
            }
        } catch (IOException e) {
            Logger.getLogger("SiDFPlugins").log(java.util.logging.Level.SEVERE, "加载据点数据时出错: {0}", e.getMessage());
        }
    }

    public void saveStrongholds() {
        if (dataFile == null) {
            return;
        }

        try {
            // 确保数据目录存在
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }
            
            // 序列化所有据点
            List<SerializedStronghold> serializedList = new ArrayList<>();
            for (Stronghold stronghold : strongholds.values()) {
                serializedList.add(stronghold.serialize());
            }
            
            // 写入文件
            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(serializedList, writer);
            }
        } catch (IOException e) {
            Logger.getLogger("SiDFPlugins").log(java.util.logging.Level.SEVERE, "保存据点数据时出错: {0}", e.getMessage());
        }
    }
}