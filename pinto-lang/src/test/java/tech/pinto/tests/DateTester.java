package tech.pinto.tests;

import java.time.LocalDate;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import tech.pinto.time.BusinessDay;
import tech.pinto.time.BusinessMonth;
import tech.pinto.time.BusinessQuarter;
import tech.pinto.time.WeekEndingFriday;
import tech.pinto.time.WeekEndingMonday;
import tech.pinto.time.WeekEndingTuesday;
import tech.pinto.time.Periodicities;
import tech.pinto.time.Periodicity;

import static java.time.Month.*;
import static org.junit.Assert.*;

public class DateTester {

	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	private LocalDate d(String s) {
		return LocalDate.parse(s);
	}

	/**
	 *   
	 *   S	M	T	W	Th  F	S  
	 *   
	 *   		May 2014
	 *   				1	2	3
	 *	4	5	6	7	8	9	10
	 *	11	12	13	14	15	16	17
	 *  18	19	20	21	22	23	24
	 *  25	26	27	28	29	30	31
	 *
	 *			June 2014
	 *	1	2	3	4	5	6	7
	 *	8	9	10	11	12	13	14
	 * 15	16	17	18	19	20	21
	 * 22	23	24	25	26	27	28
	 * 29	30
	 *   
	 *			July 2014
	 *			1	2	3	4	5
	 *	6	7	8	9	10	11	12
	 * 13	14	15	16	17	18	19
	 * 20	21	22	23	24	25	26
	 * 27	28	29	30	31	
	 *   */
	@Test
	public void testBusinessQuarterly() {
		log.info("START testBusinessQuarterly");

		Periodicity<BusinessQuarter> p = Periodicities.get("BQ-DEC");
		assertEquals("BusinessQuarter from/to localdate (1)",
				LocalDate.of(2014, SEPTEMBER, 30), p.from(LocalDate.of(2014, JULY, 14)).endDate());
		assertEquals("BusinessQuarter from/to localdate (2)",
				LocalDate.of(2014, SEPTEMBER, 30), p.from(LocalDate.of(2014, JULY, 4)).endDate());
		assertEquals("BusinessQuarter from/to localdate (saturday)",
				LocalDate.of(2014, JUNE, 30), p.from(LocalDate.of(2014, MAY, 4)).endDate());
		assertEquals("BusinessQuarter next (1)",
				LocalDate.of(2014, SEPTEMBER, 30), p.from(LocalDate.of(2014, MAY, 30)).next().endDate());
		assertEquals("BusinessQuarter previous (1)",
				LocalDate.of(2014, MARCH, 31), p.from(LocalDate.of(2014, JUNE, 3)).previous().endDate());

		assertEquals("BusinessQuarter distance (1)",  0l,
				p.distance(p.from(LocalDate.of(2014, MAY, 30)),
						p.from(LocalDate.of(2014, MAY, 30))));

		assertEquals("BusinessQuarter distance (2)",  1l,
				p.distance(p.from(LocalDate.of(2014, MAY, 4)),
						p.from(LocalDate.of(2014, JULY, 11))));

		assertEquals("BusinessQuarter distance (3)",  2l,
				p.distance(p.from(LocalDate.of(2014, MAY, 2)),
						p.from(LocalDate.of(2014, OCTOBER, 4))));

		assertEquals("BusinessQuarter distance (4)",  -2l,
				p.distance(p.from(LocalDate.of(2014, OCTOBER, 4)),
						p.from(LocalDate.of(2014, MAY, 2))));

		assertEquals("BusinessQuarter range size (1)",  2l,
				p.range(p.from(LocalDate.of(2014, MAY, 2)),
						p.from(LocalDate.of(2014, JULY, 4)), false).size());

		assertEquals("BusinessQuarter range size (2)",  1l,
				p.range(p.from(LocalDate.of(2014, JULY, 4)),
						p.from(LocalDate.of(2014, JULY, 14)), false).size());

		log.info("END testBusinessQuarter");
	}
	@Test
	public void testBusinessMonthly() {
		log.info("START testBusinessMonthly");

		Periodicity<BusinessMonth> p = Periodicities.get("BM");
		assertEquals("BusinessMonthly from/to localdate (1)",
				LocalDate.of(2014, JULY, 31), p.from(LocalDate.of(2014, JULY, 31)).endDate());
		assertEquals("BusinessMonthly from/to localdate (2)",
				LocalDate.of(2014, JULY, 31), p.from(LocalDate.of(2014, JULY, 4)).endDate());
		assertEquals("BusinessMonthly from/to localdate (saturday)",
				LocalDate.of(2014, MAY, 30), p.from(LocalDate.of(2014, MAY, 4)).endDate());
		assertEquals("BusinessMonthly next (1)",
				LocalDate.of(2014, JUNE, 30), p.from(LocalDate.of(2014, MAY, 30)).next().endDate());
		assertEquals("BusinessMonthly previous (1)",
				LocalDate.of(2014, MAY, 30), p.from(LocalDate.of(2014, JUNE, 30)).previous().endDate());

		assertEquals("BusinessMonthly distance (1)",  0l,
				p.distance(p.from(LocalDate.of(2014, MAY, 30)),
						p.from(LocalDate.of(2014, MAY, 30))));

		assertEquals("BusinessMonthly distance (2)",  1l,
				p.distance(p.from(LocalDate.of(2014, MAY, 4)),
						p.from(LocalDate.of(2014, JUNE, 11))));

		assertEquals("BusinessMonthly distance (3)",  2l,
				p.distance(p.from(LocalDate.of(2014, MAY, 2)),
						p.from(LocalDate.of(2014, JULY, 4))));

		assertEquals("BusinessMonthly distance (4)",  -2l,
				p.distance(p.from(LocalDate.of(2014, JULY, 4)),
						p.from(LocalDate.of(2014, MAY, 2))));

		assertEquals("BusinessMonthly range size (1)",  2l,
				p.range(p.from(LocalDate.of(2014, MAY, 2)),
						p.from(LocalDate.of(2014, JUNE, 4)), false).size());

		assertEquals("BusinessMonthly range size (2)",  1l,
				p.range(p.from(LocalDate.of(2014, JULY, 4)),
						p.from(LocalDate.of(2014, JULY, 14)), false).size());

		assertEquals("BusinessMonthly range values size (1)",  3l,
				p.range(p.from(LocalDate.of(2014, MAY, 2)),
						p.from(LocalDate.of(2014, JULY, 4)), false).values().size());
		
		log.info("END testBusinessMonthly");
	}

	@Test
	public void testMondayWeekly() {
		log.info("START testMondayWeekly");

		Periodicity<WeekEndingMonday> p = Periodicities.get("W-MON");
		assertEquals("MondayWeekly from/to localdate (1)",
				d("2014-07-07"), p.from(d("2014-07-07")).endDate());
		assertEquals("MondayWeekly from/to localdate (2)",
				d("2014-07-14"), p.from(d("2014-07-08")).endDate());
		assertEquals("MondayWeekly from/to localdate (3)",
				d("2014-07-07"), p.from(d("2014-07-06")).endDate());
		assertEquals("MondayWeekly next",
				d("2014-07-14"), p.from(d("2014-07-07")).next().endDate());
	}
	
	@Test
	public void testTuesdayWeekly() {
		log.info("START testMondayWeekly");

		Periodicity<WeekEndingTuesday> p = Periodicities.get("W-TUE");
		assertEquals("MondayWeekly from/to localdate (1)",
				d("2014-07-08"), p.from(d("2014-07-07")).endDate());
		assertEquals("MondayWeekly from/to localdate (2)",
				d("2014-07-15"), p.from(d("2014-07-09")).endDate());
		assertEquals("MondayWeekly from/to localdate (3)",
				d("2014-07-08"), p.from(d("2014-07-06")).endDate());
		assertEquals("MondayWeekly next",
				d("2014-07-15"), p.from(d("2014-07-07")).next().endDate());
	}
	
	@Test
	public void testFridayWeekly() {
		log.info("START testWeeklyFriday");

		Periodicity<WeekEndingFriday> p = Periodicities.get("W-FRI");
		assertEquals("FridayWeekly from/to localdate (1)",
				LocalDate.of(2014, JULY, 4), p.from(LocalDate.of(2014, JULY, 4)).endDate());
		assertEquals("FridayWeekly from/to localdate (2)",
				LocalDate.of(2014, JUNE, 6), p.from(LocalDate.of(2014, JUNE, 3)).endDate());
		assertEquals("FridayWeekly from/to localdate (saturday)",
				LocalDate.of(2014, JULY, 4), p.from(LocalDate.of(2014, JUNE, 30)).endDate());
		assertEquals("FridayWeekly from/to localdate (sunday)",
				LocalDate.of(2014, JULY, 4), p.from(LocalDate.of(2014, JUNE, 29)).endDate());
		assertEquals("FridayWeekly next (1)",
				LocalDate.of(2014, JULY, 11), p.from(LocalDate.of(2014, JULY, 4)).next().endDate());
		assertEquals("FridayWeekly previous (1)",
				LocalDate.of(2014, JULY, 4), p.from(LocalDate.of(2014, JULY, 11)).previous().endDate());

		assertEquals("FridayWeekly distance (1)",  0l,
				p.distance(p.from(LocalDate.of(2014, JULY, 4)),
						p.from(LocalDate.of(2014, JULY, 4))));

		assertEquals("FridayWeekly distance (2)",  1l,
				p.distance(p.from(LocalDate.of(2014, JULY, 4)),
						p.from(LocalDate.of(2014, JULY, 11))));

		assertEquals("FridayWeekly distance (3)",  4l,
				p.distance(p.from(LocalDate.of(2014, JUNE, 2)),
						p.from(LocalDate.of(2014, JULY, 4))));

		assertEquals("FridayWeekly distance (4)",  -4l,
				p.distance(p.from(LocalDate.of(2014, JULY, 4)),
						p.from(LocalDate.of(2014, JUNE, 2))));

		assertEquals("FridayWeekly range size (1)",  10l,
				p.range(p.from(LocalDate.of(2014, MAY, 2)),
						p.from(LocalDate.of(2014, JULY, 4)), false).size());

		assertEquals("FridayWeekly range size (2)",  1l,
				p.range(p.from(LocalDate.of(2014, JULY, 4)),
						p.from(LocalDate.of(2014, JULY, 4)), false).size());

		assertEquals("FridayWeekly range values size (1)",  10l,
				p.range(p.from(LocalDate.of(2014, MAY, 2)),
						p.from(LocalDate.of(2014, JULY, 4)), false).values().size());
		
		log.info("END testFridayWeekly");
	}

	@Test
	public void testBusinessDaily() {
		log.info("START testBusinessDaily");

		Periodicity<BusinessDay> p = Periodicities.get("B");
		assertEquals("BusinessDay from/to localdate (1)",
				LocalDate.of(2014, JULY, 7), p.from(LocalDate.of(2014, JULY, 7)).endDate());
		assertEquals("BusinessDay from/to localdate (2)",
				LocalDate.of(2014, JUNE, 30), p.from(LocalDate.of(2014, JUNE, 30)).endDate());
		assertEquals("BusinessDay from/to localdate (saturday)",
				LocalDate.of(2014, JULY, 7), p.from(LocalDate.of(2014, JULY, 5)).endDate());
		assertEquals("BusinessDay from/to localdate (sunday)",
				LocalDate.of(2014, JULY, 7), p.from(LocalDate.of(2014, JULY, 6)).endDate());
		assertEquals("BusinessDay next (1)",
				LocalDate.of(2014, JULY, 7), p.from(LocalDate.of(2014, JULY, 4)).next().endDate());
		assertEquals("BusinessDay previous (1)",
				LocalDate.of(2014, JULY, 7), p.from(LocalDate.of(2014, JULY, 8)).previous().endDate());

		assertEquals("BusinessDay distance (1)",  0l,
				p.distance(p.from(LocalDate.of(2014, JULY, 7)),
						p.from(LocalDate.of(2014, JULY, 7))));

		assertEquals("BusinessDay distance (2)",  1l,
				p.distance(p.from(LocalDate.of(2014, JULY, 4)),
						p.from(LocalDate.of(2014, JULY, 7))));

		assertEquals("BusinessDay distance (3)",  9l,
				p.distance(p.from(LocalDate.of(2014, JULY, 1)),
						p.from(LocalDate.of(2014, JULY, 14))));

		assertEquals("BusinessDay distance (4)",  -9l,
				p.distance(p.from(LocalDate.of(2014, JULY, 14)),
						p.from(LocalDate.of(2014, JULY, 1))));

		assertEquals("BusinessDay range size (1)",  10l,
				p.range(p.from(LocalDate.of(2014, JULY, 1)),
						p.from(LocalDate.of(2014, JULY, 14)), false).size());

		assertEquals("BusinessDay range size (2)",  1l,
				p.range(p.from(LocalDate.of(2014, JULY, 1)),
						p.from(LocalDate.of(2014, JULY, 1)), false).size());

		assertEquals("BusinessDay range values size (1)",  10l,
				p.range(p.from(LocalDate.of(2014, JULY, 1)),
						p.from(LocalDate.of(2014, JULY, 14)), false).values().size());
		
		log.info("END testBusinessDaily");
	}

}
