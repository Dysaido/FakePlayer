package hu.dysaido.fakeplayer.listener;

import hu.dysaido.fakeplayer.api.event.PlayerLeftClickAtNPCEvent;
import hu.dysaido.fakeplayer.api.event.PlayerRightClickAtNPCEvent;
import hu.dysaido.fakeplayer.api.npc.NPC;
import hu.dysaido.fakeplayer.packet.PacketReader;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public class EntityInteractOfWorld extends BaseListener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PacketReader.inject(player);
        if (plugin.getNpcHandler() != null)
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> plugin.getNpcHandler().show(player), 30L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PacketReader.uninject(player);
    }

//    @EventHandler
//    public void onPlayerRespawn(PlayerRespawnEvent event) {
//        Player player = event.getPlayer();
//        if (plugin.getNpcHandler() != null)
//            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> plugin.getNpcHandler().show(player), 30L);
//    }
//
//    @EventHandler
//    public void onPlayerTeleport(PlayerTeleportEvent event) {
//        Player player = event.getPlayer();
//        if (plugin.getNpcHandler() != null)
//            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> plugin.getNpcHandler().show(player), 30L);
//    }

    @EventHandler
    public void onPlayerRightClickAtNPC(PlayerRightClickAtNPCEvent event) {
        final Player player = event.getOfflinePlayer().getPlayer();
        final NPC npc = event.getNpc();
        final UUID npcUUID = npc.getUniqueId();
        final ConsoleCommandSender commandSender = Bukkit.getConsoleSender();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("fake-players");
        if (section.contains(npcUUID.toString())) {
            section = section.getConfigurationSection(npcUUID.toString());
            if (section != null) {
                if (section.getBoolean("NPCConsoleCommand")) {
                    String consoleCommand = section.getString("NPCExecuteConsole").replace("{player}", player.getName());
                    Bukkit.dispatchCommand(commandSender, consoleCommand);
                }
                if (section.getBoolean("NPCSenderCommand")) {
                    String consoleCommand = section.getString("NPCExecutePlayer").replace("{player}", player.getName());
                    Bukkit.dispatchCommand(player, consoleCommand);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeftClickAtNPC(PlayerLeftClickAtNPCEvent event) {
        final Player player = event.getOfflinePlayer().getPlayer();
        final NPC npc = event.getNpc();
        final UUID npcUUID = npc.getUniqueId();
        final ConsoleCommandSender commandSender = Bukkit.getConsoleSender();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("fake-players");
        if (section.contains(npcUUID.toString())) {
            section = section.getConfigurationSection(npcUUID.toString());
            if (section != null) {
                if (section.getBoolean("NPCConsoleCommand")) {
                    String consoleCommand = section.getString("NPCExecuteConsole").replace("{player}", player.getName());
                    Bukkit.dispatchCommand(commandSender, consoleCommand);
                }
                if (section.getBoolean("NPCSenderCommand")) {
                    String consoleCommand = section.getString("NPCExecutePlayer").replace("{player}", player.getName());
                    Bukkit.dispatchCommand(player, consoleCommand);
                }
            }
        }
    }


}
