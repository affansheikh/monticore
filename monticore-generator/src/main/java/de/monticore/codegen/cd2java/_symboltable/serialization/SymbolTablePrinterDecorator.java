package de.monticore.codegen.cd2java._symboltable.serialization;

import de.monticore.cd.cd4analysis._ast.*;
import de.monticore.cd.cd4code._ast.CD4CodeMill;
import de.monticore.codegen.cd2java.AbstractCreator;
import de.monticore.codegen.cd2java._symboltable.SymbolTableService;
import de.monticore.codegen.cd2java._visitor.VisitorService;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.generating.templateengine.StringHookPoint;
import de.monticore.generating.templateengine.TemplateHookPoint;
import de.monticore.types.mccollectiontypes._ast.ASTMCListType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.monticore.codegen.cd2java.CoreTemplates.EMPTY_BODY;
import static de.monticore.codegen.cd2java.CoreTemplates.VALUE;
import static de.monticore.codegen.cd2java._symboltable.SymbolTableConstants.I_SCOPE_SPANNING_SYMBOL;
import static de.monticore.codegen.cd2java._symboltable.SymbolTableConstants.JSON_PRINTER;
import static de.monticore.codegen.cd2java._visitor.VisitorConstants.*;
import static de.monticore.codegen.cd2java.factories.CDModifier.PROTECTED;
import static de.monticore.codegen.cd2java.factories.CDModifier.PUBLIC;

public class SymbolTablePrinterDecorator extends AbstractCreator<ASTCDCompilationUnit, ASTCDClass> {

  protected final SymbolTableService symbolTableService;

  protected final VisitorService visitorService;

  public SymbolTablePrinterDecorator(final GlobalExtensionManagement glex,
                                     final SymbolTableService symbolTableService,
                                     final VisitorService visitorService) {
    super(glex);
    this.symbolTableService = symbolTableService;
    this.visitorService = visitorService;
  }

  @Override
  public ASTCDClass decorate(ASTCDCompilationUnit input) {
    String symbolTablePrinterName = symbolTableService.getSymbolTablePrinterSimpleName();
    String scopeInterfaceFullName = symbolTableService.getScopeInterfaceFullName();
    String artifactScopeFullName = symbolTableService.getArtifactScopeFullName();
    String scopeClassFullName = symbolTableService.getScopeClassFullName();
    List<ASTCDType> symbolDefiningProds = symbolTableService.getSymbolDefiningProds(input.getCDDefinition());
    String symbolVisitorFullName = visitorService.getSymbolVisitorFullName();
    String scopeVisitorFullName = visitorService.getScopeVisitorFullName();
    return CD4CodeMill.cDClassBuilder()
        .setName(symbolTablePrinterName)
        .setModifier(PUBLIC.build())
        .addInterface(getCDTypeFacade().createQualifiedType(symbolVisitorFullName))
        .addInterface(getCDTypeFacade().createQualifiedType(scopeVisitorFullName))
        .addCDAttribute(createJsonPrinterAttribute())
        .addCDMethod(createRealThisMethod(symbolTablePrinterName))
        .addCDMethod(createGetSerializedStringMethod())
        .addCDMethod(createFilterRelevantSubScopesMethod(scopeInterfaceFullName))
        .addCDMethod(createHasSymbolsInSubScopesMethod(scopeInterfaceFullName))
        .addCDMethod(createAddScopeSpanningSymbolMethod())
        .addAllCDMethods(createScopeVisitorMethods(artifactScopeFullName, scopeClassFullName,
            scopeInterfaceFullName, symbolDefiningProds, input.getCDDefinition()))
        .addAllCDMethods(createSymbolVisitorMethods(symbolDefiningProds))
        .build();
  }

  protected ASTCDAttribute createJsonPrinterAttribute() {
    ASTCDAttribute printerAttribute = getCDAttributeFacade().createAttribute(PROTECTED, JSON_PRINTER, "printer");
    this.replaceTemplate(VALUE, printerAttribute, new StringHookPoint("= new " + JSON_PRINTER + "();"));
    return printerAttribute;
  }

  protected ASTCDMethod createRealThisMethod(String symbolTablePrinterName) {
    ASTCDMethod method = getCDMethodFacade().createMethod(PUBLIC, getCDTypeFacade().createQualifiedType(symbolTablePrinterName), GET_REAL_THIS);
    this.replaceTemplate(EMPTY_BODY, method, new StringHookPoint("return this;"));
    return method;
  }

  protected ASTCDMethod createGetSerializedStringMethod() {
    ASTCDMethod method = getCDMethodFacade().createMethod(PUBLIC, getCDTypeFacade().createStringType(), "getSerializedString");
    this.replaceTemplate(EMPTY_BODY, method, new StringHookPoint("return this.printer.getContent();"));
    return method;
  }

  protected ASTCDMethod createFilterRelevantSubScopesMethod(String scopeInterfaceName) {
    ASTMCListType listTypeOfScopeInterface = getCDTypeFacade().createListTypeOf(scopeInterfaceName);
    ASTCDParameter subScopesParam = getCDParameterFacade().createParameter(listTypeOfScopeInterface, "subScopes");
    ASTCDMethod method = getCDMethodFacade().createMethod(PROTECTED, listTypeOfScopeInterface, "filterRelevantSubScopes", subScopesParam);
    this.replaceTemplate(EMPTY_BODY, method, new TemplateHookPoint("_symboltable.serialization.symbolTablePrinter.FilterRelevantSubScopes", scopeInterfaceName));
    return method;
  }

  protected ASTCDMethod createHasSymbolsInSubScopesMethod(String scopeInterfaceName) {
    ASTCDParameter scopeParam = getCDParameterFacade().createParameter(getCDTypeFacade().createQualifiedType(scopeInterfaceName), "scope");
    ASTCDMethod method = getCDMethodFacade().createMethod(PROTECTED, getCDTypeFacade().createBooleanType(), "hasSymbolsInSubScopes", scopeParam);
    this.replaceTemplate(EMPTY_BODY, method, new TemplateHookPoint("_symboltable.serialization.symbolTablePrinter.HasSymbolsInSubScopes", scopeInterfaceName));
    return method;
  }

  protected ASTCDMethod createAddScopeSpanningSymbolMethod() {
    ASTCDParameter scopeParam = getCDParameterFacade().createParameter(getCDTypeFacade().createOptionalTypeOf(I_SCOPE_SPANNING_SYMBOL), "spanningSymbol");
    ASTCDMethod method = getCDMethodFacade().createMethod(PROTECTED, "addScopeSpanningSymbol", scopeParam);
    this.replaceTemplate(EMPTY_BODY, method, new TemplateHookPoint("_symboltable.serialization.symbolTablePrinter.AddScopeSpanningSymbol"));
    return method;
  }

  protected List<ASTCDMethod> createScopeVisitorMethods(String artifactScopeName, String scopeName,
                                                        String scopeInterfaceName, List<ASTCDType> symbolProds,
                                                        ASTCDDefinition astcdDefinition) {
    List<ASTCDMethod> visitorMethods = new ArrayList<>();
    if (symbolTableService.hasStartProd(astcdDefinition)) {
      visitorMethods.add(createVisitArtifactScopeMethod(artifactScopeName));
      visitorMethods.add(createEndVisitArtifactScopeMethod(artifactScopeName));
      visitorMethods.add(createTraverseArtifactScopeMethod(artifactScopeName, scopeName));
    }

    visitorMethods.add(createVisitScopeMethod(scopeName));
    visitorMethods.add(createEndVisitScopeMethod(scopeName));
    visitorMethods.add(createTraverseScopeMethod(scopeName, scopeInterfaceName, symbolProds));
    return visitorMethods;
  }

  protected ASTCDMethod createVisitArtifactScopeMethod(String artifactScopeFullName) {
    ASTCDMethod visitorMethod = visitorService.getVisitorMethod(VISIT, getCDTypeFacade().createQualifiedType(artifactScopeFullName));
    this.replaceTemplate(EMPTY_BODY, visitorMethod, new TemplateHookPoint(
        "_symboltable.serialization.symbolTablePrinter.VisitArtifactScope", artifactScopeFullName));
    return visitorMethod;
  }

  protected ASTCDMethod createVisitScopeMethod(String scopeName) {
    ASTCDMethod visitorMethod = visitorService.getVisitorMethod(VISIT, getCDTypeFacade().createQualifiedType(scopeName));
    this.replaceTemplate(EMPTY_BODY, visitorMethod, new TemplateHookPoint(
        "_symboltable.serialization.symbolTablePrinter.VisitScope", scopeName));
    return visitorMethod;
  }

  protected ASTCDMethod createTraverseArtifactScopeMethod(String artifactScopeFullName, String scopeFullName) {
    ASTCDMethod visitorMethod = visitorService.getVisitorMethod(TRAVERSE, getCDTypeFacade().createQualifiedType(artifactScopeFullName));
    this.replaceTemplate(EMPTY_BODY, visitorMethod, new StringHookPoint(" traverse((" + scopeFullName + ") node);"));
    return visitorMethod;
  }

  protected ASTCDMethod createTraverseScopeMethod(String scopeName, String scopeInterfaceName, List<ASTCDType> symbolProds) {
    List<String> simpleSymbolNames = symbolProds.stream()
        .map(symbolTableService::removeASTPrefix)
        .collect(Collectors.toList());
    ASTCDMethod visitorMethod = visitorService.getVisitorMethod(TRAVERSE, getCDTypeFacade().createQualifiedType(scopeName));
    this.replaceTemplate(EMPTY_BODY, visitorMethod, new TemplateHookPoint(
        "_symboltable.serialization.symbolTablePrinter.TraverseScope", simpleSymbolNames, scopeInterfaceName));
    return visitorMethod;
  }

  protected ASTCDMethod createEndVisitArtifactScopeMethod(String artifactScopeFullName) {
    ASTCDMethod visitorMethod = visitorService.getVisitorMethod(END_VISIT, getCDTypeFacade().createQualifiedType(artifactScopeFullName));
    this.replaceTemplate(EMPTY_BODY, visitorMethod, new StringHookPoint("printer.endObject();"));
    return visitorMethod;
  }

  protected ASTCDMethod createEndVisitScopeMethod(String scopeName) {
    ASTCDMethod visitorMethod = visitorService.getVisitorMethod(END_VISIT, getCDTypeFacade().createQualifiedType(scopeName));
    this.replaceTemplate(EMPTY_BODY, visitorMethod, new StringHookPoint("printer.endObject();"));
    return visitorMethod;
  }

  protected List<ASTCDMethod> createSymbolVisitorMethods(List<ASTCDType> symbolProds) {
    List<ASTCDMethod> visitorMethods = new ArrayList<>();

    for (ASTCDType symbolProd : symbolProds) {
      String symbolFullName = symbolTableService.getSymbolFullName(symbolProd);
      ASTCDMethod visitMethod = visitorService.getVisitorMethod(VISIT, getCDTypeFacade().createQualifiedType(symbolFullName));
      this.replaceTemplate(EMPTY_BODY, visitMethod, new TemplateHookPoint(
          "_symboltable.serialization.symbolTablePrinter.VisitSymbol", symbolFullName));
      visitorMethods.add(visitMethod);

      ASTCDMethod endVisitMethod = visitorService.getVisitorMethod(END_VISIT, getCDTypeFacade().createQualifiedType(symbolFullName));
      this.replaceTemplate(EMPTY_BODY, endVisitMethod, new StringHookPoint("printer.endObject();"));
      visitorMethods.add(endVisitMethod);
    }

    return visitorMethods;
  }
}