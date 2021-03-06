/* (c) https://github.com/MontiCore/monticore */
package mc.feature.symboltable;

import de.monticore.io.paths.ModelPath;
import de.monticore.symboltable.ISymbol;
import mc.feature.symboltable.notopscope.NoTopScopeMill;
import mc.feature.symboltable.notopscope._ast.ASTFoo;
import mc.feature.symboltable.notopscope._parser.NoTopScopeParser;
import mc.feature.symboltable.notopscope._symboltable.*;
import mc.feature.symboltable.subnotopscope.SubNoTopScopeMill;
import mc.feature.symboltable.subnotopscope._ast.ASTSubFoo;
import mc.feature.symboltable.subnotopscope._parser.SubNoTopScopeParser;
import mc.feature.symboltable.subnotopscope._symboltable.*;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.*;

public class NoTopScopeTest {

  /**
   * test the generation of the method getTopLevelSymbol() of an ArtifactScope
   * if the start production is only a symbol and not a scope spanning symbol
   * -> check if exactly one symbol is in the ArtifactScope
   * -> do NOT check for SubScopes since there exists none here
   */

  @Test
  public void testGetTopLevelSymbol() throws IOException {
    // parse model
    NoTopScopeParser scopeAttributesParser = new NoTopScopeParser();
    Optional<ASTFoo> astSup = scopeAttributesParser.parse("src/test/resources/mc/feature/symboltable/NoTopScope.st");
    assertFalse(scopeAttributesParser.hasErrors());
    assertTrue(astSup.isPresent());

    // create symboltable
    ModelPath modelPath = new ModelPath(Paths.get("src/test/resources/mc/feature/symboltable"));
    INoTopScopeGlobalScope globalScope = NoTopScopeMill
        .noTopScopeGlobalScopeBuilder()
        .setModelPath(modelPath)
        .setModelFileExtension("st")
        .build();
    NoTopScopeSymbolTableCreatorDelegator symbolTableCreator = NoTopScopeMill
        .noTopScopeSymbolTableCreatorDelegatorBuilder()
        .setGlobalScope(globalScope)
        .build();

    INoTopScopeArtifactScope scope = symbolTableCreator.createFromAST(astSup.get());

    // only one symbol
    Optional<ISymbol> topLevelSymbol = scope.getTopLevelSymbol();
    assertTrue(topLevelSymbol.isPresent());
    assertEquals("A", topLevelSymbol.get().getName());

    // two symbols
    FooSymbol eSymbol = new FooSymbol("E");
    scope.add(eSymbol);
    topLevelSymbol = scope.getTopLevelSymbol();
    assertFalse(topLevelSymbol.isPresent());
  }

  @Test
  public void testGetTopLevelSymbolWithInherited() throws IOException {
    // parse model
    SubNoTopScopeParser scopeAttributesParser = new SubNoTopScopeParser();
    Optional<ASTSubFoo> astSup = scopeAttributesParser.parse("src/test/resources/mc/feature/symboltable/SubNoTopScope.st");
    assertFalse(scopeAttributesParser.hasErrors());
    assertTrue(astSup.isPresent());

    // create symboltable
    ModelPath modelPath = new ModelPath(Paths.get("src/test/resources/mc/feature/symboltable"));
    ISubNoTopScopeGlobalScope globalScope = SubNoTopScopeMill
        .subNoTopScopeGlobalScopeBuilder()
        .setModelPath(modelPath)
        .setModelFileExtension("st")
        .build();
    SubNoTopScopeSymbolTableCreatorDelegator symbolTableCreator = SubNoTopScopeMill
        .subNoTopScopeSymbolTableCreatorDelegatorBuilder()
        .setGlobalScope(globalScope)
        .build();
    ISubNoTopScopeArtifactScope scope = symbolTableCreator.createFromAST(astSup.get());

    // only one symbol
    Optional<ISymbol> topLevelSymbol = scope.getTopLevelSymbol();
    assertTrue(topLevelSymbol.isPresent());
    assertEquals("A", topLevelSymbol.get().getName());

    // two symbols (add symbol from super grammar)
    FooSymbol eSymbol = NoTopScopeMill.fooSymbolBuilder().setName("E").build();
    scope.add(eSymbol);
    topLevelSymbol = scope.getTopLevelSymbol();
    assertFalse(topLevelSymbol.isPresent());
  }
}
