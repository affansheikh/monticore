/* (c) https://github.com/MontiCore/monticore */
package de.monticore.codegen.cd2java.methods;

import de.monticore.cd.cd4analysis._ast.ASTCDAttribute;
import de.monticore.cd.cd4analysis._ast.ASTCDMethod;
import de.monticore.cd.cd4analysis._ast.ASTCDParameter;
import de.monticore.codegen.cd2java.AbstractCreator;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.generating.templateengine.HookPoint;
import de.monticore.generating.templateengine.TemplateHookPoint;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types.mcfullgenerictypes.MCFullGenericTypesMill;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static de.monticore.codegen.cd2java.CoreTemplates.EMPTY_BODY;

public abstract class ListMethodDecorator extends AbstractCreator<ASTCDAttribute, List<ASTCDMethod>> {

  protected String capitalizedAttributeNameWithS;

  protected String attributeType;

  public ListMethodDecorator(final GlobalExtensionManagement glex) {
    super(glex);
  }

  @Override
  public List<ASTCDMethod> decorate(final ASTCDAttribute ast) {
    this.capitalizedAttributeNameWithS = getCapitalizedAttributeNameWithS(ast);
    this.attributeType = getAttributeType(ast);

    List<ASTCDMethod> methods = getMethodSignatures().stream()
        .map(getCDMethodFacade()::createMethodByDefinition)
        .collect(Collectors.toList());

    methods.forEach(m -> this.replaceTemplate(EMPTY_BODY, m, createListImplementation(m)));
    return methods;
  }

  protected abstract List<String> getMethodSignatures();

  protected String getTypeArgumentFromListType(ASTMCType type) {
    String typeString = type.printType(MCFullGenericTypesMill.mcFullGenericTypesPrettyPrinter());
    int lastListIndex = typeString.lastIndexOf("List<") + 5;
    return typeString.substring(lastListIndex, typeString.length() - 1);
  }

  private HookPoint createListImplementation(final ASTCDMethod method) {
    String attributeName = StringUtils.uncapitalize(capitalizedAttributeNameWithS);
    int attributeIndex = method.getName().lastIndexOf(capitalizedAttributeNameWithS);
    String methodName = method.getName().substring(0, attributeIndex);
    String parameterCall = method.getCDParametersList().stream()
        .map(ASTCDParameter::getName)
        .collect(Collectors.joining(", "));
    String returnType = method.printReturnType();

    return new TemplateHookPoint("methods.MethodDelegate", attributeName, methodName, parameterCall, returnType);
  }

  public String getCapitalizedAttributeNameWithS(ASTCDAttribute attribute) {
    return StringUtils.capitalize(getDecorationHelper().getNativeAttributeName(attribute.getName()));
  }

  public String getAttributeType(ASTCDAttribute attribute) {
    return getTypeArgumentFromListType(attribute.getMCType());
  }
}
