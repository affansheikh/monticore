package de.monticore.codegen.cd2java._symboltable.symbTabMill;

import de.monticore.cd.cd4analysis._ast.*;
import de.monticore.cd.cd4analysis._symboltable.CDDefinitionSymbol;
import de.monticore.cd.cd4analysis._symboltable.CDTypeSymbol;
import de.monticore.cd.cd4code._ast.CD4CodeMill;
import de.monticore.codegen.cd2java.AbstractCreator;
import de.monticore.codegen.cd2java._symboltable.SymbolTableService;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.generating.templateengine.StringHookPoint;
import de.monticore.generating.templateengine.TemplateHookPoint;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.se_rwth.commons.StringTransformations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.monticore.codegen.cd2java.CoreTemplates.EMPTY_BODY;
import static de.monticore.codegen.cd2java._ast.builder.BuilderConstants.BUILDER_SUFFIX;
import static de.monticore.codegen.cd2java._symboltable.SymbolTableConstants.REFERENCE_SUFFIX;
import static de.monticore.codegen.cd2java.factories.CDModifier.*;

public class SymTabMillDecorator extends AbstractCreator<ASTCDCompilationUnit, ASTCDClass> {

  protected final SymbolTableService symbolTableService;

  protected boolean isLanguageTop;

  public SymTabMillDecorator(final GlobalExtensionManagement glex,
                             final SymbolTableService symbolTableService) {
    super(glex);
    this.symbolTableService = symbolTableService;
  }

  @Override
  public ASTCDClass decorate(ASTCDCompilationUnit input) {
    String symTabMillName = symbolTableService.getSymTabMillSimpleName();
    List<ASTCDType> symbolDefiningProds = symbolTableService.getSymbolDefiningProds(input.getCDDefinition());

    List<ASTCDAttribute> millAttributeList = createMillAttributeList(symTabMillName, symbolDefiningProds, input.getCDDefinition());

    return CD4CodeMill.cDClassBuilder()
        .setName(symTabMillName)
        .setModifier(PUBLIC.build())
        .addCDConstructor(createConstructor(symTabMillName))
        .addCDAttribute(createMillAttribute(symTabMillName))
        .addAllCDAttributes(millAttributeList)
        .addCDMethod(createGetMillMethod(symTabMillName))
        .addCDMethod(createInitMeMethod(symTabMillName, millAttributeList))
        .addCDMethod(createInitMethod(symTabMillName))
        .addCDMethod(createResetMeMethod(millAttributeList))
        .addAllCDMethods(createBuilderMethods(millAttributeList))
        .addAllCDMethods(createSuperSymbolBuilderMethods())
        .build();
  }

  protected ASTCDConstructor createConstructor(String symTabMillName) {
    return getCDConstructorFacade().createConstructor(PROTECTED, symTabMillName);
  }

  protected ASTCDAttribute createMillAttribute(String symTabMill) {
    return getCDAttributeFacade().createAttribute(PROTECTED_STATIC, symTabMill, "mill");
  }

  protected List<ASTCDAttribute> createMillAttributeList(String symTabMill, List<ASTCDType> symbolDefiningProds, ASTCDDefinition astcdDefinition) {
    List<ASTCDAttribute> millAttributeList = new ArrayList<>();
    // mill attributes for symbols
    for (ASTCDType symbolDefiningProd : symbolDefiningProds) {
      String symbolSimpleName = StringTransformations.uncapitalize(symbolTableService.getSymbolSimpleName(symbolDefiningProd));
      ASTCDAttribute millSymbolAttribute = getCDAttributeFacade().createAttribute(PROTECTED_STATIC, symTabMill, symbolSimpleName);
      millAttributeList.add(millSymbolAttribute);

      ASTCDAttribute millSymbolReferenceAttribute = getCDAttributeFacade().createAttribute(PROTECTED_STATIC, symTabMill, symbolSimpleName + REFERENCE_SUFFIX);
      millAttributeList.add(millSymbolReferenceAttribute);
    }

    if (symbolTableService.hasStartProd(astcdDefinition) &&
        !(astcdDefinition.isPresentModifier() && symbolTableService.hasComponentStereotype(astcdDefinition.getModifier()))) {
      if (isLanguageTop()) {
        String languageName = StringTransformations.uncapitalize(symbolTableService.getLanguageClassSimpleName());
        ASTCDAttribute millLanguageAttribute = getCDAttributeFacade().createAttribute(PROTECTED_STATIC, symTabMill, languageName);
        millAttributeList.add(millLanguageAttribute);
      }

      String modelLoaderName = StringTransformations.uncapitalize(symbolTableService.getModelLoaderClassSimpleName());
      ASTCDAttribute millModelLoaderAttribute = getCDAttributeFacade().createAttribute(PROTECTED_STATIC, symTabMill, modelLoaderName);
      millAttributeList.add(millModelLoaderAttribute);

      String symTabCreatorName = StringTransformations.uncapitalize(symbolTableService.getSymbolTableCreatorSimpleName());
      ASTCDAttribute millSymTabCreatorAttribute = getCDAttributeFacade().createAttribute(PROTECTED_STATIC, symTabMill, symTabCreatorName);
      millAttributeList.add(millSymTabCreatorAttribute);

      String symTabCreatorDelegatorName = StringTransformations.uncapitalize(symbolTableService.getSymbolTableCreatorDelegatorSimpleName());
      ASTCDAttribute millSymTabCreatorDelegatorAttribute = getCDAttributeFacade().createAttribute(PROTECTED_STATIC, symTabMill, symTabCreatorDelegatorName);
      millAttributeList.add(millSymTabCreatorDelegatorAttribute);

      String globalScopeName = StringTransformations.uncapitalize(symbolTableService.getGlobalScopeSimpleName());
      ASTCDAttribute millGlobalScopeAttribute = getCDAttributeFacade().createAttribute(PROTECTED_STATIC, symTabMill, globalScopeName);
      millAttributeList.add(millGlobalScopeAttribute);

      String artifactScopeName = StringTransformations.uncapitalize(symbolTableService.getArtifactScopeSimpleName());
      ASTCDAttribute millArtifactScopeAttribute = getCDAttributeFacade().createAttribute(PROTECTED_STATIC, symTabMill, artifactScopeName);
      millAttributeList.add(millArtifactScopeAttribute);

    }

    String scopeName = StringTransformations.uncapitalize(symbolTableService.getScopeClassSimpleName());
    ASTCDAttribute millScopeAttribute = getCDAttributeFacade().createAttribute(PROTECTED_STATIC, symTabMill, scopeName);
    millAttributeList.add(millScopeAttribute);
    return millAttributeList;
  }

  protected ASTCDMethod createGetMillMethod(String symTabMill) {
    ASTCDMethod getMillMethod = getCDMethodFacade().createMethod(PROTECTED_STATIC, getCDTypeFacade().createQualifiedType(symTabMill), "getMill");
    this.replaceTemplate(EMPTY_BODY, getMillMethod, new TemplateHookPoint("_symboltable.symTabMill.GetMill", symTabMill));
    return getMillMethod;
  }

  protected ASTCDMethod createInitMeMethod(String symTabMill, List<ASTCDAttribute> attributeList) {
    ASTCDParameter millParam = getCDParameterFacade().createParameter(getCDTypeFacade().createQualifiedType(symTabMill), "a");
    ASTCDMethod getMillMethod = getCDMethodFacade().createMethod(PUBLIC_STATIC, "initMe", millParam);
    this.replaceTemplate(EMPTY_BODY, getMillMethod, new TemplateHookPoint("_symboltable.symTabMill.InitMe", attributeList));
    return getMillMethod;
  }

  protected ASTCDMethod createInitMethod(String symTabMill) {
    ASTCDMethod getMillMethod = getCDMethodFacade().createMethod(PUBLIC_STATIC, "init");
    this.replaceTemplate(EMPTY_BODY, getMillMethod, new StringHookPoint("mill = new " + symTabMill + "();"));
    return getMillMethod;
  }

  protected ASTCDMethod createResetMeMethod(List<ASTCDAttribute> attributeList) {
    // find all super mill names
    List<CDDefinitionSymbol> superCDsTransitive = symbolTableService.getSuperCDsTransitive();
    List<String> superMills = superCDsTransitive.stream()
        .map(symbolTableService::getSymTabMillFullName)
        .collect(Collectors.toList());

    ASTCDMethod getMillMethod = getCDMethodFacade().createMethod(PUBLIC_STATIC, "reset");
    this.replaceTemplate(EMPTY_BODY, getMillMethod, new TemplateHookPoint("_symboltable.symTabMill.Reset", attributeList, superMills));
    return getMillMethod;
  }


  protected List<ASTCDMethod> createBuilderMethods(List<ASTCDAttribute> attributeList) {
    List<ASTCDMethod> builderMethodList = new ArrayList<>();
    for (ASTCDAttribute astcdAttribute : attributeList) {
      String builderName = astcdAttribute.getName() + BUILDER_SUFFIX;
      ASTMCQualifiedType builderType = getCDTypeFacade().createQualifiedType(StringTransformations.capitalize(builderName));
      ASTCDMethod _builderMethod = getCDMethodFacade().createMethod(PROTECTED, builderType, "_" + builderName);
      this.replaceTemplate(EMPTY_BODY, _builderMethod, new StringHookPoint("return new " + StringTransformations.capitalize(builderName) + "();"));
      builderMethodList.add(_builderMethod);

      ASTCDMethod builderMethod = getCDMethodFacade().createMethod(PUBLIC_STATIC, builderType, builderName);
      this.replaceTemplate(EMPTY_BODY, builderMethod, new TemplateHookPoint("_symboltable.symTabMill.BuilderMethod", astcdAttribute.getName()));
      builderMethodList.add(builderMethod);
    }
    return builderMethodList;
  }


  protected List<ASTCDMethod> createSuperSymbolBuilderMethods() {
    List<ASTCDMethod> builderMethodList = new ArrayList<>();
    for (CDDefinitionSymbol cdDefinitionSymbol : symbolTableService.getSuperCDsTransitive()) {
      for (CDTypeSymbol type : cdDefinitionSymbol.getTypes()) {
        if (type.getAstNode().isPresent() && type.getAstNode().get().getModifierOpt().isPresent()
            && symbolTableService.hasSymbolStereotype(type.getAstNode().get().getModifierOpt().get())) {
          // for prod with symbol property create delegate builder method
          String symbolBuilderFullName = symbolTableService.getSymbolBuilderFullName(type.getAstNode().get(),cdDefinitionSymbol);
          String symTabMillFullName = symbolTableService.getSymTabMillFullName(cdDefinitionSymbol);
          String symbolBuilderSimpleName = StringTransformations.uncapitalize(symbolTableService.getSymbolBuilderSimpleName(type.getAstNode().get()));
          ASTCDMethod builderMethod = getCDMethodFacade().createMethod(PUBLIC_STATIC,
              getCDTypeFacade().createQualifiedType(symbolBuilderFullName), symbolBuilderSimpleName);

          this.replaceTemplate(EMPTY_BODY, builderMethod, new StringHookPoint("return " + symTabMillFullName + "." + symbolBuilderSimpleName + "();"));
          builderMethodList.add(builderMethod);
        }
      }
    }
    return builderMethodList;
  }

  public boolean isLanguageTop() {
    return isLanguageTop;
  }

  public void setLanguageTop(boolean languageTop) {
    isLanguageTop = languageTop;
  }
}