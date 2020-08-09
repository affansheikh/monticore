/* (c) https://github.com/MontiCore/monticore */
package de.monticore.codegen.cd2java.methods.accessor;

import de.monticore.cd.cd4analysis._ast.ASTCDAttribute;
import de.monticore.cd.cd4analysis._ast.ASTCDMethod;
import de.monticore.codegen.cd2java.AbstractCreator;
import de.monticore.codegen.cd2java.AbstractService;
import de.monticore.codegen.cd2java.DecorationHelper;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.generating.templateengine.TemplateHookPoint;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.monticore.cd.facade.CDModifier.PUBLIC;
import static de.monticore.codegen.cd2java.CoreTemplates.EMPTY_BODY;

public class OptionalAccessorDecorator extends AbstractCreator<ASTCDAttribute, List<ASTCDMethod>> {

  protected static final String GET = "get%s";

  protected static final String IS_PRESENT = "isPresent%s";

  protected String naiveAttributeName;

  protected final AbstractService service;

  public OptionalAccessorDecorator(final GlobalExtensionManagement glex,
                                   final AbstractService service) {
    super(glex);
    this.service = service;
  }

  @Override
  public List<ASTCDMethod> decorate(final ASTCDAttribute ast) {
    naiveAttributeName = getNaiveAttributeName(ast);
    ASTCDMethod get = createGetMethod(ast);
    ASTCDMethod isPresent = createIsPresentMethod(ast);
    return new ArrayList<>(Arrays.asList(get, isPresent));
  }

  protected String getNaiveAttributeName(ASTCDAttribute astcdAttribute) {
    return StringUtils.capitalize(getDecorationHelper().getNativeAttributeName(astcdAttribute.getName()));
  }

  protected ASTCDMethod createGetMethod(final ASTCDAttribute ast) {
    String name = String.format(GET, naiveAttributeName);
    ASTMCType type = getDecorationHelper().getReferenceTypeFromOptional(ast.getMCType().deepClone()).getMCTypeOpt().get();
    ASTCDMethod method = this.getCDMethodFacade().createMethod(PUBLIC, type, name);
    String generatedErrorCode = service.getGeneratedErrorCode(ast.getName() + ast.printType());
    this.replaceTemplate(EMPTY_BODY, method, new TemplateHookPoint("methods.opt.Get4Opt", ast, naiveAttributeName, generatedErrorCode));
    return method;
  }

  protected ASTCDMethod createIsPresentMethod(final ASTCDAttribute ast) {
    String name = String.format(IS_PRESENT, naiveAttributeName);
    ASTCDMethod method = this.getCDMethodFacade().createMethod(PUBLIC, getMCTypeFacade().createBooleanType(), name);
    this.replaceTemplate(EMPTY_BODY, method, new TemplateHookPoint("methods.opt.IsPresent4Opt", ast));
    return method;
  }
}
