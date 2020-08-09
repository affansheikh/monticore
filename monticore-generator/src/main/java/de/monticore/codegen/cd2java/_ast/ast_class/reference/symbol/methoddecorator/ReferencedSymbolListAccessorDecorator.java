/* (c) https://github.com/MontiCore/monticore */
package de.monticore.codegen.cd2java._ast.ast_class.reference.symbol.methoddecorator;

import de.monticore.cd.cd4analysis._ast.ASTCDAttribute;
import de.monticore.cd.cd4analysis._ast.ASTCDMethod;
import de.monticore.codegen.cd2java._symboltable.SymbolTableService;
import de.monticore.codegen.cd2java.methods.accessor.ListAccessorDecorator;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.generating.templateengine.TemplateHookPoint;

import static de.monticore.codegen.cd2java.CoreTemplates.EMPTY_BODY;

/**
 * created all list getter methods for the referencedSymbols
 */

public class ReferencedSymbolListAccessorDecorator extends ListAccessorDecorator {

  protected final SymbolTableService symbolTableService;

  public ReferencedSymbolListAccessorDecorator(final GlobalExtensionManagement glex,
                                               final SymbolTableService symbolTableService) {
    super(glex);
    this.symbolTableService = symbolTableService;
  }

  /**
   * overwrite only the getList method implementation, because the other methods are delegated to this one
   */
  @Override
  protected ASTCDMethod createGetListMethod(ASTCDAttribute ast) {
    String signature = String.format(GET_LIST, attributeType, capitalizedAttributeNameWithS);
    ASTCDMethod getList = this.getCDMethodFacade().createMethodByDefinition(signature);
    String referencedSymbolType = symbolTableService.getReferencedSymbolTypeName(ast);
    String simpleSymbolName = symbolTableService.getSimpleNameFromSymbolName(ast.getName());
    this.replaceTemplate(EMPTY_BODY, getList, new TemplateHookPoint("_ast.ast_class.refSymbolMethods.GetSymbolList",
        ast.getName(), referencedSymbolType, simpleSymbolName));
    return getList;
  }

}
