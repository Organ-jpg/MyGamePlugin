package mygameplugin.border;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class BorderManager {
    public void configure(World world, Location center, double size) {
        WorldBorder border = world.getWorldBorder();
        border.setCenter(center);
        border.setSize(size);
    }

    public void shrink(World world, double targetSize, long seconds) {
        world.getWorldBorder().setSize(targetSize, seconds);
    }
}
