/* (c) https://github.com/MontiCore/monticore */

package de.monticore.codegen.mc2cd.transl;

import de.monticore.cd.cd4analysis._ast.ASTCDAttribute;
import de.monticore.cd.cd4analysis._ast.ASTCDClass;
import de.monticore.cd.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.codegen.mc2cd.TestHelper;
import de.monticore.codegen.mc2cd.TransformationHelper;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class TokenTypeTest {

  private final ASTCDClass astTest;

  @BeforeClass
  public static void disableFailQuick() {
    LogStub.enableFailQuick(false);
  }

  public TokenTypeTest() {
    Optional<ASTCDCompilationUnit> cdCompilationUnit = TestHelper.parseAndTransform(Paths
        .get("src/test/resources/mc2cdtransformation/LexerFormat.mc4"));
    astTest = TestHelper.getCDClasss(cdCompilationUnit.get(), "ASTTest").get();
  }

  private Optional<ASTCDAttribute> getCDAttributeByName(String name) {
    return astTest.getCDAttributesList().stream()
        .filter(cdAttribute -> name.equals(cdAttribute.getName()))
        .findAny();
  }

  @Test
  public void testNumber() {
    ASTCDAttribute cdAttribute = getCDAttributeByName("a").get();
    assertTrue(TransformationHelper.prettyPrint(cdAttribute).contains("int"));
  }

  @Test
  public void testBoolean() {
    ASTCDAttribute cdAttribute = getCDAttributeByName("b").get();
    assertTrue(TransformationHelper.prettyPrint(cdAttribute).contains("boolean"));
  }

  @Test
  public void testChar() {
    ASTCDAttribute cdAttribute = getCDAttributeByName("c").get();
    assertTrue(TransformationHelper.prettyPrint(cdAttribute).contains("char"));
  }

  @Test
  public void testInt() {
    ASTCDAttribute cdAttribute = getCDAttributeByName("d").get();
    assertTrue(TransformationHelper.prettyPrint(cdAttribute).contains("int"));
  }

  @Test
  public void testFloat() {
    ASTCDAttribute cdAttribute = getCDAttributeByName("e").get();
    assertTrue(TransformationHelper.prettyPrint(cdAttribute).contains("float"));
  }

  @Test
  public void testDouble() {
    ASTCDAttribute cdAttribute = getCDAttributeByName("f").get();
    assertTrue(TransformationHelper.prettyPrint(cdAttribute).contains("double"));
  }

  @Test
  public void testLong() {
    ASTCDAttribute cdAttribute = getCDAttributeByName("g").get();
    assertTrue(TransformationHelper.prettyPrint(cdAttribute).contains("long"));
  }

  @Test
  public void testCard() {
    ASTCDAttribute cdAttribute = getCDAttributeByName("h").get();
    assertTrue(TransformationHelper.prettyPrint(cdAttribute).contains("int"));
  }

  @Test
  public void testShort() {
    ASTCDAttribute cdAttribute = getCDAttributeByName("i").get();
    assertTrue(TransformationHelper.prettyPrint(cdAttribute).contains("short"));
  }

  @Test
  public void testByte() {
    ASTCDAttribute cdAttribute = getCDAttributeByName("j").get();
    assertTrue(TransformationHelper.prettyPrint(cdAttribute).contains("byte"));
  }

  @Test
  public void testByte2() {
    ASTCDAttribute cdAttribute = getCDAttributeByName("k").get();
    assertTrue(TransformationHelper.prettyPrint(cdAttribute).contains("byte"));
  }
}
