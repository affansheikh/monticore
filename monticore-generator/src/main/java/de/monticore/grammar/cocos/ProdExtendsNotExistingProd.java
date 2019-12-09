// (c) https://github.com/MontiCore/monticore

package de.monticore.grammar.cocos;

import de.monticore.grammar.grammar._ast.ASTProd;
import de.monticore.grammar.grammar._cocos.GrammarASTProdCoCo;
import de.monticore.grammar.grammar._symboltable.ProdSymbolLoader;
import de.se_rwth.commons.logging.Log;

public class ProdExtendsNotExistingProd implements GrammarASTProdCoCo {

  public static final String ERROR_CODE = "0xA0113";

  public static final String ERROR_MSG_FORMAT = " The production %s extends or implements a non-existing production";

  @Override
  public void check(ASTProd node) {
    for(ProdSymbolLoader loader: node.getSymbol().getSuperProds()){
      if(!loader.loadSymbol().isPresent()){
        logError(node.getName());
      }
    }

    for(ProdSymbolLoader loader: node.getSymbol().getSuperInterfaceProds()){
      if(!loader.loadSymbol().isPresent()){
        logError(node.getName());
      }
    }
  }

  public void logError(String name){
    Log.error(String.format(ERROR_CODE+ERROR_MSG_FORMAT,name));
  }
}