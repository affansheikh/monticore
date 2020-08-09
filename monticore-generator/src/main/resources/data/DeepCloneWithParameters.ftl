<#-- (c) https://github.com/MontiCore/monticore -->
${tc.signature("attributeList")}
<#assign genHelper = glex.getGlobalVar("astHelper")>
<#assign service = glex.getGlobalVar("service")>
    super.deepClone(result);

<#list attributeList as attribute>
<#if attribute.isPresentModifier() && !service.isReferencedSymbolAttribute(attribute) && !service.isInheritedAttribute(attribute)>
    <#assign attributeName = attribute.getName()>
    <#assign methName = genHelper.getNativeAttributeName(attribute.getName())?cap_first>
    <#assign attrType = attribute.getMCType()>
    <#assign typeName = genHelper.printType(attribute.getMCType())>
    <#if genHelper.isSimpleAstNode(attribute)>
      result.set${methName}(get${methName}().deepClone());
    <#elseif genHelper.isPrimitive(attrType)>
      result.set${methName}(${genHelper.getPlainGetter(attribute)}());
    <#elseif genHelper.isOptional(attribute.getMCType())>
        <#assign reference = genHelper.getReferenceTypeFromOptional(attrType)>
    <#assign referenceName = typeName>
    <#if genHelper.isString(reference) || genHelper.isAttributeOfTypeEnum(attribute)>
    if (isPresent${methName}()) {
      result.set${methName}(get${methName}());
    } else {
      result.set${methName}Absent();
    }
    <#elseif genHelper.isOptionalAstNode(attribute)>
    if (isPresent${methName}()){
      result.set${methName}(get${methName}().deepClone());
    } else {
      result.set${methName}Absent();
    }
    <#elseif !service.isReferencedSymbolAttribute(attribute)>
    if (isPresent${methName}()){
      result.set${methName}(get${methName}());
    } else {
      result.set${methName}Absent();
    }
    </#if>
  <#else>
    <#if genHelper.isListAstNode(attribute)>
      get${methName}List().forEach(s -> result.${attributeName}.add(s.deepClone()));
    <#elseif genHelper.isListType(typeName)>
      for ( int i = 0; i < this.get${methName}List().size() ; i++ ) {
      result.add${methName}(get${methName}(i));
      }
    <#elseif genHelper.isString(typeName) || genHelper.isAttributeOfTypeEnum(attribute)>
      result.set${methName}(get${methName}());
    <#else>
      result.set${methName}(get${methName}());
    </#if>
  </#if>
</#if>
</#list>
    
    return result;
