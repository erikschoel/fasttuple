package org.tearsinrain.fasttuple;

import java.util.List;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class NullableTest {
    private static final FactoryHelper<NonNullElementTest> nullableHelper = new FactoryHelper<NonNullElementTest>(
	    NonNullElementTest.class);

    private static final ImmutableList<Class<?>> nullableBuilderClasses = ImmutableList.of(
	    org.tearsinrain.fasttuple.nullable.Builder.class,
	    org.tearsinrain.fasttuple.nullable.comparable.Builder.class,
	    org.tearsinrain.fasttuple.nullable.comparable.serializable.Builder.class,
	    org.tearsinrain.fasttuple.nullable.serializable.Builder.class);

    @Factory
    public Object[] createNullableTests() throws Exception {
	return nullableHelper.from(nullableBuilderClasses).toArray();
    }

    public static class NonNullElementTest extends ElementTester {
	public NonNullElementTest(Class<?> tupleClass, int size, int index) {
	    super(tupleClass, size, index);
	}
    	
    	@Override
    	protected List<Object> generateTupleArgs() {
    	    List<Object> result = Lists.newArrayList(super.generateTupleArgs());
	    result.set(index, null);
    	    return result;
    	}
    	
	@Test
	public void test_null_element_construction_succeed() throws Throwable {
	    makeTupleInstance();
	}
    }
}
