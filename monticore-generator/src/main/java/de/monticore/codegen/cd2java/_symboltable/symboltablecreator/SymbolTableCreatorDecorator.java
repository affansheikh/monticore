package de.monticore.codegen.cd2java._symboltable.symboltablecreator;

import de.monticore.cd.cd4analysis._ast.*;
import de.monticore.cd.cd4code._ast.CD4CodeMill;
import de.monticore.codegen.cd2java.AbstractCreator;
import de.monticore.codegen.cd2java._symboltable.SymbolTableService;
import de.monticore.codegen.cd2java._visitor.VisitorService;
import de.monticore.codegen.cd2java.methods.MethodDecorator;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.generating.templateengine.StringHookPoint;
import de.monticore.generating.templateengine.TemplateHookPoint;
import de.se_rwth.commons.StringTransformations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static de.monticore.codegen.cd2java.CoreTemplates.EMPTY_BODY;
import static de.monticore.codegen.cd2java.CoreTemplates.VALUE;
import static de.monticore.codegen.cd2java._symboltable.SymbolTableConstants.DEQUE_TYPE;
import static de.monticore.codegen.cd2java._symboltable.SymbolTableConstants.DEQUE_WILDCARD_TYPE;
import static de.monticore.codegen.cd2java._visitor.VisitorConstants.END_VISIT;
import static de.monticore.codegen.cd2java._visitor.VisitorConstants.VISIT;
import static de.monticore.codegen.cd2java.factories.CDModifier.*;

public class SymbolTableCreatorDecorator extends AbstractCreator<ASTCDCompilationUnit, Optional<ASTCDClass>> {

  protected final SymbolTableService symbolTableService;

  protected final VisitorService visitorService;

  protected final MethodDecorator methodDecorator;

  public SymbolTableCreatorDecorator(final GlobalExtensionManagement glex,
                                     final SymbolTableService symbolTableService,
                                     final VisitorService visitorService,
                                     final MethodDecorator methodDecorator) {
    super(glex);
    this.visitorService = visitorService;
    this.symbolTableService = symbolTableService;
    this.methodDecorator = methodDecorator;
  }

  @Override
  public Optional<ASTCDClass> decorate(ASTCDCompilationUnit input) {
    Optional<ASTCDType> startProd = symbolTableService.getStartProd(input.getCDDefinition());
    if (startProd.isPresent()) {
      String astFullName = symbolTableService.getASTPackage() + "." + startProd.get().getName();
      String symbolTableCreator = symbolTableService.getSymbolTableCreatorSimpleName();
      String visitorSimpleName = visitorService.getVisitorFullName();
      String scopeInterface = symbolTableService.getScopeInterfaceFullName();
      String dequeType = String.format(DEQUE_TYPE, scopeInterface);
      String dequeWildcardType = String.format(DEQUE_WILDCARD_TYPE, scopeInterface);

      String simpleName = symbolTableService.removeASTPrefix(startProd.get());
      List<ASTCDType> symbolDefiningClasses = symbolTableService.getSymbolDefiningClasses(input.getCDDefinition().getCDClassList());
      List<ASTCDType> noSymbolDefiningClasses = symbolTableService.getNoSymbolDefiningClasses(input.getCDDefinition().getCDClassList());
      List<ASTCDType> symbolDefiningProds = symbolTableService.getSymbolDefiningProds(input.getCDDefinition());

      ASTCDAttribute realThisAttribute = createRealThisAttribute(visitorSimpleName);
      List<ASTCDMethod> realThisMethods = methodDecorator.decorate(realThisAttribute);

      ASTCDAttribute firstCreatedScopeAttribute = createFirstCreatedScopeAttribute(scopeInterface);
      List<ASTCDMethod> firstCreatedScopeMethod = methodDecorator.getAccessorDecorator().decorate(firstCreatedScopeAttribute);

      ASTCDClass symTabCreator = CD4CodeMill.cDClassBuilder()
          .setName(symbolTableCreator)
          .setModifier(PUBLIC.build())
          .addInterface(getCDTypeFacade().createQualifiedType(visitorSimpleName))
          .addCDConstructor(createSimpleConstructor(symbolTableCreator, scopeInterface))
          .addCDConstructor(createDequeConstructor(symbolTableCreator, dequeWildcardType, dequeType))
          .addCDAttribute(createScopeStackAttribute(dequeType))
          .addCDAttribute(realThisAttribute)
          .addAllCDMethods(realThisMethods)
          .addCDAttribute(firstCreatedScopeAttribute)
          .addAllCDMethods(firstCreatedScopeMethod)
          .addCDMethod(createCreateFromASTMethod(astFullName, symbolTableCreator))
          .addCDMethod(createPutOnStackMethod(scopeInterface))
          .addAllCDMethods(createCurrentScopeMethods(scopeInterface))
          .addCDMethod(createSetScopeStackMethod(dequeType, simpleName))
          .addCDMethod(createCreateScopeMethod(scopeInterface, input.getCDDefinition().getName()))
          .addAllCDMethods(createSymbolClassMethods(symbolDefiningClasses, scopeInterface))
          .addAllCDMethods(createVisitForNoSymbolMethods(noSymbolDefiningClasses))
          .addAllCDMethods(createAddToScopeMethods(symbolDefiningProds))
          .build();
      return Optional.ofNullable(symTabCreator);
    }
    return Optional.empty();
  }

  protected ASTCDConstructor createSimpleConstructor(String symTabCreator, String scopeInterface) {
    ASTCDParameter enclosingScope = getCDParameterFacade().createParameter(getCDTypeFacade().createQualifiedType(scopeInterface), "enclosingScope");
    ASTCDConstructor constructor = getCDConstructorFacade().createConstructor(PUBLIC.build(), symTabCreator, enclosingScope);
    this.replaceTemplate(EMPTY_BODY, constructor, new StringHookPoint("putOnStack(Log.errorIfNull(enclosingScope));"));
    return constructor;
  }

  protected ASTCDConstructor createDequeConstructor(String symTabCreator, String dequeWildcardType, String dequeType) {
    ASTCDParameter enclosingScope = getCDParameterFacade().createParameter(getCDTypeFacade().createTypeByDefinition(dequeWildcardType), "scopeStack");
    ASTCDConstructor constructor = getCDConstructorFacade().createConstructor(PUBLIC.build(), symTabCreator, enclosingScope);
    this.replaceTemplate(EMPTY_BODY, constructor, new
        StringHookPoint("this.scopeStack = Log.errorIfNull((" + dequeType + ")scopeStack);"));
    return constructor;
  }

  protected ASTCDAttribute createScopeStackAttribute(String dequeType) {
    ASTCDAttribute scopeStack = getCDAttributeFacade().createAttribute(PROTECTED, dequeType, "scopeStack");
    this.replaceTemplate(VALUE, scopeStack, new StringHookPoint("= new java.util.ArrayDeque<>()"));
    return scopeStack;
  }

  protected ASTCDAttribute createFirstCreatedScopeAttribute(String scopeInterface) {
    return getCDAttributeFacade().createAttribute(PROTECTED, scopeInterface, "firstCreatedScope");
  }

  protected ASTCDAttribute createRealThisAttribute(String visitor) {
    ASTCDAttribute scopeStack = getCDAttributeFacade().createAttribute(PRIVATE, visitor, "realThis");
    this.replaceTemplate(VALUE, scopeStack, new StringHookPoint("= this"));
    return scopeStack;
  }

  protected ASTCDMethod createCreateFromASTMethod(String astStartProd, String symbolTableCreator) {
    String artifactScopeFullName = symbolTableService.getArtifactScopeFullName();
    ASTCDParameter rootNodeParam = getCDParameterFacade().createParameter(getCDTypeFacade().createQualifiedType(astStartProd), "rootNode");
    ASTCDMethod createFromAST = getCDMethodFacade().createMethod(PUBLIC,
        getCDTypeFacade().createQualifiedType(artifactScopeFullName), "createFromAST", rootNodeParam);
    this.replaceTemplate(EMPTY_BODY, createFromAST, new TemplateHookPoint(
        "_symboltable.symboltablecreator.CreateFromAST", artifactScopeFullName, symbolTableCreator));
    return createFromAST;
  }

  protected ASTCDMethod createPutOnStackMethod(String scopeInterface) {
    ASTCDParameter scopeParam = getCDParameterFacade().createParameter(getCDTypeFacade().createQualifiedType(scopeInterface), "scope");
    ASTCDMethod createFromAST = getCDMethodFacade().createMethod(PUBLIC, "putOnStack", scopeParam);
    this.replaceTemplate(EMPTY_BODY, createFromAST, new TemplateHookPoint(
        "_symboltable.symboltablecreator.PutOnStack"));
    return createFromAST;
  }

  protected List<ASTCDMethod> createCurrentScopeMethods(String scopeInterface) {
    ASTCDMethod getCurrentScope = getCDMethodFacade().createMethod(PUBLIC_FINAL,
        getCDTypeFacade().createOptionalTypeOf(scopeInterface), "getCurrentScope");
    this.replaceTemplate(EMPTY_BODY, getCurrentScope, new StringHookPoint(
        "return Optional.ofNullable(scopeStack.peekLast());"));

    ASTCDMethod removeCurrentScope = getCDMethodFacade().createMethod(PUBLIC_FINAL,
        getCDTypeFacade().createOptionalTypeOf(scopeInterface), "removeCurrentScope");
    this.replaceTemplate(EMPTY_BODY, removeCurrentScope, new StringHookPoint(
        "return Optional.ofNullable(scopeStack.pollLast());"));
    return new ArrayList<>(Arrays.asList(getCurrentScope, removeCurrentScope));
  }

  protected ASTCDMethod createSetScopeStackMethod(String dequeType, String simpleName) {
    ASTCDParameter dequeParam = getCDParameterFacade().createParameter(getCDTypeFacade().createTypeByDefinition(dequeType), "scopeStack");
    ASTCDMethod createFromAST = getCDMethodFacade().createMethod(PROTECTED,
        "set" + StringTransformations.capitalize(simpleName) + "ScopeStack", dequeParam);
    this.replaceTemplate(EMPTY_BODY, createFromAST, new StringHookPoint(
        "this.scopeStack = scopeStack;"));
    return createFromAST;
  }

  protected ASTCDMethod createCreateScopeMethod(String scopeInterfaceName, String definitionName) {
    String symTabMill = symbolTableService.getSymTabMillFullName();
    ASTCDParameter boolParam = getCDParameterFacade().createParameter(getCDTypeFacade().createBooleanType(), "shadowing");
    ASTCDMethod createFromAST = getCDMethodFacade().createMethod(PUBLIC, getCDTypeFacade().createQualifiedType(scopeInterfaceName),
        "createScope", boolParam);
    this.replaceTemplate(EMPTY_BODY, createFromAST, new TemplateHookPoint(
        "_symboltable.symboltablecreator.CreateScope", scopeInterfaceName, symTabMill, definitionName));
    return createFromAST;
  }

  protected List<ASTCDMethod> createSymbolClassMethods(List<ASTCDType> symbolClasses, String scopeInterface) {
    List<ASTCDMethod> methodList = new ArrayList<>();
    for (ASTCDType symbolClass : symbolClasses) {
      String astFullName = symbolTableService.getASTPackage() + "." + symbolClass.getName();
      String symbolFullName = symbolTableService.getSymbolFullName(symbolClass);
      String simpleName = symbolTableService.removeASTPrefix(symbolClass);
      String scopeClassFullName = symbolTableService.getScopeClassFullName();

      ASTCDMethod visitMethod = visitorService.getVisitorMethod(VISIT, getCDTypeFacade().createQualifiedType(astFullName));
      this.replaceTemplate(EMPTY_BODY, visitMethod, new TemplateHookPoint(
          "_symboltable.symboltablecreator.Visit", symbolFullName, simpleName));
      methodList.add(visitMethod);

      ASTCDMethod endVisitMethod = visitorService.getVisitorMethod(END_VISIT, getCDTypeFacade().createQualifiedType(astFullName));
      this.replaceTemplate(EMPTY_BODY, endVisitMethod, new StringHookPoint("removeCurrentScope();"));
      methodList.add(endVisitMethod);

      ASTCDParameter astParam = getCDParameterFacade().createParameter(getCDTypeFacade().createQualifiedType(astFullName), "ast");
      ASTCDMethod createSymbolMethod = getCDMethodFacade().createMethod(PROTECTED, getCDTypeFacade().createQualifiedType(symbolFullName),
          "create_" + simpleName, astParam);
      this.replaceTemplate(EMPTY_BODY, createSymbolMethod, new StringHookPoint("return new " + symbolFullName + "(ast.getName());"));
      methodList.add(createSymbolMethod);

      ASTCDParameter symbolParam = getCDParameterFacade().createParameter(getCDTypeFacade().createQualifiedType(symbolFullName), "symbol");
      methodList.add(getCDMethodFacade().createMethod(PROTECTED, "initialize_" + simpleName, symbolParam, astParam));

      if (symbolClass.getModifierOpt().isPresent()) {
        boolean isScopeSpanningSymbol = symbolTableService.hasScopeStereotype(symbolClass.getModifierOpt().get()) &&
            symbolTableService.hasSymbolStereotype(symbolClass.getModifierOpt().get());
        ASTCDMethod addToScopeAnLinkWithNode = getCDMethodFacade().createMethod(PUBLIC, "addToScopeAndLinkWithNode", symbolParam, astParam);
        this.replaceTemplate(EMPTY_BODY, addToScopeAnLinkWithNode, new TemplateHookPoint(
            "_symboltable.symboltablecreator.AddToScopeAndLinkWithNode", scopeInterface, isScopeSpanningSymbol));
        methodList.add(addToScopeAnLinkWithNode);

        ASTCDMethod setLinkBetweenSymbolAndNode = getCDMethodFacade().createMethod(PUBLIC, "setLinkBetweenSymbolAndNode", symbolParam, astParam);
        this.replaceTemplate(EMPTY_BODY, setLinkBetweenSymbolAndNode, new TemplateHookPoint(
            "_symboltable.symboltablecreator.SetLinkBetweenSymbolAndNode", simpleName, isScopeSpanningSymbol));
        methodList.add(setLinkBetweenSymbolAndNode);

        if(isScopeSpanningSymbol){
          ASTCDParameter scopeParam = getCDParameterFacade().createParameter(getCDTypeFacade().createQualifiedType(scopeInterface), "scope");
          ASTCDMethod setLinkBetweenSpannedScopeAndNode = getCDMethodFacade().createMethod(PUBLIC, "setLinkBetweenSpannedScopeAndNode", scopeParam, astParam);
          this.replaceTemplate(EMPTY_BODY, setLinkBetweenSpannedScopeAndNode, new TemplateHookPoint(
              "_symboltable.symboltablecreator.SetLinkBetweenSpannedScopeAndNode", scopeClassFullName));
          methodList.add(setLinkBetweenSpannedScopeAndNode);
        }
      }


    }
    return methodList;
  }

  protected List<ASTCDMethod> createVisitForNoSymbolMethods(List<ASTCDType> astcdClasses) {
    List<ASTCDMethod> methodList = new ArrayList<>();
    for (ASTCDType astcdClass : astcdClasses) {
      String astFullName = symbolTableService.getASTPackage() + "." + astcdClass.getName();
      ASTCDMethod visitorMethod = visitorService.getVisitorMethod(VISIT, getCDTypeFacade().createQualifiedType(astFullName));
      this.replaceTemplate(EMPTY_BODY, visitorMethod, new TemplateHookPoint("_symboltable.symboltablecreator.VisitNoSymbol"));
      methodList.add(visitorMethod);
    }
    return methodList;
  }

  protected List<ASTCDMethod> createAddToScopeMethods(List<ASTCDType> astcdClasses) {
    List<ASTCDMethod> methodList = new ArrayList<>();
    for (ASTCDType astcdClass : astcdClasses) {
      String symbolFullName = symbolTableService.getSymbolFullName(astcdClass);
      ASTCDParameter symbolParam = getCDParameterFacade().createParameter(getCDTypeFacade().createQualifiedType(symbolFullName), "symbol");
      ASTCDMethod addToScopeMethod = getCDMethodFacade().createMethod(PUBLIC, "addToScope", symbolParam);
      this.replaceTemplate(EMPTY_BODY, addToScopeMethod, new TemplateHookPoint("_symboltable.symboltablecreator.AddToScope"));
      methodList.add(addToScopeMethod);
    }
    return methodList;
  }
}