package me.limeglass.deadbycraft.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntervalUtils {

	/**
	 * The interval converter, converts to ticks.
	 * <p>
	 * Use {@link #getSeconds(long)} for grabbing String seconds from ticks.
	 * 
	 * @param interval The String input to convert.
	 * @return The ticks from the String interval.
	 */
	public static long getInterval(String interval) {
		Pattern regex = Pattern.compile("( ?and ?)?([0-9]+) ?(tick(s|)|t|second(s|)|s|minute(s|)|m|hour(s|)|h|day(s|)|d|week(s|)|w)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
		Matcher matcher = regex.matcher(interval);
		long time = 0;
		while (matcher.find()) {
			interval = matcher.group(2);
			switch (matcher.group(3).toLowerCase().charAt(0)) {
				case 't':
					time = time + Long.parseLong(interval);
					break;
				case 's':
					time = time + Long.parseLong(interval) * 20;
					break;
				case 'm':
					time = time + Long.parseLong(interval) * 60;
					break;
				case 'h':
					time = time + Long.parseLong(interval) * 60 * 60;
					break;
				case 'd':
					time = time + Long.parseLong(interval) * 60 * 60 * 24;
					break;
				case 'w':
					time = time + Long.parseLong(interval) * 60 * 60 * 24*  7;
					break;
				default:
					break;
			}
		}
		if (time == 0)
			return 1;
		return time;
	}

	/**
	 * Grab the milliseconds from the String input.
	 * 
	 * @param interval The formatted English string input.
	 * @return The time in milliseconds from the String input.
	 */
	public static long getMilliseconds(String interval) {
		long ticks = getInterval(interval);
		return (ticks / 20) * 1000;
	}

	public static String getSeconds(long ticks) {
		double seconds = ticks / 20;
		if (seconds == 1)
			return "a second";
		return seconds + " seconds";
	}

}
