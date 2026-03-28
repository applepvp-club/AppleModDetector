package org.dimasik.appleModDetector;

import api.appleduels.bukkit.events.ApplePlayerJoinEvent;
import api.appleduels.bukkit.events.ApplePlayerLeaveEvent;
import api.appleduels.bukkit.events.EventListener;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.appleModDetector.listeners.PlayerListener;

@Getter
public final class AppleModDetector extends JavaPlugin {
    @Getter
    private static AppleModDetector instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        saveDefaultConfig();
        FileConfiguration config = getConfig();

        var pm = super.getServer().getPluginManager();
        pm.registerEvents(new EventListener(), this);
        pm.registerEvents(new PlayerListener(), this);

        for(Player player : Bukkit.getOnlinePlayers())
            Bukkit.getPluginManager().callEvent(new ApplePlayerJoinEvent(player));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for(Player player : Bukkit.getOnlinePlayers())
            Bukkit.getPluginManager().callEvent(new ApplePlayerLeaveEvent(player));
    }
}
