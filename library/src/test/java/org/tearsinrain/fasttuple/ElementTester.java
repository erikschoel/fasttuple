package org.tearsinrain.fasttuple;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class ElementTester {
    protected final Class<?> tupleClass;
    protected final int size, index;

    public ElementTester(Class<?> tupleClass, int size, int index) {
	this.tupleClass = tupleClass;
	this.size = size;
	this.index = index;
    }

    protected static Class<?> argClass(Class<?> builderClass) {
	String name = builderClass.getCanonicalName();
	if (name.contains("comparable")) {
	    return Comparable.class;
	}
	if (name.contains("serializable")) {
	    return Serializable.class;
	}
	return Object.class;
    }

    protected Class<?>[] argTypes(Class<?> builderClass) {
	Class<?>[] types = new Class[size];
	Arrays.fill(types, argClass(builderClass));
	return types;
    }

    protected List<Object> generateTupleArgs() {
	return Lists.transform(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8).subList(0, size),
		new Function<Integer, Object>() {
		    @Override
		    public Object apply(Integer i) {
			return (Object) i;
		    }
		});
    }

    protected Object makeTupleInstance(List<Object> args) throws Throwable {
	Class<?> builderClass = tupleClass.getEnclosingClass();
	Method from = builderClass.getMethod("from", argTypes(builderClass));
	try {
	    return from.invoke(null, args.toArray());
	} catch (InvocationTargetException e) {
	    throw e.getCause();
	}
    }

    protected Object makeTupleInstance() throws Throwable {
	return makeTupleInstance(generateTupleArgs());
    }

    protected List<Class<?>> getInterfaces(Object x) {
	return Arrays.asList(x.getClass().getInterfaces());
    }
}
