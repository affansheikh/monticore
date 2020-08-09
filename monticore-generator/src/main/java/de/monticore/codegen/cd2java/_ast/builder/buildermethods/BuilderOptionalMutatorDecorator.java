/* (c) https://github.com/MontiCore/monticore */
package de.monticore.codegen.cd2java._ast.builder.buildermethods;

import de.monticore.cd.cd4analysis._ast.ASTCDAttribute;
import de.monticore.cd.cd4analysis._ast.ASTCDMethod;
import de.monticore.cd.cd4analysis._ast.ASTCDParameter;
import de.monticore.codegen.cd2java.methods.mutator.OptionalMutatorDecorator;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.generating.templateengine.TemplateHookPoint;
import de.monticore.types.mcbasictypes._ast.ASTMCReturnType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types.mcbasictypes.MCBasicTypesMill;

import static de.monticore.cd.facade.CDModifier.PUBLIC;
import static de.monticore.codegen.cd2java.CoreTemplates.EMPTY_BODY;

/**
 * changes return type of builder setters for optional attributes
 */
public class BuilderOptionalMutatorDecorator extends OptionalMutatorDecorator {

  protected final ASTMCType builderType;

  public BuilderOptionalMutatorDecorator(final GlobalExtensionManagement glex,
                                         final ASTMCType builderType) {
    super(glex);
    this.builderType = builderType;
  }

  @Override
  protected ASTCDMethod createSetMethod(final ASTCDAttribute attribute) {
    String name = String.format(SET, naiveAttributeName);
    ASTMCType parameterType = getDecorationHelper().getReferenceTypeFromOptional(attribute.getMCType()).getMCTypeOpt().get().deepClone();
    ASTCDParameter parameter = this.getCDParameterFacade().createParameter(parameterType, attribute.getName());
    ASTCDMethod method = this.getCDMethodFacade().createMethod(PUBLIC, name, parameter);
    ASTMCReturnType returnType = MCBasicTypesMill.mCReturnTypeBuilder().setMCType(builderType).build();
    method.setMCReturnType(returnType);
    this.replaceTemplate(EMPTY_BODY, method, new TemplateHookPoint("_ast.builder.opt.Set4ASTBuilderOpt", attribute));
    return method;
  }

  @Override
  protected ASTCDMethod createSetAbsentMethod(final ASTCDAttribute attribute) {
    String name = String.format(SET_ABSENT, naiveAttributeName);
    ASTCDMethod method = this.getCDMethodFacade().createMethod(PUBLIC, name);
    ASTMCReturnType returnType = MCBasicTypesMill.mCReturnTypeBuilder().setMCType(builderType).build();
    method.setMCReturnType(returnType);
    this.replaceTemplate(EMPTY_BODY, method, new TemplateHookPoint("_ast.builder.opt.SetAbsent4ASTBuilderOpt", attribute));
    return method;
  }
}
