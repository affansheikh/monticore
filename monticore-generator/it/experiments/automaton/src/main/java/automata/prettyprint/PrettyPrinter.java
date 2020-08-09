/* (c) https://github.com/MontiCore/monticore */
package automata.prettyprint;

import automata._ast.ASTAutomaton;
import automata._ast.ASTState;
import automata._ast.ASTTransition;
import automata._visitor.AutomataVisitor;
/**
 * Pretty prints automatons. Use {@link #print(ASTAutomaton)} to start a pretty
 * print and get the result by using {@link #getResult()}.
 *

 */
public class PrettyPrinter implements AutomataVisitor {
  private String result = "";
  
  private int indention = 0;
  
  private String indent = "";
  
  /**
   * Prints the automaton
   * 
   * @param automaton
   */
  public void print(ASTAutomaton automaton) {
    handle(automaton);
  }
  
  /**
   * Gets the printed result.
   * 
   * @return the result of the pretty print.
   */
  public String getResult() {
    return this.result;
  }
  
  @Override
  public void visit(ASTAutomaton node) {
    println("automaton " + node.getName() + " {");
    indent();
  }
  
  @Override
  public void endVisit(ASTAutomaton node) {
    unindent();
    println("}");
  }
  
  @Override
  public void traverse(ASTAutomaton node) {
    // guarantee ordering: states before transitions
    node.getStatesList().stream().forEach(s -> s.accept(getRealThis()));
    node.getTransitionsList().stream().forEach(t -> t.accept(getRealThis()));
  }
  
  @Override
  public void visit(ASTState node) {
    print("state " + node.getName());
    if (node.isInitial()) {
      print(" <<initial>>");
    }
    if (node.isFinal()) {
      print(" <<final>>");
    }
    println(";");
  }
  
  @Override
  public void visit(ASTTransition node) {
    print(node.getFrom());
    print(" - " + node.getInput() + " > ");
    print(node.getTo());
    println(";");
  }
  
  private void print(String s) {
    result += (indent + s);
    indent = "";
  }
  
  private void println(String s) {
    result += (indent + s + "\n");
    indent = "";
    calcIndention();
  }
  
  private void calcIndention() {
    indent = "";
    for (int i = 0; i < indention; i++) {
      indent += "  ";
    }
  }
  
  private void indent() {
    indention++;
    calcIndention();
  }
  
  private void unindent() {
    indention--;
    calcIndention();
  }
}
