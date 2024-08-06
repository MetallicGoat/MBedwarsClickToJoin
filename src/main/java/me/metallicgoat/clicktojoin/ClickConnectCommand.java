package me.metallicgoat.clicktojoin;

import de.marcely.bedwars.api.GameAPI;
import de.marcely.bedwars.api.command.CommandHandler;
import de.marcely.bedwars.api.command.SubCommand;
import de.marcely.bedwars.api.message.Message;
import de.marcely.bedwars.api.remote.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class ClickConnectCommand implements CommandHandler {

  @Override
  public Plugin getPlugin() {
    return ClickToJoinPlugin.getInstance();
  }

  @Override
  public void onRegister(SubCommand subCommand) {

  }

  @Override
  public void onFire(CommandSender commandSender, String s, String[] strings) {
    if (strings.length != 1)
      return;

    final Player player = (Player) commandSender;
    final RemotePlayer remotePlayer = RemoteAPI.get().getOnlinePlayer(player);
    final RemoteArena remoteArena = RemoteAPI.get().getArenaByName(strings[0]);

    // Player is null, or they are playing
    if (remotePlayer == null || remoteArena == null || GameAPI.get().getArenaByPlayer(player) != null)
      return;

    remoteArena.addPlayer(remotePlayer, remotePlayerAddResult -> {
      if (remotePlayerAddResult.getGeneralResult() != RemotePlayerAddResult.GeneralResult.SUCCESS) {
        remotePlayer.sendMessage(Message.buildByKey("Click_To_Join_Fail").done());

        if (Config.resend_new_click_to_join_on_fail) {
          final RemoteArena arena = Util.getPlayerWaitingArena();

          if (arena != null) {
            Util.sendClickToJoinMessage(player, arena);
          }
        }
      }
    });
  }

  @Override
  public List<String> onAutocomplete(CommandSender commandSender, String[] strings) {
    return Collections.emptyList();
  }
}