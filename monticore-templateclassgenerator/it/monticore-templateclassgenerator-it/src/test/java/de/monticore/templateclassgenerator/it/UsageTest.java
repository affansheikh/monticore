/* (c) https://github.com/MontiCore/monticore */
package de.monticore.templateclassgenerator.it;

import _templates._setup.GeneratorConfig;
import _templates.templates.b.JavaClass;
import _templates.templates.maintemplates.HelloMainImpl;
import de.monticore.generating.GeneratorSetup;
import de.monticore.grammar.grammar._symboltable.ProdSymbol;
import de.monticore.templateclassgenerator.EmptyNode;
import de.monticore.templateclassgenerator.util.GeneratorInterface;
import de.se_rwth.commons.logging.*;
import org.junit.BeforeClass;
import org.junit.Test;
import types.Attribute;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UsageTest  {
  private static Path outputDirectory = Paths.get("target/generated-sources/templateClasses/");

  @BeforeClass
  public static void setup(){
    LogStub.init();         // replace log by a sideffect free variant
    // LogStub.initPlusLog();  // for manual testing purpose only
    Log.enableFailQuick(false);
  }
  
  /**
   * Tests template classes generate to file method
   */
  @Test
  public void testJavaClassTemplateClass() {
    final GeneratorSetup setup = new GeneratorSetup();
    setup.setOutputDirectory(outputDirectory.toFile());
    GeneratorConfig.init(setup);
    String classname = "Test1";
    List<Attribute> attributes = new ArrayList<>();
    attributes.add(new Attribute("Integer", "i"));
    attributes.add(new Attribute("String", "s"));
    Path filePath = Paths.get("test/" + classname + ".java");
    JavaClass.generate(filePath, new EmptyNode(), "test", classname,
        attributes);
    assertTrue(Paths.get(outputDirectory+File.separator+"test"+File.separator+"Test1.java").toFile().exists());
    // TODO Test output
  }
  
  /**
   * Tests including Templates over their template class
   */
  @Test
  public void testToStringMethod() {
    String classname = "Test1";
    List<Attribute> attributes = new ArrayList<>();
    attributes.add(new Attribute("Integer", "i"));
    attributes.add(new Attribute("String", "s"));
    String s = JavaClass.generate("test", classname, attributes);
    assertNotNull(s);
  }
  
  @Test
  public void testMainTemplate() {
    GeneratorInterface gi = new HelloMainImpl();
    gi.generate(Paths.get("Test.txt"), new EmptyNode(), new ProdSymbol("ts"));
    assertTrue(Paths.get("gen"+File.separator+"Test.txt").toFile().exists());
  }

}
