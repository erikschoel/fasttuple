package org.tearsinrain.fasttuple;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

import static org.testng.Assert.*;

public class EqualsHasherTest {
    private static final FactoryHelper<EqHashClassTest> helper = new FactoryHelper<EqHashClassTest>(EqHashClassTest.class); 
    
    @Factory
    public Object[] createTests() throws Exception {
	return helper.fromSmall(FactoryHelper.builderClasses).toArray();
    }

    public static class EqHashClassTest extends ElementTester {
	public EqHashClassTest(Class<?> tupleClass, int size) {
	    super(tupleClass, size, 0);
	}

	// just for coverage....
	@Test
	public void test_zip() throws Throwable {
	    Object instance = makeTupleInstance();
	    instance.hashCode();
	    instance.equals(null); 
	    instance.equals(makeTupleInstance()); 
	    instance.equals(instance); 
	    instance.toString();
	}
    }
}
