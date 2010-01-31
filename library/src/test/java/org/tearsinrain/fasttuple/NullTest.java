package org.tearsinrain.fasttuple;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.*;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class NullTest {
    private static final FactoryHelper<NullElementTest> helper = new FactoryHelper<NullElementTest>(
	    NullElementTest.class);

    private static final ImmutableList<Class<?>> builderClasses = ImmutableList.of(
	    org.tearsinrain.fasttuple.Builder.class,
	    org.tearsinrain.fasttuple.comparable.Builder.class,
	    org.tearsinrain.fasttuple.comparable.serializable.Builder.class,
	    org.tearsinrain.fasttuple.serializable.Builder.class);

    @Factory
    public Object[] createTests() throws Exception {
	return helper.from(builderClasses).toArray();
    }

    public static class NullElementTest extends ElementTester {
	public NullElementTest(Class<?> tupleClass, int size, int index) {
	    super(tupleClass, size, index);
	}
    	
    	@Override
    	protected List<Object> generateTupleArgs() {
    	    List<Object> result = Lists.newArrayList(super.generateTupleArgs());
	    result.set(index, null);
    	    return result;
    	}
    	
	@Test(expectedExceptions = {NullPointerException.class})
	public void test_null_element_construction_fail() throws Throwable {
	    makeTupleInstance();
	}
    }
}
