package ru.kuzmin.ya;

import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@code EventPeriodCounter} registers events and stores statistic
 * about invocation for one of available {@code Period}.
 * 
 * @author Kuzmin Anton
 */
class EventPeriodCounter {

	private final Long period;
	private final Long interval;
	private final ConcurrentNavigableMap<Long, AtomicLong> eventsStat;

	EventPeriodCounter(Period period) {
		this.period = period.getPeriod();
		this.interval = period.getDelta();
		this.eventsStat = new ConcurrentSkipListMap<>();
	}

	void register(Long time) {
		addStatistic(time, 1l);
	}

	void reset() {
		eventsStat.clear();
	}

	void mergeExternalStatistic(Map<Long, AtomicLong> statistic) {
		statistic.entrySet().stream()
				  .forEach((statEntry) -> addStatistic(statEntry.getKey(), statEntry.getValue().longValue()));
	}

	Map<Long, AtomicLong> removeExpiredStatistic() {
		long currentTime = Calendar.getInstance().getTimeInMillis();
		Map<Long, AtomicLong> expiredStat = new TreeMap(eventsStat.headMap(currentTime - period));
		expiredStat.keySet().stream().forEach(eventsStat::remove);
		return expiredStat;
	}

	long getActualStatistic(Long timePeriod) {
		long fromTime = Calendar.getInstance().getTimeInMillis() - timePeriod;
		return eventsStat.tailMap(fromTime).values().stream()
				  .mapToLong(AtomicLong::get)
				  .sum();
	}

	private void addStatistic(Long time, Long count) {
		Map.Entry<Long, AtomicLong> entry = eventsStat.floorEntry(time);
		if (entry != null && time < entry.getKey() + interval) {
			entry.getValue().addAndGet(count);
		} else if (eventsStat.putIfAbsent(time, new AtomicLong(count)) != null) {
			eventsStat.get(time).addAndGet(count);
		}
	}
}
