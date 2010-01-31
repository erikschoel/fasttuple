package org.tearsinrain.fasttuple;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

import static org.testng.Assert.*;

public class GetterTest {
    private static final FactoryHelper<GetterElementTest> helper = new FactoryHelper<GetterElementTest>(
	    GetterElementTest.class);

    @Factory
    public Object[] createNullableTests() throws Exception {
	return helper.from(FactoryHelper.builderClasses).toArray();
    }

    public static class GetterElementTest extends ElementTester {
	public GetterElementTest(Class<?> tupleClass, int size, int index) {
	    super(tupleClass, size, index);
	}

	@Test
	public void test_numbered_getter() throws Throwable {
	    Object instance = makeTupleInstance();
	    Method getter = instance.getClass().getMethod("get" + (index + 1));
	    Object gottenValue = getter.invoke(instance);
	    assertEquals(gottenValue, index + 1);
	}

	private final ImmutableList<String> names = ImmutableList.of("First", "Second", "Third",
		    "Fourth", "Fifth", "Sixth", "Seventh", "Eighth");

	@Test
	public void test_named_getter() throws Throwable {
	    System.out.println(tupleClass.getName() + " " + size + " " + index );
	    Object instance = makeTupleInstance();
	    String name = names.get(index);
	    Method getter = instance.getClass().getMethod("get" + name);
	    Object gottenValue = getter.invoke(instance);
	    assertEquals(gottenValue, index + 1);
	}

	private final ImmutableList<String> letters = ImmutableList.of("a", "b", "c", "d", "e",
		"f", "g", "h");

	@Test
	public void test_lettered_field_read() throws Throwable {
	    Object instance = makeTupleInstance();
	    String letter = letters.get(index);
	    Field field = instance.getClass().getField(letter);
	    Object gottenValue = field.get(instance);
	    assertEquals(gottenValue, 1 + index);
	}
	
	@Test(expectedExceptions = {IllegalAccessException.class})
	public void test_lettered_field_write() throws Throwable {
	    Object instance = makeTupleInstance();
	    String letter = letters.get(index);
	    Field field = instance.getClass().getField(letter);
	    field.set(instance, Integer.valueOf(3));
	}
    }
}
