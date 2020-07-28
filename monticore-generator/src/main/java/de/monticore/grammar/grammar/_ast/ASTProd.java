/* (c) https://github.com/MontiCore/monticore */

package de.monticore.grammar.grammar._ast;

import java.util.ArrayList;

public interface ASTProd extends ASTProdTOP {
  
  default java.util.List<ASTSymbolDefinition> getSymbolDefinitionsList()  {
    return new ArrayList<ASTSymbolDefinition>();
 } 
    
  
}


