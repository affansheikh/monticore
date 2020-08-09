/* (c) https://github.com/MontiCore/monticore */

package de.monticore.codegen.parser.antlr;

import de.monticore.codegen.cd2java.DecorationHelper;
import de.monticore.codegen.cd2java._ast.ast_class.ASTConstants;
import de.monticore.codegen.mc2cd.MCGrammarSymbolTableHelper;
import de.monticore.codegen.parser.ParserGeneratorHelper;
import de.monticore.grammar.HelperGrammar;
import de.monticore.grammar.grammar._ast.*;
import de.monticore.grammar.grammar._symboltable.MCGrammarSymbol;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.StringTransformations;

import java.util.Optional;

public class ASTConstructionActions {

  protected ParserGeneratorHelper parserGenHelper;
  
  protected MCGrammarSymbol symbolTable;
  
  public ASTConstructionActions(ParserGeneratorHelper parserGenHelper) {
    this.parserGenHelper = parserGenHelper;
    this.symbolTable = parserGenHelper.getGrammarSymbol();
  }

  protected String getConstantClassName(MCGrammarSymbol symbol) {
    return Joiners.DOT.join(symbol.getFullName().toLowerCase(),
            ASTConstants.AST_PACKAGE,
            ASTConstants.AST_CONSTANTS + symbol.getName());
  }

  public String getConstantInConstantGroupMultipleEntries(ASTConstant constant,
      ASTConstantGroup constgroup) {
    String tmp = "";
    if (constgroup.isPresentUsageName()) {
      String constfile;
      String constantname;
      Optional<MCGrammarSymbol> ruleGrammar = MCGrammarSymbolTableHelper
          .getMCGrammarSymbol(constgroup.getEnclosingScope());
      if (ruleGrammar.isPresent()) {
        constfile = getConstantClassName(ruleGrammar.get());
        constantname = parserGenHelper.getConstantNameForConstant(constant);
      }
      else {
        constfile = getConstantClassName(symbolTable);
        constantname = parserGenHelper.getConstantNameForConstant(constant);
      }
      
      // Add as attribute to AST
      tmp = "_aNode.set%uname%(%constfile%.%constantname%);";
      
      tmp = tmp.replaceAll("%uname%",
          StringTransformations.capitalize(constgroup.getUsageName()));
      
      tmp = tmp.replaceAll("%constfile%", constfile);

      tmp = tmp.replaceAll("%constantname%", constantname);
    }
    
    return tmp;
  }
  
  public String getActionAfterConstantInEnumProdSingle(ASTConstant c) {
    return "ret = true ;";
  }
  
  public String getConstantInConstantGroupSingleEntry(ASTConstant constant,
      ASTConstantGroup constgroup) {
    String tmp = "";
    
    if (constgroup.isPresentUsageName()) {
      // Add as attribute to AST
      tmp = "_aNode.set%uname%(true);";
      
      tmp = tmp.replaceAll("%uname%",
          StringTransformations.capitalize(constgroup.getUsageName()));
    }
    else {
      if (constgroup.getConstantList().size() == 1) {
        // both == null and #constants == 1 -> use constant string as name
        tmp = "_aNode.set%cname%(true);";
        tmp = tmp.replaceAll("%cname%", StringTransformations.capitalize(HelperGrammar
            .getAttributeNameForConstant(constgroup.getConstantList().get(0))));
      }
      else {
        // both == null and #constants > 1 -> user wants to ignore token in AST
      }
    }
    
    return tmp;
  }
  
  public String getActionForRuleBeforeRuleBody(ASTClassProd a) {
    StringBuilder b = new StringBuilder();
    String type = MCGrammarSymbolTableHelper
        .getQualifiedName(symbolTable.getProdWithInherited(a.getName()).get());
    Optional<MCGrammarSymbol> grammar = MCGrammarSymbolTableHelper
        .getMCGrammarSymbol(a.getEnclosingScope());
    String name = grammar.isPresent()
        ? grammar.get().getName()
        : symbolTable.getProdWithInherited(a.getName()).get().getName();
    
        // Setup return value
        b.append(
            "// ret is normally returned, a is used to be compatible with rule using the return construct\n");
        b.append(type + " _aNode = null;\n");
        b.append("_aNode=" + Names.getQualifier(type) + "." + name + "NodeFactory.create"
            +
            Names.getSimpleName(type) + "();\n");
        b.append("$ret=_aNode;\n");
        
        return b.toString();
      }

  public String getActionForAltBeforeRuleBody(String className, ASTAlt a) {
    StringBuilder b = new StringBuilder();
    String type = MCGrammarSymbolTableHelper
        .getQualifiedName(symbolTable.getProdWithInherited(className).get());
    Optional<MCGrammarSymbol> grammar = MCGrammarSymbolTableHelper
        .getMCGrammarSymbol(a.getEnclosingScope());
    String name = grammar.isPresent()
        ? grammar.get().getName()
        : symbolTable.getProdWithInherited(className).get().getName();
    
        // Setup return value
        b.append(type + " _aNode = null;\n");
        b.append("_aNode=" + Names.getQualifier(type) + "." + name + "NodeFactory.create"
            +
            Names.getSimpleName(type) + "();\n");
        b.append("$ret=_aNode;\n");
        
        return b.toString();
      }

  
  public String getActionForLexerRuleNotIteratedAttribute(ASTNonTerminal a) {

    String tmp = "_aNode.set%u_usage%(convert" + a.getName() + "($%tmp%));";
    
    // Replace templates
    tmp = tmp.replaceAll("%u_usage%",
        StringTransformations.capitalize(HelperGrammar.getUsageName(a)));
    tmp = tmp.replaceAll("%tmp%", parserGenHelper.getTmpVarNameForAntlrCode(a));
    
    return tmp;
    
  }
  
  public String getActionForLexerRuleIteratedAttribute(ASTNonTerminal a) {

    String tmpname = parserGenHelper.getTmpVarNameForAntlrCode(a);
    String tmp = " addToIteratedAttributeIfNotNull(_aNode.get%u_usage%(), convert" + a.getName()
        + "($%tmp%));";

    // Replace templates
    tmp = tmp.replaceAll("%u_usage%",
        StringTransformations.capitalize(HelperGrammar.getListName(a, symbolTable.getAstNode())));
    tmp = tmp.replaceAll("%tmp%", tmpname);

    return tmp;
  }

  public String getActionForInternalRuleIteratedAttribute(ASTNonTerminal a) {

    String tmp = "addToIteratedAttributeIfNotNull(_aNode.get%u_usage%(), _localctx.%tmp%.ret);";
    // TODO GV: || isConst()
    if (symbolTable.getProdWithInherited(a.getName()).get().isIsEnum() ) {
      tmp = "addToIteratedAttributeIfNotNull(_aNode.get%u_usage%(), _localctx.%tmp%.ret);";
    }
    
    // Replace templates
    tmp = tmp.replaceAll("%u_usage%",
        StringTransformations.capitalize(HelperGrammar.getListName(a,symbolTable.getAstNode())));
    tmp = tmp.replaceAll("%tmp%", parserGenHelper.getTmpVarNameForAntlrCode(a));
    
    return tmp;
  }

  public String getActionForInternalRuleNotIteratedAttribute(ASTNonTerminal a) {
    
    String tmp = "_aNode.set%u_usage%(_localctx.%tmp%.ret);";
    
    // Replace templates
    tmp = tmp.replaceAll("%u_usage%",
        StringTransformations.capitalize(HelperGrammar.getUsageName(a)));
    tmp = tmp.replaceAll("%tmp%", parserGenHelper.getTmpVarNameForAntlrCode(a));
    
    return tmp;
  }
  
  public String getActionForInternalRuleNotIteratedLeftRecursiveAttribute(ASTNonTerminal a) {
    
    String type = MCGrammarSymbolTableHelper
        .getQualifiedName(symbolTable.getProdWithInherited(a.getName()).get()); // TODO
                                                                                // GV:
                                                                                // getDefinedType().getQualifiedName()
    String name = MCGrammarSymbolTableHelper.getMCGrammarSymbol(a.getEnclosingScope()).get().getName();
    SourcePositionActions sourcePositionBuilder = new SourcePositionActions(parserGenHelper);
    StringBuilder b = new StringBuilder();
    b.append("// Action code for left recursive rule \n");
    b.append("_aNode=" + Names.getQualifier(type) + "." + name + "NodeFactory.create"
        +
        Names.getSimpleName(type) + "();\n");
    b.append(sourcePositionBuilder.startPositionForLeftRecursiveRule(a));
    b.append(sourcePositionBuilder.endPositionForLeftRecursiveRule(a));
    b.append("$ret=_aNode;\n");
    return b.toString();
  }
  
  /**
   * Nothing to do for ignore
   */
  public String getActionForTerminalIgnore(ASTTerminal a) {
    return "";
  }

  public String getActionForTerminalNotIteratedAttribute(ASTITerminal a) {

    String tmp = "_aNode.set%u_usage%(\"%text%\");";

    if (!a.isPresentUsageName()) {
      return "";
    }
    // Replace templates
    tmp = tmp.replaceAll("%u_usage%", StringTransformations.capitalize(a.getUsageName()));
    tmp = tmp.replaceAll("%text%", a.getName());

    return tmp;

  }

  public String getActionForTerminalIteratedAttribute(ASTITerminal a) {

    if (!a.isPresentUsageName()) {
      return "";
    }

    String tmp = "_aNode.get%u_usage%().add(\"%text%\");";

    // Replace templates
    String usageName = StringTransformations.capitalize(a.getUsageName());
    tmp = tmp.replaceAll("%u_usage%", StringTransformations.capitalize(usageName + DecorationHelper.GET_SUFFIX_LIST));
    tmp = tmp.replaceAll("%text%", a.getName());

    return tmp;
  }

  public String getActionForKeyTerminalNotIteratedAttribute(ASTKeyTerminal a) {

    String tmp = "_aNode.set%u_usage%(_input.LT(-1).getText());";

    if (!a.isPresentUsageName()) {
      return "";
    }
    // Replace templates
    return tmp.replaceAll("%u_usage%", StringTransformations.capitalize(a.getUsageName()));
   }

  public String getActionForKeyTerminalIteratedAttribute(ASTKeyTerminal a) {

    if (!a.isPresentUsageName()) {
      return "";
    }

    String tmp = "_aNode.get%u_usage%().add(_input.LT(-1).getText());";

    // Replace templates
    String usageName = StringTransformations.capitalize(a.getUsageName());
    return tmp.replaceAll("%u_usage%", StringTransformations.capitalize(usageName + DecorationHelper.GET_SUFFIX_LIST));
  }

}
