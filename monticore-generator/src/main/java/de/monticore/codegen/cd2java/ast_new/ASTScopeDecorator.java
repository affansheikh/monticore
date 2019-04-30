package de.monticore.codegen.cd2java.ast_new;

import de.monticore.codegen.cd2java.AbstractDecorator;
import de.monticore.codegen.cd2java.methods.MethodDecorator;
import de.monticore.codegen.cd2java.symboltable.SymbolTableService;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.types.types._ast.ASTType;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDAttribute;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDClass;

import static de.monticore.codegen.cd2java.factories.CDModifier.PROTECTED;
import static de.monticore.codegen.cd2java.symboltable.SymbolTableConstants.SPANNED_SCOPE;

public class ASTScopeDecorator extends AbstractDecorator<ASTCDClass, ASTCDClass> {


  private final MethodDecorator methodDecorator;

  private final SymbolTableService symbolTableService;

  public ASTScopeDecorator(final GlobalExtensionManagement glex, final MethodDecorator methodDecorator,
                           final SymbolTableService symbolTableService) {
    super(glex);
    this.methodDecorator = methodDecorator;
    this.symbolTableService = symbolTableService;
  }

  @Override
  public ASTCDClass decorate(final ASTCDClass clazz) {
    if (symbolTableService.isScopeClass(clazz)) {
      ASTType scopeType = this.getCDTypeFacade().createOptionalTypeOf(symbolTableService.getScopeType());
      String attributeName = String.format(SPANNED_SCOPE ,symbolTableService.getCDName());
      ASTCDAttribute scopeAttribute = this.getCDAttributeFacade().createAttribute(PROTECTED, scopeType, attributeName);
      clazz.addCDAttribute(scopeAttribute);
      clazz.addAllCDMethods(methodDecorator.decorate(scopeAttribute));
    }
    return clazz;
  }
}