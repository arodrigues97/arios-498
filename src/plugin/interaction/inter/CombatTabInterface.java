package plugin.interaction.inter;

import org.arios.game.component.Component;
import org.arios.game.component.ComponentDefinition;
import org.arios.game.component.ComponentPlugin;
import org.arios.game.node.entity.combat.CombatStyle;
import org.arios.game.node.entity.combat.CombatSwingHandler;
import org.arios.game.node.entity.combat.equipment.WeaponInterface;
import org.arios.game.node.entity.combat.equipment.WeaponInterface.WeaponInterfaces;
import org.arios.game.node.entity.player.Player;
import org.arios.game.system.task.Pulse;
import org.arios.game.world.GameWorld;
import org.arios.plugin.Plugin;

/**
 * Represents the combat tab interface.
 * @author Emperor
 * @author Vexia'
 * @version 1.0
 */
public class CombatTabInterface extends ComponentPlugin {

    @Override
    public Plugin<Object> newInstance(Object arg) throws Throwable {
	for (WeaponInterfaces inter : WeaponInterfaces.values()) {
	    ComponentDefinition.put(inter.getInterfaceId(), this);
	}
	ComponentDefinition.put(92, this);
	return this;
    }

    @Override
    public boolean handle(final Player p, Component component, int opcode, int button, int slot, int itemId) {
	switch (button) {
	case 7:
	case 9:
	case 24:
	case 26:
	    GameWorld.submit(new Pulse(1, p) {
		@Override
		public boolean pulse() {
		    p.getSettings().toggleRetaliating();
		    return true;
		}
	    });
	    break;
	case 8:
	case 10:
	    GameWorld.submit(new Pulse(1, p) {
		@Override
		public boolean pulse() {
		    WeaponInterface inter = p.getExtension(WeaponInterface.class);
		    if (inter != null && inter.isSpecialBar()) {
			p.getSettings().toggleSpecialBar();
			if (p.getSettings().isSpecialToggled()) {
			    CombatSwingHandler handler;
			    if ((handler = CombatStyle.MELEE.getSwingHandler().getSpecial(p.getEquipment().getNew(3).getId())) != null) {
				@SuppressWarnings("unchecked")
				Plugin<Object> plugin = (Plugin<Object>) ((Object) handler);
				if (plugin.fireEvent("instant_spec", p) == Boolean.TRUE) {
				    handleInstantSpec(p, handler, plugin);
				}
			    }
			}
		    }
		    return true;
		}
	    });
	    break;
	case 0:
	default:
	    WeaponInterface inter = p.getExtension(WeaponInterface.class);
	    if (inter == null) {
		return false;
	    }
	    if (inter.setAttackStyle(button)) {
		if (button == 4 || button == 5) {
		    inter.openAutocastSelect();
		} else if (p.getProperties().getAutocastSpell() != null) {
		    inter.selectAutoSpell(-1, false);
		}
		return true;
	    }
	    return false;
	}
	return true;
    }

    /**
     * Method used to handle an instance special attack.
     * @param p the player.
     * @param handler the handler.
     * @param plugin the plugin.
     */
    private static void handleInstantSpec(Player p, CombatSwingHandler handler, Plugin<Object> plugin) {
	handler.swing(p, p.getProperties().getCombatPulse().getVictim(), null);
    }
}
