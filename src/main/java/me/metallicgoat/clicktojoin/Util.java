package me.metallicgoat.clicktojoin;

import de.marcely.bedwars.api.arena.ArenaStatus;
import de.marcely.bedwars.api.message.Message;
import de.marcely.bedwars.api.remote.RemoteArena;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Util {

  // Instant = last warning
  public static final List<RemoteArena> arenasWaitingStart = new ArrayList<>();

  public static RemoteArena getPlayerWaitingArena() {
    cleanOldArenas();

    if (arenasWaitingStart.isEmpty())
      return null;

    // Fill arenas with more players first
    if (arenasWaitingStart.size() > 1) {
      arenasWaitingStart.sort(Comparator.comparingInt(RemoteArena::getPlayersCount));
      Collections.reverse(arenasWaitingStart);
    }

    for (RemoteArena arena : Util.arenasWaitingStart) {
      if (arena.getMaxPlayers() <= arena.getPlayersCount())
        continue;

      // Update Last time
      return arena;
    }

    return null;
  }

  // Remove empty Arenas, or arenas not in lobby
  public static void cleanOldArenas() {
    arenasWaitingStart.removeIf(entry -> !isArenaValid(entry) || entry.getPlayersCount() == 0);
  }

  public static boolean isArenaValid(RemoteArena arena) {
    return arena.getStatus() == ArenaStatus.LOBBY && (Config.arena_condition == null || Config.arena_condition.check(arena));
  }

  public static void sendClickToJoinMessage(Player player, RemoteArena arena) {
    final String message = Message.buildByKey("Click_To_Join_Message")
        .placeholder("current-players", arena.getPlayersCount())
        .placeholder("max-players", arena.getMaxPlayers())
        .placeholder("arena", arena.getName())
        .done(player);

    final TextComponent component = new TextComponent(TextComponent.fromLegacyText(message));

    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bw clickconnect " + arena.getName()));
    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
        Message.buildByKey("Click_To_Join_Message_Hover").done())
    ));

    player.spigot().sendMessage(component);
  }
}
