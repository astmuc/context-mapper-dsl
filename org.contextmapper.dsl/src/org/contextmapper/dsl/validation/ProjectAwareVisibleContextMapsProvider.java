package org.contextmapper.dsl.validation;

import java.util.HashSet;
import java.util.Set;

import org.contextmapper.dsl.contextMappingDSL.ContextMap;
import org.contextmapper.dsl.contextMappingDSL.ContextMappingDSLPackage;
import org.contextmapper.dsl.contextMappingDSL.ContextMappingModel;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;

import com.google.inject.Inject;

/**
 * Responsible to get the context-maps available in all files of the project which hosts
 * the given model entity. The available resource-descriptions can be seen in the CM-UI
 * with "Navigate -» Open Model Element" 
 * Means, in a stand alone context no more context-maps than the (optional) containing one will
 * be discovered.
 */
public class ProjectAwareVisibleContextMapsProvider {
	
	private final ResourceDescriptionsProvider descriptionsProvider;
	
	@Inject
	public ProjectAwareVisibleContextMapsProvider(ResourceDescriptionsProvider descriptionsProvider) {
		this.descriptionsProvider = descriptionsProvider;
	}

	public Set<ContextMap> get(EObject anchor) {
		Set<ContextMap> visibleContextMaps = new HashSet<>();
		var rootContainer = EcoreUtil2.getRootContainer(anchor);
		if (rootContainer instanceof ContextMappingModel) {
			addContextMap((ContextMappingModel) rootContainer, visibleContextMaps);
			Resource anchorResource = anchor.eResource();
			var index = descriptionsProvider.getResourceDescriptions(anchorResource);
			for (var desc : index.getAllResourceDescriptions()) {
				for (var exported : desc.getExportedObjects()) {
					var exportedBC = exported.getEObjectOrProxy();
					if (exportedBC.eClass().equals(ContextMappingDSLPackage.eINSTANCE.getBoundedContext())) {
						if (exportedBC.eIsProxy()) {
							exportedBC = EcoreUtil.resolve(exportedBC, anchorResource.getResourceSet());
						}
						ContextMappingModel contextMappingModel = (ContextMappingModel) EcoreUtil2
								.getRootContainer(exportedBC);
						addContextMap(contextMappingModel, visibleContextMaps);
					}
				}
			} 
		}
		return visibleContextMaps;
	}

	private void addContextMap(ContextMappingModel anchor, Set<ContextMap> visibleContextMaps) {
		if ( anchor != null && anchor.getMap() != null ) {
			visibleContextMaps.add(anchor.getMap());
		}
	}

}
