/* (c) https://github.com/MontiCore/monticore */

package de.monticore.codegen.mc2cd.transl;

import de.monticore.cd.cd4analysis._ast.ASTCDClass;
import de.monticore.cd.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.codegen.mc2cd.TestHelper;
import de.monticore.types.mcbasictypes._ast.ASTMCObjectType;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

import static de.monticore.codegen.mc2cd.TransformationHelper.typeToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for the proper transformation of ASTClassProds to corresponding
 * ASTCDClasses
 * 
 */
public class InheritanceTest {
  
  private ASTCDClass astA;
  
  private ASTCDClass astB;
  
  private ASTCDClass astC;
  
  private ASTCDClass astD;
  
  private ASTCDClass astE;
  
  private ASTCDClass astF;
  
  public InheritanceTest() {
    ASTCDCompilationUnit cdCompilationUnit = TestHelper.parseAndTransform(Paths
        .get("src/test/resources/mc2cdtransformation/InheritanceGrammar.mc4")).get();

    astA = TestHelper.getCDClasss(cdCompilationUnit, "ASTA").get();
    astB = TestHelper.getCDClasss(cdCompilationUnit, "ASTB").get();
    astC = TestHelper.getCDClasss(cdCompilationUnit, "ASTC").get();
    astD = TestHelper.getCDClasss(cdCompilationUnit, "ASTD").get();
    astE = TestHelper.getCDClasss(cdCompilationUnit, "ASTE").get();
    astF = TestHelper.getCDClasss(cdCompilationUnit, "ASTF").get();

  }
  
  /**
   * Checks that the production "A extends X" results in ASTA having ASTX as a
   * superclass
   */
  @Test
  public void testExtends() {
    assertTrue(astA.isPresentSuperclass());
    String name = typeToString(astA.getSuperclass());
    assertEquals("mc2cdtransformation.InheritanceGrammar.ASTextendedProd", name);
  }
  
  /**
   * Checks that the production "B implements Y" results in ASTB having ASTY as
   * a superinterface
   */
  @Test
  public void testImplements() {
    List<ASTMCObjectType> superInterfaces = astB.getInterfaceList();
    assertEquals(1, superInterfaces.size());
    String name = typeToString(superInterfaces.get(0));
    assertEquals("mc2cdtransformation.InheritanceGrammar.ASTimplementedProd", name);
  }
  
  /**
   * Checks that the production "C astextends X" results in ASTC having X as a
   * superclass
   */
  @Test
  public void testAstextends() {
    assertTrue(astC.isPresentSuperclass());
    String name = typeToString(astC.getSuperclass());
    assertEquals("AstExtendedType", name);
  }
  
  /**
   * Checks that the production "D astimplements Y" results in ASTD having Y as
   * a superinterface
   */
  @Test
  public void testAstimplements() {
    List<ASTMCObjectType> superInterfaces = astD.getInterfaceList();
    assertEquals(1, superInterfaces.size());
    String name = typeToString(superInterfaces.get(0));
    assertEquals("AstImplementedType", name);
  }
  
  /**
   * Checks that the production "abstract C astextends x.y.Z" results in ASTC
   * having x.y.Z as a superclass
   */
  @Test
  public void testAstextendsQualified() {
    assertTrue(astE.isPresentSuperclass());
    String name = typeToString(astE.getSuperclass());
    assertEquals("java.util.Observable", name);
  }
  
  /**
   * Checks that the production "abstract D astimplements x.y.Z" results in ASTD
   * having x.y.Z as a superinterface
   */
  @Test
  public void testAstimplementsQualified() {
    List<ASTMCObjectType> superInterfaces = astF.getInterfaceList();
    assertEquals(1, superInterfaces.size());
    String name = typeToString(superInterfaces.get(0));
    assertEquals("java.io.Serializable", name);
  }
}
