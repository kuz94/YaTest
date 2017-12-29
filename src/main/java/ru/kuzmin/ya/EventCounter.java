package ru.kuzmin.ya;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A {@code EventCounter} provides statistic about last registered events.
 *
 * @author Kuzmin Anton
 */
public class EventCounter {

	private final Map<Period, EventPeriodCounter> eventPeriodCounters = new EnumMap<>(Period.class);

	private static class EventConterHolder {

		public static final EventCounter INSTANCE = new EventCounter();
	}

	/**
	 * Gets existence or creates the {@code EventCounter} signleton
	 *
	 * @return {@code EventCounter} instance
	 */
	public static EventCounter getCounter() {
		return EventCounter.EventConterHolder.INSTANCE;
	}

	/**
	 * Creates {@code EventPeriodCounter} instances which are corresponding to
	 * all available time {@code Period}.
	 */
	private EventCounter() {
		for (Period period : Period.values()) {
			eventPeriodCounters.put(period, new EventPeriodCounter(period));
		}
		initExpiredEventsTasks();
	}

	/**
	 * Initiates scheduled tasks which remove all expired events from each
	 * {@code EventPeriodCounter} instance.
	 */
	private void initExpiredEventsTasks() {
		Timer timer = new Timer(true);
		eventPeriodCounters.entrySet().stream().forEach((entry) -> {
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					entry.getValue().removeExpiredStatistic();
				}
			}, entry.getKey().getPeriod(), entry.getKey().getPeriod());
		});
	}

	/**
	 * Registers event
	 */
	public void register() {
		register(Calendar.getInstance().getTimeInMillis());
	}

	/**
	 * Registers event which happened in specific time
	 *
	 * @param eventTime - specific time as UTC milliseconds from the epoch
	 *
	 * @throws NullPointerException if eventTime object is null
	 * @throws IllegalArgumentException if eventTime less than 0
	 */
	public void register(Long eventTime) {
		if (eventTime == null) {
			throw new NullPointerException("Argument mustn't be null");
		}
		if (eventTime < 0) {
			throw new IllegalArgumentException("Argument must be greater than 0");
		}
		eventPeriodCounters.values().stream().forEach((counter) -> counter.register(eventTime));
	}

	/**
	 * Resets all statistic about registered events
	 *
	 */
	public void reset() {
		eventPeriodCounters.values().forEach(EventPeriodCounter::reset);
	}

	/**
	 * Counts events number for one of the available time {@code Period}
	 *
	 * @param period
	 *
	 * @return number registered events for specified period
	 *
	 * @throws NullPointerException if period is null
	 */
	public long countEvents(Period period) {
		if (period == null) {
			throw new NullPointerException("Period is null");
		}
		return eventPeriodCounters.get(period).getActualStatistic();
	}
}
