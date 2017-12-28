package ru.kuzmin.ya;

import java.util.Calendar;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A {@code EventCounter} provides statistic about last registered events.
 *
 * @author Kuzmin Anton
 */
public class EventCounter {

	private final NavigableMap<Period, EventPeriodCounter> eventPeriodCounters;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

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
		eventPeriodCounters = new TreeMap<>(
				  (period1, period2) -> period1.getPeriod().compareTo(period2.getPeriod())
		);
		for (Period period : Period.values()) {
			eventPeriodCounters.put(period, new EventPeriodCounter(period));
		}
		initExpiredEventsTasks();
	}

	/**
	 * Initiates scheduled tasks which collect all expired events from an
	 * {@code EventPeriodCounter} with one time period and merge this events with
	 * statistic in {@code EventPeriodCounter} with longer time period.
	 */
	private void initExpiredEventsTasks() {
		Timer timer = new Timer(true);
		for (Period period : Period.values()) {
			Period higherPeriod = eventPeriodCounters.higherKey(period);
			EventPeriodCounter higherStat = higherPeriod != null
					  ? eventPeriodCounters.get(higherPeriod)
					  : null;
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					lock.writeLock().lock();
					try {
						EventPeriodCounter currentStat = eventPeriodCounters.get(period);
						Map<Long, AtomicLong> expiredStatistic = currentStat.removeExpiredStatistic();
						if (higherStat != null) {
							higherStat.mergeExternalStatistic(expiredStatistic);
						}
					} finally {
						lock.writeLock().unlock();
					}
				}
			}, period.getPeriod(), period.getPeriod());
		}
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
		lock.readLock().lock();
		try {
			eventPeriodCounters.firstEntry().getValue().register(eventTime);
		} finally {
			lock.readLock().unlock();
		}
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
		lock.readLock().lock();
		try {
			return eventPeriodCounters.headMap(period, true).values().stream()
					  .mapToLong((counter) -> counter.getActualStatistic(period.getPeriod()))
					  .sum();
		} finally {
			lock.readLock().unlock();
		}
	}
}
