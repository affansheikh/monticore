package de.monticore.codegen.cd2java._symboltable.scope;

import de.monticore.cd.cd4analysis._ast.*;
import de.monticore.codegen.cd2java.AbstractCreator;
import de.monticore.codegen.cd2java._symboltable.SymbolTableService;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.generating.templateengine.StringHookPoint;
import de.monticore.generating.templateengine.TemplateHookPoint;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

import java.util.ArrayList;
import java.util.List;

import static de.monticore.codegen.cd2java.CoreTemplates.EMPTY_BODY;
import static de.monticore.codegen.cd2java._symboltable.SymbolTableConstants.*;
import static de.monticore.codegen.cd2java.factories.CDModifier.PUBLIC;
import static de.monticore.codegen.cd2java.factories.CDModifier.PUBLIC_ABSTRACT;

public class GlobalScopeInterfaceDecorator extends AbstractCreator<ASTCDCompilationUnit, ASTCDInterface> {

  protected final SymbolTableService symbolTableService;


  public GlobalScopeInterfaceDecorator(final GlobalExtensionManagement glex,
                                       final SymbolTableService symbolTableService) {
    super(glex);
    this.symbolTableService = symbolTableService;
  }

  @Override
  public ASTCDInterface decorate(ASTCDCompilationUnit input) {
    String globalScopeInterfaceName = symbolTableService.getGlobalScopeInterfaceSimpleName();
    ASTMCQualifiedType scopeInterfaceType = symbolTableService.getScopeInterfaceType();
    String definitionName = input.getCDDefinition().getName();

    List<ASTCDType> symbolClasses = symbolTableService.getSymbolProds(input.getCDDefinition());

    return CD4AnalysisMill.cDInterfaceBuilder()
        .setName(globalScopeInterfaceName)
        .setModifier(PUBLIC.build())
        .addInterface(scopeInterfaceType)
        .addCDMethod(createGetModelPathMethod())
        .addCDMethod(createGetLanguageMethod(definitionName))
        .addCDMethod(createCacheMethod())
        .addCDMethod(creatCheckIfContinueAsSubScopeMethod())
        .addCDMethod(createContinueWithModelLoaderMethod(definitionName))
        .addCDMethod(createGetRealThisMethod(globalScopeInterfaceName))
        .addAllCDMethods(createResolveMethods(symbolClasses, definitionName))
        .build();
  }

  protected ASTCDMethod createCacheMethod() {
    ASTCDParameter parameter = getCDParameterFacade().createParameter(getCDTypeFacade().createStringType(), "calculatedModelName");
    return getCDMethodFacade().createMethod(PUBLIC_ABSTRACT, "cache", parameter);
  }

  protected ASTCDMethod createGetModelPathMethod() {
    ASTMCType modelPathType = getCDTypeFacade().createQualifiedType(MODEL_PATH_TYPE);
    return getCDMethodFacade().createMethod(PUBLIC_ABSTRACT, modelPathType, "getModelPath");
  }

  protected ASTCDMethod createGetLanguageMethod(String definitionName) {
    ASTMCType languageType = getCDTypeFacade().createQualifiedType(definitionName + LANGUAGE_SUFFIX);
    return getCDMethodFacade().createMethod(PUBLIC_ABSTRACT, languageType, "get" + definitionName + LANGUAGE_SUFFIX);
  }

  protected ASTCDMethod createContinueWithModelLoaderMethod(String definitionName) {
    ASTCDParameter modelNameParameter = getCDParameterFacade().createParameter(getCDTypeFacade().createStringType(), "calculatedModelName");
    ASTMCQualifiedType modelLoaderType = getCDTypeFacade().createQualifiedType(definitionName + MODEL_LOADER_SUFFIX);
    ASTCDParameter modelLoaderParameter = getCDParameterFacade().createParameter(modelLoaderType, "modelLoader");
    return getCDMethodFacade().createMethod(PUBLIC_ABSTRACT, getCDTypeFacade().createBooleanType(), "continueWithModelLoader", modelNameParameter, modelLoaderParameter);
  }

  protected ASTCDMethod creatCheckIfContinueAsSubScopeMethod() {
    ASTCDParameter modelNameParameter = getCDParameterFacade().createParameter(getCDTypeFacade().createStringType(), "symbolName");
    ASTCDMethod method = getCDMethodFacade().createMethod(PUBLIC, getCDTypeFacade().createBooleanType(), "checkIfContinueAsSubScope", modelNameParameter);
    this.replaceTemplate(EMPTY_BODY, method, new StringHookPoint("return false;"));
    return method;
  }

  protected ASTCDMethod createGetRealThisMethod(String globalScopeName) {
    ASTMCType globalScopeInterfaceType = getCDTypeFacade().createQualifiedType(globalScopeName);
    return getCDMethodFacade().createMethod(PUBLIC_ABSTRACT, globalScopeInterfaceType, "getRealThis");
  }

  protected List<ASTCDMethod> createResolveMethods(List<? extends ASTCDType> symbolProds, String definitionName) {
    List<ASTCDMethod> resolveMethods = new ArrayList<>();
    ASTCDParameter nameParameter = getCDParameterFacade().createParameter(String.class, "name");
    ASTCDParameter accessModifierParameter = getCDParameterFacade().createParameter(getCDTypeFacade().createQualifiedType(ACCESS_MODIFIER), "modifier");
    ASTCDParameter foundSymbolsParameter = getCDParameterFacade().createParameter(getCDTypeFacade().createBooleanType(), "foundSymbols");

    for (ASTCDType symbolProd : symbolProds) {
      String className = symbolTableService.removeASTPrefix(symbolProd);
      String symbolFullTypeName = symbolTableService.getSymbolFullName(symbolProd);
      ASTMCType listSymbol = getCDTypeFacade().createCollectionTypeOf(symbolFullTypeName);

      ASTCDParameter predicateParameter = getCDParameterFacade().createParameter(getCDTypeFacade()
          .createTypeByDefinition(PREDICATE + "<" + symbolFullTypeName + ">"), "predicate");


      resolveMethods.add(createResolveManyMethod(className, symbolFullTypeName, listSymbol, foundSymbolsParameter,
          nameParameter, accessModifierParameter, predicateParameter));

      resolveMethods.add(createResolveAdaptedMethod(className, listSymbol, foundSymbolsParameter,
          nameParameter, accessModifierParameter, predicateParameter));


      resolveMethods.add(createLoadModelsForMethod(className, definitionName, nameParameter));
    }

    return resolveMethods;
  }

  protected ASTCDMethod createResolveManyMethod(String className, String fullSymbolName, ASTMCType returnType,
                                                ASTCDParameter foundSymbolsParameter, ASTCDParameter nameParameter,
                                                ASTCDParameter accessModifierParameter, ASTCDParameter predicateParameter) {
    String methodName = String.format(RESOLVE_MANY, className);
    ASTCDMethod method = getCDMethodFacade().createMethod(PUBLIC, returnType, methodName,
        foundSymbolsParameter, nameParameter, accessModifierParameter, predicateParameter);
    this.replaceTemplate(EMPTY_BODY, method,
        new TemplateHookPoint("_symboltable.scope.iglobalscope.ResolveMany", className, fullSymbolName));
    return method;
  }

  protected ASTCDMethod createResolveAdaptedMethod(String className, ASTMCType returnType,
                                                   ASTCDParameter foundSymbolsParameter, ASTCDParameter nameParameter,
                                                   ASTCDParameter accessModifierParameter, ASTCDParameter predicateParameter) {
    String methodName = String.format(RESOLVE_ADAPTED, className);
    ASTCDMethod method = getCDMethodFacade().createMethod(PUBLIC, returnType, methodName,
        foundSymbolsParameter, nameParameter, accessModifierParameter, predicateParameter);
    this.replaceTemplate(EMPTY_BODY, method,
        new StringHookPoint("return com.google.common.collect.Lists.newArrayList();"));
    return method;
  }

  protected ASTCDMethod createLoadModelsForMethod(String className, String definitionName,
                                                  ASTCDParameter nameParameter) {
    String methodName = String.format(LOAD_MODELS_FOR, className);
    ASTCDMethod method = getCDMethodFacade().createMethod(PUBLIC, methodName, nameParameter);
    this.replaceTemplate(EMPTY_BODY, method,
        new TemplateHookPoint("_symboltable.scope.iglobalscope.LoadModelsFor", className, definitionName));
    return method;
  }
}

