/* (c) https://github.com/MontiCore/monticore */
package de.monticore.codegen.cd2java._symboltable.scope;

import de.monticore.cd.cd4analysis.CD4AnalysisMill;
import de.monticore.cd.cd4analysis._ast.*;
import de.monticore.cd.cd4analysis._symboltable.CDDefinitionSymbol;
import de.monticore.cd.cd4analysis._symboltable.CDTypeSymbol;
import de.monticore.codegen.cd2java.AbstractCreator;
import de.monticore.codegen.cd2java._symboltable.SymbolTableService;
import de.monticore.codegen.cd2java._visitor.VisitorService;
import de.monticore.codegen.cd2java.methods.MethodDecorator;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.generating.templateengine.StringHookPoint;
import de.monticore.generating.templateengine.TemplateHookPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.monticore.cd.facade.CDModifier.PRIVATE;
import static de.monticore.cd.facade.CDModifier.PUBLIC;
import static de.monticore.codegen.cd2java.CoreTemplates.EMPTY_BODY;
import static de.monticore.codegen.cd2java._ast.ast_class.ASTConstants.ACCEPT_METHOD;
import static de.monticore.codegen.cd2java._symboltable.SymbolTableConstants.*;
import static de.monticore.codegen.cd2java._visitor.VisitorConstants.VISITOR_PREFIX;

/**
 * creates a artifactScope class from a grammar
 */
public class ArtifactScopeDecorator extends AbstractCreator<ASTCDCompilationUnit, ASTCDClass> {

  protected final SymbolTableService symbolTableService;

  protected final MethodDecorator methodDecorator;

  protected final VisitorService visitorService;

  /**
   * flag added to define if the ArtifactScope class was overwritten with the TOP mechanism
   * if top mechanism was used, must use setter to set flag true, before the decoration
   * is needed for different accept method implementations
   */
  protected boolean isArtifactScopeTop;

  protected static final String TEMPLATE_PATH = "_symboltable.artifactscope.";

  public ArtifactScopeDecorator(final GlobalExtensionManagement glex,
                                final SymbolTableService symbolTableService,
                                final VisitorService visitorService,
                                final MethodDecorator methodDecorator) {
    super(glex);
    this.symbolTableService = symbolTableService;
    this.visitorService = visitorService;
    this.methodDecorator = methodDecorator;
  }

  @Override
  public ASTCDClass decorate(ASTCDCompilationUnit input) {
    String artifactScopeSimpleName = symbolTableService.getArtifactScopeSimpleName();
    String scopeClassFullName = symbolTableService.getScopeClassFullName();

    ASTCDAttribute packageNameAttribute = createPackageNameAttribute();
    List<ASTCDMethod> packageNameMethods = methodDecorator.decorate(packageNameAttribute);

    ASTCDAttribute importsAttribute = createImportsAttribute();
    List<ASTCDMethod> importsMethods = methodDecorator.decorate(importsAttribute);

    List<ASTCDType> symbolProds = symbolTableService.getSymbolDefiningProds(input.getCDDefinition());

    return CD4AnalysisMill.cDClassBuilder()
        .setName(artifactScopeSimpleName)
        .setModifier(PUBLIC.build())
        .setSuperclass(getMCTypeFacade().createQualifiedType(scopeClassFullName))
        .addInterface(getMCTypeFacade().createQualifiedType(I_ARTIFACT_SCOPE_TYPE))
        .addAllCDConstructors(createConstructors(artifactScopeSimpleName))
        .addCDAttribute(packageNameAttribute)
        .addAllCDMethods(packageNameMethods)
        .addCDAttribute(importsAttribute)
        .addAllCDMethods(importsMethods)
        .addCDMethod(createGetNameMethod())
        .addCDMethod(createIsPresentNameMethod())
        .addCDMethod(createGetTopLevelSymbolMethod(symbolProds))
        .addCDMethod(createCheckIfContinueAsSubScopeMethod())
        .addCDMethod(createGetRemainingNameForResolveDownMethod())
        .addAllCDMethods(createContinueWithEnclosingScopeMethods(symbolProds, symbolTableService.getCDSymbol()))
        .addAllCDMethods(createSuperContinueWithEnclosingScopeMethods())
        .addCDMethod(createAcceptMethod(artifactScopeSimpleName))
        .build();
  }

  protected List<ASTCDConstructor> createConstructors(String artifactScopeName) {
    ASTCDParameter packageNameParam = getCDParameterFacade().createParameter(String.class, PACKAGE_NAME_VAR);
    ASTCDParameter importsParam = getCDParameterFacade().createParameter(getMCTypeFacade().createListTypeOf(IMPORT_STATEMENT), "imports");

    ASTCDConstructor constructor = getCDConstructorFacade().createConstructor(PUBLIC.build(), artifactScopeName, packageNameParam, importsParam);
    this.replaceTemplate(EMPTY_BODY, constructor, new StringHookPoint("this(Optional.empty(), packageName, imports);"));

    ASTCDParameter enclosingScopeParam = getCDParameterFacade().createParameter(
        getMCTypeFacade().createOptionalTypeOf(symbolTableService.getScopeInterfaceFullName()), ENCLOSING_SCOPE_VAR);
    ASTCDConstructor constructorWithEnclosingScope = getCDConstructorFacade().createConstructor(PUBLIC.build(),
        artifactScopeName, enclosingScopeParam, packageNameParam, importsParam);
    this.replaceTemplate(EMPTY_BODY, constructorWithEnclosingScope, new TemplateHookPoint(TEMPLATE_PATH + "ConstructorArtifactScope"));
    return new ArrayList<>(Arrays.asList(constructor, constructorWithEnclosingScope));
  }

  protected ASTCDAttribute createPackageNameAttribute() {
    return getCDAttributeFacade().createAttribute(PRIVATE, String.class, PACKAGE_NAME_VAR);
  }

  protected ASTCDAttribute createImportsAttribute() {
    ASTModifier modifier = PRIVATE.build();
    symbolTableService.addDeprecatedStereotype(modifier, Optional.empty());
    return getCDAttributeFacade()
        .createAttribute(modifier, getMCTypeFacade().createListTypeOf(IMPORT_STATEMENT), "imports");
  }

  protected ASTCDMethod createGetNameMethod() {
    ASTCDMethod getNameMethod = getCDMethodFacade().createMethod(PUBLIC, getMCTypeFacade().createStringType(), "getName");
    this.replaceTemplate(EMPTY_BODY, getNameMethod, new TemplateHookPoint(TEMPLATE_PATH + "GetName"));
    return getNameMethod;
  }

  /**
   * Creates the isPresentName method for artifact scopes.
   *
   * @return The isPresentName method.
   */
  protected ASTCDMethod createIsPresentNameMethod() {
    ASTCDMethod getNameMethod = getCDMethodFacade().createMethod(PUBLIC, getMCTypeFacade().createBooleanType(), "isPresentName");
    this.replaceTemplate(EMPTY_BODY, getNameMethod, new TemplateHookPoint(TEMPLATE_PATH + "IsPresentName"));
    return getNameMethod;
  }

  protected ASTCDMethod createGetTopLevelSymbolMethod(List<ASTCDType> symbolProds) {
    ArrayList<ASTCDType> symbolProdsWithSuper = new ArrayList<>(symbolProds);
    symbolProdsWithSuper.addAll(getSuperSymbols());
    List<String> symbolNames = symbolProdsWithSuper
        .stream()
        .map(ASTCDType::getName)
        .map(symbolTableService::removeASTPrefix)
        .collect(Collectors.toList());
    ASTCDMethod getTopLevelSymbol = getCDMethodFacade().createMethod(PUBLIC, getMCTypeFacade().createOptionalTypeOf(I_SYMBOL), "getTopLevelSymbol");
    this.replaceTemplate(EMPTY_BODY, getTopLevelSymbol, new TemplateHookPoint(TEMPLATE_PATH + "GetTopLevelSymbol", symbolNames));
    return getTopLevelSymbol;
  }

  protected ASTCDMethod createCheckIfContinueAsSubScopeMethod() {
    ASTCDParameter symbolNameParam = getCDParameterFacade().createParameter(String.class, "symbolName");
    ASTCDMethod getNameMethod = getCDMethodFacade().createMethod(PUBLIC, getMCTypeFacade().createBooleanType(), "checkIfContinueAsSubScope", symbolNameParam);
    this.replaceTemplate(EMPTY_BODY, getNameMethod, new TemplateHookPoint(TEMPLATE_PATH + "CheckIfContinueAsSubScope"));
    return getNameMethod;
  }

  protected ASTCDMethod createGetRemainingNameForResolveDownMethod() {
    ASTCDParameter parameter = getCDParameterFacade().createParameter(String.class, "symbolName");
    ASTCDMethod getRemainingNameForResolveDown = getCDMethodFacade().createMethod(PUBLIC, getMCTypeFacade().createStringType(), "getRemainingNameForResolveDown", parameter);
    this.replaceTemplate(EMPTY_BODY, getRemainingNameForResolveDown, new TemplateHookPoint(TEMPLATE_PATH + "GetRemainingNameForResolveDown"));
    return getRemainingNameForResolveDown;
  }

  protected List<ASTCDMethod> createContinueWithEnclosingScopeMethods(List<ASTCDType> symbolProds, CDDefinitionSymbol definitionSymbol) {
    List<ASTCDMethod> methodList = new ArrayList<>();
    ASTCDParameter parameterFoundSymbols = getCDParameterFacade().createParameter(getMCTypeFacade().createBooleanType(), FOUND_SYMBOLS_VAR);
    ASTCDParameter parameterName = getCDParameterFacade().createParameter(getMCTypeFacade().createStringType(), NAME_VAR);
    ASTCDParameter parameterModifier = getCDParameterFacade().createParameter(getMCTypeFacade().createQualifiedType(ACCESS_MODIFIER), MODIFIER_VAR);
    String globalScope = symbolTableService.getGlobalScopeFullName();

    for (ASTCDType type : symbolProds) {
      Optional<String> definingSymbolFullName = symbolTableService.getDefiningSymbolFullName(type, definitionSymbol);
      String className = symbolTableService.removeASTPrefix(type);

      if (definingSymbolFullName.isPresent()) {
        ASTCDParameter parameterPredicate = getCDParameterFacade().createParameter(getMCTypeFacade().createBasicGenericTypeOf(
            PREDICATE, definingSymbolFullName.get()), PREDICATE_VAR);
        String methodName = String.format(CONTINUE_WITH_ENCLOSING_SCOPE, className);

        ASTCDMethod method = getCDMethodFacade().createMethod(PUBLIC, getMCTypeFacade().createListTypeOf(definingSymbolFullName.get()),
            methodName, parameterFoundSymbols, parameterName, parameterModifier, parameterPredicate);
        this.replaceTemplate(EMPTY_BODY, method, new TemplateHookPoint(
            TEMPLATE_PATH + "ContinueWithEnclosingScope4ArtifactScope", definingSymbolFullName.get(), className, globalScope));
        methodList.add(method);
      }
    }
    return methodList;
  }

  protected List<ASTCDMethod> createSuperContinueWithEnclosingScopeMethods() {
    List<ASTCDMethod> methodList = new ArrayList<>();
    for (CDDefinitionSymbol cdDefinitionSymbol : symbolTableService.getSuperCDsTransitive()) {
      // only filter for types which define a symbol
      List<ASTCDType> symbolProds = cdDefinitionSymbol.getTypes()
          .stream()
          .filter(t -> t.isPresentAstNode())
          .filter(t -> t.getAstNode().isPresentModifier())
          .filter(t -> symbolTableService.hasSymbolStereotype(t.getAstNode().getModifier()))
          .filter(CDTypeSymbol::isPresentAstNode)
          .map(CDTypeSymbol::getAstNode)
          .collect(Collectors.toList());
      methodList.addAll(createContinueWithEnclosingScopeMethods(symbolProds, cdDefinitionSymbol));
    }
    return methodList;
  }

  protected ASTCDMethod createAcceptMethod(String artifactScopeName) {
    String visitor = visitorService.getVisitorFullName();
    ASTCDParameter parameter = getCDParameterFacade().createParameter(getMCTypeFacade().createQualifiedType(visitor), VISITOR_PREFIX);
    ASTCDMethod acceptMethod = getCDMethodFacade().createMethod(PUBLIC, ACCEPT_METHOD, parameter);
    if (!isArtifactScopeTop()) {
      this.replaceTemplate(EMPTY_BODY, acceptMethod, new StringHookPoint("visitor.handle(this);"));
    } else {
      String errorCode = symbolTableService.getGeneratedErrorCode(artifactScopeName);
      this.replaceTemplate(EMPTY_BODY, acceptMethod, new TemplateHookPoint(
          "_symboltable.AcceptTop", artifactScopeName, errorCode));
    }
    return acceptMethod;
  }


  public boolean isArtifactScopeTop() {
    return isArtifactScopeTop;
  }

  public void setArtifactScopeTop(boolean artifactScopeTop) {
    isArtifactScopeTop = artifactScopeTop;
  }


  public List<ASTCDType> getSuperSymbols() {
    List<ASTCDType> symbolAttributes = new ArrayList<>();
    for (CDDefinitionSymbol cdDefinitionSymbol : symbolTableService.getSuperCDsTransitive()) {
      for (CDTypeSymbol type : cdDefinitionSymbol.getTypes()) {
        if (type.isPresentAstNode() && type.getAstNode().isPresentModifier()
            && symbolTableService.hasSymbolStereotype(type.getAstNode().getModifier())) {
          symbolAttributes.add(type.getAstNode());
        }
      }
    }
    return symbolAttributes;
  }
}
