/* (c) https://github.com/MontiCore/monticore */

package de.monticore.codegen.mc2cd.transl;

import de.monticore.cd.cd4analysis._ast.ASTCDClass;
import de.monticore.cd.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.codegen.mc2cd.TestHelper;

import java.nio.file.Paths;

public class ConstantTest {
  
  private ASTCDClass astA;
  
  private ASTCDClass astB;
  
  public ConstantTest() {
    ASTCDCompilationUnit cdCompilationUnit = TestHelper.parseAndTransform(Paths
        .get("src/test/resources/mc2cdtransformation/ConstantsGrammar.mc4")).get();
    astA = TestHelper.getCDClasss(cdCompilationUnit, "ASTA").get();
    astB = TestHelper.getCDClasss(cdCompilationUnit, "ASTB").get();
  }
}
