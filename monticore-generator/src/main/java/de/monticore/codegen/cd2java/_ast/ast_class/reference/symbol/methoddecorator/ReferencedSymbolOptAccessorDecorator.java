/* (c) https://github.com/MontiCore/monticore */
package de.monticore.codegen.cd2java._ast.ast_class.reference.symbol.methoddecorator;

import de.monticore.cd.cd4analysis._ast.ASTCDAttribute;
import de.monticore.cd.cd4analysis._ast.ASTCDMethod;
import de.monticore.codegen.cd2java._ast.ast_class.reference.symbol.ASTReferencedSymbolDecorator;
import de.monticore.codegen.cd2java._symboltable.SymbolTableService;
import de.monticore.codegen.cd2java.methods.accessor.OptionalAccessorDecorator;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.generating.templateengine.TemplateHookPoint;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import org.apache.commons.lang3.StringUtils;

import static de.monticore.cd.facade.CDModifier.PUBLIC;
import static de.monticore.codegen.cd2java.CoreTemplates.EMPTY_BODY;

/**
 * creates all optional getter methods for the referencedSymbols
 */
public class ReferencedSymbolOptAccessorDecorator extends OptionalAccessorDecorator {

  protected final SymbolTableService symbolTableService;


  public ReferencedSymbolOptAccessorDecorator(final GlobalExtensionManagement glex, final SymbolTableService symbolTableService) {
    super(glex, symbolTableService);
    this.symbolTableService = symbolTableService;
  }

  /**
   * Overwrite the get method implementation as it requires additional logic
   * then a normal getter.
   */
  @Override
  protected ASTCDMethod createGetMethod(final ASTCDAttribute ast) {
    String name = String.format(GET, StringUtils.capitalize(ast.getName()));
    ASTCDMethod method = this.getCDMethodFacade().createMethod(PUBLIC, ast.getMCType(), name);
    //create correct Name A for resolveA method
    String simpleSymbolName = symbolTableService.getSimpleNameFromSymbolName(symbolTableService.getReferencedSymbolTypeName(ast));
    this.replaceTemplate(EMPTY_BODY, method, new TemplateHookPoint("_ast.ast_class.refSymbolMethods.GetSymbol",
        ast.getName(), simpleSymbolName));
    return method;
  }
  
  /**
   * Overwrite the isPresent method implementation as it requires additional
   * logic then the normal method.
   */
  @Override
  protected ASTCDMethod createIsPresentMethod(final ASTCDAttribute ast) {
    String name = String.format(IS_PRESENT, StringUtils.capitalize(naiveAttributeName));
    ASTCDMethod method = this.getCDMethodFacade().createMethod(PUBLIC, getMCTypeFacade().createBooleanType(), name);
    this.replaceTemplate(EMPTY_BODY, method, new TemplateHookPoint("_ast.ast_class.refSymbolMethods.IsPresentSymbol", ast.getName()));
    return method;
  }

}
