package hu.dysaido.fakeplayer.listener;

import hu.dysaido.fakeplayer.FakePlayer;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public abstract class BaseListener implements Listener {

    protected FakePlayer plugin = FakePlayer.getInstance();

    public BaseListener() {
        final PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(this, plugin);
    }


}
