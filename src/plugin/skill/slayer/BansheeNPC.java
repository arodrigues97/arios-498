package plugin.skill.slayer;

import org.arios.game.content.skill.Skills;
import org.arios.game.content.skill.member.slayer.Equipment;
import org.arios.game.node.entity.Entity;
import org.arios.game.node.entity.combat.BattleState;
import org.arios.game.node.entity.combat.CombatStyle;
import org.arios.game.node.entity.combat.CombatSwingHandler;
import org.arios.game.node.entity.combat.InteractionType;
import org.arios.game.node.entity.combat.handlers.MeleeSwingHandler;
import org.arios.game.node.entity.impl.Animator.Priority;
import org.arios.game.node.entity.npc.AbstractNPC;
import org.arios.game.node.entity.player.Player;
import org.arios.game.node.entity.player.info.portal.Perks;
import org.arios.game.world.GameWorld;
import org.arios.game.world.map.Location;
import org.arios.game.world.update.flag.context.Animation;
import org.arios.tools.RandomFunction;

/**
 * Handles the banshee npc.
 * @author Vexia
 */
public final class BansheeNPC extends AbstractNPC {

    /**
     * The skills to drain.
     */
    private static final int[] SKILLS = new int[] { Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.RANGE, Skills.MAGIC, Skills.PRAYER, Skills.AGILITY };

    /**
     * The combat handler.
     */
    private static final MeleeSwingHandler COMBAT_HANDLER = new MeleeSwingHandler() {
	@Override
	public void impact(Entity entity, Entity victim, BattleState state) {
	    if (victim instanceof Player) {
		final Player player = (Player) victim;
		if (!hasEarMuffs(player)) {
		    if (RandomFunction.random(10) < 4 && player.getProperties().getCombatPulse().getNextAttack() <= GameWorld.getTicks()) {
			player.getWalkingQueue().reset();
			player.getLocks().lockMovement(3);
			player.getProperties().getCombatPulse().setNextAttack(3);
			player.animate(new Animation(1572, Priority.HIGH));
		    }
		    for (int skill : SKILLS) {
			int drain = (int) (player.getSkills().getStaticLevel(skill) * 0.5);
			player.getSkills().updateLevel(skill, -drain, 0);
		    }
		    state.setEstimatedHit(8);
		}
	    }
	    super.impact(entity, victim, state);
	}

	@Override
	public InteractionType isAttackable(Entity entity, Entity victim) {
	    return CombatStyle.MAGIC.getSwingHandler().isAttackable(entity, victim);
	}
    };

    /**
     * Constructs a new {@code BansheeNPC} {@code Object}.
     * @param id the id.
     * @param location the location.
     */
    public BansheeNPC(int id, Location location) {
	super(id, location);
    }

    /**
     * Constructs a new {@code BansheeNPC} {@code Object}.
     */
    public BansheeNPC() {
	super(0, null);
    }

    @Override
    public AbstractNPC construct(int id, Location location, Object... objects) {
	return new BansheeNPC(id, location);
    }

    @Override
    public void checkImpact(BattleState state) {
	super.checkImpact(state);
	if (state.getAttacker() instanceof Player) {
	    final Player player = (Player) state.getAttacker();
	    if (!hasEarMuffs(player)) {
		state.neutralizeHits();
	    }
	}
	if (state.getEstimatedHit() > 0 || state.getSecondaryHit() > 0) {
	    getSkills().heal(1);
	}
    }

    @Override
    public CombatSwingHandler getSwingHandler(boolean swing) {
	return COMBAT_HANDLER;
    }

    /**
     * Checks if the player has ear muffs.
     * @param player the player.
     * @return {@code True} if they have it.
     */
    public static boolean hasEarMuffs(Player player) {
	if(player.getDetails().getShop().hasPerk(Perks.MASTER_OF_SLAYER)){
	    return true;
	}
	return player.getEquipment().containsItem(Equipment.EARMUFFS.getItem());
    }

    @Override
    public int[] getIds() {
	return new int[] { 1612 };
    }

}
