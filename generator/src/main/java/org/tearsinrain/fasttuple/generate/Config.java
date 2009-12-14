/*
Copyright (c) 2009 Michael Salib

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package org.tearsinrain.fasttuple.generate;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class Config {
    public final boolean nullable;
    public final boolean comparable;
    public final boolean serializable;
    public final int size;
    private final ImmutableList<String> genericTypes;
    private final ImmutableList<String> variables;
    private final static ImmutableList<String> allGenericTypes = ImmutableList.of("A", "B", "C",
	    "D", "E", "F", "G", "H", "I", "J", "K", "L");
    private static final Function<String, String> lowerCaser = new Function<String, String>() {
	public String apply(String o) {
	    return o.toLowerCase();
	}
    };

    private final static ImmutableList<String> allVariables = ImmutableList.copyOf(Iterables
	    .transform(allGenericTypes, lowerCaser));

    public Config(boolean nullable, boolean comparable, boolean serializable, int size) {
	this.nullable = nullable;
	this.comparable = comparable;
	this.serializable = serializable;
	this.size = size;
	genericTypes = allGenericTypes.subList(0, size);
	variables = allVariables.subList(0, size);
    }

    public ImmutableList<String> packagePath() {
	ImmutableList.Builder<String> builder = ImmutableList.builder();

	if (nullable) {
	    builder.add("nullable");
	}

	if (comparable) {
	    builder.add("comparable");
	}

	if (serializable) {
	    builder.add("serializable");
	}

	return builder.build();
    }

    public String packageName(Iterable<String> packagePrefix) {
	return Joiner.on('.').join(Iterables.concat(packagePrefix, packagePath()));
    }

    public Config clone(int newSize) {
	return new Config(nullable, comparable, serializable, newSize);
    }

    public ImmutableList<String> types() {
	return genericTypes;
    }

    public ImmutableList<String> variables() {
	return variables;
    }

    public static ImmutableList<Config> all(int size) {
	ImmutableList.Builder<Config> builder = ImmutableList.builder();
	boolean[] all = { true, false };

	for (boolean nullable : all) {
	    for (boolean comparable : all) {
		for (boolean serializable : all) {
		    builder.add(new Config(nullable, comparable, serializable, size));
		}
	    }
	}

	return builder.build();
    }

    @Override
    public int hashCode() {
	return Objects.hashCode(nullable, comparable, serializable, size);
    }

    @Override
    public boolean equals(Object other) {
	if (!(other instanceof Config)) {
	    return false;
	}

	Config config = (Config) other;

	return (nullable == config.nullable) && (comparable == config.comparable)
		&& (serializable == config.serializable) && (size == config.size);
    }
}
