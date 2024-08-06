package me.metallicgoat.clicktojoin;

import de.marcely.bedwars.api.GameAPI;
import de.marcely.bedwars.api.arena.ArenaStatus;
import de.marcely.bedwars.api.message.Message;
import de.marcely.bedwars.api.remote.RemoteArena;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Util {

  private static Instant lastMessageTime = Instant.now();
  private static BukkitTask messageTask = null;
  private static final List<RemoteArena> arenasWaitingStart = new ArrayList<>();

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

    cancelTaskIfEmpty();
  }


  public static void addArenaIfValid(RemoteArena arena) {
    if (!Util.isArenaValid(arena))
      return;

    Util.arenasWaitingStart.add(arena);

    startTaskIfNeeded();
  }

  private static boolean isArenaValid(RemoteArena arena) {
    return arena.getStatus() == ArenaStatus.LOBBY && (Config.arena_condition == null || Config.arena_condition.check(arena));
  }

  public static void removeArena(RemoteArena arena) {
    Util.arenasWaitingStart.remove(arena);

    cancelTaskIfEmpty();
  }

  private static void startTaskIfNeeded() {
    if (messageTask != null)
      return;

    messageTask = Bukkit.getScheduler().runTaskTimer(ClickToJoinPlugin.getInstance(), () -> {
      Util.cleanOldArenas();

      if (arenasWaitingStart.isEmpty()) {
        cancelTaskIfEmpty();
        return;
      }

      final Instant currTime = Instant.now();

      if (lastMessageTime == null || Duration.between(lastMessageTime, currTime).toMillis() <= Config.message_cooldown * 1000L)
        return;

      final RemoteArena arena = Util.getPlayerWaitingArena();

      if (arena != null) {
        lastMessageTime = currTime;
        sendClickToJoinMessage(arena);
      }
    }, 20L, 20L);
  }

  private static void cancelTaskIfEmpty() {
    if (!Util.arenasWaitingStart.isEmpty())
      return;

    if (messageTask != null) {
      messageTask.cancel();
      messageTask = null;
    }
  }

  private static void sendClickToJoinMessage(RemoteArena arena) {
    final Collection<Player> players = new ArrayList<>();

    if (Config.worlds_whitelist.isEmpty()) {
      players.addAll(Bukkit.getOnlinePlayers());

    } else {
      for (String worldName : Config.worlds_whitelist) {
        final World world = Bukkit.getWorld(worldName);

        players.addAll(world.getPlayers());
      }
    }

    for (Player player : players) {
      if (GameAPI.get().getArenaByPlayer(player) == null) {
        Util.sendClickToJoinMessage(player, arena);
      }
    }
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
