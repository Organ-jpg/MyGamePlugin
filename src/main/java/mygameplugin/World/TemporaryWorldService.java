package mygameplugin.world;

import java.io.File;
import mygameplugin.MyGamePlugin;
import mygameplugin.gamemode.GameModeType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.World.Environment;

public class TemporaryWorldService implements WorldService {
    private final MyGamePlugin plugin;

    public TemporaryWorldService(MyGamePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public World prepareWorld(GameModeType modeType) {
        return createMatchWorld(modeType);
    }

    public World createMatchWorld(GameModeType modeType) {
        String base = modeType == GameModeType.IMPOSTER || modeType == GameModeType.INFECTOR
                ? "temp_world"
                : "game_match";

        World overworld = createWorld(base, Environment.NORMAL);
        createWorld(base + "_nether", Environment.NETHER);

        if (modeType == GameModeType.IMPOSTER || modeType == GameModeType.INFECTOR) {
            createWorld(base + "_the_end", Environment.THE_END);
        }

        return overworld;
    }

    public void unloadAndDelete(World world) {
        if (world == null) {
            return;
        }
        String baseName = world.getName();
        unloadByName(baseName);
        unloadByName(baseName + "_nether");
        unloadByName(baseName + "_the_end");

        deleteWorldFolder(baseName);
        deleteWorldFolder(baseName + "_nether");
        deleteWorldFolder(baseName + "_the_end");
    }

    public boolean hasPlugin(String name) {
        return Bukkit.getPluginManager().getPlugin(name) != null;
    }

    private World createWorld(String name, Environment environment) {
        World existing = Bukkit.getWorld(name);
        if (existing != null) {
            return existing;
        }
        WorldCreator creator = new WorldCreator(name);
        creator.environment(environment);
        return creator.createWorld();
    }

    private void unloadByName(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Bukkit.unloadWorld(world, false);
        }
    }

    private void deleteWorldFolder(String worldName) {
        File folder = new File(plugin.getServer().getWorldContainer(), worldName);
        deleteRecursively(folder);
    }

    private void deleteRecursively(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}
