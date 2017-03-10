/**
 * Copyright (c) 2014 committers of YAKINDU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * 	committers of YAKINDU - initial API and implementation
 * 
 */
package org.yakindu.base.expressions.inferrer;

import static org.yakindu.base.types.typesystem.ITypeSystem.ANY;
import static org.yakindu.base.types.typesystem.ITypeSystem.BOOLEAN;
import static org.yakindu.base.types.typesystem.ITypeSystem.INTEGER;
import static org.yakindu.base.types.typesystem.ITypeSystem.NULL;
import static org.yakindu.base.types.typesystem.ITypeSystem.REAL;
import static org.yakindu.base.types.typesystem.ITypeSystem.STRING;
import static org.yakindu.base.types.typesystem.ITypeSystem.VOID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.yakindu.base.expressions.expressions.ArgumentExpression;
import org.yakindu.base.expressions.expressions.AssignmentExpression;
import org.yakindu.base.expressions.expressions.BitwiseAndExpression;
import org.yakindu.base.expressions.expressions.BitwiseOrExpression;
import org.yakindu.base.expressions.expressions.BitwiseXorExpression;
import org.yakindu.base.expressions.expressions.BoolLiteral;
import org.yakindu.base.expressions.expressions.ConditionalExpression;
import org.yakindu.base.expressions.expressions.DoubleLiteral;
import org.yakindu.base.expressions.expressions.ElementReferenceExpression;
import org.yakindu.base.expressions.expressions.Expression;
import org.yakindu.base.expressions.expressions.FeatureCall;
import org.yakindu.base.expressions.expressions.FloatLiteral;
import org.yakindu.base.expressions.expressions.HexLiteral;
import org.yakindu.base.expressions.expressions.IntLiteral;
import org.yakindu.base.expressions.expressions.LogicalAndExpression;
import org.yakindu.base.expressions.expressions.LogicalNotExpression;
import org.yakindu.base.expressions.expressions.LogicalOrExpression;
import org.yakindu.base.expressions.expressions.LogicalRelationExpression;
import org.yakindu.base.expressions.expressions.NullLiteral;
import org.yakindu.base.expressions.expressions.NumericalAddSubtractExpression;
import org.yakindu.base.expressions.expressions.NumericalMultiplyDivideExpression;
import org.yakindu.base.expressions.expressions.NumericalUnaryExpression;
import org.yakindu.base.expressions.expressions.ParenthesizedExpression;
import org.yakindu.base.expressions.expressions.PrimitiveValueExpression;
import org.yakindu.base.expressions.expressions.ShiftExpression;
import org.yakindu.base.expressions.expressions.StringLiteral;
import org.yakindu.base.expressions.expressions.TypeCastExpression;
import org.yakindu.base.expressions.expressions.UnaryOperator;
import org.yakindu.base.expressions.inferrer.TypeParameterResolver.TypeInferrenceException;
import org.yakindu.base.types.EnumerationType;
import org.yakindu.base.types.Enumerator;
import org.yakindu.base.types.GenericElement;
import org.yakindu.base.types.Operation;
import org.yakindu.base.types.Parameter;
import org.yakindu.base.types.Property;
import org.yakindu.base.types.Type;
import org.yakindu.base.types.TypeAlias;
import org.yakindu.base.types.TypeParameter;
import org.yakindu.base.types.TypeSpecifier;
import org.yakindu.base.types.inferrer.AbstractTypeSystemInferrer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * @author andreas muelder - Initial contribution and API
 * 
 */
public class ExpressionsTypeInferrer extends AbstractTypeSystemInferrer implements ExpressionsTypeInferrerMessages {
	@Inject
	private TypeParameterResolver typeParameterResolver;

	public InferenceResult doInfer(AssignmentExpression e) {
		InferenceResult result1 = inferTypeDispatch(e.getVarRef());
		InferenceResult result2 = inferTypeDispatch(e.getExpression());
		assertAssignable(result1, result2, String.format(ASSIGNMENT_OPERATOR, e.getOperator(), result1, result2));
		return inferTypeDispatch(e.getVarRef());
	}

	public InferenceResult doInfer(ConditionalExpression e) {
		InferenceResult result1 = inferTypeDispatch(e.getTrueCase());
		InferenceResult result2 = inferTypeDispatch(e.getFalseCase());
		assertCompatible(result1, result2, String.format(COMMON_TYPE, result1, result2));
		assertIsSubType(inferTypeDispatch(e.getCondition()), getResultFor(BOOLEAN), CONDITIONAL_BOOLEAN);
		return getCommonType(result1, result2);
	}

	public InferenceResult doInfer(LogicalOrExpression e) {
		InferenceResult result1 = inferTypeDispatch(e.getLeftOperand());
		InferenceResult result2 = inferTypeDispatch(e.getRightOperand());
		assertIsSubType(result1, getResultFor(BOOLEAN), String.format(LOGICAL_OPERATORS, "||", result1, result2));
		assertIsSubType(result2, getResultFor(BOOLEAN), String.format(LOGICAL_OPERATORS, "||", result1, result2));
		return getResultFor(BOOLEAN);
	}

	public InferenceResult doInfer(LogicalAndExpression e) {
		InferenceResult result1 = inferTypeDispatch(e.getLeftOperand());
		InferenceResult result2 = inferTypeDispatch(e.getRightOperand());
		assertIsSubType(result1, getResultFor(BOOLEAN), String.format(LOGICAL_OPERATORS, "&&", result1, result2));
		assertIsSubType(result2, getResultFor(BOOLEAN), String.format(LOGICAL_OPERATORS, "&&", result1, result2));
		return getResultFor(BOOLEAN);
	}

	public InferenceResult doInfer(LogicalNotExpression e) {
		InferenceResult type = inferTypeDispatch(e.getOperand());
		assertIsSubType(type, getResultFor(BOOLEAN), String.format(LOGICAL_OPERATOR, "!", type));
		return getResultFor(BOOLEAN);
	}

	public InferenceResult doInfer(BitwiseXorExpression e) {
		InferenceResult result1 = inferTypeDispatch(e.getLeftOperand());
		InferenceResult result2 = inferTypeDispatch(e.getRightOperand());
		assertIsSubType(result1, getResultFor(INTEGER), String.format(BITWISE_OPERATORS, "^", result1, result2));
		assertIsSubType(result2, getResultFor(INTEGER), String.format(BITWISE_OPERATORS, "^", result1, result2));
		return getResultFor(INTEGER);
	}

	public InferenceResult doInfer(BitwiseOrExpression e) {
		InferenceResult result1 = inferTypeDispatch(e.getLeftOperand());
		InferenceResult result2 = inferTypeDispatch(e.getRightOperand());
		assertIsSubType(result1, getResultFor(INTEGER), String.format(BITWISE_OPERATORS, "|", result1, result2));
		assertIsSubType(result2, getResultFor(INTEGER), String.format(BITWISE_OPERATORS, "|", result1, result2));
		return getResultFor(INTEGER);
	}

	public InferenceResult doInfer(BitwiseAndExpression e) {
		InferenceResult result1 = inferTypeDispatch(e.getLeftOperand());
		InferenceResult result2 = inferTypeDispatch(e.getRightOperand());
		assertIsSubType(result1, getResultFor(INTEGER), String.format(BITWISE_OPERATORS, "&", result1, result2));
		assertIsSubType(result2, getResultFor(INTEGER), String.format(BITWISE_OPERATORS, "&", result1, result2));
		return getResultFor(INTEGER);
	}

	public InferenceResult doInfer(ShiftExpression e) {
		InferenceResult result1 = inferTypeDispatch(e.getLeftOperand());
		InferenceResult result2 = inferTypeDispatch(e.getRightOperand());
		assertIsSubType(result1, getResultFor(INTEGER),
				String.format(BITWISE_OPERATORS, e.getOperator(), result1, result2));
		assertIsSubType(result2, getResultFor(INTEGER),
				String.format(BITWISE_OPERATORS, e.getOperator(), result1, result2));
		return getResultFor(INTEGER);
	}

	public InferenceResult doInfer(LogicalRelationExpression e) {
		InferenceResult result1 = inferTypeDispatch(e.getLeftOperand());
		InferenceResult result2 = inferTypeDispatch(e.getRightOperand());
		assertCompatible(result1, result2, String.format(COMPARSION_OPERATOR, e.getOperator(), result1, result2));
		InferenceResult result = getResultFor(BOOLEAN);
		return result;
	}

	public InferenceResult doInfer(NumericalAddSubtractExpression e) {
		InferenceResult result1 = inferTypeDispatch(e.getLeftOperand());
		InferenceResult result2 = inferTypeDispatch(e.getRightOperand());
		assertCompatible(result1, result2, String.format(ARITHMETIC_OPERATORS, e.getOperator(), result1, result2));
		assertIsSubType(result1, getResultFor(REAL),
				String.format(ARITHMETIC_OPERATORS, e.getOperator(), result1, result2));
		return getCommonType(inferTypeDispatch(e.getLeftOperand()), inferTypeDispatch(e.getRightOperand()));
	}

	public InferenceResult doInfer(NumericalMultiplyDivideExpression e) {
		InferenceResult result1 = inferTypeDispatch(e.getLeftOperand());
		InferenceResult result2 = inferTypeDispatch(e.getRightOperand());
		assertCompatible(result1, result2, String.format(ARITHMETIC_OPERATORS, e.getOperator(), result1, result2));
		assertIsSubType(result1, getResultFor(REAL),
				String.format(ARITHMETIC_OPERATORS, e.getOperator(), result1, result2));
		return getCommonType(result1, result2);
	}

	public InferenceResult doInfer(NumericalUnaryExpression e) {
		InferenceResult result1 = inferTypeDispatch(e.getOperand());
		if (e.getOperator() == UnaryOperator.COMPLEMENT)
			assertIsSubType(result1, getResultFor(INTEGER), String.format(BITWISE_OPERATOR, '~', result1));
		else {
			assertIsSubType(result1, getResultFor(REAL), String.format(ARITHMETIC_OPERATOR, e.getOperator(), result1));
		}
		return result1;
	}

	public InferenceResult doInfer(TypeCastExpression e) {
		InferenceResult result1 = inferTypeDispatch(e.getOperand());
		InferenceResult result2 = inferTypeDispatch(e.getType());
		assertCompatible(result1, result2, String.format(CAST_OPERATORS, result1, result2));
		return inferTypeDispatch(e.getType());
	}

	public InferenceResult doInfer(EnumerationType enumType) {
		return InferenceResult.from(enumType);
	}

	public InferenceResult doInfer(Enumerator enumerator) {
		return InferenceResult.from(EcoreUtil2.getContainerOfType(enumerator, Type.class));
	}

	public InferenceResult doInfer(Type type) {
		return InferenceResult.from(type.getOriginType());
	}

	/**
	 * The type of a type alias is its (recursively inferred) base type, i.e.
	 * type aliases are assignable if their inferred base types are assignable.
	 */
	public InferenceResult doInfer(TypeAlias typeAlias) {
		return inferTypeDispatch(typeAlias.getTypeSpecifier());
	}

	public InferenceResult doInfer(FeatureCall e) {
		// create type map
		Map<TypeParameter, InferenceResult> typeParameterMapping = Maps.newHashMap();
		// resolve type parameters from owner
		typeParameterResolver.resolveTypeParametersFromOwner(typeParameterMapping, inferTypeDispatch(e.getOwner()));
		
		if (e.isOperationCall()) {
			return inferOperation(typeParameterMapping, e, (Operation)e.getFeature(), e.getOwner());
		}
		InferenceResult result = inferTypeDispatch(e.getFeature());
		if (result != null && result.getType() instanceof TypeParameter) {
			result = typeParameterResolver.inferTypeParameter((TypeParameter) result.getType(),
					inferTypeDispatch(e.getOwner()));
		}
		return result;
	}

	public InferenceResult doInfer(ElementReferenceExpression e) {
		// create type map
		Map<TypeParameter, InferenceResult> typeParameterMapping = Maps.newHashMap();
		if (e.isOperationCall()) {
			return inferOperation(typeParameterMapping, e, (Operation) e.getReference(), null);
		}
		return inferTypeDispatch(e.getReference());
	}

	private InferenceResult inferOperation(Map<TypeParameter, InferenceResult> typeParameterMapping, ArgumentExpression e, Operation op, Expression operationOwner) {
		// resolve type parameter from operation call
		List<InferenceResult> argumentTypes = getArgumentTypes(e.getArgs());
		List<Parameter> parameters = op.getParameters();
		try {
			typeParameterResolver.resolveTypeParametersFromOperationArguments(typeParameterMapping, argumentTypes, parameters);
		} catch (TypeInferrenceException ex) {
			error(ex.getMessage(), NOT_COMPATIBLE_CODE);
		}
		validateParameters(typeParameterMapping, op, e.getArgs());
		return inferReturnType(op, typeParameterMapping);
	}
	
	private List<InferenceResult> getArgumentTypes(EList<Expression> args) {
		List<InferenceResult> argumentTypes = new ArrayList<>();
		for (Expression arg : args) {
			argumentTypes.add(inferTypeDispatch(arg));
		}
		return argumentTypes;
	}

//	public InferenceResult inferOperation(Operation op, ArgumentExpression e) {
//		EList<Expression> args = e.getArgs();
//
//		Expression operationOwner = null;
//		if (e instanceof FeatureCall) {
//			operationOwner = ((FeatureCall) e).getOwner();
//		}
//
//		Map<TypeParameter, InferenceResult> typeParameterMapping = inferParameters(op, args, operationOwner);
//		return inferReturnType(op, typeParameterMapping);
//	}

	protected InferenceResult inferReturnType(Operation operation,
			Map<TypeParameter, InferenceResult> typeParameterMapping) {
		if (operation.getType() instanceof TypeParameter || operation.getType() instanceof GenericElement) {
			try {
				return typeParameterResolver.inferType(operation.getTypeSpecifier(), typeParameterMapping);
			} catch (TypeInferrenceException e) {
				error(e.getMessage(), NOT_COMPATIBLE_CODE);
				return InferenceResult.from(registry.getType(ANY));
			}

		}
		return inferTypeDispatch(operation);
	}
	
//	protected InferenceResult inferFeatureType(EObject feature,
//			Map<TypeParameter, InferenceResult> typeParameterMapping) {
//		InferenceResult featureType = inferTypeDispatch(feature);
//		
//		if (featureType.getType() instanceof TypeParameter || featureType.getType() instanceof GenericElement) {
//			try {
//				return typeParameterResolver.inferType(operation.getTypeSpecifier(), typeParameterMapping);
//			} catch (TypeInferrenceException e) {
//				error(e.getMessage(), NOT_COMPATIBLE_CODE);
//				return InferenceResult.from(registry.getType(ANY));
//			}
//
//		}
//		return featureType;
//	}

//	protected Map<TypeParameter, InferenceResult> inferParameters(Operation operation, EList<Expression> args,
//			Expression operationOwner) {
//		Map<TypeParameter, InferenceResult> typeParameterMapping = Maps.newHashMap();
//		// Get type parameters from FeatureCall owner, if available
//		if (operationOwner != null) {
//			InferenceResult operationOwnerResult = inferTypeDispatch(operationOwner);
//			typeParameterResolver.buildOwnerTypeParameterBinding(typeParameterMapping, operationOwnerResult);
//		}
//		EList<Parameter> parameters = operation.getParameters();
//		if (parameters.size() <= args.size()) {
//			for (int i = 0; i < parameters.size(); i++) {
//				Parameter parameter = parameters.get(i);
//				Expression argument = args.get(i);
//				if (parameter.getType() instanceof TypeParameter || parameter.getType() instanceof GenericElement) {
//					try {
//						typeParameterResolver.buildTypeParameterMapping(typeParameterMapping,
//								parameter.getTypeSpecifier(), inferTypeDispatch(argument));
//					} catch (TypeInferrenceException e) {
//						error(e.getMessage(), NOT_COMPATIBLE_CODE);
//					}
//					if (!typeParameterMapping.isEmpty()) {
//						continue;
//					}
//				}
//				assertArgumentIsCompatible(operationOwner, parameter, argument);
//			}
//		}
//		if (operation.isVariadic() && args.size() - 1 >= operation.getVarArgIndex()) {
//			Parameter parameter = operation.getParameters().get(operation.getVarArgIndex());
//			List<Expression> varArgs = args.subList(operation.getVarArgIndex(), args.size() - 1);
//			for (Expression expression : varArgs) {
//				assertArgumentIsCompatible(operationOwner, parameter, expression);
//			}
//		}
//		return typeParameterMapping;
//	}

	protected Map<TypeParameter, InferenceResult> validateParameters(
			Map<TypeParameter, InferenceResult> typeParameterMapping, Operation operation, List<Expression> args) {
		List<Parameter> parameters = operation.getParameters();
		if (parameters.size() <= args.size()) {
			for (int i = 0; i < parameters.size(); i++) {
				Parameter parameter = parameters.get(i);
				Expression argument = args.get(i);
				if (parameter.getType() instanceof TypeParameter) {
					InferenceResult resolvedParameterType = typeParameterMapping.get(parameter.getType());
					/*if(resolvedParameterType == null) {
						error(String.format(INFER_TYPE, args.get(i)), NOT_COMPATIBLE_CODE);
					} else {*/
						InferenceResult argumentType = inferTypeDispatch(argument);
						assertCompatible(argumentType, resolvedParameterType, String.format(INCOMPATIBLE_TYPES, argumentType, resolvedParameterType));
//					}
				} else {
					assertArgumentIsCompatible(parameter, argument);
					
				}
			}
		}
		if (operation.isVariadic() && args.size() - 1 >= operation.getVarArgIndex()) {
			Parameter parameter = operation.getParameters().get(operation.getVarArgIndex());
			List<Expression> varArgs = args.subList(operation.getVarArgIndex(), args.size() - 1);
			for (Expression expression : varArgs) {
				// TODO: handle op(T...)
				assertArgumentIsCompatible(parameter, expression);
			}
		}
		return typeParameterMapping;
	}

	protected void buildTypeParameterMapping(Map<TypeParameter, InferenceResult> typeParameterMapping,
			Parameter parameter, Expression argument) {

		InferenceResult argumentType = inferTypeDispatch(argument);
		TypeSpecifier typeSpecifier = parameter.getTypeSpecifier();
		try {
			typeParameterResolver.buildTypeParameterMapping(typeParameterMapping, typeSpecifier, argumentType);
		} catch (TypeInferrenceException e) {
			error(e.getMessage(), NOT_COMPATIBLE_CODE);
		}
	}

	protected void assertArgumentIsCompatible(Parameter parameter, Expression argument) {
		InferenceResult result1 = inferTypeDispatch(parameter);
		InferenceResult result2 = inferTypeDispatch(argument);
		assertCompatible(result2, result1, String.format(INCOMPATIBLE_TYPES, result2, result1));
	}

	public InferenceResult doInfer(ParenthesizedExpression e) {
		return inferTypeDispatch(e.getExpression());
	}

	public InferenceResult doInfer(PrimitiveValueExpression e) {
		return inferTypeDispatch(e.getValue());
	}

	public InferenceResult doInfer(BoolLiteral literal) {
		return getResultFor(BOOLEAN);
	}

	public InferenceResult doInfer(IntLiteral literal) {
		return getResultFor(INTEGER);
	}

	public InferenceResult doInfer(HexLiteral literal) {
		return getResultFor(INTEGER);
	}

	public InferenceResult doInfer(DoubleLiteral literal) {
		return getResultFor(REAL);
	}

	public InferenceResult doInfer(FloatLiteral literal) {
		return getResultFor(REAL);
	}

	public InferenceResult doInfer(StringLiteral literal) {
		return getResultFor(STRING);
	}

	public InferenceResult doInfer(NullLiteral literal) {
		return getResultFor(NULL);
	}

	public InferenceResult doInfer(Property p) {
		InferenceResult type = inferTypeDispatch(p.getTypeSpecifier());
		assertNotType(type, VARIABLE_VOID_TYPE, getResultFor(VOID));
		return type;
	}

	public InferenceResult doInfer(Operation e) {
		return e.getTypeSpecifier() == null ? getResultFor(VOID) : inferTypeDispatch(e.getTypeSpecifier());
	}

	public InferenceResult doInfer(Parameter e) {
		return inferTypeDispatch(e.getTypeSpecifier());
	}

	public InferenceResult doInfer(TypeSpecifier specifier) {
		if (specifier.getType() instanceof GenericElement
				&& ((GenericElement) specifier.getType()).getTypeParameters().size() > 0) {
			List<InferenceResult> bindings = new ArrayList<>();
			EList<TypeSpecifier> arguments = specifier.getTypeArguments();
			for (TypeSpecifier typeSpecifier : arguments) {
				InferenceResult binding = inferTypeDispatch(typeSpecifier);
				if (binding != null) {
					bindings.add(binding);
				}
			}
			Type type = inferTypeDispatch(specifier.getType()).getType();
			return InferenceResult.from(type, bindings);
		}
		return inferTypeDispatch(specifier.getType());

	}
}
