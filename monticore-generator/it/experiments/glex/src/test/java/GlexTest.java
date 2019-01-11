/* (c) Monticore license: https://github.com/MontiCore/monticore */
package hierautomaton;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Optional;

import org.antlr.v4.runtime.RecognitionException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.monticore.generating.GeneratorEngine;
import de.monticore.generating.GeneratorSetup;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import hierautomaton._ast.ASTStateMachine;
import hierautomaton._parser.HierAutomatonParser;


public class GlexTest {
    
  // setup the language infrastructure
  static ASTStateMachine ast;
  
  @BeforeClass
  public static void init() throws IOException {
    // replace log by a sideffect free variant
    LogStub.init();
    HierAutomatonParser parser = new HierAutomatonParser() ;
    String model = "src/test/resources/example/HierarchyPingPong.aut";
    Optional<ASTStateMachine> optStateMachine = parser.parse(model);

    if(parser.hasErrors() || !optStateMachine.isPresent()) {
    	throw new IOException();
    }
    ast = optStateMachine.get();
    if(ast == null) {
    	throw new IOException();
    }
  }
  
  @Before
  public void setUp() throws RecognitionException, IOException {
    Log.getFindings().clear();
  }
  

  // --------------------------------------------------------------------
  @Test
  public void testSimple() throws IOException {
    GeneratorSetup s = new GeneratorSetup();
    s.setTracing(false);
    GeneratorEngine ge = new GeneratorEngine(s);

    // Empty Dummy
    StringBuilder res = ge.generate("tpl3/TestGlexOnStateMachine1.ftl", ast);

    // Stringvergleich: --------------------
    assertEquals("[]", Log.getFindings().toString());
    assertEquals("DummyTemplate with no real content\n", res.toString());
  }


  // --------------------------------------------------------------------
  @Test
  public void testTracing() throws IOException {
    GeneratorSetup s = new GeneratorSetup();

    s.setCommentStart("AAA ");
    s.setCommentEnd(" EEE");
    s.setTracing(true);
    GeneratorEngine ge = new GeneratorEngine(s);

    // Empty Dummy + tracing
    StringBuilder res = ge.generate("tpl3/TestGlexOnStateMachine1.ftl", ast);

    assertEquals("[]", Log.getFindings().toString());
    assertEquals("AAA  generated by template tpl3/TestGlexOnStateMachine1.ftl EEE\nDummyTemplate with no real content\n", res.toString());
  }


  // --------------------------------------------------------------------
  @Test
  public void testVariables() throws IOException {
    GeneratorSetup s = new GeneratorSetup();
    s.setTracing(false);
    GeneratorEngine ge = new GeneratorEngine(s);

    // two normal variables
    StringBuilder res = ge.generate("tpl3/TestVariables.ftl", ast);

    // System.out.println("****RES::\n" + res + "\n****--------");
    assertEquals("[]", Log.getFindings().toString());
    assertEquals("\nV1:17\n\nV2:35\n", res.toString());
  }

// MB: Test funtjkioniert jezt, ist also erledigt
// TODO MB: 
// dieser Test geht nicht, weil die falsche generate( methode
// aufgerufen wird (denn ast kann doppelt interpretiert werden
// Es ist wohl besser, die generate Methoden OHNE explizitem ast
// objekt umzubenennen.
// 	generate -> generateNoA
  // --------------------------------------------------------------------
  @Test
  public void testSignature() throws IOException {
    GeneratorSetup s = new GeneratorSetup();
    s.setTracing(false);
    GeneratorEngine ge = new GeneratorEngine(s);

    // ast pointer in Variable a1 abspeichern, aber keine global var
    StringBuilder res = ge.generateNoA("tpl3/TestSignature.ftl", ast);
	
    // System.out.println("****RES::\n" + res + "\n****--------");
    assertEquals("[]", Log.getFindings().toString());
    assertEquals("\n  A:OK:PingPong\n  B:OK\n", res.toString());
  }


  // --------------------------------------------------------------------
  @Test
  public void testVariables2() throws IOException {
    GeneratorSetup s = new GeneratorSetup();
    s.setTracing(false);
    GeneratorEngine ge = new GeneratorEngine(s);

    // override same variable
    StringBuilder res = ge.generate("tpl3/TestVariables2.ftl", ast);

    // System.out.println("****RES::\n" + res + "\n****--------");
    assertEquals("[0xA0122 Global Value 'v1' has already been set.\n Old value: 16\n New value: 38]", Log.getFindings().toString());
    assertEquals("\nV1:16\n\nV2:38\n\nV3:Aha\n", res.toString());
  }


  // --------------------------------------------------------------------
  @Test
  public void testVariables3() throws IOException {
    GeneratorSetup s = new GeneratorSetup();
    s.setTracing(false);
    GeneratorEngine ge = new GeneratorEngine(s);

    // override same variable
    StringBuilder res = ge.generate("tpl3/TestVariables3.ftl", ast);

    // System.out.println("****RES::\n" + res + "\n****--------");
    assertEquals("[]", Log.getFindings().toString());
    assertEquals("  A:OK\n\n  B:OK\n  C:OK\n", res.toString());
  }


  // --------------------------------------------------------------------
  @Test
  public void testVariables4() throws IOException {
    GeneratorSetup s = new GeneratorSetup();
    s.setTracing(false);
    GeneratorEngine ge = new GeneratorEngine(s);

    // override same variable
    StringBuilder res = ge.generate("tpl3/TestVariables4.ftl", ast);

    // System.out.println("****RES::\n" + res + "\n****--------");
    assertEquals("[]", Log.getFindings().toString());
    assertEquals("\n\nA:16\nB:38\nC:555\n", res.toString());
  }


  // --------------------------------------------------------------------
  @Test
  public void testVariables5() throws IOException {
    GeneratorSetup s = new GeneratorSetup();
    s.setTracing(false);
    GeneratorEngine ge = new GeneratorEngine(s);

    // test change var
    StringBuilder res = ge.generate("tpl3/TestVariables5.ftl", ast);

    // System.out.println("****RES::\n" + res + "\n****--------");
    // System.out.println("++++LOG::\n" + Log.getFindings() + "\n++++--------");
    assertEquals("[0xA0124 Global Value 'v2' has not been defined.]", 
    			Log.getFindings().toString());
    assertEquals("\n\nA:22\n\n B:OK\n", res.toString());
  }


  // --------------------------------------------------------------------
  @Test
  public void testVariables6() throws IOException {
    GeneratorSetup s = new GeneratorSetup();
    s.setTracing(false);
    GeneratorEngine ge = new GeneratorEngine(s);

    // test required
    StringBuilder res = ge.generate("tpl3/TestVariables6.ftl", ast);

    // System.out.println("****RES::\n" + res + "\n****--------");
    // System.out.println("++++LOG::\n" + Log.getFindings() + "\n++++--------");
    assertEquals("[0xA0126 Missing required value \"vU\", 0xA0126 Missing required value \"vV\"]", Log.getFindings().toString());
  }


  // --------------------------------------------------------------------
  @Test
  public void testVariables7() throws IOException {
    GeneratorSetup s = new GeneratorSetup();
    s.setTracing(false);
    GeneratorEngine ge = new GeneratorEngine(s);

    // test addToGlobalVar
    StringBuilder res = ge.generate("tpl3/TestVariables7.ftl", ast);

    // System.out.println("****RES::\n" + res + "\n****--------");
    // System.out.println("++++LOG::\n" + Log.getFindings() + "\n++++--------");
    assertEquals("[]", Log.getFindings().toString());
    assertEquals("\n\n\nA:16,17,\n\n\nB:23,25,27,\n\n\nC:34,36,\n", res.toString());
  }


  // --------------------------------------------------------------------
  @Test
  public void testShowForRefMan() throws IOException {
    GeneratorSetup s = new GeneratorSetup();
    s.setTracing(false);
    s.getGlex().defineGlobalVar("v3",67);
    GeneratorEngine ge = new GeneratorEngine(s);

    // test addToGlobalVar
    StringBuilder res = ge.generate("tpl3/TestShow.ftl", ast);

    // System.out.println("****RES::\n" + res + "\n****--------");
    // System.out.println("++++LOG::\n" + Log.getFindings() + "\n++++--------");
    assertEquals("[]", Log.getFindings().toString());
    assertEquals("\n\n\n  Var v1 is 35\n\n  Ok.\n  \n\n\n\n\n\n 16, 18, 19, 17,\n",
    								res.toString());
  }


}
