package org.contextmapper.dsl.ui.tests

import java.awt.GraphicsEnvironment
import org.eclipse.core.resources.IResource
import org.eclipse.emf.ecore.EValidator
import org.eclipse.xtext.ui.XtextProjectHelper
import org.eclipse.xtext.ui.testing.AbstractWorkbenchTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assumptions

import static extension org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.*
import static extension org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil.*
import org.junit.jupiter.api.Test

class ReferenceValidationTest extends AbstractWorkbenchTest {

	val TEST_PROJECT = "TestCmProject"

	val importContentValid = '''
		BoundedContext ContextC100 {
			Aggregate TestAggregateC100 {
				Entity TestEntityC301 {
					- @TestEntityA101 ref
				}
			}
		}
	'''

	val importContentInvalid = '''
		BoundedContext ContextC100 {
			Aggregate TestAggregateC100 {
				Entity TestEntityC301 {
					- @UnusedEntityB101 ref
				}
			}
		}
	'''

	val mainContent = '''
		ContextMap TestContextMap {
			contains ContextA100, ContextB100, ContextC100
			
			ContextA100 -> ContextC100 {
				exposedAggregates TestAggregateA100
			}
		}
		BoundedContext ContextA100 {
			Aggregate TestAggregateA100 {
				Entity TestEntityA101
			}
		}
		BoundedContext ContextB100 {
			Aggregate TestAggregateB100 {
				Entity UnusedEntityB101
			}
		}
		import "bounded-context.cml"
	'''

	
	protected def void skipWhenHeadless() {
		Assumptions.assumeTrue(!GraphicsEnvironment.isHeadless(), "Skipping UI test in headless environment")
	}
	
	@Test
	def void testValidReference() {
    	skipWhenHeadless();
    	
		createJavaProject(TEST_PROJECT) => [
			getProject().addNature(XtextProjectHelper.NATURE_ID)
			addSourceFolder("src")
		]

		val mainFileName = '''«TEST_PROJECT»/src/context-map.cml'''
		val mainFile = createFile(mainFileName, mainContent)
		val importFileName = '''«TEST_PROJECT»/src/bounded-context.cml'''
		val importFile = createFile(importFileName, importContentValid)

		waitForBuild

		Assertions.assertEquals(0, mainFile.findMarkers(EValidator.MARKER, true, IResource.DEPTH_INFINITE).size)
		Assertions.assertEquals(0, importFile.findMarkers(EValidator.MARKER, true, IResource.DEPTH_INFINITE).size)

	}


	@Test
	def void testInvalidReference() {
    	skipWhenHeadless();

		createJavaProject(TEST_PROJECT) => [
			getProject().addNature(XtextProjectHelper.NATURE_ID)
			addSourceFolder("src")
		]

		val mainFileName = '''«TEST_PROJECT»/src/context-map.cml'''
		val mainFile = createFile(mainFileName, mainContent)
		val importFileName = '''«TEST_PROJECT»/src/bounded-context.cml'''
		val importFile = createFile(importFileName, importContentInvalid)

		waitForBuild

		Assertions.assertEquals(0, mainFile.findMarkers(EValidator.MARKER, true, IResource.DEPTH_INFINITE).size)
		Assertions.assertEquals(1, importFile.findMarkers(EValidator.MARKER, true, IResource.DEPTH_INFINITE).size)
	}

}

