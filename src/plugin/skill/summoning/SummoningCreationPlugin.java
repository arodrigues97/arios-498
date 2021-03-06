package plugin.skill.summoning;

import org.arios.cache.def.impl.ItemDefinition;
import org.arios.game.component.Component;
import org.arios.game.component.ComponentDefinition;
import org.arios.game.component.ComponentPlugin;
import org.arios.game.content.skill.member.summoning.SummoningCreator;
import org.arios.game.content.skill.member.summoning.SummoningPouch;
import org.arios.game.content.skill.member.summoning.SummoningScroll;
import org.arios.game.interaction.NodeUsageEvent;
import org.arios.game.interaction.UseWithHandler;
import org.arios.game.node.entity.player.Player;
import org.arios.game.node.entity.player.link.RunScript;
import org.arios.plugin.Plugin;
import org.arios.plugin.PluginManager;

/**
 * Represents a component plugin used to handle the summoning creation of a
 * node.
 * @author 'Vexia
 */
public final class SummoningCreationPlugin extends ComponentPlugin {

    @Override
    public Plugin<Object> newInstance(Object arg) throws Throwable {
	ComponentDefinition.put(669, this);
	ComponentDefinition.put(673, this);
	PluginManager.definePlugin(new ObeliskHandler());
	return this;
    }

    @Override
    public boolean handle(Player player, final Component component, int opcode, int button, final int slot, int itemId) {
	switch (button) {
	case 18:
	case 17:
	    SummoningCreator.configure(player, button == 17);
	    break;
	}
	switch (component.getId()) {
	case 669:
	case 673:
	    switch (opcode) {
	    case 230:
	    case 205:
	    case 127:
	    case 211:
		SummoningCreator.create(player, getItemAmount(opcode), component.getId() == 669 ? SummoningPouch.forSlot(slot) : SummoningScroll.forId(slot));
		break;
	    case 203:
		player.setAttribute("runscript", new RunScript() {
		    @Override
		    public boolean handle() {
			SummoningCreator.create(player, (int) getValue(), component.getId() == 669 ? SummoningPouch.forSlot(slot) : SummoningScroll.forId(slot));
			return true;
		    }
		});
		player.getDialogueInterpreter().sendInput(false, "Enter the amount:");
		return true;
	    case 187:
		SummoningCreator.list(player, SummoningPouch.forSlot(slot));
		break;
	    }
	    break;
	case 39:
	    player.getPacketDispatch().sendMessage(ItemDefinition.forId(SummoningScroll.forId(slot).getItemId()).getExamine());
	    break;
	}
	return true;
    }

    /**
     * Method used to get the item amount based on id.
     * @param opcode the opcode.
     * @return the amount to make.
     */
    private final int getItemAmount(final int opcode) {
	return opcode == 230 ? 1 : opcode == 205 ? 5 : opcode == 127 ? 10 : opcode == 211 ? 28 : -1;
    }

    /**
     * Represents the use with handler for an obelisk.
     * @author 'Vexia
     * @version 1.0
     */
    public static final class ObeliskHandler extends UseWithHandler {

	/**
	 * Represents the ids of the obelisks.
	 */
	private static final int[] IDS = new int[] { 28716, 28719, 28722, 28725, 28278, 28731, 28734 };

	/**
	 * Constructs a new {@code ObeliskHandler} {@code Object}.
	 */
	public ObeliskHandler() {
	    super(12047, 12043, 12059, 12019, 12009, 12778, 12049, 12055, 12808, 2067, 12064, 12091, 12800, 12053, 12065, 12021, 12818, 12781, 12798, 12814, 12073, 12075, 12077, 12079, 12081, 12083, 12087, 12071, 12051, 12095, 12097, 12099, 12101, 12103, 12105, 12107, 12816, 12041, 12061, 12007, 12035, 12027, 12531, 12812, 12784, 12710, 12023, 12085, 12037, 12015, 12045, 12123, 12031, 12029, 12033, 12820, 12057, 12792, 12069, 12011, 12782, 12794, 12013, 12025, 12017, 12039, 12089, 12093, 12802, 12804, 12806, 12788, 12776, 12786, 12796, 12822, 12790);
	}

	@Override
	public Plugin<Object> newInstance(Object arg) throws Throwable {
	    for (int id : IDS) {
		addHandler(id, OBJECT_TYPE, this);
	    }
	    return this;
	}

	@Override
	public boolean handle(NodeUsageEvent event) {
	    final Player player = event.getPlayer();
	    SummoningCreator.open(player, false);
	    return true;
	}

    }
}