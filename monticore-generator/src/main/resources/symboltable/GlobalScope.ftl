<#-- (c) https://github.com/MontiCore/monticore -->
${signature("className", "languageName", "interfaceName", "symbolProds", "hasHWC")}

<#assign genHelper = glex.getGlobalVar("stHelper")>
<#assign names = glex.getGlobalVar("nameHelper")>

<#-- Copyright -->
${defineHookPoint("JavaCopyright")}

<#-- set package -->
package ${genHelper.getTargetPackage()};

import de.monticore.ast.ASTNode;
import de.monticore.io.paths.ModelPath;
import de.monticore.symboltable.modifiers.AccessModifier;

import java.util.*;
import java.util.function.Predicate;
import de.se_rwth.commons.logging.Log;

public <#if hasHWC>abstract</#if> class ${className} extends ${languageName}Scope implements ${interfaceName} {

  protected ModelPath modelPath;

  protected ${languageName}Language ${languageName?lower_case}Language;

  protected final Map<String, Set<${languageName}ModelLoader>> modelName2ModelLoaderCache = new HashMap<>();

<#list symbolProds as symbol>
  protected Set<${genHelper.getDelegatorForSymbol(symbol)}> adapted${names.getSimpleName(symbol.getName())}SymbolDelegateList = new HashSet<${genHelper.getDelegatorForSymbol(symbol)}>();
  
</#list>

  public ${className}(ModelPath modelPath, ${languageName}Language ${languageName?lower_case}Language) {
    this.modelPath = Log.errorIfNull(modelPath);
    this.${languageName?lower_case}Language = Log.errorIfNull(${languageName?lower_case}Language);
  }


  public ModelPath getModelPath() {
    return modelPath;
  }

  public ${languageName}Language get${languageName}Language() {
    return ${languageName?lower_case}Language;
  }

  @Override
  public Optional<String> getName() {
    return Optional.empty();
  }

  public void cache(${languageName}ModelLoader modelLoader, String calculatedModelName) {
    if (modelName2ModelLoaderCache.containsKey(calculatedModelName)) {
      modelName2ModelLoaderCache.get(calculatedModelName).add(modelLoader);
    } else {
      final Set<${languageName}ModelLoader> ml = new LinkedHashSet<>();
      ml.add(modelLoader);
      modelName2ModelLoaderCache.put(calculatedModelName, ml);
    }
  }

  public boolean continueWithModelLoader(String calculatedModelName, ${languageName}ModelLoader modelLoader) {
    // cache which models are loaded
    return !modelName2ModelLoaderCache.containsKey(calculatedModelName)
      || !modelName2ModelLoaderCache.get(calculatedModelName).contains(modelLoader);
    }
    
<#list symbolProds as symbol>
  @Override
  public Collection<${genHelper.getQualifiedProdName(symbol)}Symbol> resolveAdapted${names.getSimpleName(symbol.getName())}(boolean foundSymbols, String symbolName, AccessModifier modifier, Predicate<${genHelper.getQualifiedProdName(symbol)}Symbol> predicate){
    List<${genHelper.getQualifiedSymbolType(symbol)}> adaptedSymbols = new ArrayList<${genHelper.getQualifiedSymbolType(symbol)}>();
    for (${genHelper.getDelegatorForSymbol(symbol)} symDel : adapted${names.getSimpleName(symbol.getName())}SymbolDelegateList) {
      adaptedSymbols.addAll(symDel.resolveAdapted${symbol}Symbol(foundSymbols, symbolName, modifier, predicate));
    }
    return adaptedSymbols;
  }
  
</#list>  
    
}
