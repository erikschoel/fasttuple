/*
Copyright (c) 2009 Michael Salib

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package org.tearsinrain.fasttuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.tearsinrain.fasttuple.comparable.Tuple;
import org.tearsinrain.fasttuple.comparable.Tuple.Pair;

import com.google.common.collect.ImmutableList;

public class SimpleTest {
	@Test
	public void test_getters() {
		Integer a = Integer.valueOf(3);
		Double b = Double.valueOf(4.5);
		Pair<Integer, Double> p = Tuple.from(a, b);
		assertEquals(a, p.a);
		assertEquals(b, p.b);
		assertEquals(a, p.get1());
		assertEquals(b, p.get2());
		assertEquals(a, p.getFirst());
		assertEquals(b, p.getSecond());
	}

	@Test
	public void test_eq() {
		Integer a = Integer.valueOf(3);
		Double b = Double.valueOf(4.5);
		Pair<Integer, Double> p1 = Tuple.from(a, b);
		Pair<Integer, Double> p2 = Tuple.from(a, b);
		assertTrue(p1.equals(p2));
		assertTrue(p2.equals(p1));
		assertFalse(p1.equals(null));
		assertEquals(p1, p2);
	}

	@Test
	public void test_stringers() {
		assertEquals("(1, 2)", Tuple.from(1, 2).toString());
		assertEquals("<1-2]", Tuple.from(1, 2).toString("<", "-", "]"));
	}

	@Test
	public void test_zipper() {
		ImmutableList<Pair<String, Integer>> expected = ImmutableList.of(Tuple
				.from("Alice", 8), Tuple.from("Bob", 12), Tuple.from("Charlie",
				35), Tuple.from("Dana", 49));

		List<Integer> ages = Arrays.asList(8, 12, 35, 49);
		List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "Dana");
		ImmutableList<Pair<String, Integer>> actual = ImmutableList.copyOf(Pair
				.zip(names, ages));

		assertEquals(expected, actual);
	}

}
