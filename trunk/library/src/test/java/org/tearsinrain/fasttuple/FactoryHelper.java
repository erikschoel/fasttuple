package org.tearsinrain.fasttuple;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class FactoryHelper<T> {
    public static final ImmutableList<Class<?>> builderClasses = ImmutableList.of(
	    org.tearsinrain.fasttuple.Builder.class,
	    org.tearsinrain.fasttuple.comparable.Builder.class,
	    org.tearsinrain.fasttuple.comparable.serializable.Builder.class,
	    org.tearsinrain.fasttuple.serializable.Builder.class,
	    org.tearsinrain.fasttuple.nullable.Builder.class,
	    org.tearsinrain.fasttuple.nullable.comparable.Builder.class,
	    org.tearsinrain.fasttuple.nullable.comparable.serializable.Builder.class,
	    org.tearsinrain.fasttuple.nullable.serializable.Builder.class);

    public static final ImmutableList<String> tupleNames = ImmutableList.of("Single", "Pair",
	    "Triple", "Quadruple", "Quintuple", "Sextuple", "Septuple", "Octuple");

    private final Class<T> testClass;
    
    public FactoryHelper(Class<T> testClass) {
	this.testClass = testClass;
    }
    
    public static <S> FactoryHelper<S> build(Class<S> testClass) {
	return new FactoryHelper<S>(testClass);
    }
    
    public T from(Class<?> tupleClass, int size, int index) throws Exception{
	Constructor<T> ctor = testClass.getConstructor(Class.class, int.class, int.class);
	return ctor.newInstance(tupleClass, size, index);
    }
    
    public T from(Class<?> tupleClass, int size) throws Exception{
	Constructor<T> ctor = testClass.getConstructor(Class.class, int.class);
	return ctor.newInstance(tupleClass, size);
    }

    public static Class<?> getClass(Class<?> builderClass, String tupleClassName) {
	List<Class<?>> childClasses = Arrays.asList(builderClass.getClasses());
	for (Class<?> candidate : childClasses) {
	    if (candidate.getSimpleName().endsWith(tupleClassName))
		return candidate;
	}
	throw new RuntimeException();
    }

    public List<T> from(Class<?>... builderClasses) throws Exception {
	return from(Lists.newArrayList(builderClasses));
    }
    
    public List<T> from(Iterable<Class<?>> builderClasses) throws Exception {
	List<T> result = Lists.newArrayList();
	for (Class<?> builderClass: builderClasses) {
	    for (String name: tupleNames) {
		Class<?> tupleClass = getClass(builderClass, name);
		int size = tupleNames.indexOf(tupleClass.getSimpleName()) + 1;
		for (int index = 0; index < size; index++) {
		    result.add(from(tupleClass, size, index));
		}
	    }
	}
	return result;
    }
    
    public List<T> fromSmall(Iterable<Class<?>> builderClasses) throws Exception {
	List<T> result = Lists.newArrayList();
	for (Class<?> builderClass: builderClasses) {
	    for (String name: tupleNames) {
		Class<?> tupleClass = getClass(builderClass, name);
		int size = tupleNames.indexOf(tupleClass.getSimpleName()) + 1;
		result.add(from(tupleClass, size));
	    }
	}
	return result;
    }
}
