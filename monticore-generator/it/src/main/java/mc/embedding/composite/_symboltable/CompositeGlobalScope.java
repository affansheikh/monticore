/* (c) https://github.com/MontiCore/monticore */
package mc.embedding.composite._symboltable;

import de.monticore.io.paths.ModelPath;
import de.monticore.symboltable.modifiers.AccessModifier;
import mc.embedding.embedded._symboltable.TextSymbol;
import mc.embedding.host._symboltable.ContentSymbol;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CompositeGlobalScope extends CompositeGlobalScopeTOP{

  public CompositeGlobalScope(ModelPath modelPath) {
    super(modelPath, "host");
  }

  public CompositeGlobalScope(ModelPath modelPath, String modelFileExtension) {
    super(modelPath, modelFileExtension);
  }

  @Override public List<ContentSymbol> resolveAdaptedContent(boolean foundSymbols,
                                                              String symbolName, AccessModifier modifier, Predicate<ContentSymbol> predicate) {
    Collection<TextSymbol> symbols = resolveTextMany(foundSymbols, symbolName, modifier, x -> true);
    return symbols.stream().map(s -> new Text2ContentAdapter(s)).collect(Collectors.toList());
  }
  
  @Override public CompositeGlobalScope getRealThis(){
    return this;
  }
}
