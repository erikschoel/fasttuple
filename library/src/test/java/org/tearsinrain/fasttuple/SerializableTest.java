package org.tearsinrain.fasttuple;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class SerializableTest {
    @Factory
    public Object[] createTests() throws Exception {
	List<Object> result = Lists.newArrayList();
	
	ImmutableList<Class<?>> SerializableClasses = ImmutableList.of(
		    org.tearsinrain.fasttuple.comparable.serializable.Builder.class,
		    org.tearsinrain.fasttuple.serializable.Builder.class,
		    org.tearsinrain.fasttuple.nullable.comparable.serializable.Builder.class,
		    org.tearsinrain.fasttuple.nullable.serializable.Builder.class);
	FactoryHelper<SerializableElementTest> helper1 = FactoryHelper.build(SerializableElementTest.class);
	for (SerializableElementTest test: helper1.from(SerializableClasses)) {
	    result.add((Object) test);
	}

	ImmutableList<Class<?>> nonSerializableClasses = ImmutableList.of(
		    org.tearsinrain.fasttuple.Builder.class,
		    org.tearsinrain.fasttuple.comparable.Builder.class,
		    org.tearsinrain.fasttuple.nullable.Builder.class,
		    org.tearsinrain.fasttuple.nullable.comparable.Builder.class);
	FactoryHelper<NonSerializableElementTest> helper2 = FactoryHelper.build(NonSerializableElementTest.class);
	for (NonSerializableElementTest test: helper2.from(nonSerializableClasses)) {
	    result.add((Object) test);
	}

	return result.toArray();
    }

    public static class SerializableElementTest extends ElementTester {
	public SerializableElementTest(Class<?> tupleClass, int size, int index) {
	    super(tupleClass, size, index);
	}

	@Test
	public void test_implements_Serializable() throws Throwable {
	    Object instance = makeTupleInstance();
	    assertTrue(getInterfaces(instance).contains(Serializable.class));
	}
	
	@Test
	public void test_deserialization_works() throws Throwable {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(baos);
	    try {
		Object instance = makeTupleInstance();
	    	oos.writeObject(instance);
	    	oos.flush();
	    	byte[] frozen = baos.toByteArray();
	    	
	    	ByteArrayInputStream bais = new ByteArrayInputStream(frozen);
	    	ObjectInputStream ois = new ObjectInputStream(bais);
	    	try {
	    	    Object thawed = ois.readObject();
	    	    assertNotSame(thawed, instance);
	    	    assertEquals(thawed, instance);
	    	} finally {
	    	    ois.close();
	    	    bais.close();
	    	}
	    } finally {
		oos.close();
		baos.close();
	    }
	}
    }

    public static class NonSerializableElementTest extends ElementTester {
	public NonSerializableElementTest(Class<?> tupleClass, int size, int index) {
	    super(tupleClass, size, index);
	}

	@Test
	public void test_implements_Serializable() throws Throwable {
	    Object instance = makeTupleInstance();
	    assertFalse(getInterfaces(instance).contains(Serializable.class));
	}
	
	@Test(expectedExceptions = {NotSerializableException.class})
	public void test_throws_when_serialized() throws Throwable {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(baos);
	    Object instance = makeTupleInstance();
	    oos.writeObject(instance);
	}
    }

}
