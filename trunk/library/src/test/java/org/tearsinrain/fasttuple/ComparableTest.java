package org.tearsinrain.fasttuple;

import static org.testng.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

// some of this code was borrowed from junitx.extensions.ComparabilityTestCase

public class ComparableTest {
    @Factory
    public Object[] createTests() throws Exception {
	List<Object> result = Lists.newArrayList();

	ImmutableList<Class<?>> comparableClasses = ImmutableList.of(
		org.tearsinrain.fasttuple.comparable.Builder.class,
		org.tearsinrain.fasttuple.comparable.serializable.Builder.class,
		org.tearsinrain.fasttuple.nullable.comparable.Builder.class,
		org.tearsinrain.fasttuple.nullable.comparable.serializable.Builder.class);
	FactoryHelper<ComparableElementTest> helper1 = FactoryHelper
		.build(ComparableElementTest.class);
	for (ComparableElementTest test : helper1.from(comparableClasses)) {
	    result.add((Object) test);
	}

	ImmutableList<Class<?>> nonComparableClasses = ImmutableList.of(
		org.tearsinrain.fasttuple.Builder.class,
		org.tearsinrain.fasttuple.serializable.Builder.class,
		org.tearsinrain.fasttuple.nullable.Builder.class,
		org.tearsinrain.fasttuple.nullable.serializable.Builder.class);
	FactoryHelper<NonComparableElementTest> helper2 = FactoryHelper
		.build(NonComparableElementTest.class);
	for (NonComparableElementTest test : helper2.from(nonComparableClasses)) {
	    result.add((Object) test);
	}

	return result.toArray();
    }

    public static class ComparableElementTest<T extends Comparable<T>> extends
	    ElementTester {
	private T less, eq1, eq2, greater;

	public ComparableElementTest(Class<?> tupleClass, int size, int index) {
	    super(tupleClass, size, index);
	}

	@SuppressWarnings("unchecked")
	@BeforeTest
	public void setup() throws Throwable {
	    int indexToChange = size - 1;
	    List<Object> eqArgs = Lists.newArrayList(generateTupleArgs());
	    List<Object> lessArgs = Lists.newArrayList(eqArgs);
	    lessArgs.set(indexToChange, Integer.class.cast(lessArgs.get(indexToChange)) - 1);
	    List<Object> greaterArgs = Lists.newArrayList(eqArgs);
	    greaterArgs.set(indexToChange, Integer.class.cast(greaterArgs.get(indexToChange)) + 1);

	    less = (T) makeTupleInstance(lessArgs);
	    eq1 = (T) makeTupleInstance(eqArgs);
	    eq2 = (T) makeTupleInstance(eqArgs);
	    greater = (T) makeTupleInstance(greaterArgs);
	}

	@Test(expectedExceptions = {NullPointerException.class})
	public void test_compare_null_throws() {
	    less.compareTo(null);
	}
	
	@Test
	public void test_implements_comparable() throws Throwable {
	    Object instance = makeTupleInstance();
	    assertTrue(getInterfaces(instance).contains(Comparable.class));
	}

	@Test
	public void test_simple_equality() {
	    assertEquals(eq1, eq2);
	    assertNotSame(eq1, eq2);
	}

	@Test
	public void test_reversed_signs() {
	    assertEquals(sgn(less.compareTo(eq1)), -sgn(eq1.compareTo(less)), "less vs. eq1");
	    assertEquals(sgn(less.compareTo(eq2)), -sgn(eq2.compareTo(less)), "less vs. eq2");
	    assertEquals(sgn(less.compareTo(greater)), -sgn(greater.compareTo(less)),
		    "less vs. greater");
	    assertEquals(sgn(eq1.compareTo(eq2)), -sgn(eq2.compareTo(eq1)), "eq1 vs. eq2");
	    assertEquals(sgn(eq1.compareTo(greater)), -sgn(greater.compareTo(eq1)),
		    "eq1 vs. greater");
	    assertEquals(sgn(eq2.compareTo(greater)), -sgn(greater.compareTo(eq2)),
		    "eq2 vs. greater");
	}

	@Test
	public void test_same_signs() {
	    assertEquals(sgn(eq1.compareTo(less)), sgn(eq2.compareTo(less)), "equal vs. less");
	    assertEquals(sgn(eq1.compareTo(greater)), sgn(eq2.compareTo(greater)),
		    "equal vs. greater");
	}

	@Test
	public void test_return_values() {
	    assertThat(less, lessThan(eq1));
	    assertThat(less, lessThan(eq2));
	    assertThat(greater, greaterThan(less));
	    assertThat(eq1, equalTo(eq2));
	    assertThat(greater, greaterThan(eq1));
	    assertThat(greater, greaterThan(eq2));
	}
	
	private int sgn(int x) {
	    return (x == 0) ? 0 : (x / Math.abs(x));
	}
    }

    public static class NonComparableElementTest extends ElementTester {
	public NonComparableElementTest(Class<?> tupleClass, int size, int index) {
	    super(tupleClass, size, index);
	}

	@Test
	public void test_implements_comparable() throws Throwable {
	    Object instance = makeTupleInstance();
	    assertFalse(getInterfaces(instance).contains(Comparable.class));
	}
    }

}
