package api.appleduels.bukkit.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
    @EventHandler
    public void on(PlayerJoinEvent event){
        Bukkit.getPluginManager().callEvent(new ApplePlayerJoinEvent(event.getPlayer()));
    }

    @EventHandler
    public void on(PlayerQuitEvent event){
        Bukkit.getPluginManager().callEvent(new ApplePlayerLeaveEvent(event.getPlayer()));
    }

    @EventHandler
    public void on(PlayerKickEvent event){
        Bukkit.getPluginManager().callEvent(new ApplePlayerLeaveEvent(event.getPlayer()));
    }
}
