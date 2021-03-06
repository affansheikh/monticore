/* (c) https://github.com/MontiCore/monticore */

package de.monticore.grammar.cocos;

import de.monticore.grammar.grammar._ast.ASTAbstractProd;
import de.monticore.grammar.grammar._cocos.GrammarASTAbstractProdCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * Checks that abstract nonterminals do not extend more than one nonterminals/class.
 *

 */
public class AbstractNTOnlyExtendsOneNTOrClass implements GrammarASTAbstractProdCoCo {

  public static final String ERROR_CODE = "0xA4012";

  public static final String ERROR_MSG_FORMAT = " The abstract nonterminal %s must not %s more than one %s.";

  @Override
  public void check(ASTAbstractProd a) {
    if (a.getSuperRuleList().size()>1) {
      Log.error(String.format(ERROR_CODE + ERROR_MSG_FORMAT, a.getName(), "extend", "nonterminal"),
              a.get_SourcePositionStart());
    }
    if(a.getASTSuperClassList().size()>1){
      Log.error(String.format(ERROR_CODE + ERROR_MSG_FORMAT, a.getName(), "astextend", "class"),
              a.get_SourcePositionStart());
    }
  }

}
