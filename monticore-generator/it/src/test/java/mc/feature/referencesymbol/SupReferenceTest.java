/* (c) https://github.com/MontiCore/monticore */
package mc.feature.referencesymbol;

import de.monticore.io.paths.ModelPath;
import mc.feature.referencesymbol.reference._ast.ASTTest;
import mc.feature.referencesymbol.reference._symboltable.TestSymbol;
import mc.feature.referencesymbol.supgrammarref.SupGrammarRefMill;
import mc.feature.referencesymbol.supgrammarref._ast.ASTSupRand;
import mc.feature.referencesymbol.supgrammarref._ast.ASTSupRef;
import mc.feature.referencesymbol.supgrammarref._ast.ASTSupRefList;
import mc.feature.referencesymbol.supgrammarref._ast.ASTSupRefOpt;
import mc.feature.referencesymbol.supgrammarref._parser.SupGrammarRefParser;
import mc.feature.referencesymbol.supgrammarref._symboltable.ISupGrammarRefScope;
import mc.feature.referencesymbol.supgrammarref._symboltable.SupGrammarRefGlobalScope;
import mc.feature.referencesymbol.supgrammarref._symboltable.SupGrammarRefScope;
import mc.feature.referencesymbol.supgrammarref._symboltable.SupGrammarRefSymbolTableCreatorDelegator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class SupReferenceTest {
  private ASTSupRand astsupRand;
  private TestSymbol a;
  private TestSymbol b;
  private TestSymbol c;
  private TestSymbol d;

  @Before
  public void setUp() throws IOException {

    SupGrammarRefParser parser = new SupGrammarRefParser();
    Optional<ASTSupRand> astRand = parser.parse("src/test/resources/mc/feature/referencesymbol/SupReferenceModel.ref");
    assertFalse(parser.hasErrors());
    assertTrue(astRand.isPresent());
    //create symboltable
    this.astsupRand = astRand.get();


    ModelPath modelPath = new ModelPath(Paths.get("src/test/resources/mc/feature/referencesymbol"));
    SupGrammarRefGlobalScope globalScope = SupGrammarRefMill
        .supGrammarRefGlobalScopeBuilder()
        .setModelPath(modelPath)
        .setModelFileExtension("ref")
        .build();
    SupGrammarRefSymbolTableCreatorDelegator symbolTableCreator = SupGrammarRefMill
        .supGrammarRefSymbolTableCreatorDelegatorBuilder()
        .setGlobalScope(globalScope)
        .build();
    SupGrammarRefScope artifact = symbolTableCreator.createFromAST(astsupRand);
    Optional<ISupGrammarRefScope> scopeOpt = artifact.getSubScopes().stream().findAny();
    assertTrue(scopeOpt.isPresent());
    ISupGrammarRefScope innerScope = scopeOpt.get();


    Optional<TestSymbol> a = globalScope.resolveTest("SupReferenceTest.A");
    Optional<TestSymbol> b = artifact.resolveTest("SupReferenceTest.B");
    Optional<TestSymbol> c = innerScope.resolveTest("C");
    Optional<TestSymbol> d = innerScope.resolveTest("D");

    assertTrue(b.isPresent());
    assertTrue(c.isPresent());
    assertTrue(d.isPresent());
    assertTrue(a.isPresent());

    this.a = a.get();
    this.b = b.get();
    this.c = c.get();
    this.d = d.get();
  }


  @Test
  public void testWithoutSymbolTable() throws IOException {
    SupGrammarRefParser parser = new SupGrammarRefParser();
    Optional<ASTSupRand> astRand = parser.parse("src/test/resources/mc/feature/referencesymbol/SupReferenceModel.ref");
    assertFalse(parser.hasErrors());
    assertTrue(astRand.isPresent());
    ASTSupRef supRef = astRand.get().getSupRefs(0);

    assertFalse(supRef.isPresentNameDefinition());
    assertFalse(supRef.isPresentNameSymbol());
    assertEquals("A", supRef.getName());
    supRef.setName("B");
    assertEquals("B", supRef.getName());
  }

  @Test
  public void testWithoutSymbolTableOpt() throws IOException {
    SupGrammarRefParser parser = new SupGrammarRefParser();
    Optional<ASTSupRand> astRand = parser.parse("src/test/resources/mc/feature/referencesymbol/SupReferenceModel.ref");
    assertFalse(parser.hasErrors());
    assertTrue(astRand.isPresent());
    ASTSupRefOpt supRefOpt = astRand.get().getSupRefOpts(0);

    assertFalse(supRefOpt.isPresentNameDefinition());
    assertFalse(supRefOpt.isPresentNameSymbol());
    assertEquals("A", supRefOpt.getName());
    supRefOpt.setName("B");
    assertEquals("B", supRefOpt.getName());
  }

  @Test
  public void testWithoutSymbolTableList() throws IOException {
    SupGrammarRefParser parser = new SupGrammarRefParser();
    Optional<ASTSupRand> astRand = parser.parse("src/test/resources/mc/feature/referencesymbol/SupReferenceModel.ref");
    assertFalse(parser.hasErrors());
    assertTrue(astRand.isPresent());
    ASTSupRefList supRefList = astRand.get().getSupRefLists(0);

    assertTrue(supRefList.getNamesDefinitionList().isEmpty());
    assertTrue(supRefList.getNamesSymbolList().isEmpty());
    assertFalse(supRefList.getNamesList().isEmpty());
    assertEquals(supRefList.sizeNames(), 3);
    assertEquals(supRefList.sizeNamesDefinition(), 0);
    assertEquals(supRefList.sizeNamesSymbol(), 0);
    assertEquals("A", supRefList.getNames(0));
    supRefList.setNames(0, "B");
    assertEquals("B", supRefList.getNames(0));
  }

  @Test
  public void testSupRef() {
    ASTSupRef supRef = this.astsupRand.getSupRefs(1);
    assertTrue(supRef.isPresentNameDefinition());
    assertTrue(supRef.isPresentNameSymbol());
    assertEquals(supRef.getName(), "B");

    assertEquals(supRef.getNameSymbol(), b);

    assertEquals(supRef.getNameDefinition(), b.getAstNode());
  }

  @Test
  public void testSupRefSet() {
    ASTSupRef supRef = this.astsupRand.getSupRefs(1);

    //setName
    supRef.setName("C");
    assertTrue(supRef.isPresentNameDefinition());
    assertTrue(supRef.isPresentNameSymbol());
    assertEquals(supRef.getName(), "C");

    assertEquals(supRef.getNameSymbol(), c);

    assertEquals(supRef.getNameDefinition(), c.getAstNode());
  }

  @Test
  public void testSupRefOpt() {
    ASTSupRefOpt supRefOpt = this.astsupRand.getSupRefOpts(0);
    assertTrue(supRefOpt.isPresentNameDefinition());
    assertTrue(supRefOpt.isPresentNameSymbol());
    assertTrue(supRefOpt.isPresentName());
    assertEquals(supRefOpt.getName(), "A");

    assertEquals(supRefOpt.getNameSymbol(), a);

    assertEquals(supRefOpt.getNameDefinition(), a.getAstNode());
  }

  @Test
  public void testSupRefOptSet() {
    ASTSupRefOpt supRefOpt = this.astsupRand.getSupRefOpts(0);
    //setName
    supRefOpt.setName("C");
    assertTrue(supRefOpt.isPresentNameDefinition());
    assertTrue(supRefOpt.isPresentNameSymbol());
    assertTrue(supRefOpt.isPresentName());
    assertEquals(supRefOpt.getName(), "C");

    assertEquals(supRefOpt.getNameSymbol(), c);

    assertEquals(supRefOpt.getNameDefinition(), c.getAstNode());
  }

  @Test
  public void testSupRefOptSetAbsent() {
    ASTSupRefOpt supRefOpt = this.astsupRand.getSupRefOpts(0);

    //setNameAbsent
    supRefOpt.setNameAbsent();
    assertFalse(supRefOpt.isPresentNameDefinition());
    assertFalse(supRefOpt.isPresentNameSymbol());
    assertFalse(supRefOpt.isPresentName());
  }

  @Test
  public void testSupRefList() {
    ASTSupRefList supRefList = astsupRand.getSupRefLists(2);
    assertFalse(supRefList.getNamesDefinitionList().isEmpty());
    assertFalse(supRefList.getNamesSymbolList().isEmpty());
    assertFalse(supRefList.getNamesList().isEmpty());

    assertEquals(supRefList.sizeNames(), 4);
    assertEquals(supRefList.sizeNamesDefinition(), 4);
    assertEquals(supRefList.sizeNamesSymbol(), 4);

    assertEquals("A", supRefList.getNames(0));

    assertTrue(supRefList.getNamesSymbol(0).isPresent());
    assertEquals(a, supRefList.getNamesSymbol(0).get());

    assertTrue(supRefList.getNamesDefinition(0).isPresent());
    assertEquals(a.getAstNode(), supRefList.getNamesDefinition(0).get());

    assertTrue(supRefList.containsNames("B"));
    assertTrue(supRefList.containsNamesDefinition(Optional.ofNullable(b.getAstNode())));
    assertTrue(supRefList.containsNamesSymbol(Optional.ofNullable(b)));

    assertEquals(supRefList.toArrayNames().length, 4);
    assertEquals(supRefList.toArrayNamesSymbol().length, 4);
    assertEquals(supRefList.toArrayNamesDefinition().length, 4);
  }

  @Test
  public void testSupRefListSetEmpty() {
    ASTSupRefList supRefList = astsupRand.getSupRefLists(0);
    //setEmptyList
    supRefList.setNamesList(new ArrayList<>());
    assertTrue(supRefList.getNamesDefinitionList().isEmpty());
    assertTrue(supRefList.getNamesSymbolList().isEmpty());
    assertTrue(supRefList.getNamesList().isEmpty());
  }

  @Test
  public void testSupRefListAdd() {
    ASTSupRefList supRefList = astsupRand.getSupRefLists(1);

    //add "D"
    supRefList.addNames("D");
    assertFalse(supRefList.getNamesDefinitionList().isEmpty());
    assertFalse(supRefList.getNamesSymbolList().isEmpty());
    assertFalse(supRefList.getNamesList().isEmpty());

    assertEquals("D", supRefList.getNames(0));

    assertTrue(supRefList.getNamesSymbol(0).isPresent());
    assertEquals(d, supRefList.getNamesSymbol(0).get());

    assertTrue(supRefList.getNamesDefinition(0).isPresent());
    assertEquals(d.getAstNode(), supRefList.getNamesDefinition(0).get());
  }


  @Test
  public void testSupRefListSet() {
    ASTSupRefList supRefList = astsupRand.getSupRefLists(1);
    List<String> list = new ArrayList<>();
    list.add("B");
    list.add("A");
    //setNameList(list)
    supRefList.setNamesList(list);

    assertEquals(supRefList.sizeNames(), 2);
    assertEquals(supRefList.sizeNamesDefinition(), 2);
    assertEquals(supRefList.sizeNamesSymbol(), 2);


    assertEquals("B", supRefList.getNames(0));

    assertTrue(supRefList.getNamesSymbol(0).isPresent());
    assertEquals(b, supRefList.getNamesSymbol(0).get());

    assertTrue(supRefList.getNamesDefinition(0).isPresent());
    assertEquals(b.getAstNode(), supRefList.getNamesDefinition(0).get());

    assertTrue(supRefList.containsNames("A"));
    assertTrue(supRefList.containsNamesDefinition(Optional.ofNullable(a.getAstNode())));
    assertTrue(supRefList.containsNamesSymbol(Optional.ofNullable(a)));
  }

  @Test
  public void testSupRefListRemove() {
    ASTSupRefList supRefList = astsupRand.getSupRefLists(2);
    assertEquals(supRefList.sizeNames(), 4);
    assertEquals(supRefList.sizeNamesDefinition(), 4);
    assertEquals(supRefList.sizeNamesSymbol(), 4);

    //remove "B"
    supRefList.removeNames("B");

    assertEquals(supRefList.sizeNames(), 3);
    assertEquals(supRefList.sizeNamesDefinition(), 3);
    assertEquals(supRefList.sizeNamesSymbol(), 3);


    assertEquals("C", supRefList.getNames(1));

    assertTrue(supRefList.getNamesSymbol(1).isPresent());
    assertEquals(c, supRefList.getNamesSymbol(1).get());

    assertTrue(supRefList.getNamesDefinition(1).isPresent());
    assertEquals(c.getAstNode(), supRefList.getNamesDefinition(1).get());

    List<String> list = new ArrayList<>();
    list.add("A");
    list.add("C");
    list.add("D");
    assertEquals(supRefList.getNamesList(), list);

    List<Optional<TestSymbol>> symbolList = new ArrayList<>();
    symbolList.add(Optional.ofNullable(a));
    symbolList.add(Optional.ofNullable(c));
    symbolList.add(Optional.ofNullable(d));
    assertEquals(supRefList.getNamesSymbolList(), symbolList);

    List<Optional<ASTTest>> definitionList = new ArrayList<>();
    definitionList.add(Optional.ofNullable(a.getAstNode()));
    definitionList.add(Optional.ofNullable(c.getAstNode()));
    definitionList.add(Optional.ofNullable(d.getAstNode()));
    assertEquals(supRefList.getNamesDefinitionList(), definitionList);
  }
}
