/* (c) https://github.com/MontiCore/monticore */
package de.monticore.templateclassgenerator.it;

import de.se_rwth.commons.logging.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/**
 * Tests the correctness of the generated template classes methods
 *
 */
public class CorrectnessTest  {
  
  private static Path outputDirectory = Paths.get("target/generated-sources/templateClasses");

  @BeforeClass
  public static void setup() {
    LogStub.init();         // replace log by a sideffect free variant
    // LogStub.initPlusLog();  // for manual testing purpose only
    Log.enableFailQuick(false);
  }
  

  /**
   * Tests completely empty template
   */
  @Test
  public void testEmptyTemplate() {
    assertTrue(Paths.get(outputDirectory+"/_templates/templates/a/EmptyTemplateA.java").toFile().exists());
    
    boolean hasCorrectGenerate = false;
    boolean hasCorrectToString = false;

    // TODO Check des Inhalts
//    List<JavaMethodSymbol> methods = emptyTemplateClass.getMethods();
//    assertEquals(6, methods.size());
//
//    for (JavaMethodSymbol method : methods) {
//      if (method.getName().equals("generate") && method.getReturnType().getName().equals("void")) {
//        assertEquals(3, method.getParameters().size());
//        hasCorrectGenerate = true;
//      }
//
//      if (method.getName().equals("generate") && method.getReturnType().getName().equals("String")) {
//        assertEquals(1, method.getParameters().size());
//        hasCorrectToString = true;
//      }
//
//    }
//    assertTrue(hasCorrectGenerate);
//    assertTrue(hasCorrectToString);
  }
  
  /**
   * Tests template with tc.params but without tc.result
   */
  @Test
  public void testTemplateWithoutResult() {
    assertTrue(Paths.get(outputDirectory+"/_templates/templates/a/TemplateWithoutResultA.java").toFile().exists());

    boolean hasCorrectGenerate = false;
    boolean hasCorrectToString = false;
    // TODO Check des Inhalts
//    List<JavaMethodSymbol> methods = templateWithoutResultClass.getMethods();
//    assertEquals(6, methods.size());
//    ;
//    for (JavaMethodSymbol method : methods) {
//      if (method.getName().equals("generate") && method.getReturnType().getName().equals("void")) {
//        assertEquals(4, method.getParameters().size());
//        hasCorrectGenerate = true;
//      }
//
//      if (method.getName().equals("generate") && method.getReturnType().getName().equals("String")) {
//        assertEquals(2, method.getParameters().size());
//        hasCorrectToString = true;
//      }
//    }
//    assertTrue(hasCorrectGenerate);
//    assertTrue(hasCorrectToString);
  }
  
  /**
   * Template that does not contain a signature, but template code
   */
  @Test
  public void testTemplateWithoutSignature() {
    assertTrue(Paths.get(outputDirectory+"/_templates/templates/a/TemplateWithoutSignatureA.java").toFile().exists());

    boolean hasCorrectGenerate = false;
    boolean hasCorrectToString = false;

    // TODO Check des Inhalts
//    List<JavaMethodSymbol> methods = templateWithoutSignature.getMethods();
//    assertEquals(6, methods.size());
//    for (JavaMethodSymbol method : methods) {
//      if (method.getName().equals("generate") && method.getReturnType().getName().equals("void")) {
//        assertEquals(3, method.getParameters().size());
//        hasCorrectGenerate = true;
//      }
//
//      if (method.getName().equals("generate") && method.getReturnType().getName().equals("String")) {
//        assertEquals(1, method.getParameters().size());
//        hasCorrectToString = true;
//      }
//    }
//    assertTrue(hasCorrectGenerate);
//    assertTrue(hasCorrectToString);
//
  }
  
  /**
   * Tests template with tc.params and tc.result
   */
  @Test
  public void testTemplateWithResult() {
    assertTrue(Paths.get(outputDirectory+"/_templates/templates/a/TemplateWithResultA.java").toFile().exists());

    boolean hasCorrectGenerate = false;
    boolean hasCorrectToString = false;
    boolean hasCorrectToResult = false;

    // TODO Check des Inhalts
//    List<JavaMethodSymbol> methods = templateWithResult.getMethods();
//    assertEquals(8, methods.size());
//    for (JavaMethodSymbol method : methods) {
//      if (method.getName().equals("generate") && method.getReturnType().getName().equals("void")) {
//        assertEquals(4, method.getParameters().size());
//        hasCorrectGenerate = true;
//      }
//
//      if (method.getName().equals("generate") && method.getReturnType().getName().equals("String")) {
//        assertEquals(2, method.getParameters().size());
//        hasCorrectToString = true;
//      }
//
//      if (method.getName().equals("generate") && method.getReturnType().getName().equals("Integer")) {
//        assertEquals(3, method.getParameters().size());
//        hasCorrectToResult = true;
//      }
//    }
//    assertTrue(hasCorrectGenerate);
//    assertTrue(hasCorrectToString);
//    assertTrue(hasCorrectToResult);
  }
  
}
