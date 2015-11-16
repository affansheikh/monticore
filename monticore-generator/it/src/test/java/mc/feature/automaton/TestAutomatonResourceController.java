/* generated from model null*/
/* generated by template ast.Class*/
// Class declaration
/* generated by template ast_emf.ResourceController*/




package mc.feature.automaton;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import mc.feature.automaton.automaton._ast.ASTTransition;
import mc.feature.automaton.automaton._ast.AutomatonPackage;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import de.monticore.emf._ast.ASTENodePackage;

public class TestAutomatonResourceController {
 
    private static final TestAutomatonResourceController controller = new TestAutomatonResourceController();
 
    // Private constructor for Singleton-Pattern
    private TestAutomatonResourceController() {
    }
 
    public static TestAutomatonResourceController getInstance() {
        return controller;
    }
    
    public void serializeAstToECoreModelFile() {
        serializeAstToECoreModelFile("");
    }
    
    public void serializeAstToECoreModelFile(String path) {
    
        // Create a resource set. 
        ResourceSet resourceSet = new ResourceSetImpl(); 
        // Register the default resource factory -- only needed for standalone 
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
          Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl()); 
        
        // For Current Package 
        URI fileURI = URI.createFileURI(new File(path + "Automaton.ecore").getAbsolutePath());
        // Create a resource for this file. 
        Resource resource = resourceSet.createResource(fileURI);
        // Add instance of package to the contents.
        resource.getContents().add(AutomatonPackage.eINSTANCE);
        
        // For ASTNodePackage
        URI fileURIASTNode = URI.createFileURI(new File(path + "ASTENode.ecore").getAbsolutePath()); 
        Resource resourceASTNode = resourceSet.createResource(fileURIASTNode); 
        resourceASTNode.getContents().add(ASTENodePackage.eINSTANCE);
        
        // For SuperGrammars
           
        // Save the contents of the resources to the file system. 
        try { 
            resource.save(Collections.EMPTY_MAP);
            resourceASTNode.save(Collections.EMPTY_MAP);
        } 
        catch (IOException e) {e.printStackTrace();}
    
    }
    
    public void serializeASTClassInstance (ASTTransition object) {
      serializeASTClassInstance(object, "ASTTransition");
  }
  
  
  public void serializeASTClassInstance (ASTTransition object, String fileName) {
       // Create a resource set. 
      ResourceSet resourceSet = new ResourceSetImpl(); 
      // Register the default resource factory -- only needed for stand-alone! 
      resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put( Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl()); 
      // Get the URI of the model file. 
      URI fileURI = URI.createFileURI(new File(fileName + ".xmi").getAbsolutePath()); 
      // Create a resource for this file. 
      Resource resource = resourceSet.createResource(fileURI);
       // Add instance of package to the contents. 
      resource.getContents().add(object);
      // Save the contents of the resource to the file system. 
      try { resource.save(Collections.EMPTY_MAP); } catch (IOException e) {e.printStackTrace();}
  }
    

}

