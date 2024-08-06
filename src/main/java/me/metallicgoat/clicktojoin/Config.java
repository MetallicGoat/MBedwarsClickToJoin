package me.metallicgoat.clicktojoin;

import de.marcely.bedwars.api.arena.picker.ArenaPickerAPI;
import de.marcely.bedwars.api.arena.picker.condition.ArenaConditionGroup;
import de.marcely.bedwars.api.exception.ArenaConditionParseException;
import de.marcely.bedwars.tools.YamlConfigurationDescriptor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Config {

  public static int message_cooldown = 5;
  // public static int min_time_to_wait = 10; TODO
  public static List<String> worlds_whitelist = new ArrayList<>();
  public static boolean resend_new_click_to_join_on_fail = false;
  public static ArenaConditionGroup arena_condition;

  private static String arena_condition_string = "";

  public static void load() {
    synchronized (Config.class) {
      try {
        loadUnchecked();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static void loadUnchecked() throws Exception {
    final File file = new File(ClickToJoinPlugin.getAddon().getDataFolder(), "config.yml");

    if (!file.exists()) {
      save();
      return;
    }

    // load it
    final FileConfiguration config = new YamlConfiguration();

    try {
      config.load(file);
    } catch (Exception e) {
      e.printStackTrace();
    }


    message_cooldown = config.getInt("message_cooldown", 60);

    final List<String> worlds_whitelist_config = config.getStringList("worlds_whitelist");

    if (worlds_whitelist_config != null)
      worlds_whitelist = worlds_whitelist_config;

    arena_condition_string = config.getString("arena_condition_string", arena_condition_string);

    if (arena_condition_string != null && !arena_condition_string.isEmpty()) {
      try {
        arena_condition = ArenaPickerAPI.get().parseCondition(arena_condition_string);
      } catch (ArenaConditionParseException e) {
        Console.printConfigWarn("Failed to parse the arena condition '" + arena_condition_string + "'", "Main");
      }
    } else {
      arena_condition = null;
    }


    // auto update file if newer version
    {
      final String fileVersion = config.getString("file-version");

      if (fileVersion == null || !fileVersion.equals(ClickToJoinPlugin.getInstance().getDescription().getVersion()))
        save();

    }
  }

  private static void save() throws Exception {
    final YamlConfigurationDescriptor config = new YamlConfigurationDescriptor();;

    config.addComment("Used for auto-updating the config file. Ignore it");
    config.set("file-version", ClickToJoinPlugin.getInstance().getDescription().getVersion());

    config.addEmptyLine();

    config.addComment("Minimum wait between each to join message sent. (in seconds)");
    config.set("message-cooldown", message_cooldown);

    config.addEmptyLine();

    config.addComment("Click To Join messages will only be sent in these world.");
    config.addComment("Leave this empty to enable all worlds.");
    config.set("worlds_whitelist", worlds_whitelist);

    config.addEmptyLine();

    config.addComment("Resends a new click to join message if a players clicks on an arena that is already full.");
    config.set("resend_new_click_to_join_on_fail", resend_new_click_to_join_on_fail);

    config.addEmptyLine();

    config.addComment("The condition that specifies what arenas support Click To Join.");
    config.addComment("Leave this blank for all arenas.");
    config.set("arena_condition", arena_condition_string);

    config.save(new File(ClickToJoinPlugin.getAddon().getDataFolder(), "config.yml"));
  }
}
