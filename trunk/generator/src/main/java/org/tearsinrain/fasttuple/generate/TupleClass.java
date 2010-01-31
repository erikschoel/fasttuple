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

import static org.tearsinrain.jcodemodel.JExpr._null;
import static org.tearsinrain.jcodemodel.JExpr.lit;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.tearsinrain.jcodemodel.JBlock;
import org.tearsinrain.jcodemodel.JClass;
import org.tearsinrain.jcodemodel.JClassAlreadyExistsException;
import org.tearsinrain.jcodemodel.JCodeModel;
import org.tearsinrain.jcodemodel.JConditional;
import org.tearsinrain.jcodemodel.JDefinedClass;
import org.tearsinrain.jcodemodel.JDocComment;
import org.tearsinrain.jcodemodel.JExpr;
import org.tearsinrain.jcodemodel.JExpression;
import org.tearsinrain.jcodemodel.JFieldVar;
import org.tearsinrain.jcodemodel.JGenerifiable;
import org.tearsinrain.jcodemodel.JInvocation;
import org.tearsinrain.jcodemodel.JMethod;
import org.tearsinrain.jcodemodel.JMod;
import org.tearsinrain.jcodemodel.JOp;
import org.tearsinrain.jcodemodel.JType;
import org.tearsinrain.jcodemodel.JTypeVar;
import org.tearsinrain.jcodemodel.JVar;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

// add javadoc for zip methods, 
// add more tests
// add maven coverage report
// add nullness annotation verifier
public class TupleClass {
    private final Config config;
    private final String className;
    private final JDefinedClass _class;
    private final JCodeModel model;
    private final JType narrowedClass;
    private final ImmutableList<JTypeVar> genericTypes;
    private final ImmutableList<JVar> variables;

    private final int maxN;
    public TupleClass(Config config, int maxN, JDefinedClass owner) {
	this.maxN = maxN;
	this.config = config;
	className = Constants.names.get(config.size - 1);

	try {
	    _class = owner._class(JMod.PUBLIC | JMod.STATIC, className);
	} catch (JClassAlreadyExistsException e) {
	    throw new RuntimeException(e);
	}

	model = _class.owner();
	_class.annotate(Immutable.class);
	buildJavadoc(_class, config, className, "A high-performance, immutable " + className);
	_class.method(JMod.PUBLIC | JMod.FINAL, int.class, "size").body()._return(lit(config.size));
	
	genericTypes = generify(_class);
	narrowedClass = _class.narrow(genericTypes);
	variables = ImmutableList.copyOf(buildVariables(null, _class));
	
	buildConstructor();
	buildGetters();
	buildHasher();
	buildEquals();
	buildZippers();
	buildStringers();

	for (int n = 1; n < ((1 + maxN) - config.size); n++) {
	    buildElementAdder(config.clone(config.size + n));
	}

	for (int n = 1; n < ((1 + maxN) - config.size); n++) {
	    buildTupleAdder(config.clone(n));
	}

	if (config.comparable) {
	    _class._implements(model.ref(Comparable.class).narrow(narrowedClass));
	    buildComparator();
	}

	if (config.serializable) {
	    _class._implements(Serializable.class);
	    buildSerializer();
	}
    }

    private final static Joiner ander = Joiner.on(" and ");

    public static void buildJavadoc(JDefinedClass _class, Config config, String className,
	    String header) {
	JDocComment javadoc = _class.javadoc();
	String body = String.format("%s suitable "
		+ "for use as a {@Code HashMap} key. Component values can be "
		+ "accessed using either field names (i.e., tuple.a, "
		+ "tuple.b, tuple.c) or accessor methods using either "
		+ "numbers (as in tuple.get1(), tuple.get2(), tuple.get3()) "
		+ "or ordinals (as in tuple.getFirst(), tuple.getSecond()). ", header);
	javadoc.add(body);

	if (config.nullable) {
	    javadoc.add("Null component values are permitted.");
	} else {
	    javadoc.add("Null component values are NOT permitted.");
	    javadoc.addThrows(NullPointerException.class).add("if any component value is null.");
	}

	if (config.comparable || config.serializable) {
	    List<String> parts = Lists.newArrayList();

	    if (config.comparable) {
		parts.add("comparable");
	    }

	    if (config.serializable) {
		parts.add("serializable");
	    }

	    String elementDescriptor = ander.join(parts);
	    List<String> capParts = Lists.newArrayList();

	    for (String part : parts) {
		capParts.add(part.substring(0, 1).toUpperCase() + part.substring(1));
	    }

	    String interfaceNames = ander.join(capParts);
	    javadoc.add(String.format(" %s instances are %s. All elements must implement %s.",
		    className, elementDescriptor, interfaceNames));
	}

	// since this %s is immutable, it is inherantly threadsafe, but you can
	// still encounter problems if you use place an object containing shared
	// mutable state in it.

	// Note: this class does not implement {@Code Iterable} because tuples
	// are not homogenous collections.

	// In addition to holding elements, %s 's provide an {@Code add} method
	// that constructs larger tuples containing all of the %'s elements plus
	// those supplied individually as arguments or those in a seperate
	// tuple.
    }

    private void buildConstructor() {
	JMethod m = _class.constructor(JMod.PRIVATE);
	JBlock body = m.body();

	for (Pair<JTypeVar, JVar> pair : typesAndVariables()) {
	    JVar param = m.param(JMod.FINAL, pair.get1(), pair.get2().name());
	    param.annotate(config.nullable ? Nullable.class : Nonnull.class);
	    if (!config.nullable) {
		body._if(pair.get2().eq(_null()))._then()._throw(JExpr._new(model.ref(NullPointerException.class)));
	    }
	    body.assign(JExpr.refthis(pair.get2().name()), param);
	}

	JDocComment comment = m.javadoc();
	comment.add(String.format("Returns a new %s containing each of the parameters.",
		className));

	if (!config.nullable) {
	    comment.addThrows(NullPointerException.class).add(
		    "if any of the supplied parameters are null.");
	}
    }

    private void buildGetters() {
	int i = 1;

	for (Pair<JTypeVar, JVar> pair : typesAndVariables()) {
	    for (String suffix : Arrays.asList(String.valueOf(i), Constants.capitalOrdinals.get(i - 1))) {
		String name = "get" + suffix;
		JMethod m = _class.method(JMod.PUBLIC | JMod.FINAL, pair.get1(), name);
		m.annotate(config.nullable ? Nullable.class : Nonnull.class);
		m.body()._return(pair.get2());
		m.javadoc().add(
			String.format("Return the %s item in this %s.", Constants.ordinals.get(i - 1),
				className));
	    }
	    i += 1;
	}
    }

    private void buildHasher() {
	JFieldVar precalc = _class.field(JMod.PRIVATE, int.class, "calculatedHash", lit(0));
	JMethod hasher = _class.method(JMod.PRIVATE | JMod.FINAL, int.class, "calculateHash");

	JBlock body = hasher.body();
	JVar result = body.decl(model._ref(int.class), "result", lit(1));

	for (JVar variable : variables) {
	    JExpression e = variable.invoke("hashCode");

	    if (config.nullable) {
		e = JOp.cond(JOp.eq(variable, _null()), lit(0), e);
	    }

	    body.assign(result, result.mul(lit(31)).plus(e));
	}

	body._return(result);

	JMethod hashCode = _class.method(JMod.PUBLIC | JMod.FINAL, int.class, "hashCode");
	hashCode.annotate(Override.class);

	JBlock hashCodeBody = hashCode.body();
	hashCodeBody._if(precalc.eq(lit(0)))._then().assign(precalc, JExpr.invoke(hasher));
	hashCode.body()._return(precalc);
    }

    private void buildEquals() {
	JMethod eq = _class.method(JMod.PUBLIC | JMod.FINAL, boolean.class, "equals");
	eq.annotate(Override.class);

	JVar other = eq.param(Object.class, "other");
	other.annotate(Nullable.class);

	JBlock body = eq.body();
	body._if(JExpr._this().eq(other))._then()._return(lit(true));
	
	JType wildTuple = _class.narrow(Collections.nCopies(config.size, model.wildcard()));
	body._if((other._instanceof(wildTuple)).not())._then()._return(lit(false));

	JVar xx = body.decl(JMod.FINAL, wildTuple, "xx", JExpr.cast(wildTuple, other));

	if (config.nullable) {
	    for (JVar variable : variables) {
		body._if(variable.eq(_null()).xor(xx.ref(variable).eq(_null())))._then()._return(
			lit(false));
		body._if(
			variable.ne(_null()).cand(xx.ref(variable).ne(_null())).cand(
				variable.invoke("equals").arg(xx.ref(variable)).not()))._then()
			._return(lit(false));
	    }

	    body._return(lit(true));
	} else {
	    JExpression result = variables.get(0).invoke("equals").arg(xx.ref(variables.get(0)));

	    for (JVar variable : variables.subList(1, variables.size())) {
		result = result.cand(variable.invoke("equals").arg(xx.ref(variable)));
	    }

	    body._return(result);
	}
    }

    private void buildComparator() {
	JMethod m = _class.method(JMod.PUBLIC | JMod.FINAL, int.class, "compareTo");
	m.annotate(Override.class);

	JVar other = m.param(JMod.FINAL, narrowedClass, "other");
	other.annotate(Nonnull.class);

	JBlock body = m.body();
	body._if(other.eq(_null()))._then()._throw(
		JExpr._new(model.ref(NullPointerException.class)));

	JVar result = body.decl(model._ref(int.class), "result", lit(0));
	final JExpression Null = _null();

	for (JVar variable : variables) {
	    JBlock parent = body;
	    
	    if (config.nullable) {
		JConditional nullTester = body._if(variable.eq(Null).cor(other.ref(variable).eq(Null)));
		JBlock oneIsNull = nullTester._then();
		JBlock bothNull = oneIsNull._if(variable.eq(Null).cand(other.ref(variable).eq(Null)).not())._then();
		bothNull._if(variable.eq(Null))._then()._return(lit(-1));
		bothNull._if(other.ref(variable).eq(Null))._then()._return(lit(1));
		parent = nullTester._else();
	    }

	    parent.assign(result, variable.invoke("compareTo").arg(other.ref(variable)));
	    parent._if(result.ne(lit(0)))._then()._return(result);
	}

	body._return(result);
    }

    private void buildSerializer() {
	JDefinedClass proxy = null;

	try {
	    proxy = _class._class(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, "SerializationProxy");
	} catch (JClassAlreadyExistsException e) {
	    throw new RuntimeException();
	}
	
	ImmutableList<JTypeVar> types = generify(proxy);
	proxy._implements(Serializable.class);

	List<JVar> fields = Lists.newArrayList();

	for (JTypeVar type : types) {
	    JVar field = proxy.field(JMod.PRIVATE | JMod.FINAL, type, type.name().toLowerCase());
	    field.annotate(SuppressWarnings.class).param("value", "unused");
	    fields.add(field);
	}

	long hash = config.hashCode();
	hash = (hash << 32) - hash;
	proxy.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, long.class, "serialVersionUID",
		lit(hash));
	_class.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, long.class, "serialVersionUID",
		lit(hash * hash));

	JMethod ctor = proxy.constructor(JMod.NONE);
	JVar ctorArg = ctor.param(JMod.FINAL, narrowedClass, "x");
	JBlock ctorBody = ctor.body();

	for (JVar field : fields) {
	    ctorBody.assign(field, ctorArg.ref(field));
	}
	
	JMethod resolver = proxy.method(JMod.PRIVATE, Object.class, "readResolve");
	JInvocation resolverReturn = JExpr._new(_class);
	for (JExpression variable: variables()) {
	    resolverReturn.arg(variable);
	}
	resolver.body()._return(resolverReturn);

	JMethod writeReplace = _class.method(JMod.PRIVATE | JMod.FINAL, Object.class,
		"writeReplace");
	writeReplace.body()._return(JExpr._new(proxy.narrow(types)).arg(JExpr._this()));

	JMethod readObject = _class.method(JMod.PRIVATE | JMod.FINAL, model.VOID, "readObject");
	readObject.param(JMod.FINAL, ObjectInputStream.class, "stream");
	readObject._throws(InvalidObjectException.class);
	readObject.body()._throw(
		JExpr._new(model.ref(InvalidObjectException.class)).arg(lit("Proxy required")));
	readObject.annotate(SuppressWarnings.class).param("value", "unused");
    }

    private void buildTupleAdder(Config argConfig) {
	// n is the number of parameters to take...
	// first, we construct a config for the result

	// then, we build a method and generify it with our special config and a
	// restricted set of type names
	Config resultConfig = config.clone(config.size + argConfig.size);
	ImmutableList<String> adderGenericTypeNames = resultConfig.types().subList(config.size,
		resultConfig.size);

	String argClassName = Constants.names.get(argConfig.size - 1);
	JClass argType = model.directClass(argClassName);
	List<JClass> argNarrower = Lists.newArrayList();

	for (String name : adderGenericTypeNames) {
	    argNarrower.add(model.directClass(name));
	}

	String resultClassName = Constants.names.get(resultConfig.size - 1);
	JClass resultType = model.directClass(resultClassName);
	List<JClass> resultNarrower = Lists.newArrayList();

	for (String name : resultConfig.types()) {
	    resultNarrower.add(model.directClass(name));
	}

	JMethod adder = _class.method(JMod.PUBLIC | JMod.FINAL, resultType.narrow(resultNarrower),
		"add");
	generify(adder, resultConfig, adderGenericTypeNames);

	JVar arg = adder.param(JMod.FINAL, argType.narrow(argNarrower), "other");
	arg.annotate(Nonnull.class);

	List<JExpression> argParts = Lists.newArrayList();

	for (String fieldName : argConfig.variables()) {
	    argParts.add(arg.ref(fieldName));
	}

	// then we construct the list of variables by combining the field
	// variables for this class with the params
	JInvocation ret = JExpr.invoke("from");

	for (JExpression variable : Iterables.concat(variables(), argParts)) {
	    ret.arg(variable);
	}

	JBlock body = adder.body();
	body._if(arg.eq(_null()))._then()._throw(JExpr._new(model.ref(NullPointerException.class)));
	body._return(ret);
	adder.annotate(Nonnull.class);

	JDocComment javadoc = adder.javadoc();
	javadoc.add(String.format("Returns a new %s containing all the elements "
		+ "in this tuple followed by the elements in the " + "supplied %s.",
		resultClassName, argClassName));
	javadoc.addThrows(NullPointerException.class).add("if the supplied argument is null.");
    }

    private void buildElementAdder(Config resultConfig) {
	// then, we build a method and generify it with our special config and a
	// restricted set of type names
	ImmutableList<String> adderGenericTypeNames = resultConfig.types().subList(config.size,
		resultConfig.size);

	String resultClassName = Constants.names.get(resultConfig.size - 1);
	JClass resultType = model.directClass(resultClassName);
	List<JClass> narrower = Lists.newArrayList();

	for (String name : resultConfig.types()) {
	    narrower.add(model.directClass(name));
	}

	JMethod adder = _class.method(JMod.PUBLIC | JMod.FINAL, resultType.narrow(narrower), "add");
	ImmutableList<JTypeVar> adderGenericTypes = generify(adder, resultConfig,
		adderGenericTypeNames);

	List<JVar> params = Lists.newArrayList();

	for (JTypeVar type : adderGenericTypes) {
	    JVar param = adder.param(JMod.FINAL, type, type.name().toLowerCase());
	    params.add(param);
	}

	// then we construct the list of variables by combining the field
	// variables for this class with the params
	JInvocation ret = JExpr.invoke("from");

	for (JVar variable : Iterables.concat(variables(), params)) {
	    ret.arg(variable);
	}

	adder.body()._return(ret);
	adder.annotate(Nonnull.class);

	JDocComment javadoc = adder.javadoc();
	javadoc.add(String.format("Returns a new %s containing all the elements "
		+ "in this tuple followed by the supplied arguments.", resultClassName));

	if (!config.nullable) {
	    javadoc.addThrows(NullPointerException.class).add(
		    "if any of the supplied arguments are null.");
	}
    }

    private void buildZippers() {
	JDefinedClass iterable = null;
	JDefinedClass iterator = null;

	try {
	    iterable = _class._class(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, "Zipper");
	    iterator = _class._class(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, "ZipperIterator");
	} catch (JClassAlreadyExistsException e) {
	    throw new RuntimeException();
	}

	JClass iterableType = model.ref(Iterable.class).narrow(narrowedClass);
	JClass iteratorType = model.ref(Iterator.class).narrow(narrowedClass);
	ImmutableList<JTypeVar> iterableTypes = generify(iterable);
	ImmutableList<JTypeVar> iteratorTypes = generify(iterator);
	iterable._implements(iterableType);
	iterator._implements(iteratorType);

	JVar iteratorField = iterable.field(JMod.PRIVATE | JMod.FINAL, iteratorType, "iterator");
	JMethod iterableCtor = iterable.constructor(JMod.PUBLIC);
	List<JVar> ctorArguments = Lists.newArrayList();

	for (JTypeVar type : iterableTypes) {
	    ctorArguments.add(iterableCtor.param(JMod.FINAL,
		    model.ref(Iterator.class).narrow(type), type.name().toLowerCase()));
	}

	JInvocation maker = JExpr._new(iterator.narrow(genericTypes));

	for (JVar arg : ctorArguments) {
	    maker.arg(arg);
	}

	iterableCtor.body().assign(iteratorField, maker);
	JMethod iterableIterator = iterable.method(JMod.PUBLIC, iteratorType,
		"iterator");
	iterableIterator.body()._return(iteratorField);
	iterableIterator.annotate(Override.class);

	// ZipperIterator fields...
	List<JVar> iteratorFields = Lists.newArrayList();

	for (JTypeVar type : iteratorTypes) {
	    iteratorFields.add(iterator.field(JMod.PRIVATE | JMod.FINAL, model.ref(Iterator.class)
		    .narrow(type), type.name().toLowerCase()));
	}

	// ZipperIterator ctor...
	JMethod iteratorCtor = iterator.constructor(JMod.PUBLIC);
	List<JVar> iteratorCtorArgs = Lists.newArrayList();

	for (JVar field : iteratorFields) {
	    iteratorCtorArgs.add(iteratorCtor.param(JMod.FINAL, field.type(), field.name()));
	}

	for (int i = 0; i < iteratorFields.size(); i++) {
	    iteratorCtor.body().assign(JExpr.refthis(iteratorFields.get(i).name()),
		    iteratorCtorArgs.get(i));
	}

	// hasNext implementation
	JMethod hasNext = iterator.method(JMod.PUBLIC, boolean.class, "hasNext");
	hasNext.annotate(Override.class);
	JExpression hasNextReturnValue = iteratorFields.get(0).invoke("hasNext");

	for (JVar field : iteratorFields.subList(1, iteratorFields.size())) {
	    hasNextReturnValue = hasNextReturnValue.cand(field.invoke("hasNext"));
	}

	hasNext.body()._return(hasNextReturnValue);

	// next implementation
	JMethod next = iterator.method(JMod.PUBLIC, narrowedClass, "next");
	next.annotate(Override.class);
	JInvocation builder = JExpr._new(narrowedClass);

	for (JVar field : iteratorFields) {
	    builder.arg(field.invoke("next"));
	}

	next.body()._return(builder);

	// remove implementation
	JMethod remove = iterator.method(JMod.PUBLIC, model.VOID, "remove");
	remove.annotate(Override.class);
	remove.body()._throw(JExpr._new(model.ref(UnsupportedOperationException.class)));

	JMethod iterableMaker = _class.method(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, iterableType,
		"zip");
	generify(iterableMaker);
	iterableMaker.annotate(Nonnull.class);

	List<JVar> iterableMakerArgs = Lists.newArrayList();

	for (JTypeVar type : iterableTypes) {
	    iterableMakerArgs.add(iterableMaker.param(JMod.FINAL, model.ref(Iterable.class).narrow(
		    type), type.name().toLowerCase()));
	}

	JInvocation makerValue = JExpr._new(iterable.narrow(iterableTypes));

	for (JVar arg : iterableMakerArgs) {
	    makerValue.arg(arg.invoke("iterator"));
	}

	iterableMaker.body()._return(makerValue);

	JMethod iteratorMaker = _class.method(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, iteratorType,
		"zip");
	generify(iteratorMaker);
	iteratorMaker.annotate(Nonnull.class);

	List<JVar> iteratorMakerArgs = Lists.newArrayList();

	for (JTypeVar type : iteratorTypes) {
	    iteratorMakerArgs.add(iteratorMaker.param(JMod.FINAL, model.ref(Iterator.class).narrow(
		    type), type.name().toLowerCase()));
	}

	makerValue = JExpr._new(iterator.narrow(iteratorTypes));

	for (JVar arg : iteratorMakerArgs) {
	    makerValue.arg(arg);
	}

	iteratorMaker.body()._return(makerValue);
    }

    private void buildStringers() {
	JMethod complex = _class.method(JMod.FINAL | JMod.PUBLIC, String.class, "toString");
	JVar opener = complex.param(JMod.FINAL, String.class, "opener");
	JVar sep = complex.param(JMod.FINAL, String.class, "seperator");
	JVar closer = complex.param(JMod.FINAL, String.class, "closer");
	JBlock body = complex.body();
	JVar sb = body.decl(JMod.FINAL, model._ref(StringBuilder.class), "sb", JExpr._new(model
		.ref(StringBuilder.class)));
	body.invoke(sb, "append").arg(opener);

	int variableCount = 0;
	boolean isLast = false;
	JExpression stringVal = null;

	for (JVar variable : variables) {
	    if (config.nullable) {
		stringVal = JOp.cond(variable.eq(_null()), lit(""), variable.invoke("toString"));
	    } else {
		stringVal = variable.invoke("toString");
	    }

	    body.invoke(sb, "append").arg(stringVal);

	    if (!isLast) {
		body.invoke(sb, "append").arg(sep);
	    }

	    variableCount += 1;
	    isLast = (variableCount == (variables.size() - 1));
	}

	body.invoke(sb, "append").arg(closer);
	body._return(sb.invoke("toString"));
	complex.annotate(Nonnull.class);

	JMethod simple = _class.method(JMod.FINAL | JMod.PUBLIC, String.class, "toString");
	simple.body()._return(JExpr.invoke(complex).arg(lit("(")).arg(lit(", ")).arg(lit(")")));
	simple.annotate(Override.class);
	simple.annotate(Nonnull.class);
    }

    public Iterable<Pair<JTypeVar, JVar>> typesAndVariables() {
	return Pair.zip(genericTypes, variables);
    }

    public ImmutableList<JTypeVar> types() {
	return genericTypes;
    }

    public ImmutableList<JVar> variables() {
	return variables;
    }

    public JType narrow() {
	return narrowedClass;
    }

    public JDefinedClass getDefinedClass() {
	return _class;
    }

    public String getName() {
	return className;
    }

    public ImmutableList<JTypeVar> generify(JGenerifiable element, Config elementConfig,
	    ImmutableList<String> types) {
	ImmutableList.Builder<JTypeVar> builder = ImmutableList.builder();

	for (String typeName : types) {
	    JTypeVar v = element.generify(typeName);

	    if (elementConfig.comparable) {
		v.bound(model.ref(Comparable.class).narrow(v.wildcard(false)));
	    }

	    if (elementConfig.serializable) {
		v.bound(model.ref(Serializable.class));
	    }

	    builder.add(v);
	}

	return builder.build();
    }

    public ImmutableList<JTypeVar> generify(JGenerifiable element) {
	return generify(element, config, config.types());
    }

    public List<JVar> buildVariables(JMethod m, JDefinedClass c) {
	Preconditions.checkArgument((m == null) ^ (c == null));

	List<String> names = config.variables();
	int i = 0;
	List<JVar> variables = Lists.newArrayList();

	for (JTypeVar genericType : types()) {
	    String name = names.get(i);
	    JVar item = null;

	    if (m != null) {
		item = m.param(JMod.FINAL, genericType, name);
	    } else {
		item = c.field(JMod.FINAL | JMod.PUBLIC | JMod.TRANSIENT, genericType, name);
	    }

	    variables.add(item);
	    i += 1;
	}

	return variables;
    }
}
