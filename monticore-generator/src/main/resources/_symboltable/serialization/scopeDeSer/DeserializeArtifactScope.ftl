<#-- (c) https://github.com/MontiCore/monticore -->
${tc.signature("symTabMill", "artifactScope", "artifactScopeBuilder", "scopeRuleAttrList")}
<#assign genHelper = glex.getGlobalVar("astHelper")>
  de.monticore.symboltable.serialization.JsonDeSers.checkCorrectDeSerForKind("${artifactScope}", scopeJson);
  String packageName = scopeJson.getStringMemberOpt(de.monticore.symboltable.serialization.JsonDeSers.PACKAGE).orElse("");
  ${artifactScope} scope = ${symTabMill}.${artifactScopeBuilder?uncap_first}().setPackageName(packageName).build();
  if (scopeJson.hasStringMember(de.monticore.symboltable.serialization.JsonDeSers.NAME)) {
    scope.setName(scopeJson.getStringMember(de.monticore.symboltable.serialization.JsonDeSers.NAME));
  }
  scope.setExportingSymbols(true);

<#list scopeRuleAttrList as attr>
  <#if genHelper.isOptional(attr.getMCType())>
  ${attr.printType()} _${attr.getName()} = deserialize${attr.getName()?cap_first}(scopeJson);
  if (_${attr.getName()}.isPresent()) {
    scope.${genHelper.getPlainSetter(attr)}(_${attr.getName()}.get());
  } else {
    scope.${genHelper.getPlainSetter(attr)}Absent();
  }
  <#else>
  scope.${genHelper.getPlainSetter(attr)}(deserialize${attr.getName()?cap_first}(scopeJson));
  </#if>
</#list>
  deserializeAdditionalArtifactScopeAttributes(scope,scopeJson);
  addSymbols(scopeJson, scope);
  return scope;