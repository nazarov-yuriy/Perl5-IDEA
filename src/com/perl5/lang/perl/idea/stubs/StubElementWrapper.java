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

package com.perl5.lang.perl.idea.stubs;

import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Wrapper for one of the stub elements created by real IStubElementType deserialize methods
 */
public class StubElementWrapper
{
	@NotNull
	private final StubElement myRealStub;
	@NotNull
	private final StubElementTypeWrapper myStubElementTypeWrapper;

	public StubElementWrapper(@NotNull StubElement realStub, @NotNull StubElementTypeWrapper stubElementTypeWrapper)
	{
		myRealStub = realStub;
		myStubElementTypeWrapper = stubElementTypeWrapper;
	}

	@NotNull
	public StubElement getRealStub()
	{
		return myRealStub;
	}

	@NotNull
	public StubElementTypeWrapper getStubElementTypeWrapper()
	{
		return myStubElementTypeWrapper;
	}

	public void serialize(@NotNull StubOutputStream dataStream) throws IOException
	{
		getStubElementTypeWrapper().serialize(this, dataStream);
	}

	@NotNull
	public String getMapName()
	{
		getStubElementTypeWrapper().get
	}
}
