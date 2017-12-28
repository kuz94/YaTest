package ru.kuzmin.ya;

/**
 * A {@code Period} represents all available
 * periods of time for statistic providing
 * 
 * @author Kuzmin Anton
 */
public enum Period {
	MINUTE(60 * 1000l),
	HOUR(60 * 60 * 1000l),
	DAY(24 * 60 * 60 * 1000l);

	private final Long period;
	private final Long delta;

	Period(Long period) {
		this.period = period;
		this.delta = period / 100;
	}

	Long getPeriod() {
		return period;
	}

	Long getDelta() {
		return delta;
	}
}
