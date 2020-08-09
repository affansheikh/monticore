/* (c) https://github.com/MontiCore/monticore */
package de.monticore.codegen.cd2java._symboltable.serialization;

import com.google.common.collect.Lists;
import de.monticore.cd.cd4analysis._ast.*;
import de.monticore.cd.cd4code.CD4CodeMill;
import de.monticore.codegen.cd2java.AbstractCreator;
import de.monticore.codegen.cd2java._symboltable.SymbolTableService;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.generating.templateengine.HookPoint;
import de.monticore.generating.templateengine.StringHookPoint;
import de.monticore.generating.templateengine.TemplateHookPoint;
import de.monticore.types.mcsimplegenerictypes._ast.ASTMCBasicGenericType;
import de.monticore.utils.Names;
import de.se_rwth.commons.StringTransformations;
import de.monticore.types.typesymbols._symboltable.BuiltInJavaTypeSymbolResolvingDelegate;

import java.util.ArrayList;
import java.util.List;

import static de.monticore.cd.facade.CDModifier.PROTECTED;
import static de.monticore.cd.facade.CDModifier.PUBLIC;
import static de.monticore.codegen.cd2java.CoreTemplates.EMPTY_BODY;
import static de.monticore.codegen.cd2java._symboltable.SymbolTableConstants.*;

/**
 * creates a SymbolDeSer class from a grammar
 */
public class SymbolDeSerDecorator extends AbstractCreator<ASTCDType, ASTCDClass> {

  protected final SymbolTableService symbolTableService;

  protected static final String TEMPLATE_PATH = "_symboltable.serialization.symbolDeSer.";

  protected ASTCDParameter enclosingScopeParameter;

  public SymbolDeSerDecorator(final GlobalExtensionManagement glex,
      final SymbolTableService symbolTableService) {
    super(glex);
    this.symbolTableService = symbolTableService;
  }

  @Override
  public ASTCDClass decorate(ASTCDType symbolClass) {
    String symbolDeSerName = symbolTableService.getSymbolDeSerSimpleName(symbolClass);
    String symbolFullName = symbolTableService.getSymbolFullName(symbolClass);
    String symbolSimpleName = symbolTableService.getSymbolSimpleName(symbolClass);
    String symbolBuilderFullName = symbolTableService.getSymbolBuilderFullName(symbolClass);
    String symbolBuilderSimpleName = symbolTableService.getSymbolBuilderSimpleName(symbolClass);
    String symTabMillFullName = symbolTableService.getMillFullName();
    this.enclosingScopeParameter = getCDParameterFacade().createParameter(getMCTypeFacade().createQualifiedType(symbolTableService.getScopeInterfaceFullName()), ENCLOSING_SCOPE_VAR);

    ASTCDAttribute symbolTablePrinter = getCDAttributeFacade().createAttribute(PROTECTED,
        getMCTypeFacade().createQualifiedType(symbolTableService.getSymbolTablePrinterFullName()),"symbolTablePrinter");


    return CD4CodeMill.cDClassBuilder()
        .setName(symbolDeSerName)
        .setModifier(PUBLIC.build())
        .addCDConstructor(createConstructor(symbolDeSerName))
        .addCDAttribute(symbolTablePrinter)
        .addCDMethod(createGetSerializedKindMethod(symbolFullName))
        .addCDMethod(createSerializeMethod(symbolFullName))
        .addAllCDMethods(
            createDeserializeMethods(symbolFullName, symbolSimpleName, symbolBuilderFullName,
                symbolBuilderSimpleName, symTabMillFullName,
                symbolClass.deepClone().getCDAttributeList()))
        .addAllCDMethods(createDeserializeSymbolRuleAttributesMethod(
            symbolClass.deepClone().getCDAttributeList(), symbolDeSerName))
        .build();
  }

  protected ASTCDConstructor createConstructor(String symbolDeSerName) {
    ASTCDConstructor constructor = getCDConstructorFacade().createConstructor(PUBLIC, symbolDeSerName);
    this.replaceTemplate(EMPTY_BODY, constructor, new StringHookPoint("this.symbolTablePrinter = new "
        + symbolTableService.getSymbolTablePrinterFullName() + "();"));
    return constructor;
  }

  protected List<ASTCDMethod> createDeserializeMethods(String symbolFullName,
      String symbolSimpleName,
      String symbolBuilderFullName, String symbolBuilderSimpleName,
      String symTabMill, List<ASTCDAttribute> symbolRuleAttributes) {
    ASTCDMethod deserializeStringMethod = createDeserializeStringMethod(symbolFullName, symbolSimpleName);
    ASTCDParameter jsonParam = getCDParameterFacade()
        .createParameter(getMCTypeFacade().createQualifiedType(JSON_OBJECT), SYMBOL_JSON_VAR);
    ASTCDMethod deserializeSymbolMethod = createDeserializeSymbolMethod(symbolBuilderFullName,
        symbolBuilderSimpleName,
        symTabMill, symbolFullName, symbolSimpleName, jsonParam, symbolRuleAttributes);
    ASTCDMethod deserializeAdditionalAttributesSymbolMethod = createDeserializeAdditionalAttributesSymbolMethod(
        symbolFullName, jsonParam);

    return Lists.newArrayList(deserializeStringMethod, deserializeSymbolMethod,
        deserializeAdditionalAttributesSymbolMethod);
  }

  protected ASTCDMethod createGetSerializedKindMethod(String symbolName) {
    ASTCDMethod getSerializedKindMethod = getCDMethodFacade()
        .createMethod(PUBLIC, getMCTypeFacade().createStringType(), "getSerializedKind");
    this.replaceTemplate(EMPTY_BODY, getSerializedKindMethod,
        new StringHookPoint("return \"" + symbolName + "\";"));
    return getSerializedKindMethod;
  }

  protected ASTCDMethod createSerializeMethod(String symbolName) {
    ASTCDParameter toSerializeParam = getCDParameterFacade()
        .createParameter(getMCTypeFacade().createQualifiedType(symbolName), "toSerialize");
    ASTCDMethod serializeMethod = getCDMethodFacade()
        .createMethod(PUBLIC, getMCTypeFacade().createStringType(), "serialize", toSerializeParam);
    this.replaceTemplate(EMPTY_BODY, serializeMethod, new StringHookPoint("  toSerialize.accept(symbolTablePrinter);\n"
        + "  return symbolTablePrinter.getSerializedString();"));
    return serializeMethod;
  }

  protected ASTCDMethod createDeserializeStringMethod(String symbolFullName, String symbolSimpleName) {
    ASTCDParameter stringParam = getCDParameterFacade()
        .createParameter(getMCTypeFacade().createStringType(), "serialized");
    ASTCDMethod deserializeMethod = getCDMethodFacade()
        .createMethod(PUBLIC, getMCTypeFacade().createQualifiedType(symbolFullName), DESERIALIZE,
            stringParam, enclosingScopeParameter);
    this.replaceTemplate(EMPTY_BODY, deserializeMethod,
        new TemplateHookPoint(TEMPLATE_PATH + "DeserializeString4SymbolDeSer", symbolSimpleName));
    return deserializeMethod;
  }

  protected ASTCDMethod createDeserializeSymbolMethod(String symbolBuilderFullName,
      String symbolBuilderSimpleName,
      String symTabMill, String symbolFullName, String symbolSimpleName,
      ASTCDParameter jsonParam, List<ASTCDAttribute> symbolRuleAttributes) {
    ASTCDMethod deserializeMethod = getCDMethodFacade()
        .createMethod(PUBLIC, getMCTypeFacade().createQualifiedType(symbolFullName),
            DESERIALIZE + symbolSimpleName, jsonParam, enclosingScopeParameter);
    this.replaceTemplate(EMPTY_BODY, deserializeMethod,
        new TemplateHookPoint(TEMPLATE_PATH + "DeserializeSymbol4SymbolDeSer",
            symbolBuilderFullName, symbolBuilderSimpleName, symTabMill, symbolFullName,
            symbolSimpleName, symbolRuleAttributes));
    return deserializeMethod;
  }

  protected List<ASTCDMethod> createDeserializeSymbolRuleAttributesMethod(
      List<ASTCDAttribute> attributeList, String deSerName) {
    List<ASTCDMethod> methodList = new ArrayList<>();
    for (ASTCDAttribute astcdAttribute : attributeList) {
      String methodName = DESERIALIZE +
          StringTransformations.capitalize(astcdAttribute.getName());
      ASTCDParameter jsonParam = getCDParameterFacade()
          .createParameter(getMCTypeFacade().createQualifiedType(JSON_OBJECT), "symbolJson");
      ASTCDMethod deserializeMethod = getCDMethodFacade()
          .createMethod(PUBLIC, astcdAttribute.getMCType(), methodName , jsonParam, enclosingScopeParameter);
      String generatedErrorCode = symbolTableService.getGeneratedErrorCode(methodName);
      HookPoint deserImplementation = DeSerMap.getDeserializationImplementation(astcdAttribute, methodName, "symbolJson",
//          astcdAttribute.getEnclosingScope()); //TODO AB Replace line below with this line after release of 5.5.0-SNAPSHOT
          BuiltInJavaTypeSymbolResolvingDelegate.getScope(), generatedErrorCode);
        this.replaceTemplate(EMPTY_BODY, deserializeMethod, deserImplementation);
      methodList.add(deserializeMethod);
    }
    return methodList;
  }

  protected ASTCDMethod createDeserializeAdditionalAttributesSymbolMethod(String symbolFullName,
      ASTCDParameter jsonParam) {
    ASTCDParameter symbolParam = getCDParameterFacade()
        .createParameter(getMCTypeFacade().createQualifiedType(symbolFullName), SYMBOL_VAR);
    String methodName = "deserializeAdditional"+ Names.getSimpleName(symbolFullName)+"Attributes";
    return getCDMethodFacade()
        .createMethod(PROTECTED, methodName, symbolParam, jsonParam, enclosingScopeParameter);
  }
}
