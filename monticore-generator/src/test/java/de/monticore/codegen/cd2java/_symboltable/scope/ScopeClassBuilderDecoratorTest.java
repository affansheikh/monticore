/* (c) https://github.com/MontiCore/monticore */
package de.monticore.codegen.cd2java._symboltable.scope;

import de.monticore.cd.cd4analysis._ast.*;
import de.monticore.cd.facade.CDModifier;
import de.monticore.cd.prettyprint.CD4CodePrinter;
import de.monticore.codegen.cd2java.AbstractService;
import de.monticore.codegen.cd2java.DecorationHelper;
import de.monticore.codegen.cd2java.DecoratorTestCase;
import de.monticore.codegen.cd2java._ast.builder.BuilderDecorator;
import de.monticore.codegen.cd2java._symboltable.SymbolTableService;
import de.monticore.codegen.cd2java.methods.AccessorDecorator;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.se_rwth.commons.logging.*;
import org.junit.Before;
import org.junit.Test;

import static de.monticore.cd.facade.CDModifier.PROTECTED;
import static de.monticore.cd.facade.CDModifier.PUBLIC;
import static de.monticore.codegen.cd2java.DecoratorAssert.*;
import static de.monticore.codegen.cd2java.DecoratorTestUtil.*;
import static org.junit.Assert.*;

public class ScopeClassBuilderDecoratorTest extends DecoratorTestCase {
  private ASTCDClass scopeBuilderClass;

  private GlobalExtensionManagement glex;

  private de.monticore.types.MCTypeFacade MCTypeFacade;

  private ASTCDCompilationUnit decoratedCompilationUnit;

  private ASTCDCompilationUnit originalCompilationUnit;

  private static final String ENCLOSING_SCOPE_TYPE = "de.monticore.codegen.builder.builder._symboltable.IBuilderScope";

  private static final String A_NODE_TYPE_OPT = "Optional<de.monticore.codegen.builder.builder._ast.ASTA>";

  private static final String I_SCOPE_SPANNING_SYMBOL = "de.monticore.symboltable.IScopeSpanningSymbol";

  @Before
  public void setUp() {
    LogStub.init();         // replace log by a sideffect free variant
        // LogStub.initPlusLog();  // for manual testing purpose only
    this.MCTypeFacade = MCTypeFacade.getInstance();
    this.glex = new GlobalExtensionManagement();

    this.glex.setGlobalValue("astHelper", DecorationHelper.getInstance());
    this.glex.setGlobalValue("cdPrinter", new CD4CodePrinter());
    decoratedCompilationUnit = this.parse("de", "monticore", "codegen", "symboltable","cdForBuilder", "Scope_Builder");
    ASTCDClass cdClass = getClassBy("AScope", decoratedCompilationUnit);

    originalCompilationUnit = decoratedCompilationUnit.deepClone();
    this.glex.setGlobalValue("service", new AbstractService(decoratedCompilationUnit));
    BuilderDecorator builderDecorator = new BuilderDecorator(glex, new AccessorDecorator(glex,
        new SymbolTableService(decoratedCompilationUnit)), new SymbolTableService(decoratedCompilationUnit));

    ScopeClassBuilderDecorator decorator = new ScopeClassBuilderDecorator(this.glex, builderDecorator);

    //creates normal Symbol
    this.scopeBuilderClass = decorator.decorate(cdClass);
  }

  @Test
  public void testCompilationUnitNotChanged() {
    assertDeepEquals(originalCompilationUnit, decoratedCompilationUnit);
  }

  // ScopeSpanningSymbol

  @Test
  public void testClassName() {
    assertEquals("AScopeBuilder", scopeBuilderClass.getName());
  }

  @Test
  public void testNoSuperInterfaces() {
    assertTrue( scopeBuilderClass.isEmptyInterfaces());
  }

  @Test
  public void testNoSuperClass() {
    assertFalse(scopeBuilderClass.isPresentSuperclass());
  }

  @Test
  public void testConstructorCount() {
    assertEquals(1, scopeBuilderClass.sizeCDConstructors());
  }

  @Test
  public void testDefaultConstructor() {
    ASTCDConstructor cdConstructor = scopeBuilderClass.getCDConstructor(0);
    assertDeepEquals(PUBLIC, cdConstructor.getModifier());
    assertEquals("AScopeBuilder", cdConstructor.getName());

    assertTrue(cdConstructor.isEmptyCDParameters());

    assertTrue(cdConstructor.isEmptyExceptions());
  }

  @Test
  public void testAttributes() {
    assertEquals(8, scopeBuilderClass.getCDAttributeList().size());
  }

  @Test
  public void testNameAttribute() {
    ASTCDAttribute astcdAttribute = getAttributeBy("name", scopeBuilderClass);
    assertDeepEquals(CDModifier.PROTECTED, astcdAttribute.getModifier());
    assertOptionalOf(String.class, astcdAttribute.getMCType());
  }

  @Test
  public void testEnclosingScopeAttribute() {
    ASTCDAttribute astcdAttribute = getAttributeBy("enclosingScope", scopeBuilderClass);
    assertDeepEquals(CDModifier.PROTECTED, astcdAttribute.getModifier());
    assertDeepEquals(MCTypeFacade.createQualifiedType(ENCLOSING_SCOPE_TYPE),
        astcdAttribute.getMCType());
  }

  @Test
  public void testASTNodeAttribute() {
    ASTCDAttribute astcdAttribute = getAttributeBy("astNode", scopeBuilderClass);
    assertDeepEquals(CDModifier.PROTECTED, astcdAttribute.getModifier());
    assertDeepEquals(MCTypeFacade.createQualifiedType(A_NODE_TYPE_OPT), astcdAttribute.getMCType());
  }

  @Test
  public void testSubScopesAttribute() {
    ASTCDAttribute astcdAttribute = getAttributeBy("subScopes", scopeBuilderClass);
    assertDeepEquals(CDModifier.PROTECTED, astcdAttribute.getModifier());
    assertListOf(ENCLOSING_SCOPE_TYPE, astcdAttribute.getMCType());
  }

  @Test
  public void testSpanningSymbolAttribute() {
    ASTCDAttribute astcdAttribute = getAttributeBy("spanningSymbol", scopeBuilderClass);
    assertDeepEquals(CDModifier.PROTECTED, astcdAttribute.getModifier());
    assertOptionalOf(I_SCOPE_SPANNING_SYMBOL, astcdAttribute.getMCType());
  }

  @Test
  public void testShadowingAttribute() {
    ASTCDAttribute astcdAttribute = getAttributeBy("shadowing", scopeBuilderClass);
    assertDeepEquals(CDModifier.PROTECTED, astcdAttribute.getModifier());
    assertBoolean( astcdAttribute.getMCType());
  }

  @Test
  public void testExportSymbolsAttribute() {
    ASTCDAttribute astcdAttribute = getAttributeBy("exportsSymbols", scopeBuilderClass);
    assertDeepEquals(CDModifier.PROTECTED, astcdAttribute.getModifier());
    assertBoolean(astcdAttribute.getMCType());
  }

  @Test
  public void testMethods() {
    assertEquals(54, scopeBuilderClass.getCDMethodList().size());
  }

  @Test
  public void testBuildMethod() {
    ASTCDMethod method = getMethodBy("build", scopeBuilderClass);
    assertDeepEquals(PUBLIC, method.getModifier());
    assertDeepEquals(MCTypeFacade.createQualifiedType("AScope"), method.getMCReturnType().getMCType());

    assertTrue(method.isEmptyCDParameters());
  }

}
