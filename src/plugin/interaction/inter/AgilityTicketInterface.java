package plugin.interaction.inter;

import org.arios.game.component.Component;
import org.arios.game.component.ComponentDefinition;
import org.arios.game.component.ComponentPlugin;
import org.arios.game.content.skill.Skills;
import org.arios.game.node.entity.player.Player;
import org.arios.game.node.item.Item;
import org.arios.plugin.Plugin;
import org.arios.tools.StringUtils;

/**
 * Represents the plugin used to handle the agility ticket interface.
 * @author 'Vexia
 * @version 1.0
 */
public final class AgilityTicketInterface extends ComponentPlugin {

    /**
     * Represents the pirate hook item.
     */
    private static final Item PIRATE_HOOK = new Item(2997);

    /**
     * Represents the toadflax item.
     */
    private static final Item TOADFLAX = new Item(2998);

    /**
     * Represents the snapdragon item.
     */
    private static final Item SNAPDRAGON = new Item(3000);

    /**
     * Represents the aerna ticket.
     */
    private static final int ARENA_TICKET = 2996;

    @Override
    public Plugin<Object> newInstance(Object arg) throws Throwable {
	ComponentDefinition.forId(6).setPlugin(this);
	return this;
    }

    @Override
    public boolean handle(Player player, Component component, int opcode, int button, int slot, int itemId) {
	Item reward = null;
	double experience = 0;
	int tickets = 0;
	switch (button) {
	case 93:// 1 exp
	    experience = 240;
	    tickets = 1;
	    break;
	case 95:// 10 exp.
	    experience = 2480;
	    tickets = 10;
	    break;
	case 96:// 25 exp.
	    experience = 6500;
	    tickets = 25;
	    break;
	case 97:// 100 exp.
	    experience = 28000;
	    tickets = 100;
	    break;
	case 98:// 1000 exp.
	    experience = 320000;
	    tickets = 1000;
	    break;
	case 89:// toadflax.
	    reward = TOADFLAX;
	    tickets = 3;
	    break;
	case 99:// snap dragon.
	    reward = SNAPDRAGON;
	    tickets = 10;
	    break;
	case 88:// pirates hook.
	    reward = PIRATE_HOOK;
	    tickets = 800;
	    break;
	}
	if (experience > 0 && !player.getInventory().contains(ARENA_TICKET, tickets)) {
	    player.getPacketDispatch().sendMessage("This Agility experience costs " + tickets + " tickets.");
	}
	if (reward != null && !player.getInventory().contains(ARENA_TICKET, tickets)) {
	    player.getPacketDispatch().sendMessage(StringUtils.formatDisplayName(reward.getName().replace("Clean", "").trim()) + " costs " + tickets + " tickets.");
	}
	if (!player.getInventory().contains(ARENA_TICKET, tickets)) {
	    return true;
	}
	if (experience > 0) {
	    if (!player.getInventory().containsItem(new Item(ARENA_TICKET, tickets))) {
		return false;
	    }
	    if (player.getInventory().remove(new Item(ARENA_TICKET, tickets))) {
		player.getSkills().addExperience(Skills.AGILITY, experience);
		player.getPacketDispatch().sendMessage("You have been granted some Agility experience!");
	    }
	}
	if (reward != null) {
	    if (player.getInventory().remove(new Item(ARENA_TICKET, tickets))) {
		player.getInventory().add(reward);
		player.getPacketDispatch().sendMessage("You have been granted a " + StringUtils.formatDisplayName(reward.getName().replace("Clean", "").trim()) + ".");
	    }
	}
	return true;
    }

}
