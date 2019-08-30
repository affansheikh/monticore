/* (c) https://github.com/MontiCore/monticore */
package de.monticore.typescalculator;

import java.util.Arrays;
import java.util.List;

public class TypeConstant extends TypeExpression {

  /**
   * List of potential constants
   * (on purpose not implemented as enum)
   */
   public static List<String> primitiveTypes = Arrays
            .asList("boolean", "byte", "char", "short", "int", "long", "float", "double","void");

  /**
   * A typeConstant has a name
   */
  protected String constName;

  public TypeConstant(String constName) {
    this.constName = constName;
  }
  
  public String getConstName() {
    return constName;
  }

  public void setConstName(String constName) {
    if (primitiveTypes.contains(name)) {
      this.constName = constName;
    } else {
      throw new IllegalArgumentException("0xD3482 Only primitive types allowed (" + primitiveTypes.toString() + "), but was:" + name);
    }
  }
  
  /**
   * print: Umwandlung in einen kompakten String
   */
  public String print() {
    return getConstName();
  }


  // --------------------------------------------------------------------------


    @Override @Deprecated
  public boolean deepEquals(TypeExpression typeExpression) {
    if(!(typeExpression instanceof TypeConstant)){
      return false;
    }
    if(!this.name.equals(typeExpression.name)){
      return false;
    }
    if(!this.typeSymbol.equals(typeExpression.typeSymbol)){
      return false;
    }
    for(int i = 0; i<this.superTypes.size();i++){
      if(!this.superTypes.get(i).deepEquals(typeExpression.superTypes.get(i))){
        return false;
      }
    }
    return true;
  }

  @Override @Deprecated
  public TypeExpression deepClone() {
    TypeConstant clone = new TypeConstant();
    clone.setName(this.name);
    clone.setEnclosingScope(this.enclosingScope);
    for(TypeExpression expr: superTypes){
      clone.addSuperType(expr.deepClone());
    }
    clone.typeSymbol = this.typeSymbol;
    return clone;
  }
  //hier enum attr für primitive types

}
