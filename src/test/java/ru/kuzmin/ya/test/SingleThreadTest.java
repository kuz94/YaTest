/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kuzmin.ya.test;

import java.util.Calendar;
import org.junit.Test;
import ru.kuzmin.ya.EventCounter;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import ru.kuzmin.ya.Period;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Kuzmin Anton
 */
public class SingleThreadTest {

	private final long numberEvents = 1000;
	private final static EventCounter COUNTER = EventCounter.getCounter();

	@Before
	public void beforeTest() {
		COUNTER.reset();
		assertEquals(0, COUNTER.countEvents(Period.MINUTE));
		assertEquals(0, COUNTER.countEvents(Period.HOUR));
		assertEquals(0, COUNTER.countEvents(Period.DAY));
	}

	@Test
	public void basicTest() {
		for (long i = 0; i < numberEvents; i++) {
			COUNTER.register();
		}
		assertEquals(numberEvents, COUNTER.countEvents(Period.MINUTE));
		assertEquals(numberEvents, COUNTER.countEvents(Period.HOUR));
		assertEquals(numberEvents, COUNTER.countEvents(Period.DAY));
	}

	@Test
	public void timeShiftTest() {
		Long timeShift = 120 * 1000l;
		Calendar calendar = Calendar.getInstance();
		for (int i = 0; i < numberEvents; i++) {
			Long currentTime = calendar.getTimeInMillis();
			COUNTER.register(currentTime);
			COUNTER.register(currentTime - timeShift);
		}
		assertEquals(numberEvents, COUNTER.countEvents(Period.MINUTE));
		assertEquals(2 * numberEvents, COUNTER.countEvents(Period.HOUR));
		assertEquals(2 * numberEvents, COUNTER.countEvents(Period.DAY));
	}

	@Test(expected = IllegalArgumentException.class)
	public void illegalArgumentTest() {
		COUNTER.register(-1l);

	}
	
	@Test(expected = NullPointerException.class)
	public void nullPointerTest() {
		COUNTER.register(null);
	}
}
