/*
 * Copyright 2016 Alexandr Evstigneev
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

package com.perl5.lang.perl.parser.perlswitch;

import com.intellij.psi.tree.IElementType;
import com.perl5.lang.perl.parser.elementTypes.PerlElementType;
import com.perl5.lang.perl.parser.elementTypes.PerlTokenType;

/**
 * Created by hurricup on 15.12.2015.
 */
public interface PerlSwitchElementTypes
{
	IElementType RESERVED_SWITCH = new PerlTokenType("switch");
	IElementType RESERVED_CASE = new PerlTokenType("case");

	IElementType SWITCH_COMPOUND = new PerlElementType("SWITCH_COMPOUND");
	IElementType SWITCH_CONDITION = new PerlElementType("SWITCH_CONDITION");
	IElementType CASE_COMPOUND = new PerlElementType("CASE_COMPOUND");
	IElementType CASE_DEFAULT = new PerlElementType("CASE_DEFAULT");
	IElementType CASE_CONDITION = new PerlElementType("CASE_CONDITION");
}
