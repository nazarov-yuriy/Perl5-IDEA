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

import com.intellij.psi.stubs.*;
import com.intellij.util.containers.ConcurrentIntObjectMap;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Class for custom serialization of light elements
 */
public class StubElementTypeWrapper
{
	private static final ConcurrentIntObjectMap<StubElementTypeWrapper> myIdMap = ContainerUtil.createConcurrentIntObjectMap();
	private final int myId;
	@NotNull
	private final IStubElementType myRealElementType;

	private StubElementTypeWrapper(int id, @NotNull IStubElementType realElementType)
	{
		myId = id;
		myRealElementType = realElementType;
	}

	public static synchronized StubElementTypeWrapper create(int id, @NotNull IStubElementType realElementType)
	{
		assert !myIdMap.containsKey(id) : "Duplicate entry: " + id + " already contains " + myIdMap.get(id);

		StubElementTypeWrapper newLightElement = new StubElementTypeWrapper(id, realElementType);
		myIdMap.put(id, newLightElement);
		return newLightElement;
	}

	private static synchronized StubElementTypeWrapper get(int id)
	{
		assert myIdMap.containsKey(id) : "There is no registered wrapper with id " + id;
		return myIdMap.get(id);
	}

	public static StubElementWrapper deserialize(@NotNull StubInputStream dataStream) throws IOException
	{
		int serializerId = dataStream.readInt();
		StubElementTypeWrapper stubElementTypeWrapper = get(serializerId);
		@SuppressWarnings("unchecked")
		Stub realStub = stubElementTypeWrapper.getRealElementType().deserialize(dataStream, null);
		assert realStub instanceof StubElement : realStub + " is not a StubElement";
		return new StubElementWrapper((StubElement) realStub, stubElementTypeWrapper);
	}

	@NotNull
	public IStubElementType getRealElementType()
	{
		return myRealElementType;
	}

	public void serialize(@NotNull StubElementWrapper stubElementWrapper, @NotNull StubOutputStream dataStream) throws IOException
	{
		dataStream.writeInt(myId);
		//noinspection unchecked
		getRealElementType().serialize(stubElementWrapper.getRealStub(), dataStream);
	}
}
