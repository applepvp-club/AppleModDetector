package api.appleduels.bukkit.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


@Getter
public class ApplePlayerLeaveEvent extends Event {
    private final Player player;
    @Getter
    private static final HandlerList handlerList = new HandlerList();

    public ApplePlayerLeaveEvent(Player player) {
        this.player = player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}