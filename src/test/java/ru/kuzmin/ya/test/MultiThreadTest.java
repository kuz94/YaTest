package ru.kuzmin.ya.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import ru.kuzmin.ya.EventCounter;
import ru.kuzmin.ya.Period;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Kuzmin Anton
 */
public class MultiThreadTest {

	private final static EventCounter COUNTER = EventCounter.getCounter();
	private final int threadNumber = 100;
	private final long eventsNumber = 1000;

	@Before
	public void beforeTest() {
		// Reset all counter's values
		COUNTER.reset();
		assertEquals(0, COUNTER.countEvents(Period.MINUTE));
		assertEquals(0, COUNTER.countEvents(Period.HOUR));
		assertEquals(0, COUNTER.countEvents(Period.DAY));
	}

	@Test
	public void commonTest() {
		// Setting time shift values in milliseconds
		long minuteTimeShift = 2 * 60 * 1000l;
		long hourTimeShift = 2 * 60 * 60 * 1000l;
		long dayTimeShift = 48 * 60 * 60 * 1000l;
		Calendar calendar = Calendar.getInstance();
		// Creating runnables objects
		Runnable currentThread = () -> {
			EventCounter threadCounter = EventCounter.getCounter();
			for (int e = 0; e < eventsNumber; e++) {
				threadCounter.register(calendar.getTimeInMillis());
			}
		};
		Runnable minuteThread = () -> {
			EventCounter threadCounter = EventCounter.getCounter();
			for (int e = 0; e < eventsNumber; e++) {
				long randomNoize = (long)(Math.random() * 60 * 1000);
				threadCounter.register(calendar.getTimeInMillis() - minuteTimeShift - randomNoize);
			}
		};
		Runnable hourThread = () -> {
			for (int e = 0; e < eventsNumber; e++) {
				long randomNoize = (long)(Math.random() * 60 * 60 * 1000);
				EventCounter.getCounter().register(calendar.getTimeInMillis() - hourTimeShift - randomNoize);
			}
		};
		Runnable dayThread = () -> {
			for (int e = 0; e < eventsNumber; e++) {
				long randomNoize = (long)(Math.random() * 24 *60 * 60 * 1000);
				EventCounter.getCounter().register(calendar.getTimeInMillis() - dayTimeShift - randomNoize);
			}
		};
		Runnable readerThread = () -> {
			for (int e = 0; e < eventsNumber; e++) {
				EventCounter.getCounter().countEvents(Period.MINUTE);
			}
		};
		// Creating threads
		List<Thread> threads = new ArrayList<>();
		for (int t = 0; t < threadNumber; t++) {
			threads.add(new Thread(currentThread));
			threads.add(new Thread(minuteThread));
			threads.add(new Thread(hourThread));
			threads.add(new Thread(dayThread));
			threads.add(new Thread(readerThread));
		}
		// Stating threads
		threads.parallelStream().forEach(Thread::start);
		// Waiting for finish all threads
		threads.stream().forEach((thread) -> {
			try {
				thread.join();
			} catch (InterruptedException ignore) {
			}
		});
		// Check results
		assertEquals(threadNumber * eventsNumber, COUNTER.countEvents(Period.MINUTE));
		assertEquals(2 * threadNumber * eventsNumber, COUNTER.countEvents(Period.HOUR));
		assertEquals(3 * threadNumber * eventsNumber, COUNTER.countEvents(Period.DAY));
	}
}
