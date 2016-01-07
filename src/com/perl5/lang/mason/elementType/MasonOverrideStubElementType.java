/*
 * Copyright 2015 Alexandr Evstigneev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.perl5.lang.mason.elementType;

import com.perl5.lang.mason.MasonLanguage;
import com.perl5.lang.mason.psi.impl.MasonOverrideStatementImpl;
import com.perl5.lang.perl.idea.stubs.subsdefinitions.PerlSubDefinitionStub;
import com.perl5.lang.perl.parser.moose.stubs.override.PerlMooseOverrideStub;
import com.perl5.lang.perl.parser.moose.stubs.override.PerlMooseOverrideStubElementType;
import com.perl5.lang.perl.psi.PerlSubDefinitionBase;
import org.jetbrains.annotations.NotNull;

/**
 * Created by hurricup on 03.01.2016.
 */
public class MasonOverrideStubElementType extends PerlMooseOverrideStubElementType
{
	public MasonOverrideStubElementType(String name)
	{
		super(name, MasonLanguage.INSTANCE);
	}

	@Override
	public PerlSubDefinitionBase<PerlMooseOverrideStub> createPsi(@NotNull PerlSubDefinitionStub stub)
	{
		return new MasonOverrideStatementImpl((PerlMooseOverrideStub) stub, this);
	}
}