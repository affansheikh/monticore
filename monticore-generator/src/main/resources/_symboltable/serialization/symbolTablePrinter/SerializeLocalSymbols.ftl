<#-- (c) https://github.com/MontiCore/monticore -->
${tc.signature("symbolList")}
<#list symbolList as symbol>
  if (!node.getLocal${symbol}Symbols().isEmpty()) {
    printer.beginArray("${symbol?uncap_first}Symbols");
    node.getLocal${symbol}Symbols().stream().forEach(s -> s.accept(getRealThis()));
    printer.endArray();
  }
</#list>