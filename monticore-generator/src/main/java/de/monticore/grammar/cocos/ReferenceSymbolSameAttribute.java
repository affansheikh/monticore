/* (c) https://github.com/MontiCore/monticore */
package de.monticore.grammar.cocos;

import de.monticore.grammar.grammar._ast.*;
import de.monticore.grammar.grammar._cocos.GrammarASTMCGrammarCoCo;

public class ReferenceSymbolSameAttribute implements GrammarASTMCGrammarCoCo {

  @Override
  public void check(ASTMCGrammar node) {
    for (ASTClassProd classProd : node.getClassProdsList()) {
      ReferenceSymbolSameAttributeVisitor visitor = new ReferenceSymbolSameAttributeVisitor();
      classProd.accept(visitor);
    }
    for (ASTAbstractProd abstractProd : node.getAbstractProdsList()) {
      ReferenceSymbolSameAttributeVisitor visitor = new ReferenceSymbolSameAttributeVisitor();
      abstractProd.accept(visitor);
    }
    for (ASTInterfaceProd interfaceProd : node.getInterfaceProdsList()) {
      ReferenceSymbolSameAttributeVisitor visitor = new ReferenceSymbolSameAttributeVisitor();
      interfaceProd.accept(visitor);
    }
  }
}
