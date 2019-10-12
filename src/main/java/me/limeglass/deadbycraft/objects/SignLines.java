package me.limeglass.deadbycraft.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.block.Sign;

public class SignLines {

	private final List<String> lines = new ArrayList<>();

	public SignLines(List<String> lines) {
		this.lines.addAll(lines);
	}

	public String[] getLines() {
		return lines.toArray(new String[lines.size()]);
	}

	public void setFirst(String line) {
		lines.set(0, line);
	}

	public void setSecond(String line) {
		lines.set(1, line);
	}

	public void setThird(String line) {
		lines.set(2, line);
	}

	public void setFourth(String line) {
		lines.set(3, line);
	}

	public String getFirst() {
		return Optional.ofNullable(lines.get(0)).orElse("");
	}

	public String getSecond() {
		return Optional.ofNullable(lines.get(1)).orElse("");
	}

	public String getThird() {
		return Optional.ofNullable(lines.get(2)).orElse("");
	}

	public String getFourth() {
		return Optional.ofNullable(lines.get(3)).orElse("");
	}

	public void apply(Sign sign) {
		sign.setLine(0, getFirst());
		sign.setLine(1, getSecond());
		sign.setLine(2, getThird());
		sign.setLine(3, getFourth());
		sign.update(true);
	}

}
