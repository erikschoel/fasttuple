package org.tearsinrain.fasttuple;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import com.google.common.collect.Lists;

public class AdderTest {
    private static final int maxTupleSize = 8;
    
    @Factory
    public Object[] createTests() throws Exception {
	List<Object> result = Lists.newArrayList();
	for (Class<?> builderClass: FactoryHelper.builderClasses) {
	    int index = 0;
	    for (String name: FactoryHelper.tupleNames) {
		Class<?> tupleClass = FactoryHelper.getClass(builderClass, name);
		int size = FactoryHelper.tupleNames.indexOf(tupleClass.getSimpleName()) + 1;
		int addSize = 1;
		while (addSize + size <= maxTupleSize) {
		    result.add(new AdderTestCase(tupleClass, size, index, addSize));
		    addSize += 1;
		}
		index += 1;
	    }
	}
	return result.toArray();
    }
    
    public static class AdderTestCase extends ElementTester {
	private final int addSize;
	public AdderTestCase(Class<?> tupleClass, int size, int index, int addSize) {
	    super(tupleClass, size, index);
	    this.addSize = addSize;
	}
	
	@Test
	public void test_element_addition() throws Throwable {
	    Object instance = makeTupleInstance();
	    List<Integer> elementsToAdd = Lists.newArrayList();
	    for (int i = 0; i < addSize; i++) {
		elementsToAdd.add(i);
	    }
	    Class<?>[] elementTypes = new Class<?>[addSize];
	    Arrays.fill(elementTypes, argClass(tupleClass));

	    Method adder = instance.getClass().getDeclaredMethod("add", elementTypes);
	    Object combinedTuple = adder.invoke(instance, elementsToAdd.toArray());
	    
	    Method sizer = combinedTuple.getClass().getMethod("size");
	    Object combinedSize = sizer.invoke(combinedTuple);
	    assertEquals(combinedSize, size + addSize);
	    
	    for (int position = 1; position <= size; position++) {
		Method getter = combinedTuple.getClass().getMethod("get" + position);
		Object component = getter.invoke(combinedTuple);
		assertEquals(component, position);
	    }
	    
	    for (int position = size + 1; position <= (size + addSize); position++) {
		Method getter = combinedTuple.getClass().getMethod("get" + position);
		Object component = getter.invoke(combinedTuple);
		assertEquals(component, position - size - 1);
	    }
	}

	@Test(expectedExceptions = {NullPointerException.class})
	public void test_null_tuple_addition() throws Throwable {
	    Object instance = makeTupleInstance();
	    List<Integer> elementsToAdd = Lists.newArrayList();
	    for (int i = 0; i < addSize; i++) {
		elementsToAdd.add(i);
	    }
	    Class<?>[] elementTypes = new Class<?>[addSize];
	    Arrays.fill(elementTypes, argClass(tupleClass));
	    
	    Class<?> builderClass = tupleClass.getEnclosingClass();
	    Method build = builderClass.getMethod("from", elementTypes);
	    Object tupleToAdd;
	    try {
		tupleToAdd = build.invoke(null, elementsToAdd.toArray());
	    } catch (InvocationTargetException e) {
		throw e.getCause();
	    }

	    Method adder = instance.getClass().getDeclaredMethod("add", tupleToAdd.getClass());
	    Object[] args = new Object[1];
	    args[0] = null;
	    try {
		adder.invoke(instance, args);
	    } catch (InvocationTargetException e) {
		throw e.getCause();
	    }
	}

	@Test
	public void test_tuple_addition() throws Throwable {
	    Object instance = makeTupleInstance();
	    List<Integer> elementsToAdd = Lists.newArrayList();
	    for (int i = 0; i < addSize; i++) {
		elementsToAdd.add(i);
	    }
	    Class<?>[] elementTypes = new Class<?>[addSize];
	    Arrays.fill(elementTypes, argClass(tupleClass));
	    
	    Class<?> builderClass = tupleClass.getEnclosingClass();
	    Method build = builderClass.getMethod("from", elementTypes);
	    Object tupleToAdd;
	    try {
		tupleToAdd = build.invoke(null, elementsToAdd.toArray());
	    } catch (InvocationTargetException e) {
		throw e.getCause();
	    }

	    Method adder = instance.getClass().getDeclaredMethod("add", tupleToAdd.getClass());
	    Object combinedTuple = adder.invoke(instance, tupleToAdd);
	    
	    Method sizer = combinedTuple.getClass().getMethod("size");
	    Object combinedSize = sizer.invoke(combinedTuple);
	    assertEquals(combinedSize, size + addSize);
	    
	    for (int position = 1; position <= size; position++) {
		Method getter = combinedTuple.getClass().getMethod("get" + position);
		Object component = getter.invoke(combinedTuple);
		assertEquals(component, position);
	    }
	    
	    for (int position = size + 1; position <= (size + addSize); position++) {
		Method getter = combinedTuple.getClass().getMethod("get" + position);
		Object component = getter.invoke(combinedTuple);
		assertEquals(component, position - size - 1);
	    }
	}
    }
}
