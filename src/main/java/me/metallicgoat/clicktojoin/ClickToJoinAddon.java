package me.metallicgoat.clicktojoin;

import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.BedwarsAddon;
import de.marcely.bedwars.api.command.SubCommand;
import de.marcely.bedwars.api.message.DefaultMessageMappings;
import de.marcely.bedwars.api.message.MessageAPI;
import org.bukkit.plugin.PluginManager;

public class ClickToJoinAddon extends BedwarsAddon {

  private final ClickToJoinPlugin plugin;

  public ClickToJoinAddon(ClickToJoinPlugin plugin) {
    super(plugin);

    this.plugin = plugin;
  }

  @Override
  public String getName() {
    return plugin.getName();
  }

  public void registerMessageMappings() {
    try {
      MessageAPI.get().registerDefaultMappings(
          DefaultMessageMappings.loadInternalYAML(this.plugin, this.plugin.getResource("messages.yml"))
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void registerEvents() {
    final PluginManager manager = this.plugin.getServer().getPluginManager();

    manager.registerEvents(new Events(), this.plugin);
  }

  public void registerCommand() {
    final SubCommand command = BedwarsAPI.getRootCommandsCollection().addCommand("clickconnect");

    if (command == null)
      throw new RuntimeException("Failed to register command");

    command.setOnlyForPlayers(true);
    command.setVisible(false);
    command.setHandler(new ClickConnectCommand());
  }
}
