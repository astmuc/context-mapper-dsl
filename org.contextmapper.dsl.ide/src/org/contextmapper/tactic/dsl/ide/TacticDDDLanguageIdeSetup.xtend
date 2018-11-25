/*
 * Copyright 2018 The Context Mapper Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.contextmapper.tactic.dsl.ide

import com.google.inject.Guice
import org.contextmapper.tactic.dsl.TacticDDDLanguageRuntimeModule
import org.contextmapper.tactic.dsl.TacticDDDLanguageStandaloneSetup
import org.eclipse.xtext.util.Modules2

/**
 * Initialization support for running Xtext languages as language servers.
 */
class TacticDDDLanguageIdeSetup extends TacticDDDLanguageStandaloneSetup {

	override createInjector() {
		Guice.createInjector(Modules2.mixin(new TacticDDDLanguageRuntimeModule, new TacticDDDLanguageIdeModule))
	}

}
