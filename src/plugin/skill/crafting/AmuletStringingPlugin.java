package plugin.skill.crafting;

import org.arios.game.content.skill.Skills;
import org.arios.game.content.skill.free.crafting.jewellery.JewelleryCrafting;
import org.arios.game.interaction.NodeUsageEvent;
import org.arios.game.interaction.UseWithHandler;
import org.arios.game.node.entity.player.Player;
import org.arios.game.node.item.Item;
import org.arios.plugin.Plugin;

/**
 * Represents the plugin used to string an amulet.
 * @author 'Vexia
 * @version 1.0
 */
public final class AmuletStringingPlugin extends UseWithHandler {

    /**
     * Constructs a new {@code AmuletStringingPlugin} {@code Object}.
     */
    public AmuletStringingPlugin() {
	super(1673, 1675, 1677, 1679, 1681, 1683, 6579);
    }

    @Override
    public Plugin<Object> newInstance(Object arg) throws Throwable {
	addHandler(1759, ITEM_TYPE, this);
	return this;
    }

    @Override
    public boolean handle(NodeUsageEvent event) {
	final Player player = event.getPlayer();
	final JewelleryCrafting.JewelleryItem data = JewelleryCrafting.JewelleryItem.forProduct(event.getUsedItem().getId() == 6579 ? 6579 : ((Item) event.getUsedWith()).getId());
	if (data == null) {
	    return true;
	}
	if (player.getSkills().getLevel(Skills.CRAFTING) < data.getLevel()) {
	    player.getPacketDispatch().sendMessage("You need a Crafting level of at least " + data.getLevel() + " to do that.");
	    return true;
	}
	if (player.getInventory().remove(event.getUsedItem(), event.getBaseItem())) {
	    player.getInventory().add(new Item(data == JewelleryCrafting.JewelleryItem.ONYX_AMULET ? 6581 : data.getSendItem() + 19));
	}
	return true;
    }

}
