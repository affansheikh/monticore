package de.monticore.expressions.expressionsbasis._symboltable;

import de.monticore.types.mcbasictypes._ast.ASTMCType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class EVariableSymbol extends EVariableSymbolTOP {

  public EVariableSymbol(String name) {
    super(name);
  }

  public ASTMCType getType() {
    throw new NotImplementedException();
  }
}
