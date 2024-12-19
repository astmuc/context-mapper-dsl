package org.contextmapper.dsl;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IGlobalScopeProvider;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.DefaultGlobalScopeProvider;
import org.eclipse.xtext.scoping.impl.ImportUriGlobalScopeProvider;

import com.google.common.base.Predicate;
import com.google.inject.Inject;

public class ContextMapperGlobalScopeProvider implements IGlobalScopeProvider {

	
	private final ImportUriGlobalScopeProvider importUriGlobalScopeProvider;
	private final DefaultGlobalScopeProvider defaultGlobalScopeProvider;
	
	@Inject
	public ContextMapperGlobalScopeProvider(ImportUriGlobalScopeProvider importUriGlobalScopeProvider, 
			DefaultGlobalScopeProvider defaultGlobalScopeProvider) {
		this.defaultGlobalScopeProvider = defaultGlobalScopeProvider;
		this.importUriGlobalScopeProvider = importUriGlobalScopeProvider;
	}

	/**
	 * The default scope-provider is required to investigate references inside a resource-set.
	 * E.g., a resource-set of main CML and one imported CML (with BC) will NOT complain
	 * about 'Entity not found' because it can be found via a resource-set walk.
	 * This is important in workbench context which keeps the resource-set up-to-date with
	 * a builder seeing all project cml-files
	 *  
	 * The import scope-provider can additionally investigate imported resources.
	 * E.g. a main CML with an import NOT in the resource-set will see the imported resources in
	 * stand alone context. 
	 */
	@Override
	public IScope getScope(Resource context, EReference reference, Predicate<IEObjectDescription> filter) {
		var resultScope = defaultGlobalScopeProvider.getScope(context, reference, filter);
		if ( resultScope.equals(IScope.NULLSCOPE)) {
			resultScope = importUriGlobalScopeProvider.getScope(context, reference, filter);
		}
		return resultScope;
	}
}
