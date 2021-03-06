package org.arios.game.content.global.quest.impl.free;

import org.arios.game.component.Component;
import org.arios.game.content.global.quest.Quest;
import org.arios.game.content.global.quest.QuestState;
import org.arios.game.content.skill.Skills;
import org.arios.game.node.entity.player.Player;
import org.arios.game.node.item.GroundItemManager;
import org.arios.game.node.item.Item;

/**
 * Represents the sheep shearer quest.
 * @author 'Veixa
 */
public class SheepShearer extends Quest {

    /**
     * Constructs a new {@code SheepShearer} {@code Object}.
     * @param player The player to construct the class for.
     */
    public SheepShearer(final Player player) {
	super(player);
    }

    /**
     * Represents the wool item.
     */
    public static final Item WOOL = new Item(1759);

    @Override
    public void finish() {
	player.getQuestRepository().update(player);
	player.getQuestRepository().incrementPoints(getQuestPoints());
	player.getConfigManager().set(101, player.getQuestRepository().getPoints());
	player.getInterfaceManager().open(new Component(277));
	for (int i = 8; i < 15; i++) {
	    player.getPacketDispatch().sendString("", 277, i);
	}
	player.getPacketDispatch().sendString("1 Quest Point", 277, 8);
	player.getPacketDispatch().sendString("150 Crafting XP", 277, 9);
	player.getPacketDispatch().sendString("60 coins", 277, 10);
	player.getPacketDispatch().sendString("You have completed the Sheep Shearer Quest!", 277, 2);
	setStage(100);
	setState(QuestState.COMPLETED);
	player.getPacketDispatch().sendItemZoomOnInterface(1735, 240, 277, 3);
	player.getSkills().addExperience(Skills.CRAFTING, 150);
	player.getInterfaceManager().closeChatbox();
	if (player.getInventory().freeSlots() == 0) {
	    GroundItemManager.create(new Item(995, 60), player.getLocation());
	} else {
	    player.getInventory().add(new Item(995, 60));
	}
	player.getPacketDispatch().sendMessage("Congratulations! Quest complete!");
    }

    @Override
    public void update() {
	for (int i = 0; i < 256; i++) {
	    player.getPacketDispatch().sendString("", 275, i);
	}
	player.getPacketDispatch().sendString("<col=8A0808>Sheep Shearer</col>", 275, 2);
	if (stage < 10) {
	    player.getPacketDispatch().sendString("<col=08088A>I can start this quest by speaking to <col=8A0808>Farmer Fred</col> <col=08088A>at his", 275, 4);
	    player.getPacketDispatch().sendString("<col=8A0808>farm</col> <col=08088A>just a little way <col=8A0808>North West of Lumbridge", 275, 5);
	    return;
	}
	if (stage == 10 || stage == 90) {
	    player.getPacketDispatch().sendString("<str>I can start this quest by speaking to Farmer Fred at his", 275, 4);
	    player.getPacketDispatch().sendString("<str>farm just a little way North West of Lumbridge</str>", 275, 5);
	    /**
	     * System.out.println("left=" + getWoolLeft(player));
	     * System.out.println("remove=" + getWoolRemove(player));
	     * System.out.println("getWoolCollect=" + getWoolCollect(player));
	     * System.out.println("given=" + getWoolGiven(player));
	     */
	    int wool = getWoolCollect(player);
	    if (wool == 0) {
		player.getPacketDispatch().sendString("<col=08088A>I have enough <col=8A0808>balls of wool</col> <col=08088A>to give <col=8A0808>Fred</col> <col=08088A> and get my</col> <col=8A0808>reward", 275, 7);
		player.getPacketDispatch().sendString("<col=8A0808>money!", 275, 8);
	    }
	    if (wool != 0) {
		player.getPacketDispatch().sendString("<col=08088A>I need to collect " + wool + " <col=8A0808>more balls of wool.", 275, 7);
	    }
	}
	if (stage == 100) {
	    player.getPacketDispatch().sendString("<str>I brought Farmer Fred 20 balls of wool, and he paid me for", 275, 4);
	    player.getPacketDispatch().sendString("<str>it!</str>", 275, 5);
	    player.getPacketDispatch().sendString("<col=FF0000>QUEST COMPLETE!</col>", 275, 7);
	}
    }

    /**
     * Method used to get the wool given.
     * @param player the player.
     * @return the wool given.
     */
    public static int getWoolGiven(final Player player) {
	return player.getAttribute("sheep-shearer:wool", 0);
    }

    /**
     * Method used to get the wool left to give.
     * @param player the player.
     * @return the wool.
     */
    public static int getWoolLeft(final Player player) {
	int left = 20 - getWoolGiven(player);
	return left;
    }

    /**
     * Method used to add wool to the current amoutn given.
     * @param player the player.
     * @return <code>True</code> if added.
     */
    public static boolean addWool(final Player player) {
	final int removeAmount = getWoolRemove(player);
	if (player.getInventory().remove(new Item(1759, removeAmount))) {
	    player.setAttribute("sheep-shearer:wool", getWoolGiven(player) + removeAmount);
	    return true;
	}
	return false;
    }

    /**
     * Method used to get the wool to remove.
     * @param player the player.
     * @return the amount that can be removed from the inventory.
     */
    public static int getWoolRemove(final Player player) {
	int woolAmount = player.getInventory().getAmount(WOOL);
	int remove = getWoolLeft(player) - (getWoolLeft(player) - woolAmount);
	int realRemove = remove > 20 ? remove - (remove - getWoolLeft(player)) : remove;
	if (realRemove > getWoolLeft(player)) {
	    realRemove = getWoolLeft(player);
	}
	return realRemove;
    }

    /**
     * Method used to get the wool left to collect.
     * @param player the player.
     * @return the wool.
     */
    public static int getWoolCollect(final Player player) {
	return 20 - (getWoolGiven(player) + getWoolRemove(player));
    }

    @Override
    public String getName() {
	return "Sheep Shearer";
    }

    @Override
    public int[] getConfig() {
	if (state == QuestState.COMPLETED) {
	    return new int[] { 179, 21 };
	} else if (state == QuestState.STARTED) {
	    return new int[] { 179, 20 };
	} else {
	    return new int[] { 179, 0 };
	}
    }

    @Override
    public int getIndex() {
	return 28;
    }

    @Override
    public int getQuestPoints() {
	return 1;
    }

}
