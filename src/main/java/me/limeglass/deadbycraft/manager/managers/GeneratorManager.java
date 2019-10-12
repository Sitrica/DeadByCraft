package me.limeglass.deadbycraft.manager.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import me.limeglass.deadbycraft.DeadByCraft;
import me.limeglass.deadbycraft.manager.Manager;
import me.limeglass.deadbycraft.objects.Generator;
import me.limeglass.deadbycraft.utils.MessageBuilder;

public class GeneratorManager extends Manager {

	private final FileConfiguration configuration;
	private final int progressBars;

	public GeneratorManager() {
		super(true);
		this.configuration = DeadByCraft.getInstance().getConfig();
		this.progressBars = configuration.getInt("game.generators.progress-bar-amount", 100);
	}

	public String getProgressBar(Generator generator) {
		String uncomplete = new MessageBuilder(false, "game.generators.uncomplete-colour")
				.fromConfiguration(configuration)
				.setPlaceholderObject(generator)
				.get();
		String symbol = new MessageBuilder(false, "game.generators.symbol")
				.fromConfiguration(configuration)
				.setPlaceholderObject(generator)
				.get();
		float percent = (float) generator.getProgress() / generator.getCompleteTime();

		int javaFinalBS = progressBars;
		int progressBars = (int) (javaFinalBS * percent);

		int leftOver = (progressBars - progressBars);

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < progressBars; i++) {
			builder.append(symbol);
		}
		builder.append(ChatColor.translateAlternateColorCodes('&', uncomplete));
		for (int i = 0; i < leftOver; i++) {
			builder.append(symbol);
		}
		return builder.toString();
	}

	public double getProgress(Generator generator) {
		long percent = generator.getProgress() * 100 / generator.getCompleteTime();
		return Math.round(percent * 10.0) / 10.0;
	}

}
