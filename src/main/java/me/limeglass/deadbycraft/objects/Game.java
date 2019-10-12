package me.limeglass.deadbycraft.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Lists;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.managers.CharacterManager;
import me.limeglass.deadbycraft.manager.managers.CharacterManager.GameCharacter;
import me.limeglass.deadbycraft.manager.managers.PlayerManager;

public class Game {

	private final Map<UUID, GameCharacter> selections = new HashMap<>();
	private final List<RoleRequest> requests = new ArrayList<>();
	private final Map<UUID, Long> experience = new HashMap<>();
	private final Set<Generator> generators = new HashSet<>();
	private final Set<GamePlayer> players = new HashSet<>();
	private final Set<Toolbox> toolboxes = new HashSet<>();
	private State state = State.LOBBY;
	private GamePlayer monster;
	private ArenaInfo info;

	/**
	 * Used for Bungeecord mode.
	 */
	public Game() {}

	public Game(ArenaInfo info) {
		this.info = info;
	}

	public void setMonster(GamePlayer mosnter) {
		this.monster = mosnter;
	}

	public Optional<GamePlayer> getMonster() {
		return Optional.ofNullable(monster);
	}

	@Nullable
	public ArenaInfo getArenaInfo() {
		return info;
	}

	public void addToolbox(Toolbox toolbox) {
		toolboxes.add(toolbox);
	}

	/**
	 * SHOULD ONLY BE USED INTERNALLY
	 */
	public boolean addPlayer(GamePlayer player) {
		if (players.size() >= info.getMaximumPlayers())
			return false;
		player.setCurrentGame(this);
		player.setJoinLocation(player.getPlayer().get().getLocation());
		players.add(player);
		return true;
	}

	public void addExperience(GamePlayer player, long value) {
		UUID uuid = player.getUniqueId();
		long existing = Optional.ofNullable(experience.get(uuid)).orElse(0L);
		experience.put(uuid, existing + value);
	}

	public long getExperience(GamePlayer player) {
		return  Optional.ofNullable(experience.get(player.getUniqueId())).orElse(0L);
	}

	public boolean addGenerator(Generator generator) {
		return generators.add(generator);
	}

	public Set<Generator> getGenerators() {
		return generators;
	}

	public boolean isSelected(GameCharacter character) {
		return selections.values().contains(character);
	}

	public void setSelection(GamePlayer player, GameCharacter character) {
		selections.put(player.getUniqueId(), character);
	}

	public Map<UUID, GameCharacter> getSelections() {
		return selections;
	}

	public Optional<GameCharacter> getSelection(GamePlayer player) {
		return selections.entrySet().stream()
				.filter(entry -> entry.getKey().equals(player.getUniqueId()))
				.map(entry -> entry.getValue())
				.filter(element -> element != null)
				.findFirst();
	}

	public Optional<RoleRequest> getRoleRequest(GamePlayer gamePlayer) {
		return requests.stream()
				.filter(request -> request.getUniqueId().equals(gamePlayer.getUniqueId()))
				.findFirst();
	}

	public Role getRole(GamePlayer gamePlayer) throws IllegalAccessException {
		if (isLobby())
			throw new IllegalAccessException("Cannot grab a players role during lobby");
		return getMonster().isPresent() && getMonster().get().equals(gamePlayer) ? Role.MONSTER : Role.SURVIVOR;
	}

	public Optional<GameCharacter> getSelectionOrAssign(GamePlayer gamePlayer) {
		return Optional.ofNullable(getSelection(gamePlayer).orElseGet(() -> {
			Role role;
			try {
				role = getRole(gamePlayer);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
			Optional<GameCharacter> next = DeadByCraft.getInstance().getManager(CharacterManager.class).getCharactersByRole(role).stream()
					.filter(character -> !selections.containsValue(character))
					.findFirst();
			if (next.isPresent())
				return next.get();
			return null;
		}));
	}

	public boolean hasSelection(GamePlayer player) {
		return getSelection(player).isPresent();
	}

	public boolean hasSelected(GamePlayer player, GameCharacter character) {
		Optional<GameCharacter> selection = getSelection(player);
		if (!selection.isPresent())
			return false;
		return selection.get().equals(character);
	}

	public boolean generatorsAreComplete() {
		return generators.stream().allMatch(generator -> generator.isRunning());
	}

	/**
	 * SHOULD ONLY BE USED INTERNALLY
	 */
	public void removePlayer(GamePlayer player) {
		player.setCurrentGame(null);
		player.setJoinLocation(null);
		if (monster != null && monster.equals(player))
			monster = null;
		players.remove(player);
	}

	public void setArenaInfo(ArenaInfo info) {
		this.info = info;
	}

	public Set<GamePlayer> getSurvivors() {
		if (monster == null)
			return players;
		return players.stream()
				.filter(player -> !player.equals(monster))
				.collect(Collectors.toSet());
	}

	public Set<Player> getBukkitSurvivors() {
		return getSurvivors().stream()
				.map(player -> player.getPlayer())
				.filter(optional -> optional.isPresent())
				.map(optional -> optional.get())
				.collect(Collectors.toSet());
	}

	public boolean hasRequested(GamePlayer player, Role role) {
		UUID uuid = player.getUniqueId();
		return requests.stream().anyMatch(request -> request.getRequestedRole() == role && request.getUniqueId().equals(uuid));
	}

	public void determineMonster() {
		if (monster != null)
			return;
		RoleRequest request = getFirstMonsterRequest().orElseGet(() -> {
				List<GamePlayer> victims = Lists.newArrayList(players);
				Collections.shuffle(victims);
				GamePlayer victim = victims.get(0);
				return new RoleRequest(victim.getUniqueId(), Role.MONSTER);
		});
		Optional<GamePlayer> player = DeadByCraft.getInstance().getManager(PlayerManager.class).getGamePlayer(request.getUniqueId());
		if (!player.isPresent())
			return;
		Optional<GameCharacter> selection = getSelection(player.get());
		if (selection.isPresent() && selection.get().getRole() != Role.MONSTER)
			setSelection(player.get(), null);
		monster = player.get();
	}

	/**
	 * Adds a player role request.
	 * 
	 * @param player The player requesting the role.
	 * @param role The role the player is requesting.
	 * @return Their spot in the queue.
	 */
	public int requestRole(GamePlayer player, Role role) {
		UUID uuid = player.getUniqueId();
		requests.removeIf(request -> request.getUniqueId().equals(uuid));
		requests.add(new RoleRequest(uuid, role));
		return requests.size();
	}

	public Set<Player> getBukkitPlayers() {
		return players.stream()
				.map(player -> player.getPlayer())
				.filter(optional -> optional.isPresent())
				.map(optional -> optional.get())
				.collect(Collectors.toSet());
	}

	public List<RoleRequest> getRequests() {
		return requests;
	}

	public Optional<RoleRequest> getFirstMonsterRequest() {
		return requests.stream()
				.filter(request -> request.getRequestedRole() == Role.MONSTER)
				.findFirst();
	}

	public Set<GamePlayer> getPlayers() {
		return players;
	}

	@Nullable
	public String getName() {
		return info.getName();
	}

	public boolean isLobby() {
		return state == State.LOBBY;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public enum State {
		LOBBY, GAME, ENDING;
	}

	public enum Role {
		MONSTER, SURVIVOR;
	}

	public class RoleRequest {

		private final UUID uuid;
		private final Role role;

		public RoleRequest(UUID uuid, Role role) {
			this.uuid = uuid;
			this.role = role;
		}

		public UUID getUniqueId() {
			return uuid;
		}

		public Role getRequestedRole() {
			return role;
		}

	}

}
