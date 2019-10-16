/* (c) https://github.com/MontiCore/monticore */
package de.monticore.types.check;

import de.monticore.types.typesymbols._symboltable.TypeSymbol;
import de.monticore.types.typesymbols._symboltable.TypeSymbolsScope;
import de.monticore.types.typesymbols._symboltable.TypeVarSymbol;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * SymTypeExpressionFactory contains static functions that create
 * the various forms of TypeExpressions used for Sym-Types.
 *
 * This factory therefore should be the only source to create SymTypeExpressions.
 * No other source is needed.
 * (That is ok, as the set of SymTypeExpressions is rather fixed and we do not expect
 * many modular extensions that would be needed. Saying this, we know that function types and
 * potentially also union types (A|B) might still be added in the future.)
 */
public class SymTypeExpressionFactory {
  
  /**
   * createTypeVariable vor Variables
   */
  public static SymTypeVariable createTypeVariable(String name) {
    SymTypeVariable o = new SymTypeVariable(name);
    return o;
  }

  public static SymTypeVariable createTypeVariable(String name, TypeVarSymbol typeVarSymbol){
    SymTypeVariable o = new SymTypeVariable(name,typeVarSymbol);
    return o;
  }

  public static SymTypeVariable createTypeVariable(TypeVarSymbol typeVarSymbol){
    return createTypeVariable(typeVarSymbol.getName(),typeVarSymbol);
  }
  
  /**
   * for constants, such as "int"
   */
  public static SymTypeConstant createTypeConstant(String name) {
    SymTypeConstant o = new SymTypeConstant(name);
    return o;
  }
  
  /**
   * for ObjectTypes, as e.g. "Person"
   * @param name  Name of the type
   * @param objTypeSymbol  TODO 4: is this needed?
   * @return
   */
  public static SymTypeOfObject createTypeObject(String name, TypeSymbol objTypeSymbol) {
    SymTypeOfObject o = new SymTypeOfObject(name,objTypeSymbol);
    return o;
  }

  /**
   * for ObjectTypes, as e.g. "Person"
   * @param name  Name of the type
   * @return
   */
  public static SymTypeOfObject createTypeObject(String name) {
    SymTypeOfObject o = new SymTypeOfObject(name);
    o.setObjName(name);
    return o;
  }

  /**
   * creates the "Void"-type, i.e. a pseudotype that represents the absence of a real type
   * @return
   */
  public static SymTypeVoid createTypeVoid() {
    SymTypeVoid o = new SymTypeVoid();
    return o;
  }
  
  /**
   * That is the pseudo-type of "null"
   */
  public static SymTypeOfNull createTypeOfNull() {
    SymTypeOfNull o = new SymTypeOfNull();
    return o;
  }
  
  /**
   * creates an array-Type Expression
   * @param dim   the dimension of the array
   * @param argument the argument type (of the elements)
   * @return
   */
  public static SymTypeArray createTypeArray(int dim, SymTypeExpression argument) {
    SymTypeArray o = new SymTypeArray(dim, argument);
    return o;
  }

  /**
   * creates a TypeExpression for primitives, such as "int", for "null", "void" and
   * also for object types, such as "Person" from a given symbol
   * @param type
   * @return
   */
  public static SymTypeExpression createTypeExpression(TypeSymbol type){
    List<String> primitiveTypes = Arrays
        .asList("boolean", "byte", "char", "short", "int", "long", "float", "double");

    SymTypeExpression o;

    if (primitiveTypes.contains(type.getName())) {
      o = createTypeConstant(type.getName());
    } else if("void".equals(type.getName())){
      o = createTypeVoid();
    }else if("null".equals(type.getName())) {
      o = createTypeOfNull();
    }else {
      o = createTypeObject(type.getName());
    }
    return o;
  }
  
  
  // -------------------------------------------------------- GenericTypeExpression
  
  /**
   * createGenerics: for a generic Type
   * @param name    name of the Generic, such as "Map"
   * @param arguments   the SymTypes for the arguments
   * @param objTypeConstructorSymbol  and the symbol-Object the generic type is linked to
   * @return
   */
  public static SymTypeOfGenerics createGenerics(String name, List<SymTypeExpression> arguments,
                                                 TypeSymbol objTypeConstructorSymbol){
    SymTypeOfGenerics o = new SymTypeOfGenerics(name, arguments, objTypeConstructorSymbol);
    return o;
  }
  
  /**
   * createGenerics: is created using the enclosing Scope to ask for the appropriate symbol.
   * @param name    name of the Generic, such as "Map"
   * @param arguments   the SymTypes for the arguments
   * @param enclosingScope  used to derive the Symbol
   */
  public static SymTypeOfGenerics createGenerics(String name, List<SymTypeExpression> arguments,
                                                 TypeSymbolsScope enclosingScope){
    Optional<TypeSymbol> objTypeConstructorSymbol = enclosingScope.resolveType(name);
    // No check, whether the symbol actually exists: Exception may be thrown
    SymTypeOfGenerics o = new SymTypeOfGenerics(name, arguments, objTypeConstructorSymbol.get());
    return o;
  }

  @Deprecated // TODO: delete, because TypeSymbol is not set
  // aktuell nur noch benutzt von DeriveSymTypeOfMCType
  public static SymTypeOfGenerics createGenerics(String fullName, List<SymTypeExpression> arguments){
    SymTypeOfGenerics o = new SymTypeOfGenerics(fullName, arguments);
    // XXX BR: here we also have to add the Symbol
    // being retrieved from somewhere ...
    return o;
  }

}
