package me.metallicgoat.clicktojoin;

import de.marcely.bedwars.api.event.arena.ArenaStatusChangeEvent;
import de.marcely.bedwars.api.event.player.PlayerJoinArenaEvent;
import de.marcely.bedwars.api.event.remote.RemoteArenaPropertiesChangeEvent;
import de.marcely.bedwars.api.event.remote.RemotePlayerJoinArenaEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Events implements Listener {


  @EventHandler
  public void onPlayerJoinLocalArena(PlayerJoinArenaEvent event) {
    Util.addArenaIfValid(event.getArena().asRemote());
  }

  @EventHandler
  public void onPlayerJoinRemoteArena(RemotePlayerJoinArenaEvent event) {
    Util.addArenaIfValid(event.getArena());
  }

  @EventHandler
  public void onLocalArenaStatusChangeEvent(ArenaStatusChangeEvent event) {
    Util.removeArena(event.getArena().asRemote());
  }

  @EventHandler
  public void onRemoteArenaStatusChangeEvent(RemoteArenaPropertiesChangeEvent event) {
    if (!event.getProperties().contains(RemoteArenaPropertiesChangeEvent.Property.STATUS))
      return;

    Util.removeArena(event.getArena());
  }
}
