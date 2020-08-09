<#-- (c) https://github.com/MontiCore/monticore -->
${tc.signature("grammarName", "grammarPackage")}
  de.monticore.modelloader.ParserBasedAstProvider astProvider =
  new de.monticore.modelloader.ParserBasedAstProvider(new ${grammarPackage}._parser.${grammarName}Parser(), "${grammarName}");

  ${grammarPackage}._symboltable.${grammarName}SymbolTableCreatorDelegator stc = ${grammarPackage}.${grammarName}Mill
  .${grammarName?uncap_first}SymbolTableCreatorDelegatorBuilder()
  .setGlobalScope(this)
  .build();

  ${grammarPackage}._symboltable.${grammarName}ModelLoader ml = ${grammarPackage}.${grammarName}Mill
  .${grammarName?uncap_first}ModelLoaderBuilder()
  .setAstProvider(astProvider)
  .setSymbolTableCreator(stc)
  .setModelFileExtension(getModelFileExtension())
  .setSymbolFileExtension(getModelFileExtension()+"sym")
  .build();

  this.modelLoader = Optional.ofNullable(ml);