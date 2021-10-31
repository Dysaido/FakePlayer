package hu.dysaido.fakeplayer.api.event;

import hu.dysaido.fakeplayer.api.npc.NPC;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLeftClickAtNPCEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final OfflinePlayer player;
    private final NPC npc;
    private boolean isCancelled;

    public PlayerLeftClickAtNPCEvent(OfflinePlayer player, NPC npc) {
        super(true);
        this.player = player;
        this.npc = npc;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

    public OfflinePlayer getOfflinePlayer() {
        return player;
    }

    public NPC getNpc() {
        return npc;
    }
}
