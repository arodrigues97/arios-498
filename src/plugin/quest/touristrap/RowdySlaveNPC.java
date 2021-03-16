package plugin.quest.touristrap;

import org.arios.cache.def.impl.NPCDefinition;
import org.arios.game.content.global.quest.impl.member.TouristTrap;
import org.arios.game.interaction.OptionHandler;
import org.arios.game.node.Node;
import org.arios.game.node.entity.Entity;
import org.arios.game.node.entity.npc.AbstractNPC;
import org.arios.game.node.entity.npc.NPC;
import org.arios.game.node.entity.player.Player;
import org.arios.game.node.item.GroundItemManager;
import org.arios.game.node.item.Item;
import org.arios.game.world.map.Location;
import org.arios.plugin.Plugin;
import org.arios.plugin.PluginManager;
import org.arios.tools.RandomFunction;

/**
 * The rowdy slave npc.
 * @author 'Vexia
 * @version 1.0
 */
public final class RowdySlaveNPC extends AbstractNPC {

    /**
     * The chats to say when talked to.
     */
    private static final String[] CHATS = new String[] { "Oi! Are you looking at me?", "I'm going to teach you some respect!", "Hey, you're in for a good beating!" };

    /**
     * Constructs a new {@code RowdySlaveNPC} {@code Object}.
     */
    public RowdySlaveNPC() {
	super(0, null);
    }

    /**
     * Constructs a new {@code RowdySlaveNPC} {@code Object}.
     * @param id the id.
     * @param location the location.
     */
    public RowdySlaveNPC(int id, Location location) {
	super(id, location);
	this.setAggressive(true);
    }

    @Override
    public AbstractNPC construct(int id, Location location, Object... objects) {
	return new RowdySlaveNPC(id, location);
    }

    @Override
    public void finalizeDeath(Entity killer) {
	super.finalizeDeath(killer);
	if (killer instanceof Player) {
	    final Player player = (Player) killer;
	    GroundItemManager.create(new Item(526), getLocation(), player);
	    if (!TouristTrap.hasSlaveClothes(player) && !player.getEquipment().containsItems(TouristTrap.SLAVE_CLOTHES)) {
		player.getPacketDispatch().sendMessages("The slave drops his shirt.", "The slave drops his robe.", "The slave drops his boots.");
		for (Item i : TouristTrap.SLAVE_CLOTHES) {
		    GroundItemManager.create(i, getLocation(), player);
		}
	    }
	}
    }

    @Override
    public Plugin<Object> newInstance(Object arg) throws Throwable {
	PluginManager.definePlugin(new OptionHandler() {

	    @Override
	    public Plugin<Object> newInstance(Object arg) throws Throwable {
		NPCDefinition.forId(getIds()[0]).getConfigurations().put("option:talk-to", this);
		return this;
	    }

	    @Override
	    public boolean handle(Player player, Node node, String option) {
		((NPC) node).sendChat(CHATS[RandomFunction.random(CHATS.length)]);
		((NPC) node).attack(player);
		return true;
	    }

	});
	return super.newInstance(arg);
    }

    @Override
    public int[] getIds() {
	return new int[] { 827 };
    }

}
