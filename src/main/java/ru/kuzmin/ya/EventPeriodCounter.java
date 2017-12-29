package ru.kuzmin.ya;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@code EventPeriodCounter} registers events and stores statistic about
 * invocation for one of available {@code Period}.
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
		Map.Entry<Long, AtomicLong> entry = eventsStat.floorEntry(time);
		if (entry != null && time < entry.getKey() + interval) {
			entry.getValue().addAndGet(1);
		} else if (eventsStat.putIfAbsent(time, new AtomicLong(1)) != null) {
			eventsStat.get(time).addAndGet(1);
		}
	}

	void reset() {
		eventsStat.clear();
	}

	void removeExpiredStatistic() {
		long currentTime = Calendar.getInstance().getTimeInMillis();
		Map<Long, AtomicLong> expiredStat = eventsStat.headMap(currentTime - period);
		expiredStat.keySet().stream().forEach(eventsStat::remove);
	}

	long getActualStatistic() {
		long fromTime = Calendar.getInstance().getTimeInMillis() - period;
		return eventsStat.tailMap(fromTime).values().stream()
				  .mapToLong(AtomicLong::get)
				  .sum();
	}
}
