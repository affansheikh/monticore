package de.monticore.codegen.cd2java.typecd2java;

import de.monticore.MontiCoreScript;
import de.monticore.cd.cd4analysis._ast.ASTCDAttribute;
import de.monticore.cd.cd4analysis._ast.ASTCDClass;
import de.monticore.cd.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.cd.cd4analysis._symboltable.CD4AnalysisGlobalScope;
import de.monticore.cd.cd4analysis._symboltable.CD4AnalysisLanguage;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.grammar.grammar._ast.ASTMCGrammar;
import de.monticore.grammar.grammar_withconcepts._symboltable.Grammar_WithConceptsGlobalScope;
import de.monticore.grammar.grammar_withconcepts._symboltable.Grammar_WithConceptsLanguage;
import de.monticore.io.paths.ModelPath;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mccollectiontypes._ast.ASTMCListType;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.*;

public class TypeCD2JavaTest {

  private ASTCDCompilationUnit cdCompilationUnit;

  @Before
  public void setUp() {
    //create grammar from ModelPath
    Path modelPathPath = Paths.get("src/test/resources");
    ModelPath modelPath = new ModelPath(modelPathPath);
    Optional<ASTMCGrammar> grammar = new MontiCoreScript()
        .parseGrammar(Paths.get(new File(
            "src/test/resources/Automaton.mc4").getAbsolutePath()));
    assertTrue(grammar.isPresent());

    CD4AnalysisGlobalScope cd4AnalysisGlobalScope = new CD4AnalysisGlobalScope(modelPath, new CD4AnalysisLanguage());
    Grammar_WithConceptsGlobalScope grammar_withConceptsGlobalScope = new Grammar_WithConceptsGlobalScope(modelPath, new Grammar_WithConceptsLanguage());

    //create ASTCDDefinition from MontiCoreScript
    MontiCoreScript script = new MontiCoreScript();
    script.createSymbolsFromAST(grammar_withConceptsGlobalScope, grammar.get());
    cdCompilationUnit = script.deriveCD(grammar.get(), new GlobalExtensionManagement(),
        cd4AnalysisGlobalScope,grammar_withConceptsGlobalScope);

    cdCompilationUnit.setEnclosingScope2(cd4AnalysisGlobalScope);
    //make types java compatible
    TypeCD2JavaDecorator decorator = new TypeCD2JavaDecorator();
    decorator.decorate(cdCompilationUnit);
  }

  @Test
  public void testTypeNamesSplittet() {
    //that names = ["java", "util", "List"] and names != ["java.util.List"]
    for (ASTCDClass astcdClass : cdCompilationUnit.getCDDefinition().getCDClassList()) {
      for (ASTCDAttribute astcdAttribute : astcdClass.getCDAttributeList()) {
        assertTrue(astcdAttribute.getMCType().getNameList().stream().noneMatch((s) -> s.contains(".")));
      }
    }
  }

  @Test
  public void testTypeJavaConformList() {
    assertTrue(cdCompilationUnit.getCDDefinition().getCDClass(0).getCDAttribute(1).getMCType() instanceof ASTMCListType);
    ASTMCListType simpleReferenceType = (ASTMCListType) cdCompilationUnit.getCDDefinition().getCDClass(0).getCDAttribute(1).getMCType();
    assertFalse(simpleReferenceType.getNameList().isEmpty());
    assertEquals(3, simpleReferenceType.getNameList().size());
    assertEquals("java", simpleReferenceType.getNameList().get(0));
    assertEquals("util", simpleReferenceType.getNameList().get(1));
    assertEquals("List", simpleReferenceType.getNameList().get(2));
  }

  @Test
  public void testTypeJavaConformASTPackage() {
    //test that for AST classes the package is now java conform
    assertTrue(cdCompilationUnit.getCDDefinition().getCDClass(0).getCDAttribute(1).getMCType() instanceof ASTMCListType);
    ASTMCListType listType = (ASTMCListType) cdCompilationUnit.getCDDefinition().getCDClass(0).getCDAttribute(1).getMCType();
    assertTrue(listType.getMCTypeArgument().getMCTypeOpt().isPresent());
    assertEquals(1, listType.getMCTypeArgumentList().size());
    assertTrue(listType.getMCTypeArgument().getMCTypeOpt().get() instanceof ASTMCQualifiedType);
    ASTMCQualifiedType typeArgument = (ASTMCQualifiedType) listType.getMCTypeArgument().getMCTypeOpt().get();
    assertEquals(3, typeArgument.getNameList().size());
    assertEquals("automaton", typeArgument.getNameList().get(0));
    assertEquals("_ast", typeArgument.getNameList().get(1));
    assertEquals("ASTState", typeArgument.getNameList().get(2));
  }

  @Test
  public void testStringType() {
    //test that types like String are not changed
    assertTrue(cdCompilationUnit.getCDDefinition().getCDClass(0).getCDAttribute(0).getMCType() instanceof ASTMCQualifiedType);
    ASTMCQualifiedType simpleReferenceType = (ASTMCQualifiedType) cdCompilationUnit.getCDDefinition().getCDClass(0).getCDAttribute(0).getMCType();
    assertFalse(simpleReferenceType.getNameList().isEmpty());
    assertEquals(1, simpleReferenceType.getNameList().size());
    assertEquals("String", simpleReferenceType.getNameList().get(0));
  }
}
