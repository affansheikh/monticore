package de.monticore.types.typesymbols._symboltable;

import com.google.common.collect.Lists;
import de.monticore.types.check.SymTypeExpression;

import java.util.ArrayList;
import java.util.List;

public class TypeSymbol extends TypeSymbolTOP {

  public TypeSymbol(String name) {
    super(name);
  }

  /**
   * returns a clone of this
   */
  public TypeSymbol deepClone() {
    TypeSymbol clone = new TypeSymbol(name);
    clone.setEnclosingScope(this.getEnclosingScope());
    clone.setFullName(this.getFullName());
    clone.setAccessModifier(this.getAccessModifier());
    clone.setSpannedScope(this.getSpannedScope());
    if (isPresentAstNode()) {
      clone.setAstNode(this.getAstNode());
    }
    List<MethodSymbol> methods = new ArrayList<>();
    for (MethodSymbol method : this.getMethodList()) {
      methods.add(method.deepClone());
    }
    clone.setMethodList(methods);

    List<FieldSymbol> fields = new ArrayList<>();
    for (FieldSymbol field : this.getFieldList()) {
      fields.add(field.deepClone());
    }
    clone.setFieldList(fields);

    List<SymTypeExpression> superTypes = new ArrayList<>();
    for (SymTypeExpression superType : this.getSuperTypeList()) {
      superTypes.add(superType.deepClone());
    }
    clone.setSuperTypeList(superTypes);

    List<TypeVarSymbol> typeParameters = new ArrayList<>();
    for (TypeVarSymbol typeParameter : this.getTypeParameterList()) {
      typeParameters.add(typeParameter.deepClone());
    }
    clone.setTypeParameterList(typeParameters);

    return clone;
  }

  /**
   * get a list of all the methods the type definition can access
   */
  @Override
  public List<MethodSymbol> getMethodList() {
    if (spannedScope == null || spannedScope.getMethodSymbols() == null || spannedScope.getMethodSymbols().isEmpty()) {
      return Lists.newArrayList();
    }
    return spannedScope.getMethodSymbols().values();
  }

  /**
   * search in the scope for methods with a specific name
   */
  public List<MethodSymbol> getMethodList(String methodname) {
    return spannedScope.resolveMethodMany(methodname);
  }

  /**
   * get a list of all the fields the type definition can access
   */
  @Override
  public List<FieldSymbol> getFieldList() {
    if (spannedScope == null || spannedScope.getFieldSymbols() == null || spannedScope.getFieldSymbols().isEmpty()) {
      return Lists.newArrayList();
    }
    return spannedScope.getFieldSymbols().values();
  }

  /**
   * search in the scope for methods with a specific name
   */
  public List<FieldSymbol> getFieldList(String fieldname) {
    return spannedScope.resolveFieldMany(fieldname);
  }

}
