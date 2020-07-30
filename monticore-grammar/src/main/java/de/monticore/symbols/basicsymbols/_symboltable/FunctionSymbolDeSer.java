// (c) https://github.com/MontiCore/monticore

// (c) https://github.com/MontiCore/monticore

/* (c) https://github.com/MontiCore/monticore */
package de.monticore.symbols.basicsymbols._symboltable;

import de.monticore.symboltable.serialization.json.JsonObject;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionDeSer;

public class FunctionSymbolDeSer extends FunctionSymbolDeSerTOP {

  @Override
  public SymTypeExpression deserializeReturnType(JsonObject symbolJson,
      IBasicSymbolsScope enclosingScope) {
    return SymTypeExpressionDeSer.deserializeMember("returnType", symbolJson, enclosingScope);
  }

}
