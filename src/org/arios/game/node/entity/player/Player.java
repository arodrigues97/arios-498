package org.arios.game.node.entity.player;

import java.util.ArrayList;
import java.util.List;

import org.arios.ServerConstants;
import org.arios.game.component.Component;
import org.arios.game.container.Container;
import org.arios.game.container.impl.BankContainer;
import org.arios.game.container.impl.EquipmentContainer;
import org.arios.game.container.impl.InventoryListener;
import org.arios.game.content.ame.AntiMacroHandler;
import org.arios.game.content.dialogue.DialogueInterpreter;
import org.arios.game.content.eco.ge.GrandExchange;
import org.arios.game.content.global.quest.QuestRepository;
import org.arios.game.content.global.ttrail.TreasureTrailManager;
import org.arios.game.content.skill.Skills;
import org.arios.game.content.skill.member.construction.HouseManager;
import org.arios.game.content.skill.member.farming.FarmingManager;
import org.arios.game.content.skill.member.hunter.HunterManager;
import org.arios.game.content.skill.member.slayer.SlayerManager;
import org.arios.game.content.skill.member.summoning.familiar.FamiliarManager;
import org.arios.game.interaction.Interaction;
import org.arios.game.node.entity.Entity;
import org.arios.game.node.entity.combat.BattleState;
import org.arios.game.node.entity.combat.CombatStyle;
import org.arios.game.node.entity.combat.CombatSwingHandler;
import org.arios.game.node.entity.combat.DeathTask;
import org.arios.game.node.entity.combat.ImpactHandler.HitsplatType;
import org.arios.game.node.entity.combat.equipment.DegradableEquipment;
import org.arios.game.node.entity.combat.handlers.ChinchompaSwingHandler;
import org.arios.game.node.entity.combat.handlers.SalamanderSwingHandler;
import org.arios.game.node.entity.npc.NPC;
import org.arios.game.node.entity.player.info.PlayerDetails;
import org.arios.game.node.entity.player.info.RenderInfo;
import org.arios.game.node.entity.player.info.Rights;
import org.arios.game.node.entity.player.info.UIDInfo;
import org.arios.game.node.entity.player.info.login.LoginConfiguration;
import org.arios.game.node.entity.player.info.portal.DonatorType;
import org.arios.game.node.entity.player.info.portal.Perks;
import org.arios.game.node.entity.player.link.BankPinManager;
import org.arios.game.node.entity.player.link.BarcrawlManager;
import org.arios.game.node.entity.player.link.ConfigurationManager;
import org.arios.game.node.entity.player.link.EmoteData;
import org.arios.game.node.entity.player.link.GlobalData;
import org.arios.game.node.entity.player.link.HintIconManager;
import org.arios.game.node.entity.player.link.InterfaceManager;
import org.arios.game.node.entity.player.link.IronmanManager;
import org.arios.game.node.entity.player.link.IronmanMode;
import org.arios.game.node.entity.player.link.PacketDispatch;
import org.arios.game.node.entity.player.link.SavedData;
import org.arios.game.node.entity.player.link.Settings;
import org.arios.game.node.entity.player.link.SkullManager;
import org.arios.game.node.entity.player.link.SpellBookManager;
import org.arios.game.node.entity.player.link.WarningMessages;
import org.arios.game.node.entity.player.link.appearance.Appearance;
import org.arios.game.node.entity.player.link.audio.AudioManager;
import org.arios.game.node.entity.player.link.diary.AchievementDiaryManager;
import org.arios.game.node.entity.player.link.grave.GraveManager;
import org.arios.game.node.entity.player.link.music.MusicPlayer;
import org.arios.game.node.entity.player.link.prayer.Prayer;
import org.arios.game.node.entity.player.link.prayer.PrayerType;
import org.arios.game.node.entity.player.link.request.RequestManager;
import org.arios.game.node.item.GroundItem;
import org.arios.game.node.item.GroundItemManager;
import org.arios.game.node.item.Item;
import org.arios.game.system.communication.CommunicationInfo;
import org.arios.game.system.monitor.PlayerMonitor;
import org.arios.game.system.task.LogoutTask;
import org.arios.game.world.GameWorld;
import org.arios.game.world.map.Direction;
import org.arios.game.world.map.Location;
import org.arios.game.world.map.Region;
import org.arios.game.world.map.RegionChunk;
import org.arios.game.world.map.RegionManager;
import org.arios.game.world.map.Viewport;
import org.arios.game.world.map.build.DynamicRegion;
import org.arios.game.world.map.zone.ZoneType;
import org.arios.game.world.repository.Repository;
import org.arios.game.world.update.MapChunkRenderer;
import org.arios.game.world.update.NPCRenderer;
import org.arios.game.world.update.PlayerRenderer;
import org.arios.game.world.update.UpdateSequence;
import org.arios.game.world.update.flag.PlayerFlags;
import org.arios.game.world.update.flag.player.FaceEntityFlag;
import org.arios.game.world.update.flag.player.FaceLocationFlag;
import org.arios.game.world.update.flag.player.ForceChatFlag;
import org.arios.net.IoSession;
import org.arios.net.packet.PacketRepository;
import org.arios.net.packet.context.DynamicSceneContext;
import org.arios.net.packet.context.SceneGraphContext;
import org.arios.net.packet.context.SkillContext;
import org.arios.net.packet.out.BuildDynamicScene;
import org.arios.net.packet.out.SkillLevel;
import org.arios.net.packet.out.UpdateSceneGraph;
import org.arios.plugin.Plugin;
import org.arios.tools.StringUtils;

/**
 * Represents a player entity.
 * @author Emperor
 * @author Vexia
 */
public class Player extends Entity {

    /**
     * The details of the player.
     */
    private final PlayerDetails details;

    /**
     * The inventory.
     */
    private final Container inventory = new Container(28).register(new InventoryListener(this));

    /**
     * The equipment.
     */
    private final EquipmentContainer equipment = new EquipmentContainer(this);

    /**
     * The bank container.
     */
    private final BankContainer bank = new BankContainer(this);

    /**
     * The packet dispatcher.
     */
    private final PacketDispatch packetDispatch = new PacketDispatch(this);

    /**
     * The spell book manager.
     */
    private final SpellBookManager spellBookManager = new SpellBookManager();

    /**
     * The rendering info.
     */
    private final RenderInfo renderInfo = new RenderInfo(this);

    /**
     * The interface manager.
     */
    private final InterfaceManager interfaceManager = new InterfaceManager(this);

    /**
     * The emote data.
     */
    private final EmoteData emotes = new EmoteData(this);

    /**
     * The player flags.
     */
    private final PlayerFlags playerFlags = new PlayerFlags();

    /**
     * The appearance.
     */
    private Appearance appearance = new Appearance(this);

    /**
     * The settings of the player.
     */
    private final Settings settings = new Settings(this);

    /**
     * The dialogue interpreter.
     */
    private final DialogueInterpreter dialogueInterpreter = new DialogueInterpreter(this);

    /**
     * The hint icon manager.
     */
    private final HintIconManager hintIconManager = new HintIconManager();

    /**
     * The slayer manager.
     */
    private final SlayerManager slayer = new SlayerManager(this);

    /**
     * The quest repository.
     */
    private final QuestRepository questRepository = new QuestRepository(this);

    /**
     * The prayer manager.
     */
    private final Prayer prayer = new Prayer(this);

    /**
     * The skull manager.
     */
    private final SkullManager skullManager = new SkullManager(this);

    /**
     * The grave stone manager.
     */
    private final GraveManager graveManager = new GraveManager(this);

    /**
     * The grand exchange manager.
     */
    private final GrandExchange grandExchange = new GrandExchange(this);

    /**
     * The familiar manager.
     */
    private final FamiliarManager familiarManager = new FamiliarManager(this);

    /**
     * The config manager.
     */
    private final ConfigurationManager configManager = new ConfigurationManager(this);

    /**
     * The saved data.
     */
    private final SavedData savedData = new SavedData(this);

    /**
     * The request manager.
     */
    private final RequestManager requestManager = new RequestManager(this);

    /**
     * The farming manager.
     */
    private final FarmingManager farmingManager = new FarmingManager(this);

    /**
     * The monitor which monitors player actions.
     */
    private final PlayerMonitor monitor = new PlayerMonitor();

    /**
     * Represents the players warning messages.
     */
    private final WarningMessages warningMessages = new WarningMessages();

    /**
     * The music player instance.
     */
    private final MusicPlayer musicPlayer = new MusicPlayer(this);

    /**
     * The house manager.
     */
    private final HouseManager houseManager = new HouseManager(this);

    /**
     * The barcrawl miniquest manager.
     */
    private final BarcrawlManager barcrawlManager = new BarcrawlManager(this);

    /**
     * The anti macro handler.
     */
    private final AntiMacroHandler antiMacroHandler = new AntiMacroHandler(this);

    /**
     * The hunter manager.
     */
    private final HunterManager hunterManager = new HunterManager(this);

    /**
     * The treasure trail manager.
     */
    private final TreasureTrailManager treasureTrailManager = new TreasureTrailManager(this);

    /**
     * The audio manager.
     */
    private final AudioManager audioManager = new AudioManager(this);

    /**
     * The bank pin manager.
     */
    private final BankPinManager bankPinManager = new BankPinManager(this);

    /**
     * The achievement diary manager.
     */
    private final AchievementDiaryManager achievementDiaryManager = new AchievementDiaryManager(this);

    /**
     * The Ironman manager.
     */
    private final IronmanManager ironmanManager = new IronmanManager(this);
    /**
     * The logout plugins.
     */
    private List<Plugin<Player>> logoutPlugins;

    /**
     * The boolean for the player playing.
     */
    private boolean playing;

    /**
     * If the player is artificial.
     */
    protected boolean artificial;

    /**
     * If the player is invisible.
     */
    private boolean invisible;

    /**
     * Constructs a new {@code Player} {@code Object}.
     * @param details The player's details.
     */
    public Player(PlayerDetails details) {
	super(details.getUsername(), ServerConstants.getStartLocation());
	super.active = false;
	super.interaction = new Interaction(this);
	this.details = details;
	this.direction = Direction.SOUTH;
    }

    @Override
    public void init() {
	if (!isArtificial()) {
	    getProperties().setSpawnLocation(ServerConstants.getHomeLocation());
	    getDetails().getSession().setObject(this);
	    getDetails().getSession().setLastPing(System.currentTimeMillis() + 10_000L);
	}
	super.init();
	LoginConfiguration.configureLobby(this);
    }

    @Override
    public void clear() {
	if (!allowRemoval()) {
	    Repository.getDisconnectionQueue().add(this, true);
	    return;
	}
	setPlaying(false);
	getWalkingQueue().reset();
	LogoutTask task = getExtension(LogoutTask.class);
	if (task != null) {
	    task.fire(this);
	}
	if (logoutPlugins != null && !logoutPlugins.isEmpty()) {
	    for (int i = 0; i < logoutPlugins.size(); i++) {
		if (i == logoutPlugins.size()) {
		    break;
		}
		Plugin<Player> plugin = logoutPlugins.get(i);
		if (plugin != null) {
		    try {
			plugin.newInstance(this);
		    } catch (Throwable e) {
			e.printStackTrace();
		    }
		}
	    }
	}
	if (familiarManager.hasFamiliar()) {
	    familiarManager.getFamiliar().clear();
	}
	interfaceManager.close();
	interfaceManager.closeSingleTab();
	super.clear();
	getZoneMonitor().clear();
	CommunicationInfo.notifyPlayers(this, false, false);
	hunterManager.logout();
	UpdateSequence.getRenderablePlayers().remove(this);
	Repository.getDisconnectionQueue().add(this);
    }

    @Override
    public void tick() {
	super.tick();
	antiMacroHandler.pulse();
	hunterManager.pulse();
	if (!artificial && (System.currentTimeMillis() - getSession().getLastPing()) > 10_000L) {
	    details.getSession().disconnect();
	    getSession().setLastPing(Long.MAX_VALUE);
	}
    }

    @Override
    public void update() {
	super.update();
	if (playerFlags.isUpdateSceneGraph()) {
	    updateSceneGraph(false);
	}
	PlayerRenderer.render(this);
	NPCRenderer.render(this);
	MapChunkRenderer.render(this);
    }

    @Override
    public void reset() {
	super.reset();
	playerFlags.setUpdateSceneGraph(false);
	renderInfo.updateInformation();
	if (getSkills().isLifepointsUpdate()) {
	    PacketRepository.send(SkillLevel.class, new SkillContext(this, Skills.HITPOINTS));
	    getSkills().setLifepointsUpdate(false);
	}
    }

    @Override
    public boolean face(Entity entity) {
	if (entity == null) {
	    if (getUpdateMasks().unregisterSynced(FaceEntityFlag.getOrdinal())) {
		return getUpdateMasks().register(new FaceEntityFlag(entity));
	    }
	    return true;
	}
	return getUpdateMasks().register(new FaceEntityFlag(entity), true);
    }

    @Override
    public boolean faceLocation(Location location) {
	if (location == null) {
	    getUpdateMasks().unregisterSynced(FaceLocationFlag.getOrdinal());
	    return true;
	}
	return getUpdateMasks().register(new FaceLocationFlag(location), true);
    }

    @Override
    public boolean sendChat(String string) {
	return getUpdateMasks().register(new ForceChatFlag(string));
    }

    @Override
    public int getClientIndex() {
	return this.getIndex() | 0x8000;
    }

    @Override
    public CombatSwingHandler getSwingHandler(boolean swing) {
	CombatStyle style = getProperties().getCombatPulse().getStyle();
	if (swing) {
	    if (getProperties().getSpell() != null || getProperties().getAutocastSpell() != null) {
		return CombatStyle.MAGIC.getSwingHandler();
	    }
	    if (settings.isSpecialToggled()) {
		int itemId = equipment.getNew(3).getId();
		CombatSwingHandler handler;
		if ((handler = style.getSwingHandler().getSpecial(itemId)) != null) {
		    return handler;
		}
		packetDispatch.sendMessage("Unhandled special attack for item " + itemId + "!");
	    }
	}
	if (style == CombatStyle.RANGE && equipment.getNew(3).getId() == 10034) {
	    return ChinchompaSwingHandler.getInstance();
	}
	if (equipment.getNew(3).getId() == 10149 || equipment.getNew(3).getId() == 10146 || equipment.getNew(3).getId() == 10147 || equipment.getNew(3).getId() == 10148) {
	    return SalamanderSwingHandler.INSTANCE;
	}
	return style.getSwingHandler();
    }

    @Override
    public void commenceDeath(Entity killer) {
	super.commenceDeath(killer);
	if (prayer.get(PrayerType.RETRIBUTION)) {
	    prayer.startRetribution(killer);
	}
    }

    @Override
    public void finalizeDeath(Entity killer) {
	settings.setSpecialEnergy(100);
	settings.updateRunEnergy(settings.getRunEnergy() - 100);
	Player k = killer instanceof Player ? (Player) killer : this;
	if (!k.isActive()) {
	    k = this;
	}
	getPacketDispatch().sendMessage("Oh dear, you are dead!");
	packetDispatch.sendTempMusic(90);
	if (!getZoneMonitor().handleDeath(killer) && (!getProperties().isSafeZone() && getZoneMonitor().getType() != ZoneType.SAFE.getId()) && getDetails().getRights() != Rights.ADMINISTRATOR) {
	    GroundItemManager.create(new Item(526), getLocation(), k);
	    final Container[] c = DeathTask.getContainers(this);
	    boolean gravestone = graveManager.generateable() && getIronmanManager().getMode() != IronmanMode.ULTIMATE;
	    int seconds = graveManager.getType().getDecay() * 60;
	    int ticks = (1000 * seconds) / 600;
	    if (getDetails().getShop().hasPerk(Perks.OUT_OF_GRAVE_DANGER)) {
		ticks *= 2;
	    }
	    List<GroundItem> items = new ArrayList<>();
	    for (Item item : c[1].toArray()) {
		if (item != null) {
		    GroundItem ground;
		    if (item.hasItemPlugin()) {
			item = item.getPlugin().getDeathItem(item);
		    }
		    if (gravestone || !item.getDefinition().isTradeable()) {
			ground = new GroundItem(item, getLocation(), gravestone ? ticks + 100 : 200, this);
		    } else {
			ground = new GroundItem(item.getDropItem(), getLocation(), k);
		    }
		    items.add(ground);
		    /*if killer is an ironman then we set the dropper to the player who was killed, so the ironman can't pick it up*/
		    if(k.getIronmanManager().checkRestriction()){
			ground.setDropper(this);
		    }
		    if (getIronmanManager().getMode() != IronmanMode.ULTIMATE) {
			GroundItemManager.create(ground);
		    }
		}
	    }
	    equipment.clear();
	    inventory.clear();
	    inventory.addAll(c[0]);
	    if (gravestone) {
		graveManager.create(ticks, items);
	    }
	    familiarManager.dismiss();
	}
	skullManager.setSkulled(false);
	removeAttribute("combat-time");
	getPrayer().reset();
	super.finalizeDeath(killer);
	appearance.sync();
	if (killer instanceof Player && !GameWorld.isEconomyWorld() && getSkullManager().isWilderness() && killer.asPlayer().getSkullManager().isWilderness()) {
	    killer.asPlayer().getSavedData().getSpawnData().onDeath(killer.asPlayer(), this);
	}
	if (GameWorld.isEconomyWorld() && !getSavedData().getGlobalData().isDeathScreenDisabled()) {
	    getInterfaceManager().open(new Component(153));
	}
    }

    @Override
    public boolean hasProtectionPrayer(CombatStyle style) {
	if (style == null) {
	    return false;
	}
	return prayer.get(style.getProtectionPrayer());
    }

    @Override
    public int getDragonfireProtection(boolean fire) {
	int value = 0;
	if (fire) {
	    if (hasFireResistance()) {
		value |= 0x2;
	    }
	}
	Item item = equipment.get(EquipmentContainer.SLOT_SHIELD);
	if (item != null && (fire && (item.getId() == 11283 || item.getId() == 11284 || item.getId() == 1540) || (!fire && (item.getId() == 2890 || item.getId() == 9731)))) {
	    value |= 0x4;
	}
	if (prayer.get(PrayerType.PROTECT_FROM_MAGIC)) {
	    value |= 0x8;
	}
	setAttribute("fire_resistance", value);
	return value;
    }

    @Deprecated
    @Override
    public void setLocation(Location location) {
	super.setLocation(location);
    }

    @Override
    public void fullRestore() {
	prayer.reset();
	settings.setSpecialEnergy(100);
	settings.updateRunEnergy(-100);
	super.fullRestore();
    }

    @Override
    public boolean isAttackable(Entity entity, CombatStyle style) {
	if (entity instanceof NPC && !((NPC) entity).getDefinition().hasAction("attack") && !((NPC) entity).isIgnoreAttackRestrictions(this)) {
	    getPacketDispatch().sendMessage("You can't attack this npc.");
	    return false;
	}
	return super.isAttackable(entity, style);
    }

    @Override
    public boolean isPoisonImmune() {
	return getAttribute("poison:immunity", -1) > GameWorld.getTicks();
    }

    @Override
    public void onImpact(final Entity entity, BattleState state) {
	super.onImpact(entity, state);
	boolean recoil = getEquipment().getNew(EquipmentContainer.SLOT_RING).getId() == 2550;
	if (state.getEstimatedHit() > 0) {
	    if (getAttribute("vengeance", false)) {
		removeAttribute("vengeance");
		final int hit = (int) (state.getEstimatedHit() * 0.75);
		sendChat("Taste vengeance!");
		if (hit > -1) {
		    entity.getImpactHandler().manualHit(Player.this, hit, HitsplatType.NORMAL);
		}
	    }
	    if (recoil) {
		getImpactHandler().handleRecoilEffect(entity, state.getEstimatedHit());
	    }
	}
	if (state.getSecondaryHit() > 0) {
	    if (recoil) {
		getImpactHandler().handleRecoilEffect(entity, state.getSecondaryHit());
	    }
	}
	DegradableEquipment.degrade(this, entity, false);
    }

    /**
     * Checks if the containers have this item.
     * @param item the item.
     * @return {@code True} if so.
     */
    public boolean hasItem(Item item) {
	return getInventory().containsItem(item) || getBank().containsItem(item) || getEquipment().containsItem(item);
    }

    /**
     * Gets the player extra experience mod.
     * @return the mod.
     */
    public double getExperienceMod() {
	return getDetails().getPortal().getShop().hasDoubleXp() ? 2 : 0;
    }

    /**
     * Initializes the player for reconnection.
     */
    public void initReconnect() {
	getInterfaceManager().setChatbox(null);
	getPulseManager().clear();
	getZoneMonitor().getZones().clear();
	getViewport().setCurrentPlane(RegionManager.forId(66666).getPlanes()[3]);
	configManager.reset();
	playerFlags.setLastSceneGraph(null);
	playerFlags.setUpdateSceneGraph(false);
	playerFlags.setLastViewport(new RegionChunk[Viewport.CHUNK_SIZE][Viewport.CHUNK_SIZE]);
	renderInfo.getLocalNpcs().clear();
	renderInfo.getLocalPlayers().clear();
	renderInfo.setLastLocation(null);
	renderInfo.setOnFirstCycle(true);
	for (int i = 0; i < renderInfo.getAppearanceStamps().length; i++) {
	    renderInfo.getAppearanceStamps()[i] = 0;
	}
    }

    /**
     * Updates the player's scene graph.
     * @param login If the player is logging in.
     */
    public void updateSceneGraph(boolean login) {
	Region region = getViewport().getRegion();
	if (region instanceof DynamicRegion || (region == null && (region = RegionManager.getRegionCache().get(location.getRegionId())) instanceof DynamicRegion || region == null)) {
	    PacketRepository.send(BuildDynamicScene.class, new DynamicSceneContext(this, login));
	} else {
	    PacketRepository.send(UpdateSceneGraph.class, new SceneGraphContext(this, login));
	}
    }

    /**
     * Toggles the debug mode.
     */
    public void toggleDebug() {
	boolean debug = getAttribute("debug", false);
	setAttribute("debug", !debug);
	getPacketDispatch().sendMessage("Your debug mode is toggled to " + !debug + ".");
    }

    /**
     * Checks if the player is allowed to be removed from the game.
     * @return {@code True} if so.
     */
    public boolean allowRemoval() {
	if (inCombat() || getSkills().getLifepoints() < 1 || DeathTask.isDead(this)) {
	    return false;
	}
	return true;
    }

    /**
     * Gets the name.
     * @param display the display.
     * @return the name.
     */
    public String getName(boolean display) {
	// return display ? details.getDisplayName() : super.getName();
	return super.getName();
    }

    /**
     * Gets the formatted name.
     * @param display the display.
     * @param formatted if formatted.
     * @return the name.
     */
    public String getName(boolean display, boolean formatted) {
	return StringUtils.formatDisplayName(getName(display));
    }

    /**
     * Wrapper method for sending a message.
     * @param messages the messages.
     */
    public void sendMessages(String... messages) {
	packetDispatch.sendMessages(messages);
    }

    /**
     * Wrapper method for sending a message.
     * @param message the message.
     */
    public void sendMessage(String message) {
	sendMessages(message);
    }

    @Override
    public String getUsername() {// display name is set to real name by default
	return StringUtils.formatDisplayName(super.getName());// details.getDisplayName());
    }

    /**
     * Checks if the player has a perk.
     * @param perk the perk.
     * @return {@code True} if so.
     */
    public boolean hasPerk(Perks perk) {
	return details.getShop().hasPerk(perk);
    }

    /**
     * Wrapper method for sending a message on a tick.
     * @param message the message.
     * @param ticks the ticks.
     */
    public void sendMessage(String message, int ticks) {
	packetDispatch.sendMessage(message, ticks);
    }

    /**
     * Sends a message to the player if it's an administrator.
     * @param string The message.
     */
    public void debug(String string) {
	if (isDebug()) {
	    packetDispatch.sendMessage(string);
	}
    }

    /**
     * Checks if the player is a staff member.
     * @return {@code True} if so.
     */
    public boolean isStaff() {
	return getDetails().getRights() != Rights.REGULAR_PLAYER;
    }

    /**
     * IF the player is an admin.
     * @return true if so.
     */
    public boolean isAdmin() {
	return getDetails().getRights() == Rights.ADMINISTRATOR;
    }

    /**
     * Checks if the player can spawn.
     * @return {@code True} if so.
     */
    public boolean canSpawn() {
	if (inCombat() || getLocks().isInteractionLocked() || getSkullManager().isWilderness() || getAttribute("activity", null) != null) {
	    sendMessage("<col=FF0000>You can't spawn items at the moment.");
	    return false;
	}
	return true;
    }

    /**
     * Checks if the player is debugging.
     * @return {@code True} if so.
     */
    public boolean isDebug() {
	return details.getRights() == Rights.ADMINISTRATOR && getAttribute("debug", false);
    }

    /**
     * Used to check if the player is a member.
     * @return <code>True</code> if so.
     */
    public boolean isDonator() {
	return getDetails().getPortal().isDonator();
    }

    /**
     * Gets the uid info.
     * @return the info.
     */
    public UIDInfo getUidInfo() {
	return details.getInfo();
    }

    /**
     * Gets the {@code PlayerDetails}.
     * @return the details.
     */
    public PlayerDetails getDetails() {
	return details;
    }

    /**
     * Gets the players {@code Session}.
     * @return the {@code PlayerDetails} {@code Object} session.
     */
    public IoSession getSession() {
	return details.getSession();
    }

    /**
     * Gets the {@code Equipment}
     * @return {@code Equipment}.
     */
    public EquipmentContainer getEquipment() {
	return equipment;
    }

    /**
     * Gets the bank.
     * @return The bank.
     */
    public BankContainer getBank() {
	return bank;
    }

    /**
     * @return the inventory
     */
    public Container getInventory() {
	return inventory;
    }

    /**
     * Sets the playing flag.
     * @param playing the flag to set.
     */
    public void setPlaying(boolean playing) {
	this.playing = playing;
    }

    /**
     * Checks if the player is playing.
     * @return {@code True} if so.
     */
    public boolean isPlaying() {
	return playing;
    }

    /**
     * Gets the rights of the player.
     * @return the rights.
     */
    public Rights getRights() {
	return details.getRights();
    }

    /**
     * Gets the renderInfo.
     * @return The renderInfo.
     */
    public RenderInfo getRenderInfo() {
	return renderInfo;
    }

    /**
     * Gets the appearance.
     * @return The appearance.
     */
    public Appearance getAppearance() {
	return appearance;
    }

    /**
     * Gets the playerFlags.
     * @return The playerFlags.
     */
    public PlayerFlags getPlayerFlags() {
	return playerFlags;
    }

    /**
     * Gets the {@code PacketDispatch}.
     * @return the packet dispatch.
     */
    public PacketDispatch getPacketDispatch() {
	return packetDispatch;
    }

    /**
     * Gets the {@code Emotes}.
     * @return the emotes.
     */
    public EmoteData getEmotes() {
	return emotes;
    }

    /**
     * @return the spellBookManager
     */
    public SpellBookManager getSpellBookManager() {
	return spellBookManager;
    }

    /**
     * Gets the settings.
     * @return The settings.
     */
    public Settings getSettings() {
	return settings;
    }

    /**
     * @return the interface manager.
     */
    public InterfaceManager getInterfaceManager() {
	return interfaceManager;
    }

    /**
     * Gets the dialogue interpreter.
     * @return The dialogue interpreter.
     */
    public DialogueInterpreter getDialogueInterpreter() {
	return dialogueInterpreter;
    }

    /**
     * @return the hintIconManager
     */
    public HintIconManager getHintIconManager() {
	return hintIconManager;
    }

    /**
     * Gets the slayer.
     * @return The slayer.
     */
    public SlayerManager getSlayer() {
	return slayer;
    }

    /**
     * Checks if the player is artifical (AIPlayer).
     * @return {@code True} if so.
     */
    public boolean isArtificial() {
	return artificial;
    }

    /**
     * @return the questRepository.
     */
    public QuestRepository getQuestRepository() {
	return questRepository;
    }

    /**
     * @return the prayer.
     */
    public Prayer getPrayer() {
	return prayer;
    }

    /**
     * @return the skullManager.
     */
    public SkullManager getSkullManager() {
	return skullManager;
    }

    /**
     * @return the grandExchange.
     */
    public GrandExchange getGrandExchange() {
	return grandExchange;
    }

    /**
     * @return the familiarManager.
     */
    public FamiliarManager getFamiliarManager() {
	return familiarManager;
    }

    /**
     * Gets the configManager.
     * @return The configManager.
     */
    public ConfigurationManager getConfigManager() {
	return configManager;
    }

    /**
     * Gets the communication.
     * @return The communication.
     */
    public CommunicationInfo getCommunication() {
	return details.getCommunication();
    }

    /**
     * Gets the requestManager.
     * @return The requestManager.
     */
    public RequestManager getRequestManager() {
	return requestManager;
    }

    /**
     * Gets the savedData.
     * @return The savedData.
     */
    public SavedData getSavedData() {
	return savedData;
    }
    
    /**
     * Gets the global data.
     * @return the global data.
     */
    public GlobalData getGlobalData() {
	return savedData.getGlobalData();
    }

    /**
     * Gets the farmingManager.
     * @return The farmingManager.
     */
    public FarmingManager getFarmingManager() {
	return farmingManager;
    }

    /**
     * Gets the monitor.
     * @return The monitor.
     */
    public PlayerMonitor getMonitor() {
	return monitor;
    }

    /**
     * Gets the warningMessages.
     * @return The warningMessages.
     */
    public WarningMessages getWarningMessages() {
	return warningMessages;
    }

    /**
     * Gets the musicPlayer.
     * @return The musicPlayer.
     */
    public MusicPlayer getMusicPlayer() {
	return musicPlayer;
    }

    /**
     * Gets the houseManager.
     * @return The houseManager.
     */
    public HouseManager getHouseManager() {
	return houseManager;
    }

    /**
     * Gets the barcrawlManager.
     * @return The barcrawlManager.
     */
    public BarcrawlManager getBarcrawlManager() {
	return barcrawlManager;
    }

    /**
     * Gets the antiMacroHandler.
     * @return The antiMacroHandler.
     */
    public AntiMacroHandler getAntiMacroHandler() {
	return antiMacroHandler;
    }

    /**
     * Gets the hunterManager.
     * @return The hunterManager.
     */
    public HunterManager getHunterManager() {
	return hunterManager;
    }

    /**
     * Gets the btreasureTrailManager.
     * @return the treasureTrailManager
     */
    public TreasureTrailManager getTreasureTrailManager() {
	return treasureTrailManager;
    }

    /**
     * Gets the graveManager.
     * @return the graveManager
     */
    public GraveManager getGraveManager() {
	return graveManager;
    }

    /**
     * Gets the audioManager.
     * @return the audioManager
     */
    public AudioManager getAudioManager() {
	return audioManager;
    }

    /**
     * Gets the logoutPlugins.
     * @return the logoutPlugins
     */
    public List<Plugin<Player>> getLogoutPlugins() {
	if (logoutPlugins == null) {
	    logoutPlugins = new ArrayList<>();
	}
	return logoutPlugins;
    }

    /**
     * Sets the balogoutPlugins.
     * @param logoutPlugins the logoutPlugins to set.
     */
    public void setLogoutPlugins(List<Plugin<Player>> logoutPlugins) {
	this.logoutPlugins = logoutPlugins;
    }

    /**
     * Gets the bankPinManager.
     * @return the bankPinManager
     */
    public BankPinManager getBankPinManager() {
	return bankPinManager;
    }

    /**
     * Gets the achievementDiaryManager.
     * @return the achievementDiaryManager
     */
    public AchievementDiaryManager getAchievementDiaryManager() {
	return achievementDiaryManager;
    }

    /**
     * Gets the invisible.
     * @return the invisible
     */
    public boolean isInvisible() {
	return invisible;
    }

    /**
     * Sets the invisible.
     * @param invisible the invisible to set.
     */
    public void setInvisible(boolean invisible) {
	this.invisible = invisible;
    }

    /**
     * Gets the donator type.
     * @return the type.
     */
    public DonatorType getDonatorType() {
	return details.getPortal().getDonatorType();
    }

    /**
     * Gets the ironmanManager.
     * @return the ironmanManager
     */
    public IronmanManager getIronmanManager() {
	return ironmanManager;
    }

    @Override
    public String toString() {
	return "Player [name=" + name + ", getRights()=" + getRights() + "]";
    }

}