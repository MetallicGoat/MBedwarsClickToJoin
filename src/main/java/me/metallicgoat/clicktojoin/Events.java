package me.metallicgoat.clicktojoin;

import de.marcely.bedwars.api.GameAPI;
import de.marcely.bedwars.api.event.arena.ArenaStatusChangeEvent;
import de.marcely.bedwars.api.event.player.PlayerJoinArenaEvent;
import de.marcely.bedwars.api.event.remote.RemoteArenaPropertiesChangeEvent;
import de.marcely.bedwars.api.event.remote.RemotePlayerJoinArenaEvent;
import de.marcely.bedwars.api.remote.RemoteArena;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Events implements Listener {

  private Instant lastMessage = Instant.now();
  private BukkitTask messageTask = null;

  @EventHandler
  public void onPlayerJoinLocalArena(PlayerJoinArenaEvent event) {
    addArenaIfValid(event.getArena().asRemote());
  }

  @EventHandler
  public void onPlayerJoinRemoteArena(RemotePlayerJoinArenaEvent event) {
    addArenaIfValid(event.getArena());
  }

  @EventHandler
  public void onLocalArenaStatusChangeEvent(ArenaStatusChangeEvent event) {
    removeArena(event.getArena().asRemote());
  }

  @EventHandler
  public void onRemoteArenaStatusChangeEvent(RemoteArenaPropertiesChangeEvent event) {
    if (!event.getProperties().contains(RemoteArenaPropertiesChangeEvent.Property.STATUS))
      return;

    removeArena(event.getArena());
  }

  private void addArenaIfValid(RemoteArena arena) {
    if (!Util.isArenaValid(arena))
      return;

    Util.arenasWaitingStart.add(arena);

    startTask();
  }

  private void removeArena(RemoteArena arena) {
    Util.arenasWaitingStart.remove(arena);

    cancelTask();
  }

  private void startTask() {
    if (this.messageTask != null)
      return;

    this.messageTask = Bukkit.getScheduler().runTaskTimer(ClickToJoinPlugin.getInstance(), () -> {
      Util.cleanOldArenas();

      if (Util.arenasWaitingStart.isEmpty()) {
        cancelTask();
        return;
      }

      final Instant currTime = Instant.now();

      if (Duration.between(this.lastMessage, currTime).toMillis() <= Config.message_cooldown * 1000L)
        return;

      final RemoteArena arena = Util.getPlayerWaitingArena();

      if (arena != null) {
        this.lastMessage = currTime;
        sendClickToJoinMessage(arena);
      }
    }, 20L, 20L);
  }

  private void sendClickToJoinMessage(RemoteArena arena) {
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

  private void cancelTask() {
    if (!Util.arenasWaitingStart.isEmpty())
      return;

    if (this.messageTask != null) {
      this.messageTask.cancel();
      this.messageTask = null;
    }
  }
}
