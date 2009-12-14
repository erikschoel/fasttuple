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

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.tearsinrain.jcodemodel.CodeWriter;
import org.tearsinrain.jcodemodel.JClass;
import org.tearsinrain.jcodemodel.JClassAlreadyExistsException;
import org.tearsinrain.jcodemodel.JClassContainer;
import org.tearsinrain.jcodemodel.JCodeModel;
import org.tearsinrain.jcodemodel.JDefinedClass;
import org.tearsinrain.jcodemodel.JDocComment;
import org.tearsinrain.jcodemodel.JExpr;
import org.tearsinrain.jcodemodel.JInvocation;
import org.tearsinrain.jcodemodel.JMethod;
import org.tearsinrain.jcodemodel.JMod;
import org.tearsinrain.jcodemodel.JPackage;
import org.tearsinrain.jcodemodel.JVar;
import org.tearsinrain.jcodemodel.writer.FileCodeWriter;
import org.tearsinrain.jcodemodel.writer.PrologCodeWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.acl.Owner;
import java.util.List;

import javax.annotation.Nonnull;

public class Generator {

    public static class TupleClassContainer {
	private static final ImmutableList<String> packagePrefix = ImmutableList.of("org",
		"tearsinrain", "fasttuple");
	private final Config config;
	private final JCodeModel model;
	private final JPackage _package;
	private final JDefinedClass _class;
	private final List<TupleClass> tupleClasses;

	public TupleClassContainer(Config c, JCodeModel owner) {
	    config = c;
	    model = owner;
	    _package = model._package(config.packageName(packagePrefix));

	    try {
		_class = _package._class(JMod.PUBLIC | JMod.ABSTRACT, "Tuple");
	    } catch (JClassAlreadyExistsException e) {
		throw new RuntimeException(e);
	    }

	    _class.method(JMod.PUBLIC | JMod.ABSTRACT, int.class, "getSize");
	    tupleClasses = Lists.newArrayList();

	    for (Integer i = 1; i <= config.size; i++) {
		Config tupleClassConfig = config.clone(i);
		tupleClasses.add(new TupleClass(tupleClassConfig, _class));
		makeFromMethod(tupleClassConfig);
	    }

	    TupleClass.buildJavadoc(_class, config, "Tuple",
		    "A provider of high-performance, immutable tuples");
	}

	private void makeFromMethod(Config tupleClassConfig) {
	    TupleClass c = tupleClasses.get(tupleClassConfig.size - 1);

	    JMethod m = _class.method(defaultModifiers, c.narrow(), "from");
	    c.generify(m);

	    // arguments...gah, I long for python's zip or common lisp's loop or
	    // anything that allows you to do parallel iteration
	    List<JVar> variables = c.buildVariables(m, null);

	    // a from method will always return something
	    m.annotate(Nonnull.class);

	    // add body...
	    JInvocation ctorCall = JExpr._new(c.narrow());

	    for (JVar variable : variables) {
		ctorCall.arg(variable);
	    }

	    m.body()._return(ctorCall);

	    JDocComment comment = m.javadoc();
	    comment.add(String.format(
		    "Returns a new %s containing each of the supplied paramaters. \n\n", c
			    .getName()));
	    if (!tupleClassConfig.nullable) {
		comment.addThrows(NullPointerException.class).add(
			"if any of the supplied paramaters are null.");
	    }

	}
    }

    private static final int defaultModifiers = JMod.PUBLIC | JMod.STATIC | JMod.FINAL;

    private static String readFile(File f) throws IOException {
	BufferedReader reader = new BufferedReader(new FileReader(f));
	StringBuilder sb = new StringBuilder((int) f.length());
	String line = reader.readLine();
	while (line != null) {
	    sb.append(line);
	    sb.append('\n');
	    line = reader.readLine();
	}
	sb.deleteCharAt(sb.length() - 1);
	return sb.toString();
    }

    private static final Logger logger = Logger.getLogger(Generator.class);

    public static void main(String[] args) throws IOException {
	Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("[%-p] %m%n")));

	JCodeModel cm = new JCodeModel();

	// args are output directory and path to license file
	File output = new File(args[0]);
	output.mkdirs();

	File licenseFile = new File(args[1]);

	logger.info(String.format("writing output to %s using license file %s", args[0], args[1]));

	String license = readFile(licenseFile);

	for (Config c : Config.all(12)) {
	    new TupleClassContainer(c, cm);
	    CodeWriter sourceWriter = new PrologCodeWriter(new FileCodeWriter(output), license);
	    CodeWriter resourceWriter = new FileCodeWriter(output);
	    logger.info(String.format("generating %s...", c.packagePath()));
	    try {
		cm.build(sourceWriter, resourceWriter);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }
}
