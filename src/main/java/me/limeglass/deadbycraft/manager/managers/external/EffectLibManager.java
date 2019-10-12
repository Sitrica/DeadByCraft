package me.limeglass.deadbycraft.manager.managers.external;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.LineEffect;
import de.slikey.effectlib.util.DynamicLocation;
import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.ExternalManager;

public class EffectLibManager extends ExternalManager {

	private final EffectManager effectManager;

	public EffectLibManager() {
		super("effectlib", false);
		effectManager = new EffectManager(DeadByCraft.getInstance());
	}

	public class LineBuilder {
		
		private final Location start, end;
		private final LineEffect effect;
		
		public LineBuilder(Location start, Location end) {
			this.start = start;
			this.end = end;
			effect = new LineEffect(effectManager);
			effect.setDynamicOrigin(new DynamicLocation(start));
			effect.setDynamicTarget(new DynamicLocation(end));
			effect.asynchronous = true;
		}
		
		public LineBuilder withColor(Color color) {
			effect.color = color;
			return this;
		}
		
		/**
		 * How many times in seconds should the effect last.
		 */
		public LineBuilder iterations(int iterations) {
			effect.iterations = iterations;
			return this;
		}
		
		public LineBuilder withCallback(Runnable runnable) {
			effect.callback = runnable;
			return this;
		}
		
		public LineBuilder withMaterial(Material material) {
			effect.material = material;
			return this;
		}
		
		public LineBuilder withParticle(Particle particle) {
			effect.particle = particle;
			return this;
		}
		
		public LineEffect send(int radius) {
			Set<Entity> entities = Sets.newHashSet(start.getWorld().getNearbyEntities(start, radius, radius, radius));
			entities.addAll(end.getWorld().getNearbyEntities(end, radius, radius, radius));
			effect.targetPlayers = entities.parallelStream()
					.filter(entity -> entity instanceof Player)
					.map(entity -> (Player) entity)
					.collect(Collectors.toList());
			effect.start();
			return effect;
		}
		
		public LineEffect send(List<Player> players) {
			effect.targetPlayers = players;
			effect.start();
			return effect;
		}
		
		public LineEffect send(Player player) {
			effect.targetPlayer = player;
			effect.start();
			return effect;
		}
		
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public EffectManager getEffectManager() {
		return effectManager;
	}

}
