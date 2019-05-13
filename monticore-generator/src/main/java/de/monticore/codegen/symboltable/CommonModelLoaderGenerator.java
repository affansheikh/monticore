/* (c) https://github.com/MontiCore/monticore */

package de.monticore.codegen.symboltable;

import de.monticore.generating.GeneratorEngine;
import de.monticore.grammar.symboltable.MCGrammarSymbol;
import de.monticore.io.paths.IterablePath;

import java.nio.file.Path;

import static de.monticore.codegen.GeneratorHelper.getSimpleTypeNameToGenerate;
import static de.se_rwth.commons.Names.getPathFromPackage;
import static de.se_rwth.commons.Names.getSimpleName;
import static java.nio.file.Paths.get;

public class CommonModelLoaderGenerator implements ModelLoaderGenerator {

  @Override
  public void generate(GeneratorEngine genEngine, SymbolTableGeneratorHelper genHelper,
                       IterablePath handCodedPath, MCGrammarSymbol grammarSymbol) {
    final String className = getSimpleTypeNameToGenerate(getSimpleName(grammarSymbol.getFullName() + "ModelLoader"),
            genHelper.getTargetPackage(), handCodedPath);

    final Path filePath = get(getPathFromPackage(genHelper.getTargetPackage()), className + ".java");

    if(grammarSymbol.getStartProd().isPresent()) {
      genEngine.generate("symboltable.ModelLoader", filePath, grammarSymbol.getAstNode().get(),
          className);
    }
  }
}
