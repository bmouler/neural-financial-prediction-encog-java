//asdlfkjasldkfj

package Tracker;

import java.util.HashMap;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

public class Tracker {
	HashMap<String, DateTime> startTimes = new HashMap<String, DateTime>();
	HashMap<String, DateTime> stopTimes = new HashMap<String, DateTime>();

	public void start(String key) throws Exception {
		DateTime dt = DateTime.now();
		if (startTimes.containsKey(key)) {
			throw new Exception("Repeat label for time tracker start");
		}
		startTimes.put(key, dt);
	}

	public void stop(String key) throws Exception {
		DateTime dt = DateTime.now();
		if (stopTimes.containsKey(key)) {
			throw new Exception("Repeat label for time tracker stop");
		}
		stopTimes.put(key, dt);
	}

	// no stop key is ever set
	public String stopAndClearNOW(String key) throws Exception {
		DateTime stop = DateTime.now();
		DateTime start = startTimes.get(key);
		String out = getShortPrint(start, stop);
		clearLabel(key);
		return out;
	}

	public String getDuration(String key, boolean remove) {
		DateTime start = startTimes.get(key);
		DateTime stop = stopTimes.get(key);

		String out = getShortPrint(start, stop);
		if (remove) {
			clearLabel(key);
		}
		return out;
	}

	public DateTime getStartDateTime(String key) {
		return startTimes.get(key);
	}

	public String getStart(String key) {
		return startTimes.get(key).toString();
	}

	public DateTime getStopDateTime(String key) {
		return stopTimes.get(key);
	}

	public String getStop(String key) {
		return stopTimes.get(key).toString();
	}

	public void clearLabel(String key) {
		startTimes.remove(key);
		stopTimes.remove(key);
	}

	//TODO tracker time output is not working, floor is wrong
	private String getShortPrint(DateTime start, DateTime stop) {
		int hours = Hours.hoursBetween(start, stop).getHours();
		int minutes = Minutes.minutesBetween(start, stop).getMinutes();
		minutes = (int) Math.floor( ( ((double) minutes) / 60.0 ) );
		int seconds = Seconds.secondsBetween(start, stop).getSeconds();
		seconds = (int) Math.floor( ( ((double) seconds) / 60.0 ) );	
		

		String out = hours + ":" + minutes + ":" + seconds;
		return out;
	}
	
	// TODO add long print from MiniThreadManager
}
