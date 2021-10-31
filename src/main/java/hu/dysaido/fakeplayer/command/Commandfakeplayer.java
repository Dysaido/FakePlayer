package hu.dysaido.fakeplayer.command;

import hu.dysaido.fakeplayer.FakePlayer;
import hu.dysaido.fakeplayer.api.npc.NPC;
import hu.dysaido.fakeplayer.util.Format;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandfakeplayer implements CommandExecutor {

    private final FakePlayer plugin = FakePlayer.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (sender.isOp()) {
            Player player = (Player) sender;
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (args.length == 1) {
                        plugin.reloadConfig();
                        plugin.getNpcManager().initializationNPCs();
                        player.sendMessage(Format.color("&cFakePlayer plugin reloaded!"));
                    } else {
                        player.sendMessage(Format.color("&7/&cfakeplayer reload"));
                    }
                } else if (args[0].equalsIgnoreCase("create")) {
                    if (args.length == 2) {
                        String name = args[1];
                        plugin.getNpcManager().createNPC(name, null, player.getLocation(), null);
                    } else if (args.length == 3) {
                        String name = args[1];
                        String skin = args[2];
                        plugin.getNpcManager().createNPC(name, skin, player.getLocation(), null);
                    } else {
                        player.sendMessage(Format.color("&7/&cfakeplayer create name skin-name"));
                    }
                } else if (args[0].equalsIgnoreCase("list")) {
                    if (args.length == 1) {
                        plugin.getNpcManager().getNPCs().forEach(npc -> player.sendMessage(npc.toString()));
                    } else {
                        player.sendMessage(Format.color("&7/&cfakeplayer list"));
                    }
                } else if (args[0].equalsIgnoreCase("despawn")) {
                    if (args.length == 1) {
                        plugin.getNpcManager().getNPCs().forEach(npc -> npc.onDespawn(player));
                    } else {
                        player.sendMessage(Format.color("&7/&cfakeplayer list"));
                    }
                } else if (args[0].equalsIgnoreCase("spawn")) {
                    if (args.length == 1) {
                        plugin.getNpcManager().getNPCs().forEach(npc -> npc.onSpawn(player));
                    } else {
                        player.sendMessage(Format.color("&7/&cfakeplayer list"));
                    }
                } else {
                    player.sendMessage(Format.color("&7/&cfakeplayer create [name] [skin-name]"));
                    player.sendMessage(Format.color("&7/&cfakeplayer list"));
                    player.sendMessage(Format.color("&7/&cfakeplayer reload"));
                }

            } else {
                player.sendMessage(Format.color("&7/&cfakeplayer create [name] [skin-name]"));
                player.sendMessage(Format.color("&7/&cfakeplayer list"));
                player.sendMessage(Format.color("&7/&cfakeplayer reload"));
            }
        }
        return true;
    }

}
