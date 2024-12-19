package org.contextmapper.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.contextmapper.dsl.cml.CMLResource;
import org.contextmapper.dsl.contextMappingDSL.ContextMappingModel;
import org.contextmapper.dsl.tests.ContextMappingDSLInjectorProvider;
import org.contextmapper.dsl.validation.ProjectAwareVisibleContextMapsProvider;
import org.contextmapper.tactic.dsl.tacticdsl.Entity;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.builder.builderState.BuilderStateUtil;
import org.eclipse.xtext.naming.SimpleNameProvider;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.impl.DefaultResourceDescription;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsData;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

@ExtendWith(InjectionExtension.class)
@InjectWith(ContextMappingDSLInjectorProvider.class)
public class ProjectAwareVisibleContextMapsProviderTest extends AbstractCMLInputFileTest {

	@Inject
	SimpleNameProvider simpleNameProvider;
	
	/**
	 * Two files:
	 * <ol>
	 * 	<li>Map and two bounded contexts with one aggregate and entity each</li>
	 *  <li>import file with one BC, one aggregate and an entity referencing a foreign entity</li>
	 * </ol>
	 * The expectation is, that the main context map is discovered starting with a reference in
	 * an entity of the imported bounded context. A {@link ResourceDescriptionsProvider} provides
	 * the necessary infrastructure. It keeps a list of resource-descriptions for the active project
	 * and allows to investigate imported and exported objects. It is prepared/stubbed according
	 * the behavior in the UI with an project containing these two files.   
	 * @throws IOException
	 */
	@Test
	void testWithSimulatedProjectResourceDescriptions() throws IOException {
		var resourceSet = getResourceSetOfTestCMLFiles("domain-object-validator-test-bounded-context.cml",
				"domain-object-validator-test-context-map.cml");
	
		// given 
		var importedCmlResource = new CMLResource(resourceSet.getResources().get(0));
		ContextMappingModel importedModel = importedCmlResource.getContextMappingModel();
		var mainCmlResource = new CMLResource(resourceSet.getResources().get(1));
		ContextMappingModel mainModel = mainCmlResource.getContextMappingModel();		
		
		
		var resourceDescriptionsProvider = prepareResourceDescriptionsProvider(importedModel, mainModel);
		var testee = new ProjectAwareVisibleContextMapsProvider(resourceDescriptionsProvider);

		var importedBoundedContext = importedModel.getBoundedContexts().get(0);
		var aggregate = importedBoundedContext.getAggregates().get(0);
		Entity entity = (Entity) aggregate.getDomainObjects().get(0);
		var reference = entity.getReferences().get(0);		
		
		// when
		// The reference originates from the imported bounded-context having no context-map
		var result = testee.get(reference);
		
		// then
		
		assertEquals(result.size(), 1);
		var theVisibleContextMap = result.iterator().next();
		assertEquals(theVisibleContextMap.getName(), "TestContextMap");
		
	}


	/**
	 * Simulates the {@link ResourceDescriptionsData} you get for a eclipse project with all files
	 * underlying (eResrource) the given model objects. 
	 *  
	 * @param importedModel
	 * @param mainModel
	 * @return
	 */
	private ResourceDescriptionsProvider prepareResourceDescriptionsProvider(EObject... activeModelObjects) {
		var strategy = new DefaultResourceDescriptionStrategy();
		strategy.setQualifiedNameProvider(simpleNameProvider);

		List<IResourceDescription> resourceDescriptionList = new ArrayList<>();
		for (var activeModelObject : activeModelObjects) {
			IResourceDescription resourceDescription = BuilderStateUtil
					.create(new DefaultResourceDescription(activeModelObject.eResource(), strategy));
			resourceDescriptionList.add(resourceDescription);
		}
		var data = new ResourceDescriptionsData(resourceDescriptionList);
		var resourceDescriptionsProvider = new ResourceDescriptionsProvider();
		resourceDescriptionsProvider.setResourceDescriptions(() -> data);
		return resourceDescriptionsProvider;
	}

	
	protected String getTestFileDirectory() {
		return "/integ-test-files/imports/";
	}
}
