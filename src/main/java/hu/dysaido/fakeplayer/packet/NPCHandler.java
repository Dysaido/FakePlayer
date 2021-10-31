package hu.dysaido.fakeplayer.packet;

import hu.dysaido.fakeplayer.FakePlayer;
import hu.dysaido.fakeplayer.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NPCHandler {

    private final FakePlayer plugin = FakePlayer.getInstance();

    public void show(Player player) {
        for (NPC npc : plugin.getNpcManager().getNPCs()) {
            if (!player.getWorld().equals(npc.getWorld())) continue;
            npc.onSpawn(player);
        }
    }

    public void schedulerNPCLook() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (NPC npc : plugin.getNpcManager().getNPCs()) {
                    if (!player.getWorld().equals(npc.getWorld())) continue;
                    npc.lookAtPlayer(player);
                }
            }
        }, 3, 3);
    }

}
