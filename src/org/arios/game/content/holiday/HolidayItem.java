package org.arios.game.content.holiday;

import org.arios.game.node.item.GroundItem;
import org.arios.game.node.item.GroundItemManager;
import org.arios.game.node.item.Item;
import org.arios.game.system.task.Pulse;
import org.arios.game.world.GameWorld;
import org.arios.game.world.map.Location;
import org.arios.game.world.update.flag.context.Graphics;
import org.arios.tools.RandomFunction;

/**
 * Used for releasing trade-able holiday items.
 * @author Emperor
 */
public final class HolidayItem extends GroundItem {

    /**
     * Constructs a new {@code HolidayItem} {@code Object}.
     * @param item the item.
     * @param location the location.
     */
    public HolidayItem(Item item, Location location) {
	super(item, location);
    }

    /**
     * Starts random holiday item spawning.
     * @param spawn The item.
     * @param interval The interval between moving the location.
     * @param spawnLocations The spawn locations.
     */
    public static void startRandomSpawn(final Item spawn, int interval, final Location... spawnLocations) {
	GameWorld.submit(new Pulse(interval) {
	    GroundItem item;
	    int lastIndex = -1;

	    @Override
	    public boolean pulse() {
		if (item != null) {
		    if (!item.isRemoved()) {
			Graphics.send(Graphics.create(85), item.getLocation());
		    } else if (ItemLimitation.decreaseAmount(spawn.getId())) {
			System.out.println("Released all " + spawn.getName() + "s!");
			return true;
		    }
		    GroundItemManager.destroy(item);
		}
		int index = RandomFunction.random(spawnLocations.length);
		if (index == lastIndex) {
		    index = (index + 1) % spawnLocations.length;
		}
		spawn.setLocation(spawnLocations[index]);
		item = GroundItemManager.create(new HolidayItem(spawn, spawnLocations[index]));
		lastIndex = index;
		return false;
	    }
	});
    }

    @Override
    public String toString() {
	return "HolidaySpawn [name=" + getName() + ", loc=" + getLocation() + "]";
    }

    @Override
    public boolean isActive() {
	return true;
    }

    @Override
    public boolean isPrivate() {
	return false;
    }

    @Override
    public boolean isAutoSpawn() {
	return false;
    }

    @Override
    public void respawn() {
	/*
	 * empty.
	 */
    }
}