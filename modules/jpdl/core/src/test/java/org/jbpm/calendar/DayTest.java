/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.calendar;

import java.text.*;

import junit.framework.*;

public class DayTest extends TestCase {

  public void testDaySingleDayPartParsing() {
    DateFormat dateFormat = new SimpleDateFormat("HH:mm");
    Day day = new Day("9:00-12:15", dateFormat, null);
    assertEquals(1, day.dayParts.length);
    DayPart dayPart = day.dayParts[0];
    assertEquals(9, dayPart.fromHour);
    assertEquals(0, dayPart.fromMinute);
    assertEquals(12, dayPart.toHour);
    assertEquals(15, dayPart.toMinute);
  }

  public void testDayMultipleDayPartsParsing() {
    DateFormat dateFormat = new SimpleDateFormat("HH:mm");
    Day day = new Day("9:00-12:15 & 13:00-17:00", dateFormat, null);
    assertEquals(2, day.dayParts.length);
    DayPart dayPart = day.dayParts[1];
    assertEquals(13, dayPart.fromHour);
    assertEquals(0, dayPart.fromMinute);
    assertEquals(17, dayPart.toHour);
    assertEquals(0, dayPart.toMinute);
  }

  public void testEmptyDayParsing() {
    DateFormat dateFormat = new SimpleDateFormat("HH:mm");
    Day day = new Day("", dateFormat, null);
    assertEquals(0, day.dayParts.length);
  }

  public void testDayEndingAtMidnight() {
    DateFormat dateFormat = new SimpleDateFormat("HH:mm");
    Day day = new Day("8:00-12:30 & 13:30-24:00", dateFormat, null);
    assertEquals(2, day.dayParts.length);
    DayPart dayPart = day.dayParts[0];
    assertEquals(8, dayPart.fromHour);
    assertEquals(0, dayPart.fromMinute);
    assertEquals(12, dayPart.toHour);
    assertEquals(30, dayPart.toMinute);
    dayPart = day.dayParts[1];
    assertEquals(13, dayPart.fromHour);
    assertEquals(30, dayPart.fromMinute);
    assertEquals(24, dayPart.toHour);
    assertEquals(0, dayPart.toMinute);
  }
}
