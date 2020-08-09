/* (c) https://github.com/MontiCore/monticore */

package de.monticore.codegen.mc2cd.transl;

import com.google.common.collect.Lists;
import de.monticore.ast.ASTNode;
import de.monticore.cd.cd4analysis._ast.*;
import de.monticore.codegen.cd2java.DecorationHelper;
import de.monticore.codegen.mc2cd.MC2CDStereotypes;
import de.monticore.codegen.mc2cd.TransformationHelper;
import de.monticore.grammar.grammar._ast.ASTAdditionalAttribute;
import de.monticore.grammar.grammar._ast.ASTClassProd;
import de.monticore.grammar.grammar._ast.ASTMCGrammar;
import de.monticore.types.mcfullgenerictypes.MCFullGenericTypesMill;
import de.monticore.utils.Link;
import de.se_rwth.commons.StringTransformations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static de.monticore.codegen.mc2cd.TransformationHelper.getName;
import static de.monticore.codegen.mc2cd.TransformationHelper.getUsageName;

/**
 * The CDAttributes generated from AttributeInASTs completely hide any CDAttributes derived from
 * NonTerminals.
 */
public class RemoveOverriddenAttributesTranslation implements
    UnaryOperator<Link<ASTMCGrammar, ASTCDCompilationUnit>> {

  @Override
  public Link<ASTMCGrammar, ASTCDCompilationUnit> apply(
      Link<ASTMCGrammar, ASTCDCompilationUnit> rootLink) {
    for (Link<ASTClassProd, ASTCDClass> classLink : rootLink.getLinks(ASTClassProd.class,
        ASTCDClass.class)) {

      for (Link<ASTNode, ASTCDAttribute> link : classLink.getLinks(ASTNode.class, ASTCDAttribute.class)) {
        if (isOverridden(link, classLink) && isNotInherited(link.target())) {
          ASTCDAttribute target = link.target();
          classLink.target().getCDAttributeList().remove(target);
        }
      }
    }
    return rootLink;
  }

  private boolean isOverridden(Link<ASTNode, ASTCDAttribute> link, Link<?, ASTCDClass> classLink) {
    ASTNode source = link.source();
    Optional<String> usageName = getUsageName(classLink.source(), source);
    if (!usageName.isPresent()) {
      Optional<String> name = getName(source);
      if (name.isPresent()) {
        usageName = Optional.ofNullable(StringTransformations.uncapitalize(name.get()));
      }
    }
    Set<ASTAdditionalAttribute> attributesInASTLinkingToSameClass = attributesInASTLinkingToSameClass(
        classLink);
    attributesInASTLinkingToSameClass.remove(source);

    List<ASTAdditionalAttribute> attributes = new ArrayList<>();
    if (usageName.isPresent()) {
      String name = usageName.get();
      attributes = attributesInASTLinkingToSameClass.stream()
          .filter(ASTAdditionalAttribute::isPresentName)
          .filter(x -> name.equals(x.getName()))
          .collect(Collectors.toList());
    }

    boolean matchByUsageName = !attributes.isEmpty();
    boolean matchByTypeName = false;
    if (!matchByUsageName && !usageName.isPresent()) {
      for (ASTAdditionalAttribute attributeInAST : attributesInASTLinkingToSameClass) {
        if (!attributeInAST.isPresentName()) {
          String name = attributeInAST.getMCType().printType(MCFullGenericTypesMill.mcFullGenericTypesPrettyPrinter());
          if (getName(source).orElse("").equals(name)) {
            attributes = Lists.newArrayList(attributeInAST);
            matchByTypeName = true;
            break;
          }
        }
      }
    }

    // add derived stereotype if needed
    if (link.target().isPresentModifier() &&
        hasDerivedStereotype(link.target().getModifier()) &&
        DecorationHelper.getInstance().isListType(link.target().printType())) {
      for (Link<ASTAdditionalAttribute, ASTCDAttribute> attributeLink :
          classLink.rootLink().getLinks(ASTAdditionalAttribute.class, ASTCDAttribute.class)) {
        if(attributes.contains(attributeLink.source())){
          addDerivedStereotypeToAttributes(attributeLink.target());
        }
      }
    }

    return matchByUsageName || matchByTypeName;
  }

  private boolean hasDerivedStereotype(ASTModifier modifier) {
    if (modifier.isPresentStereotype()) {
      return modifier.getStereotype().getValueList()
          .stream()
          .anyMatch(v -> v.getName().equals(MC2CDStereotypes.DERIVED_ATTRIBUTE_NAME.toString()));
    }
    return false;
  }

  private void addDerivedStereotypeToAttributes(ASTCDAttribute attribute) {
    TransformationHelper.addStereoType(attribute,
        MC2CDStereotypes.DERIVED_ATTRIBUTE_NAME.toString(), "");
  }

  private Set<ASTAdditionalAttribute> attributesInASTLinkingToSameClass(Link<?, ASTCDClass> link) {
    return link.rootLink().getLinks(ASTNode.class, ASTCDClass.class).stream()
        .filter(attributeLink -> attributeLink.target() == link.target())
        .flatMap(astRuleLink ->
            astRuleLink.getLinks(ASTAdditionalAttribute.class, ASTCDAttribute.class).stream())
        .map(Link::source).collect(Collectors.toSet());
  }

  private boolean isNotInherited(ASTCDAttribute cdAttribute) {
    if (!cdAttribute.isPresentModifier()) {
      return true;
    }
    if (!cdAttribute.getModifier().isPresentStereotype()) {
      return true;
    }
    return cdAttribute.getModifier().getStereotype().getValueList().stream()
        .map(ASTCDStereoValue::getName)
        .noneMatch(MC2CDStereotypes.INHERITED.toString()::equals);
  }
}
