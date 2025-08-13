package com.sidfplugins.models;

import org.bukkit.Location;

public class Stronghold {
    private final String id;
    private Location center;
    private double radius;

    public Stronghold(String id, Location center, double radius) {
        this.id = id;
        this.center = center;
        this.radius = radius;
    }

    public String getId() {
        return id;
    }

    public Location getCenter() {
        return center;
    }

    public void setCenter(Location center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
    
    // 用于序列化的方法
    public SerializedStronghold serialize() {
        return new SerializedStronghold(
            this.id,
            (this.center != null && this.center.getWorld() != null) ? this.center.getWorld().getName() : null,
            this.center != null ? this.center.getX() : 0,
            this.center != null ? this.center.getY() : 0,
            this.center != null ? this.center.getZ() : 0,
            this.radius
        );
    }
    
    // 用于反序列化的方法
    public static Stronghold deserialize(SerializedStronghold serialized) {
        // 注意：这里需要获取World对象，实际使用时需要确保世界存在
        return new Stronghold(
            serialized.id,
            new Location(
                null, // World将在加载时设置
                serialized.x,
                serialized.y,
                serialized.z
            ),
            serialized.radius
        );
    }
    
    // 用于JSON序列化的内部类
    public static class SerializedStronghold {
        public String id;
        public String world;
        public double x;
        public double y;
        public double z;
        public double radius;
        
        public SerializedStronghold(String id, String world, double x, double y, double z, double radius) {
            this.id = id;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
        }
    }
}