package org.dimasik.appleModDetector.listeners;

import api.appleduels.bukkit.events.ApplePlayerJoinEvent;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.nbt.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.TileEntityType;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateSign;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenSignEditor;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.dimasik.appleModDetector.AppleModDetector;
import org.dimasik.appleModDetector.utility.Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerListener implements Listener {
    private final Map<UUID, String> pendingMod = new ConcurrentHashMap<>();
    private final Map<UUID, List<Map.Entry<String, String>>> pendingQueue = new ConcurrentHashMap<>();

    public PlayerListener() {
        registerPacketListener();
    }

    private void debug(String message) {
        if (AppleModDetector.getInstance().getConfig().getBoolean("debug", false)) {
            AppleModDetector.getInstance().getLogger().info("[DEBUG] " + message);
        }
    }

    @EventHandler
    public void on(ApplePlayerJoinEvent event) {
        Player player = event.getPlayer();
        ConfigurationSection configurationSection = AppleModDetector.getInstance().getConfig().getConfigurationSection("disallowed-mods");
        if (configurationSection == null) return;
        List<Map.Entry<String, String>> mods = new ArrayList<>(configurationSection
                .getValues(false)
                .entrySet()
                .stream()
                .map(e -> Map.entry(e.getKey(), (String) e.getValue()))
                .toList());

        if (mods.isEmpty()) return;

        AppleModDetector.getInstance().getServer().getScheduler().runTaskLater(AppleModDetector.getInstance(), () -> {
            Map.Entry<String, String> first = mods.removeFirst();
            pendingQueue.put(player.getUniqueId(), mods);
            sendModCheck(player, first.getKey(), first.getValue());
        }, 20L);
    }

    private void sendModCheck(Player player, String modName, String translationKey) {
        String defaultValue = AppleModDetector.getInstance().getConfig().getString("default", "fallb");

        Location loc = player.getLocation().clone();
        loc.setY(loc.getBlockY() - 5);

        WrappedBlockState oakSign = WrappedBlockState.getByGlobalId(5135);
        Vector3i peLocation = new Vector3i(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        WrapperPlayServerBlockChange blockChange = new WrapperPlayServerBlockChange(peLocation, oakSign.getGlobalId());
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, blockChange);
        debug("Sent BlockChange to " + player.getName() + " at " + peLocation + " for mod check [" + modName + "]");

        NBTCompound translationMessage = new NBTCompound();
        translationMessage.setTag("translate", new NBTString(translationKey));
        translationMessage.setTag("fallback", new NBTString(defaultValue));

        NBTCompound emptyMessage = new NBTCompound();
        emptyMessage.setTag("", new NBTString(""));

        NBTList<NBTCompound> messages = new NBTList<>(NBTType.COMPOUND, List.of(
                translationMessage, emptyMessage, emptyMessage, emptyMessage
        ));
        NBTCompound text = new NBTCompound();
        text.setTag("messages", messages);
        text.setTag("color", new NBTString("black"));
        text.setTag("has_glowing_text", new NBTByte((byte) 0));

        NBTCompound nbtCompound = new NBTCompound();
        nbtCompound.setTag("front_text", text);
        nbtCompound.setTag("back_text", text);
        nbtCompound.setTag("is_waxed", new NBTByte((byte) 0));

        //noinspection deprecation
        WrapperPlayServerBlockEntityData blockEntityData = new WrapperPlayServerBlockEntityData(peLocation, BlockEntityTypes.SIGN, nbtCompound);

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, blockEntityData);
        debug("Sent BlockEntityData to " + player.getName() + " with translation key \"" + translationKey + "\" and fallback \"" + defaultValue + "\"");

        WrapperPlayServerOpenSignEditor openSign = new WrapperPlayServerOpenSignEditor(peLocation, true);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, openSign);
        debug("Sent OpenSignEditor to " + player.getName());

        WrapperPlayServerCloseWindow closeWindow = new WrapperPlayServerCloseWindow(0);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, closeWindow);
        debug("Sent CloseWindow to " + player.getName());

        pendingMod.put(player.getUniqueId(), modName);
    }

    private void registerPacketListener() {
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerAbstract() {
            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                if (event.getPacketType() != PacketType.Play.Client.UPDATE_SIGN) return;
                Player player = event.getPlayer() instanceof Player p ? p : null;
                if (player == null) return;
                UUID uuid = player.getUniqueId();
                if (!pendingMod.containsKey(uuid)) return;

                WrapperPlayClientUpdateSign wrapper = new WrapperPlayClientUpdateSign(event);
                String[] lines = wrapper.getTextLines();
                String defaultValue = AppleModDetector.getInstance().getConfig().getString("default", "fallb");

                debug("Received UpdateSign from " + player.getName() + ": " + Arrays.toString(lines));

                boolean translated = false;
                for (String line : lines) {
                    if (!line.isEmpty() && !line.equals(defaultValue)) {
                        translated = true;
                        break;
                    }
                }

                String modName = pendingMod.remove(uuid);
                debug("Check result for " + player.getName() + " [" + modName + "]: " + (translated ? "MOD DETECTED" : "clean"));

                if (translated) {
                    pendingQueue.remove(uuid);
                    AppleModDetector.getInstance().getServer().getScheduler().runTask(AppleModDetector.getInstance(), () -> {
                        List<String> rawLines = AppleModDetector.getInstance().getConfig().getStringList("kick-message");
                        Component kickMessage = rawLines.stream()
                                .map(l -> l.replace("%mod%", modName))
                                .map(Parser::color)
                                .reduce(Component.empty(),
                                        (a, b) -> a.equals(Component.empty()) ? b : a.append(Component.newline()).append(b));
                        debug("Kicking " + player.getName() + " for using " + modName);
                        player.kick(kickMessage);
                    });
                } else {
                    List<Map.Entry<String, String>> queue = pendingQueue.get(uuid);
                    if (queue != null && !queue.isEmpty()) {
                        Map.Entry<String, String> next = queue.removeFirst();
                        debug("Moving to next check for " + player.getName() + ": [" + next.getKey() + "]");
                        sendModCheck(player, next.getKey(), next.getValue());
                    } else {
                        pendingQueue.remove(uuid);
                        debug("All checks passed for " + player.getName());

                        Location loc = player.getLocation().clone();
                        loc.setY(loc.getBlockY() - 5);
                        int globalId = WrappedBlockState.getByString(player.getWorld().getBlockAt(loc).getBlockData().getAsString()).getGlobalId();
                        Vector3i peLocation = new Vector3i(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                        WrapperPlayServerBlockChange restore = new WrapperPlayServerBlockChange(peLocation, globalId);
                        PacketEvents.getAPI().getPlayerManager().sendPacket(player, restore);
                        debug("Restored block at " + peLocation + " for " + player.getName());
                    }
                }
            }
        });
    }
}