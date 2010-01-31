package org.tearsinrain.fasttuple;

//import static org.junit.Assert.*;
//import static org.junit.Assume.*;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.hamcrest.Matcher;
/*
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.matchers.JUnitMatchers.*;

@RunWith(Parameterized.class)
*/
public class TupleTest {
    /*
    @Parameters
    public static Collection<Object[]> getBuilders() {
	Collection<Object[]> result = new ArrayList<Object[]>();
	result.add(new Object[] {org.tearsinrain.fasttuple.Builder., false, false, false});
	result.add(new Object[] {org.tearsinrain.fasttuple.comparable.Builder.Builder, true, false, false});
	result.add(new Object[] {org.tearsinrain.fasttuple.serializable.Builder.Builder, false, true, false});
	result.add(new Object[] {org.tearsinrain.fasttuple.nullable.Builder.Builder, false, false, true});
	result.add(new Object[] {org.tearsinrain.fasttuple.comparable.serializable.Builder.Builder, true, true, false});
	return result;
    }
    
    private final Object builderInstance;
    private final boolean isComparable, isSerializable, isNullable;
    
    public TupleTest(Object builderInstance, boolean isComparable, boolean isSerializable, boolean isNullable) {
	this.builderInstance = builderInstance;
	this.isComparable = isComparable;
	this.isSerializable = isSerializable;
	this.isNullable = isNullable;
    }
    
    private Object getTupleInstance(Object...args) throws Throwable {
	Class<?> argClass = Object.class;
	if (isComparable) {
	    argClass = Comparable.class;
	} else if (isSerializable) {
	    argClass = Serializable.class;
	}
	
	Class<?>[] argTypes = new Class[args.length];
	Arrays.fill(argTypes, argClass);
	
	Method from = builderInstance.getClass().getDeclaredMethod("from", argTypes);
	try {
	    return from.invoke(builderInstance, args);
	} catch (InvocationTargetException e) {
	    throw e.getCause();
	}
    }
    
    private List<Class<?>> getInterfaces(Object x) {
	return Arrays.asList(x.getClass().getInterfaces());
    }
    
    @Test
    public void test_implementsComparable() throws Throwable {
	assumeTrue(isComparable);
	assertTrue(getInterfaces(getTupleInstance(3, "hi")).contains(Comparable.class));
    }

    @Test
    public void test_implementsSerializable() throws Throwable {
	assumeTrue(isSerializable);
	assertTrue(getInterfaces(getTupleInstance(3, "hi")).contains(Serializable.class));
    }
    
    @Test
    public void test_nullability() throws Throwable {
	assumeTrue(isNullable);
	Object t = getTupleInstance(3, null);
	assertEquals(null, t.getClass().getField("b").get(t));
    }
    
    @Test(expected = NullPointerException.class)
    public void test_nonNullability() throws Throwable {
	if (!isNullable) {
	    Object t = getTupleInstance(3, null);
	    assertEquals(null, t.getClass().getField("b").get(t));
	} else {
	    throw new NullPointerException();
	}
    }
*/
}
