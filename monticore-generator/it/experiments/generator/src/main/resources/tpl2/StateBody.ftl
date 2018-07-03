<#--
   Template, belongs to StateBody @ grammar HierAutomaton
-->
  /*StateBody*/
  {
    <#list ast.stateList as s>
        ${tc.include("tpl2.State", s)};
    </#list>
    <#list ast.transitionList as t>
        ${tc.include("tpl2.Transition", t)};
    </#list>
  }/*end*/
