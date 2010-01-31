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

//import static org.junit.Assert.*;
import static org.testng.AssertJUnit.*;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.core.IsInstanceOf;
//import org.junit.Test;
import org.testng.annotations.*;

import static org.tearsinrain.fasttuple.Builder.*;

import com.google.common.collect.ImmutableList;

public class SimpleTest {
    /*
    @Test
    public void test_builder_state_machine() {
	assertThat(Builder, new IsInstanceOf(
		org.tearsinrain.fasttuple.Builder.class));
	assertThat(Builder.comparable(), new IsInstanceOf(
		org.tearsinrain.fasttuple.comparable.Builder.class));
	assertThat(Builder.serializable(), new IsInstanceOf(
		org.tearsinrain.fasttuple.serializable.Builder.class));
	assertThat(Builder.comparable().serializable(), new IsInstanceOf(
		org.tearsinrain.fasttuple.comparable.serializable.Builder.class));
	assertThat(Builder.serializable().comparable(), new IsInstanceOf(
		org.tearsinrain.fasttuple.comparable.serializable.Builder.class));
	
	assertThat(Builder.nullable(), new IsInstanceOf(
		org.tearsinrain.fasttuple.nullable.Builder.class));
	assertThat(Builder.nullable().comparable(), new IsInstanceOf(
		org.tearsinrain.fasttuple.nullable.comparable.Builder.class));
	assertThat(Builder.nullable().serializable(), new IsInstanceOf(
		org.tearsinrain.fasttuple.nullable.serializable.Builder.class));
	assertThat(Builder.nullable().comparable().serializable(), new IsInstanceOf(
		org.tearsinrain.fasttuple.nullable.comparable.serializable.Builder.class));
	assertThat(Builder.nullable().serializable().comparable(), new IsInstanceOf(
		org.tearsinrain.fasttuple.nullable.comparable.serializable.Builder.class));
    }
    */

    @Test
    public void test_getters() {
	Integer a = Integer.valueOf(3);
	Double b = Double.valueOf(4.5);
	Pair<Integer, Double> p = Builder.from(a, b);
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
	Pair<Integer, Double> p1 = Builder.from(a, b);
	Pair<Integer, Double> p2 = Builder.from(a, b);
	assertTrue(p1.equals(p2));
	assertTrue(p2.equals(p1));
	assertFalse(p1.equals(null));
	assertEquals(p1, p2);
    }

    @Test
    public void test_stringers() {
	assertEquals("(1, 2)", Builder.from(1, 2).toString());
	assertEquals("<1-2]", Builder.from(1, 2).toString("<", "-", "]"));
    }

    @Test
    public void test_zipper() {
	ImmutableList<Pair<String, Integer>> expected = ImmutableList.of(Builder.from("Alice", 8),
		Builder.from("Bob", 12), Builder.from("Charlie", 35), Builder.from("Dana", 49));

	List<Integer> ages = Arrays.asList(8, 12, 35, 49);
	List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "Dana");
	ImmutableList<Pair<String, Integer>> actual = ImmutableList.copyOf(Pair.zip(names, ages));

	assertEquals(expected, actual);
    }

}
