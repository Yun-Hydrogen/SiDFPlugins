package com.sidfplugins.commands;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;

import com.sidfplugins.managers.StrongholdManager;
import com.sidfplugins.models.Stronghold;
import com.sidfplugins.SiDFPlugins;

public class SidfCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false; // 参数不足，显示用法
        }

        // 处理 /sidf set ...
        if (args[0].equalsIgnoreCase("set")) {
            handleSetCommand(sender, args);
            return true;
        }

        // 处理 /sidf remove ...
        if (args[0].equalsIgnoreCase("remove")) {
            handleRemoveCommand(sender, args);
            return true;
        }

        // 新增: 处理 /sidf list
        if (args[0].equalsIgnoreCase("list")) {
            handleListCommand(sender);
            return true;
        }

        return false; // 未知的子命令，显示用法
    }

    private void handleListCommand(CommandSender sender) {
        Map<String, Stronghold> strongholds = StrongholdManager.getInstance().getAllStrongholds();

        if (strongholds.isEmpty()) {
            sender.sendMessage("§e提示: §r当前没有设置任何据点。");
            return;
        }

        // 检查发送者是否为玩家
        if (!(sender instanceof Player)) {
            // 对于控制台发送者，只显示文本列表
            sender.sendMessage("§a===== §l所有据点列表 §a=====");
            for (Stronghold stronghold : strongholds.values()) {
                Location center = stronghold.getCenter();
                String worldName = center.getWorld() != null ? center.getWorld().getName() : "未知世界";
                String message = String.format("§e据点 %s: §7世界: %s, 中心: (%.1f, %.1f, %.1f), 半径: %.1f",
                    stronghold.getId(),
                    worldName,
                    center.getX(),
                    center.getY(),
                    center.getZ(),
                    stronghold.getRadius());
                sender.sendMessage(message);
            }
            sender.sendMessage("§a=========================");
            return;
        }

        Player player = (Player) sender;
        sender.sendMessage("§a===== §l所有据点列表 §a=====");
        for (Stronghold stronghold : strongholds.values()) {
            Location center = stronghold.getCenter();
            String worldName = center.getWorld() != null ? center.getWorld().getName() : "未知世界";
            String message = String.format("§e据点 %s: §7世界: %s, 中心: (%.1f, %.1f, %.1f), 半径: %.1f",
                stronghold.getId(),
                worldName,
                center.getX(),
                center.getY(),
                center.getZ(),
                stronghold.getRadius());
            sender.sendMessage(message);
        }
        sender.sendMessage("§a=========================");

        // 显示悬浮文字和粒子效果
        displayHologramsAndParticles(player, strongholds);
    }

    private void displayHologramsAndParticles(Player player, Map<String, Stronghold> strongholds) {
        World world = player.getWorld();
        
        // 为每个据点创建悬浮文字和粒子效果
        for (Stronghold stronghold : strongholds.values()) {
            Location center = stronghold.getCenter();
            if (center.getWorld() == null) continue;
            
            // 在据点中心创建悬浮文字
            Location hologramLocation = center.clone().add(0, 1.5, 0); // 稍微抬高一点
            showHologram(hologramLocation, stronghold.getId(), world);
            
            // 显示边缘粒子效果
            showParticlesAroundStronghold(center, stronghold.getRadius(), world);
        }
    }

    private void showHologram(Location location, String text, World world) {
        ArmorStand hologram = world.spawn(location, ArmorStand.class);
        hologram.setGravity(false);
        hologram.setInvisible(true);
        hologram.setCustomName("§e§l" + text);
        hologram.setCustomNameVisible(true);
        hologram.setInvulnerable(true);
        hologram.setVisible(false);
        hologram.setSilent(true);
        hologram.setMarker(true);
        
        // 10秒后移除悬浮文字
        new BukkitRunnable() {
            @Override
            public void run() {
                hologram.remove();
            }
        }.runTaskLater(SiDFPlugins.getInstance(), 200L); // 200 ticks = 10 seconds
    }

    private void showParticlesAroundStronghold(Location center, double radius, World world) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 200) { // 200 ticks = 10 seconds
                    this.cancel();
                    return;
                }
                
                // 在圆周上显示粒子效果
                for (int i = 0; i < 360; i += 15) { // 每15度显示一个粒子
                    double angle = Math.toRadians(i);
                    double x = center.getX() + radius * Math.cos(angle);
                    double z = center.getZ() + radius * Math.sin(angle);
                    Location particleLocation = new Location(world, x, center.getY() + 0.5, z);
                    
                    // 使用发光粒子效果
                    world.spawnParticle(org.bukkit.Particle.GLOW, particleLocation, 1, 0, 0, 0, 0);
                }
                
                ticks += 5; // 每5个tick运行一次
            }
        }.runTaskTimer(SiDFPlugins.getInstance(), 0L, 5L);
    }

    private void handleSetCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            if (sender != null) {
                sender.sendMessage("§c错误: §r此命令只能由玩家执行。");
            }
            return;
        }
        // /sidf set area <ID> <radius> -> 4个参数
        if (args.length != 4 || !args[1].equalsIgnoreCase("area")) {
            sender.sendMessage("§c用法: §r/sidf set area <A/B/C/D> <半径>");
            return;
        }

        Player player = (Player) sender;
        String areaId = args[2].toUpperCase();

        if (!areaId.matches("[ABCD]")) {
            player.sendMessage("§c错误: §r据点标号必须是 A, B, C, 或 D。");
            return;
        }

        // 检查据点是否已存在
        if (StrongholdManager.getInstance().getStronghold(areaId) != null) {
            player.sendMessage("§c错误: §r据点 " + areaId + " 已存在，请先删除或使用其他标号。");
            return;
        }

        double radius;
        try {
            radius = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage("§c错误: §r半径必须是一个有效的数字。");
            return;
        }

        Location center = player.getLocation();
        StrongholdManager.getInstance().createOrUpdateStronghold(areaId, center, radius);

        player.sendMessage("§a成功! §r据点 " + areaId + " 已设置。");
        player.sendMessage("  §7中心: " + String.format("%.1f, %.1f, %.1f", center.getX(), center.getY(), center.getZ()));
        player.sendMessage("  §7半径: " + radius);
    }

    private void handleRemoveCommand(CommandSender sender, String[] args) {
        // /sidf remove <ID> -> 2个参数
        if (args.length != 2) {
            sender.sendMessage("§c用法: §r/sidf remove <A/B/C/D>");
            return;
        }

        String areaId = args[1].toUpperCase();

        if (!areaId.matches("[ABCD]")) {
            sender.sendMessage("§c错误: §r据点标号必须是 A, B, C, 或 D。");
            return;
        }

        // 检查据点是否存在
        if (StrongholdManager.getInstance().getStronghold(areaId) == null) {
            sender.sendMessage("§c错误: §r据点 " + areaId + " 不存在。");
            return;
        }

        StrongholdManager.getInstance().removeStronghold(areaId);
        sender.sendMessage("§a成功! §r据点 " + areaId + " 已被删除。");
    }
}