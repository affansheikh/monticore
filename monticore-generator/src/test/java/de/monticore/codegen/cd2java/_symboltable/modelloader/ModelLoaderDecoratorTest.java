/* (c) https://github.com/MontiCore/monticore */
package de.monticore.codegen.cd2java._symboltable.modelloader;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import de.monticore.cd.cd4analysis._ast.*;
import de.monticore.cd.prettyprint.CD4CodePrinter;
import de.monticore.codegen.cd2java.AbstractService;
import de.monticore.codegen.cd2java.CoreTemplates;
import de.monticore.codegen.cd2java.DecorationHelper;
import de.monticore.codegen.cd2java.DecoratorTestCase;
import de.monticore.codegen.cd2java._symboltable.SymbolTableService;
import de.monticore.codegen.cd2java.methods.AccessorDecorator;
import de.monticore.generating.GeneratorEngine;
import de.monticore.generating.GeneratorSetup;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.se_rwth.commons.logging.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static de.monticore.cd.facade.CDModifier.*;
import static de.monticore.codegen.cd2java.DecoratorAssert.assertDeepEquals;
import static de.monticore.codegen.cd2java.DecoratorTestUtil.getAttributeBy;
import static de.monticore.codegen.cd2java.DecoratorTestUtil.getMethodBy;
import static org.junit.Assert.*;

public class ModelLoaderDecoratorTest extends DecoratorTestCase {

  private ASTCDClass modelLoaderClass;

  private GlobalExtensionManagement glex;

  private ASTCDCompilationUnit decoratedCompilationUnit;

  private ASTCDCompilationUnit originalCompilationUnit;

  private de.monticore.types.MCTypeFacade MCTypeFacade;

  private ModelLoaderDecorator decorator;

  @Before
  public void setUp() {
    LogStub.init();         // replace log by a sideffect free variant
        // LogStub.initPlusLog();  // for manual testing purpose only
    this.glex = new GlobalExtensionManagement();
    this.MCTypeFacade = MCTypeFacade.getInstance();

    this.glex.setGlobalValue("astHelper", DecorationHelper.getInstance());
    this.glex.setGlobalValue("cdPrinter", new CD4CodePrinter());
    decoratedCompilationUnit = this.parse("de", "monticore", "codegen", "ast", "Automaton");
    originalCompilationUnit = decoratedCompilationUnit.deepClone();
    this.glex.setGlobalValue("service", new AbstractService(decoratedCompilationUnit));

    this.decorator = new ModelLoaderDecorator(this.glex,
        new SymbolTableService(decoratedCompilationUnit), new AccessorDecorator(glex, new SymbolTableService(decoratedCompilationUnit)));

    Optional<ASTCDClass> astcdClass = decorator.decorate(decoratedCompilationUnit);
    assertTrue(astcdClass.isPresent());
    modelLoaderClass = astcdClass.get();
  }

  @Test
  public void testNoStartProd() {
    ASTCDCompilationUnit noStartProd = this.parse("de", "monticore", "codegen", "ast", "NoStartProd");

    Optional<ASTCDClass> astcdClass = decorator.decorate(noStartProd);
    assertFalse(astcdClass.isPresent());
  }

  @Test
  public void testCompilationUnitNotChanged() {
    assertDeepEquals(originalCompilationUnit, decoratedCompilationUnit);
  }

  @Test
  public void testClassName() {
    assertEquals("AutomatonModelLoader", modelLoaderClass.getName());
  }

  @Test
  public void testSuperInterfacesCount() {
    assertEquals(1, modelLoaderClass.sizeInterfaces());
  }

  @Test
  public void testSuperInterfaces() {
    assertDeepEquals("de.monticore.modelloader.IModelLoader<de.monticore.codegen.ast.automaton._ast.ASTAutomaton" +
        ",de.monticore.codegen.ast.automaton._symboltable.AutomatonGlobalScope>", modelLoaderClass.getInterface(0));
  }

  @Test
  public void testSuperClassNotPresent() {
    assertFalse(modelLoaderClass.isPresentSuperclass());
  }

  @Test
  public void testConstructorCount() {
    assertEquals(1, modelLoaderClass.sizeCDConstructors());
  }

  @Test
  public void testConstructor() {
    ASTCDConstructor cdConstructor = modelLoaderClass.getCDConstructor(0);
    assertDeepEquals(PUBLIC, cdConstructor.getModifier());
    assertEquals("AutomatonModelLoader", cdConstructor.getName());

    assertEquals(4, cdConstructor.sizeCDParameters());
    assertDeepEquals("de.monticore.modelloader.AstProvider<de.monticore.codegen.ast.automaton._ast.ASTAutomaton>", cdConstructor.getCDParameter(0).getMCType());
    assertEquals("astProvider", cdConstructor.getCDParameter(0).getName());
    assertDeepEquals("de.monticore.codegen.ast.automaton._symboltable.AutomatonSymbolTableCreatorDelegator", cdConstructor.getCDParameter(1).getMCType());
    assertEquals("symbolTableCreator", cdConstructor.getCDParameter(1).getName());
    assertDeepEquals("String", cdConstructor.getCDParameter(2).getMCType());
    assertEquals("modelFileExtension", cdConstructor.getCDParameter(2).getName());
    assertDeepEquals("String", cdConstructor.getCDParameter(3).getMCType());
    assertEquals("symbolFileExtension", cdConstructor.getCDParameter(3).getName());
  }

  @Test
  public void testAttributeSize() {
    assertEquals(4, modelLoaderClass.sizeCDAttributes());
  }

  @Test
  public void testSymbolTableCreatorAttribute() {
    ASTCDAttribute astcdAttribute = getAttributeBy("symbolTableCreator", modelLoaderClass);
    assertDeepEquals(PROTECTED, astcdAttribute.getModifier());
    assertDeepEquals("de.monticore.codegen.ast.automaton._symboltable.AutomatonSymbolTableCreatorDelegator", astcdAttribute.getMCType());
  }

  @Test
  public void testModelFileExtensionAttribute() {
    ASTCDAttribute astcdAttribute = getAttributeBy("modelFileExtension", modelLoaderClass);
    assertDeepEquals(PROTECTED, astcdAttribute.getModifier());
    assertDeepEquals("String", astcdAttribute.getMCType());
  }

  @Test
  public void testASTProviderAttribute() {
    ASTCDAttribute astcdAttribute = getAttributeBy("astProvider", modelLoaderClass);
    assertDeepEquals(PROTECTED, astcdAttribute.getModifier());
    assertDeepEquals("de.monticore.modelloader.AstProvider<de.monticore.codegen.ast.automaton._ast.ASTAutomaton>", astcdAttribute.getMCType());
  }

  @Test
  public void testMethodCount() {
    assertEquals(7, modelLoaderClass.getCDMethodList().size());
  }

  @Test
  public void testCreateSymbolTableFromASTMethod() {
    ASTCDMethod method = getMethodBy("createSymbolTableFromAST", modelLoaderClass);

    assertDeepEquals(PUBLIC, method.getModifier());
    assertTrue(method.getMCReturnType().isPresentMCVoidType());

    assertEquals(3, method.sizeCDParameters());
    assertDeepEquals("de.monticore.codegen.ast.automaton._ast.ASTAutomaton", method.getCDParameter(0).getMCType());
    assertEquals("ast", method.getCDParameter(0).getName());

    assertDeepEquals(String.class, method.getCDParameter(1).getMCType());
    assertEquals("modelName", method.getCDParameter(1).getName());

    assertDeepEquals("de.monticore.codegen.ast.automaton._symboltable.AutomatonGlobalScope", method.getCDParameter(2).getMCType());
    assertEquals("enclosingScope", method.getCDParameter(2).getName());
  }


  @Test
  public void testLoadModelsIntoScopeMethod() {
    ASTCDMethod method = getMethodBy("loadModelsIntoScope", modelLoaderClass);

    assertDeepEquals(PUBLIC, method.getModifier());
    assertDeepEquals(MCTypeFacade.createListTypeOf("de.monticore.codegen.ast.automaton._ast.ASTAutomaton"),
        method.getMCReturnType().getMCType());

    assertEquals(3, method.sizeCDParameters());
    assertDeepEquals(String.class, method.getCDParameter(0).getMCType());
    assertEquals("qualifiedModelName", method.getCDParameter(0).getName());

    assertDeepEquals("de.monticore.io.paths.ModelPath", method.getCDParameter(1).getMCType());
    assertEquals("modelPath", method.getCDParameter(1).getName());

    assertDeepEquals("de.monticore.codegen.ast.automaton._symboltable.AutomatonGlobalScope", method.getCDParameter(2).getMCType());
    assertEquals("enclosingScope", method.getCDParameter(2).getName());
  }

  @Test
  public void testResolveMethod() {
    ASTCDMethod method = getMethodBy("resolve", modelLoaderClass);

    assertDeepEquals(PRIVATE, method.getModifier());
    assertDeepEquals("de.monticore.io.paths.ModelCoordinate",
        method.getMCReturnType().getMCType());

    assertEquals(2, method.sizeCDParameters());
    assertDeepEquals(String.class, method.getCDParameter(0).getMCType());
    assertEquals("qualifiedModelName", method.getCDParameter(0).getName());

    assertDeepEquals("de.monticore.io.paths.ModelPath", method.getCDParameter(1).getMCType());
    assertEquals("modelPath", method.getCDParameter(1).getName());
  }

  @Test
  public void testResolveSymbolMethod() {
    ASTCDMethod method = getMethodBy("resolveSymbol", modelLoaderClass);

    assertDeepEquals(PROTECTED, method.getModifier());
    assertDeepEquals("de.monticore.io.paths.ModelCoordinate",
        method.getMCReturnType().getMCType());

    assertEquals(2, method.sizeCDParameters());
    assertDeepEquals(String.class, method.getCDParameter(0).getMCType());
    assertEquals("qualifiedModelName", method.getCDParameter(0).getName());

    assertDeepEquals("de.monticore.io.paths.ModelPath", method.getCDParameter(1).getMCType());
    assertEquals("modelPath", method.getCDParameter(1).getName());
  }

  @Test
  public void testShowWarningIfParsedModelsMethod() {
    ASTCDMethod method = getMethodBy("showWarningIfParsedModels", modelLoaderClass);

    assertDeepEquals(PROTECTED, method.getModifier());
    assertTrue(method.getMCReturnType().isPresentMCVoidType());

    assertEquals(2, method.sizeCDParameters());
    assertDeepEquals("List<?>", method.getCDParameter(0).getMCType());
    assertEquals("asts", method.getCDParameter(0).getName());

    assertDeepEquals(String.class, method.getCDParameter(1).getMCType());
    assertEquals("modelName", method.getCDParameter(1).getName());
  }

  @Test
  public void testGeneratedCode() {
    GeneratorSetup generatorSetup = new GeneratorSetup();
    generatorSetup.setGlex(glex);
    GeneratorEngine generatorEngine = new GeneratorEngine(generatorSetup);
    StringBuilder sb = generatorEngine.generate(CoreTemplates.CLASS, modelLoaderClass, modelLoaderClass);
    // test parsing
    ParserConfiguration configuration = new ParserConfiguration();
    JavaParser parser = new JavaParser(configuration);
    ParseResult parseResult = parser.parse(sb.toString());
    assertTrue(parseResult.isSuccessful());
  }
}
