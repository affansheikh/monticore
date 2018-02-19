/*
 * Copyright (c) 2017, MontiCore. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package de.monticore.prettyprint;

import de.monticore.ast.ASTNode;

public interface AstPrettyPrinter<T extends ASTNode> {
  
  String prettyPrint(T node);

}
