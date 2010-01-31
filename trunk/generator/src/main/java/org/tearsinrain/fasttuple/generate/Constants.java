package org.tearsinrain.fasttuple.generate;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

public class Constants {

    public static final ImmutableList<String> names = ImmutableList.of("Single", "Pair", "Triple",
	    "Quadruple", "Quintuple", "Sextuple", "Septuple", "Octuple", "Nonuple", "Decuple",
	    "Undecuple", "Duodecuple");
    public static final ImmutableList<String> ordinals = ImmutableList.of("first", "second",
	    "third", "fourth", "fifth", "sixth", "seventh", "eighth", "nineth", "tenth",
	    "eleventh", "twelfth");
    static final Function<String, String> toTitleCase = new Function<String, String>() {
	public String apply(String ord) {
	    return Character.toTitleCase(ord.charAt(0)) + ord.substring(1);
	}
    };
    public static final ImmutableList<String> capitalOrdinals = ImmutableList.copyOf(Collections2
	    .transform(ordinals, toTitleCase));

}
