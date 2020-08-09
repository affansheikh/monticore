/* (c) https://github.com/MontiCore/monticore */
package de.monticore.codegen.cd2java._symboltable;

import de.monticore.cd.cd4analysis._ast.*;
import de.monticore.cd.prettyprint.CD4CodePrinter;
import de.monticore.codegen.cd2java.AbstractService;
import de.monticore.codegen.cd2java.DecorationHelper;
import de.monticore.codegen.cd2java.DecoratorTestCase;
import de.monticore.codegen.cd2java._ast.builder.BuilderDecorator;
import de.monticore.codegen.cd2java._parser.ParserService;
import de.monticore.codegen.cd2java._symboltable.modelloader.ModelLoaderBuilderDecorator;
import de.monticore.codegen.cd2java._symboltable.modelloader.ModelLoaderDecorator;
import de.monticore.codegen.cd2java._symboltable.scope.*;
import de.monticore.codegen.cd2java._symboltable.serialization.*;
import de.monticore.codegen.cd2java._symboltable.symbol.*;
import de.monticore.codegen.cd2java._symboltable.symbol.symbolsurrogatemutator.MandatoryMutatorSymbolSurrogateDecorator;
import de.monticore.codegen.cd2java._symboltable.symboltablecreator.*;
import de.monticore.codegen.cd2java._visitor.VisitorService;
import de.monticore.codegen.cd2java.methods.AccessorDecorator;
import de.monticore.codegen.cd2java.methods.MethodDecorator;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.io.paths.IterablePath;
import de.se_rwth.commons.logging.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.Optional;

import static de.monticore.codegen.cd2java.DecoratorAssert.assertDeepEquals;
import static de.monticore.codegen.cd2java.DecoratorTestUtil.getClassBy;
import static de.monticore.codegen.cd2java.DecoratorTestUtil.getInterfaceBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SymbolTableCDDecoratorTest extends DecoratorTestCase {

  private GlobalExtensionManagement glex;

  private ASTCDCompilationUnit decoratedASTCompilationUnit;

  private ASTCDCompilationUnit decoratedSymbolCompilationUnit;

  private ASTCDCompilationUnit decoratedScopeCompilationUnit;

  private ASTCDCompilationUnit originalASTCompilationUnit;

  private ASTCDCompilationUnit originalSymbolCompilationUnit;

  private ASTCDCompilationUnit originalScopeCompilationUnit;

  private ASTCDCompilationUnit symTabCD;

  private ASTCDCompilationUnit symTabCDWithHC;

  private ASTCDCompilationUnit symTabCDComponent;

  @Before
  public void setUp() {
    // LogStub.init() prevents a lot of warnings like 0xA1042 Scope "equals" has already an enclosing scope
    // to be issued (the warnings are not checked)
    LogStub.init();         // replace log by a sideffect free variant
    // LogStub.initPlusLog();  // for manual testing purpose only
    this.glex = new GlobalExtensionManagement();
    IterablePath targetPath = Mockito.mock(IterablePath.class);

    this.glex.setGlobalValue("astHelper", DecorationHelper.getInstance());
    this.glex.setGlobalValue("cdPrinter", new CD4CodePrinter());
    decoratedASTCompilationUnit = this.parse("de", "monticore", "codegen", "symboltable", "Automaton");
    decoratedScopeCompilationUnit = this.parse("de", "monticore", "codegen", "symboltable", "AutomatonScopeCD");
    decoratedSymbolCompilationUnit = this.parse("de", "monticore", "codegen", "symboltable", "AutomatonSymbolCD");
    originalASTCompilationUnit = decoratedASTCompilationUnit.deepClone();
    originalSymbolCompilationUnit = decoratedSymbolCompilationUnit.deepClone();
    originalScopeCompilationUnit = decoratedScopeCompilationUnit.deepClone();

    this.glex.setGlobalValue("service", new AbstractService(decoratedASTCompilationUnit));

    SymbolTableService symbolTableService = new SymbolTableService(decoratedASTCompilationUnit);
    VisitorService visitorService = new VisitorService(decoratedASTCompilationUnit);
    ParserService parserService = new ParserService(decoratedASTCompilationUnit);
    MethodDecorator methodDecorator = new MethodDecorator(glex, symbolTableService);
    AccessorDecorator accessorDecorator = new AccessorDecorator(glex, symbolTableService);

    SymbolDecorator symbolDecorator = new SymbolDecorator(glex, symbolTableService, visitorService, methodDecorator);
    BuilderDecorator builderDecorator = new BuilderDecorator(glex, accessorDecorator, symbolTableService);
    SymbolBuilderDecorator symbolBuilderDecorator = new SymbolBuilderDecorator(glex, symbolTableService, builderDecorator);
    ScopeInterfaceDecorator scopeInterfaceDecorator = new ScopeInterfaceDecorator(glex, symbolTableService, visitorService, methodDecorator);
    ScopeClassDecorator scopeClassDecorator = new ScopeClassDecorator(glex, symbolTableService, visitorService, methodDecorator);
    ScopeClassBuilderDecorator scopeClassBuilderDecorator = new ScopeClassBuilderDecorator(glex, builderDecorator);
    GlobalScopeInterfaceDecorator globalScopeInterfaceDecorator = new GlobalScopeInterfaceDecorator(glex, symbolTableService, methodDecorator);
    GlobalScopeClassDecorator globalScopeClassDecorator = new GlobalScopeClassDecorator(glex, symbolTableService, methodDecorator);
    GlobalScopeClassBuilderDecorator globalScopeClassBuilderDecorator = new GlobalScopeClassBuilderDecorator(glex, symbolTableService, builderDecorator);
    ArtifactScopeDecorator artifactScopeDecorator = new ArtifactScopeDecorator(glex, symbolTableService, visitorService, methodDecorator);
    ArtifactScopeBuilderDecorator artifactScopeBuilderDecorator = new ArtifactScopeBuilderDecorator(glex, symbolTableService, builderDecorator, accessorDecorator);
    SymbolSurrogateDecorator symbolReferenceDecorator = new SymbolSurrogateDecorator(glex, symbolTableService, methodDecorator, new MandatoryMutatorSymbolSurrogateDecorator(glex));
    SymbolSurrogateBuilderDecorator symbolReferenceBuilderDecorator = new SymbolSurrogateBuilderDecorator(glex, symbolTableService, accessorDecorator);
    CommonSymbolInterfaceDecorator commonSymbolInterfaceDecorator = new CommonSymbolInterfaceDecorator(glex, symbolTableService, visitorService, methodDecorator);
    ModelLoaderDecorator modelLoaderDecorator = new ModelLoaderDecorator(glex, symbolTableService, accessorDecorator);
    ModelLoaderBuilderDecorator modelLoaderBuilderDecorator = new ModelLoaderBuilderDecorator(glex, builderDecorator);
    SymbolResolvingDelegateInterfaceDecorator symbolResolvingDelegateInterfaceDecorator = new SymbolResolvingDelegateInterfaceDecorator(glex, symbolTableService);
    SymbolTableCreatorDecorator symbolTableCreatorDecorator = new SymbolTableCreatorDecorator(glex, symbolTableService, visitorService, methodDecorator);
    SymbolTableCreatorBuilderDecorator symbolTableCreatorBuilderDecorator = new SymbolTableCreatorBuilderDecorator(glex, symbolTableService);
    SymbolTableCreatorDelegatorDecorator symbolTableCreatorDelegatorDecorator = new SymbolTableCreatorDelegatorDecorator(glex, symbolTableService, visitorService);
    SymbolTableCreatorForSuperTypes symbolTableCreatorForSuperTypes = new SymbolTableCreatorForSuperTypes(glex, symbolTableService);
    SymbolTableCreatorDelegatorBuilderDecorator symbolTableCreatorDelegatorBuilderDecorator = new SymbolTableCreatorDelegatorBuilderDecorator(glex, builderDecorator);
    SymbolTableCreatorForSuperTypesBuilder symbolTableCreatorForSuperTypesBuilder = new SymbolTableCreatorForSuperTypesBuilder(glex, builderDecorator, symbolTableService);
    SymbolDeSerDecorator symbolDeSerDecorator = new SymbolDeSerDecorator(glex, symbolTableService);
    SymbolDeSerBuilderDecorator symbolDeSerBuilderDecorator = new SymbolDeSerBuilderDecorator(glex, builderDecorator);
    ScopeDeSerDecorator scopeDeSerDecorator = new ScopeDeSerDecorator(glex, symbolTableService, methodDecorator, visitorService);
    ScopeDeSerBuilderDecorator scopeDeSerBuilderDecorator = new ScopeDeSerBuilderDecorator(glex, builderDecorator);
    SymbolTablePrinterDecorator symbolTablePrinterDecorator = new SymbolTablePrinterDecorator(glex, symbolTableService, visitorService);
    SymbolTablePrinterBuilderDecorator symbolTablePrinterBuilderDecorator = new SymbolTablePrinterBuilderDecorator(glex, builderDecorator);

    SymbolTableCDDecorator symbolTableCDDecorator = new SymbolTableCDDecorator(glex, targetPath, symbolTableService, symbolDecorator,
        symbolBuilderDecorator, symbolReferenceDecorator, symbolReferenceBuilderDecorator,
        scopeInterfaceDecorator, scopeClassDecorator, scopeClassBuilderDecorator,
        globalScopeInterfaceDecorator, globalScopeClassDecorator, globalScopeClassBuilderDecorator,
        artifactScopeDecorator, artifactScopeBuilderDecorator,
        commonSymbolInterfaceDecorator, modelLoaderDecorator, modelLoaderBuilderDecorator,
        symbolResolvingDelegateInterfaceDecorator, symbolTableCreatorDecorator, symbolTableCreatorBuilderDecorator,
        symbolTableCreatorDelegatorDecorator, symbolTableCreatorForSuperTypes, symbolTableCreatorDelegatorBuilderDecorator,
        symbolTableCreatorForSuperTypesBuilder, symbolDeSerDecorator, scopeDeSerDecorator, symbolTablePrinterDecorator, scopeDeSerBuilderDecorator,
        symbolDeSerBuilderDecorator, symbolTablePrinterBuilderDecorator);

    // cd with no handcoded classes
    this.symTabCD = symbolTableCDDecorator.decorate(decoratedASTCompilationUnit, decoratedSymbolCompilationUnit, decoratedScopeCompilationUnit);

    // cd with handcoded classes and component and no start prod
    Mockito.when(targetPath.getResolvedPath(Mockito.any(Path.class))).thenReturn(Optional.of(Mockito.mock(Path.class)));
    this.symTabCDWithHC = symbolTableCDDecorator.decorate(decoratedASTCompilationUnit, decoratedSymbolCompilationUnit, decoratedScopeCompilationUnit);

    SymbolTableService mockService = Mockito.spy(new SymbolTableService(decoratedASTCompilationUnit));
    SymbolTableCDDecorator mockDecorator = new SymbolTableCDDecorator(glex, targetPath, mockService, symbolDecorator,
        symbolBuilderDecorator, symbolReferenceDecorator, symbolReferenceBuilderDecorator,
        scopeInterfaceDecorator, scopeClassDecorator, scopeClassBuilderDecorator,
        globalScopeInterfaceDecorator, globalScopeClassDecorator, globalScopeClassBuilderDecorator,
        artifactScopeDecorator, artifactScopeBuilderDecorator,
        commonSymbolInterfaceDecorator, modelLoaderDecorator, modelLoaderBuilderDecorator,
        symbolResolvingDelegateInterfaceDecorator, symbolTableCreatorDecorator, symbolTableCreatorBuilderDecorator,
        symbolTableCreatorDelegatorDecorator, symbolTableCreatorForSuperTypes, symbolTableCreatorDelegatorBuilderDecorator,
        symbolTableCreatorForSuperTypesBuilder,
        symbolDeSerDecorator, scopeDeSerDecorator, symbolTablePrinterDecorator, scopeDeSerBuilderDecorator, symbolDeSerBuilderDecorator,
        symbolTablePrinterBuilderDecorator);
    Mockito.doReturn(false).when(mockService).hasStartProd(Mockito.any(ASTCDDefinition.class));
    Mockito.doReturn(true).when(mockService).hasComponentStereotype(Mockito.any(ASTModifier.class));
    this.symTabCDComponent = mockDecorator.decorate(decoratedASTCompilationUnit, decoratedSymbolCompilationUnit, decoratedScopeCompilationUnit);
  }

  @Test
  public void testCompilationUnitNotChanged() {
    assertDeepEquals(originalASTCompilationUnit, decoratedASTCompilationUnit);
    assertDeepEquals(originalSymbolCompilationUnit, originalSymbolCompilationUnit);
    assertDeepEquals(originalScopeCompilationUnit, originalScopeCompilationUnit);
  }

  @Test
  public void testCDName() {
    assertEquals("Automaton", symTabCD.getCDDefinition().getName());
  }

  @Test
  public void testClassCount() {
    assertEquals(34, symTabCD.getCDDefinition().getCDClassList().size());
  }

  @Test
  public void testClassNames() {
    ASTCDClass automatonSymbol = getClassBy("AutomatonSymbol", symTabCD);
    ASTCDClass stateSymbol = getClassBy("StateSymbol", symTabCD);
    ASTCDClass fooSymbol = getClassBy("FooSymbol", symTabCD);
    ASTCDClass automatonSymbolBuilder = getClassBy("AutomatonSymbolBuilder", symTabCD);
    ASTCDClass stateSymbolBuilder = getClassBy("StateSymbolBuilder", symTabCD);
    ASTCDClass fooSymbolBuilder = getClassBy("FooSymbolBuilder", symTabCD);
    ASTCDClass automatonScopeCDScope = getClassBy("AutomatonScopeCDScope", symTabCD);
    ASTCDClass automatonScopeCDScopeBuilder = getClassBy("AutomatonScopeCDScopeBuilder", symTabCD);
    ASTCDClass automatonSymbolSurrogate = getClassBy("AutomatonSymbolSurrogate", symTabCD);
    ASTCDClass stateSymbolSurrogate = getClassBy("StateSymbolSurrogate", symTabCD);
    ASTCDClass fooSymbolSurrogate = getClassBy("FooSymbolSurrogate", symTabCD);
    ASTCDClass automatonSymbolSurrogateBuilder = getClassBy("AutomatonSymbolSurrogateBuilder", symTabCD);
    ASTCDClass stateSymbolSurrogateBuilder = getClassBy("StateSymbolSurrogateBuilder", symTabCD);
    ASTCDClass fooSymbolSurrogateBuilder = getClassBy("FooSymbolSurrogateBuilder", symTabCD);
    ASTCDClass automatonGlobalScope = getClassBy("AutomatonGlobalScope", symTabCD);
    ASTCDClass automatonGlobalScopeBuilder = getClassBy("AutomatonGlobalScopeBuilder", symTabCD);
    ASTCDClass automatonArtifactScope = getClassBy("AutomatonArtifactScope", symTabCD);
    ASTCDClass automatonArtifactScopeBuilder = getClassBy("AutomatonArtifactScopeBuilder", symTabCD);
    ASTCDClass automatonModelLoader = getClassBy("AutomatonModelLoader", symTabCD);
    ASTCDClass automatonModelLoaderBuilder = getClassBy("AutomatonModelLoaderBuilder", symTabCD);
    ASTCDClass automatonSymbolTableCreator = getClassBy("AutomatonSymbolTableCreator", symTabCD);
    ASTCDClass automatonSymbolTableCreatorBuilder = getClassBy("AutomatonSymbolTableCreatorBuilder", symTabCD);
    ASTCDClass automatonSymbolTableCreatorDelegator = getClassBy("AutomatonSymbolTableCreatorDelegator", symTabCD);
    ASTCDClass automatonSymbolTableCreatorDelegatorBuilder = getClassBy("AutomatonSymbolTableCreatorDelegatorBuilder", symTabCD);
    ASTCDClass automatonScopeDeSer = getClassBy("AutomatonScopeDeSer", symTabCD);
    ASTCDClass automatonSymbolDeSer = getClassBy("AutomatonSymbolDeSer", symTabCD);
    ASTCDClass stateSymbolDeSer = getClassBy("StateSymbolDeSer", symTabCD);
    ASTCDClass fooSymbolDeSer = getClassBy("FooSymbolDeSer", symTabCD);
    ASTCDClass automatonSymbolTablePrinter = getClassBy("AutomatonSymbolTablePrinter", symTabCD);
    ASTCDClass automatonScopeDeSerBuilder = getClassBy("AutomatonScopeDeSerBuilder", symTabCD);
    ASTCDClass automatonSymbolDeSerBuilder = getClassBy("AutomatonSymbolDeSerBuilder", symTabCD);
    ASTCDClass stateSymbolDeSerBuilder = getClassBy("StateSymbolDeSerBuilder", symTabCD);
    ASTCDClass fooSymbolDeSerBuilder = getClassBy("FooSymbolDeSerBuilder", symTabCD);
    ASTCDClass automatonSymbolTablePrinterBuilder = getClassBy("AutomatonSymbolTablePrinterBuilder", symTabCD);
  }

  @Test
  public void testInterfaceCount() {
    assertEquals(6, symTabCD.getCDDefinition().getCDInterfaceList().size());
  }

  @Test
  public void testInterfaces() {
    ASTCDInterface iAutomatonScope = getInterfaceBy("IAutomatonScope", symTabCD);
    ASTCDInterface iAutomatonGlobalScope = getInterfaceBy("IAutomatonGlobalScope", symTabCD);
    ASTCDInterface iCommonAutomatonSymbol = getInterfaceBy("ICommonAutomatonSymbol", symTabCD);
    ASTCDInterface iAutomatonSymbolResolvingDelegate = getInterfaceBy("IAutomatonSymbolResolvingDelegate", symTabCD);
    ASTCDInterface iStateSymbolResolvingDelegate = getInterfaceBy("IStateSymbolResolvingDelegate", symTabCD);
    ASTCDInterface symbolInterfaceSymbol = getInterfaceBy("ISymbolInterfaceSymbolResolvingDelegate", symTabCD);
  }

  @Test
  public void testNoEnum() {
    assertTrue(symTabCD.getCDDefinition().isEmptyCDEnums());
  }

  @Test
  public void testPackage() {
    assertEquals(6, symTabCD.getPackageList().size());
    assertEquals("de", symTabCD.getPackageList().get(0));
    assertEquals("monticore", symTabCD.getPackageList().get(1));
    assertEquals("codegen", symTabCD.getPackageList().get(2));
    assertEquals("symboltable", symTabCD.getPackageList().get(3));
    assertEquals("automaton", symTabCD.getPackageList().get(4));
    assertEquals("_symboltable", symTabCD.getPackageList().get(5));

  }

  @Test
  public void testImports() {
    assertEquals(0, symTabCD.getMCImportStatementList().size());
  }


  @Test
  public void testCDNameWithHC() {
    assertEquals("Automaton", symTabCDWithHC.getCDDefinition().getName());
  }

  @Test
  public void testClassCountWithHC() {
    assertEquals(34, symTabCDWithHC.getCDDefinition().getCDClassList().size());
  }

  @Test
  public void testClassNamesWithHC() {
    ASTCDClass automatonSymbol = getClassBy("AutomatonSymbol", symTabCDWithHC);
    ASTCDClass stateSymbol = getClassBy("StateSymbol", symTabCDWithHC);
    ASTCDClass fooSymbol = getClassBy("FooSymbol", symTabCDWithHC);
    ASTCDClass automatonSymbolBuilder = getClassBy("AutomatonSymbolBuilder", symTabCDWithHC);
    ASTCDClass stateSymbolBuilder = getClassBy("StateSymbolBuilder", symTabCDWithHC);
    ASTCDClass fooSymbolBuilder = getClassBy("FooSymbolBuilder", symTabCDWithHC);
    ASTCDClass automatonScopeCDScope = getClassBy("AutomatonScopeCDScope", symTabCDWithHC);
    ASTCDClass automatonScopeCDScopeBuilder = getClassBy("AutomatonScopeCDScopeBuilder", symTabCDWithHC);
    ASTCDClass automatonSymbolSurrogate = getClassBy("AutomatonSymbolSurrogate", symTabCDWithHC);
    ASTCDClass stateSymbolSurrogate = getClassBy("StateSymbolSurrogate", symTabCDWithHC);
    ASTCDClass fooSymbolSurrogate = getClassBy("FooSymbolSurrogate", symTabCDWithHC);
    ASTCDClass automatonSymbolSurrogateBuilder = getClassBy("AutomatonSymbolSurrogateBuilder", symTabCDWithHC);
    ASTCDClass stateSymbolSurrogateBuilder = getClassBy("StateSymbolSurrogateBuilder", symTabCDWithHC);
    ASTCDClass fooSymbolSurrogateBuilder = getClassBy("FooSymbolSurrogateBuilder", symTabCDWithHC);
    ASTCDClass automatonGlobalScope = getClassBy("AutomatonGlobalScope", symTabCDWithHC);
    ASTCDClass automatonGlobalScopeBuilder = getClassBy("AutomatonGlobalScopeBuilder", symTabCDWithHC);
    ASTCDClass automatonArtifactScope = getClassBy("AutomatonArtifactScope", symTabCDWithHC);
    ASTCDClass automatonArtifactScopeBuilder = getClassBy("AutomatonArtifactScopeBuilder", symTabCDWithHC);
    ASTCDClass automatonModelLoader = getClassBy("AutomatonModelLoader", symTabCDWithHC);
    ASTCDClass automatonModelLoaderBuilder = getClassBy("AutomatonModelLoaderBuilder", symTabCDWithHC);
    ASTCDClass automatonSymbolTableCreator = getClassBy("AutomatonSymbolTableCreator", symTabCDWithHC);
    ASTCDClass automatonSymbolTableCreatorBuilder = getClassBy("AutomatonSymbolTableCreatorBuilder", symTabCDWithHC);
    ASTCDClass automatonSymbolTableCreatorDelegator = getClassBy("AutomatonSymbolTableCreatorDelegator", symTabCDWithHC);
    ASTCDClass automatonSymbolTableCreatorDelegatorBuilder = getClassBy("AutomatonSymbolTableCreatorDelegatorBuilder", symTabCDWithHC);
  }

  @Test
  public void testInterfaceCountWithHC() {
    assertEquals(6, symTabCDWithHC.getCDDefinition().getCDInterfaceList().size());
  }

  @Test
  public void testInterfacesWithHC() {
    ASTCDInterface iAutomatonScope = getInterfaceBy("IAutomatonScope", symTabCDWithHC);
    ASTCDInterface iAutomatonGlobalScope = getInterfaceBy("IAutomatonGlobalScope", symTabCDWithHC);
    ASTCDInterface iCommonAutomatonSymbol = getInterfaceBy("ICommonAutomatonSymbol", symTabCDWithHC);
    ASTCDInterface iAutomatonSymbolResolvingDelegate = getInterfaceBy("IAutomatonSymbolResolvingDelegate", symTabCDWithHC);
    ASTCDInterface iStateSymbolResolvingDelegate = getInterfaceBy("IStateSymbolResolvingDelegate", symTabCDWithHC);
    ASTCDInterface symbolInterfaceSymbol = getInterfaceBy("ISymbolInterfaceSymbolResolvingDelegate", symTabCDComponent);
  }

  @Test
  public void testNoEnumWithHC() {
    assertTrue(symTabCDWithHC.getCDDefinition().isEmptyCDEnums());
  }

  @Test
  public void testCDNameComponent() {
    assertEquals("Automaton", symTabCDComponent.getCDDefinition().getName());
  }

  @Test
  public void testClassCountComponent() {
    assertEquals(22, symTabCDComponent.getCDDefinition().getCDClassList().size());
  }

  @Test
  public void testClassNamesComponent() {
    ASTCDClass automatonSymbol = getClassBy("AutomatonSymbol", symTabCDComponent);
    ASTCDClass stateSymbol = getClassBy("StateSymbol", symTabCDComponent);
    ASTCDClass fooSymbol = getClassBy("FooSymbol", symTabCDComponent);
    ASTCDClass automatonSymbolBuilder = getClassBy("AutomatonSymbolBuilder", symTabCDComponent);
    ASTCDClass stateSymbolBuilder = getClassBy("StateSymbolBuilder", symTabCDComponent);
    ASTCDClass fooSymbolBuilder = getClassBy("FooSymbolBuilder", symTabCDComponent);
    ASTCDClass automatonScopeCDScope = getClassBy("AutomatonScopeCDScope", symTabCDComponent);
    ASTCDClass automatonScopeCDScopeBuilder = getClassBy("AutomatonScopeCDScopeBuilder", symTabCDComponent);
    ASTCDClass automatonSymbolSurrogate = getClassBy("AutomatonSymbolSurrogate", symTabCDComponent);
    ASTCDClass stateSymbolSurrogate = getClassBy("StateSymbolSurrogate", symTabCDComponent);
    ASTCDClass fooSymbolSurrogate = getClassBy("FooSymbolSurrogate", symTabCDComponent);
    ASTCDClass automatonSymbolSurrogateBuilder = getClassBy("AutomatonSymbolSurrogateBuilder", symTabCDComponent);
    ASTCDClass stateSymbolSurrogateBuilder = getClassBy("StateSymbolSurrogateBuilder", symTabCDComponent);
    ASTCDClass fooSymbolSurrogateBuilder = getClassBy("FooSymbolSurrogateBuilder", symTabCDComponent);
  }

  @Test
  public void testInterfaceCountComponent() {
    assertEquals(6, symTabCDComponent.getCDDefinition().getCDInterfaceList().size());
  }

  @Test
  public void testInterfacesComponent() {
    ASTCDInterface iAutomatonScope = getInterfaceBy("IAutomatonScope", symTabCDComponent);
    ASTCDInterface iAutomatonGlobalScope = getInterfaceBy("IAutomatonGlobalScope", symTabCDComponent);
    ASTCDInterface iCommonAutomatonSymbol = getInterfaceBy("ICommonAutomatonSymbol", symTabCDComponent);
    ASTCDInterface iAutomatonSymbolResolvingDelegate = getInterfaceBy("IAutomatonSymbolResolvingDelegate", symTabCDComponent);
    ASTCDInterface iStateSymbolResolvingDelegate = getInterfaceBy("IStateSymbolResolvingDelegate", symTabCDComponent);
    ASTCDInterface symbolInterfaceSymbol = getInterfaceBy("ISymbolInterfaceSymbolResolvingDelegate", symTabCDComponent);
  }

  @Test
  public void testNoEnumComponent() {
    assertTrue(symTabCDComponent.getCDDefinition().isEmptyCDEnums());
  }
}
