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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import com.intellij.util.io.StringRef;
import com.perl5.lang.perl.PerlLanguage;
import com.perl5.lang.perl.psi.PerlDelegatingStubBasedLightNamedElement;
import com.perl5.lang.perl.psi.PerlPolyNamedElement;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

public abstract class PerlPolyNamedStubElementType<Psi extends PerlPolyNamedElement> extends IStubElementType<PerlPolyNamedStub<?>, Psi>
{
	public PerlPolyNamedStubElementType(@NotNull @NonNls String debugName)
	{
		super(debugName, PerlLanguage.INSTANCE);
	}

	protected void serializeEntity(@NotNull StubElement stubElement, @NotNull StubOutputStream dataStream)
	{
		// fixme re-use serializier
		PerlStubSerializationUtil.serializeEntity(dataStream, stubElement);
	}

	// @NotNull
	protected StubElement deserializeEntity(@NotNull StubInputStream dataStream)
	{
		// fixme re-use serializier
		return PerlStubSerializationUtil.deserializeEntity(dataStream);
	}

	@Nullable
	protected StubElement createEntityStub(@NotNull PerlDelegatingStubBasedLightNamedElement entity)
	{
		// fixme implement
		return null;
	}

	public void indexEntityStub(@NotNull StubElement stub, @NotNull IndexSink sink)
	{
		// fixme re-use serializier

	}

	/**
	 * Serializes each of light elements in container using respective IStubElementType
	 */
	@Override
	public final void serialize(@NotNull PerlPolyNamedStub stub, @NotNull StubOutputStream dataStream) throws IOException
	{
		@SuppressWarnings("unchecked")
		Map<String, StubElementWrapper> stubsMap = stub.getStubsMap();
		dataStream.writeInt(stubsMap.size());
		for (StubElementWrapper stubElementWrapper : stubsMap.values())
		{
			stubElementWrapper.serialize(dataStream);
		}
	}

	@NotNull
	@Override
	public final PerlPolyNamedStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException
	{
		int recordsNumber = dataStream.readInt();
		Map<String, StubElementWrapper> stubsMap = new THashMap<>(recordsNumber);
		for (int i = 0; i < recordsNumber; i++)
		{
			StubElementWrapper deserializedElement = StubElementTypeWrapper.deserialize(dataStream);
			stubsMap.put(deserializedElement)
			StringRef stringRef = dataStream.readName();
			assert stringRef != null;
			stubsMap.put(stringRef.getString(), deserializeEntity(dataStream));
		}
		return new PerlPolyNamedStub(parentStub, this, stubsMap);
	}

	@NotNull
	@Override
	public final PerlPolyNamedStub createStub(@NotNull Psi psi, StubElement parentStub)
	{
		@SuppressWarnings("unchecked") Map<String, PerlDelegatingStubBasedLightNamedElement> psiMap = psi.getLightElementsMap();
		Map<String, StubElement> stubsMap = new THashMap<>();

		for (Map.Entry<String, PerlDelegatingStubBasedLightNamedElement> psiEntry : psiMap.entrySet())
		{
			StubElement entityStub = createEntityStub(psiEntry.getValue());
			if (entityStub != null)
			{
				stubsMap.put(psiEntry.getKey(), entityStub);
			}
		}

		return new PerlPolyNamedStub(parentStub, this, stubsMap);
	}

	@Override
	public final void indexStub(@NotNull PerlPolyNamedStub<?> stub, @NotNull IndexSink sink)
	{
		Map<String, StubElement> stubsMap = stub.getStubsMap();
	}

	@Override
	public boolean shouldCreateStub(ASTNode node)
	{
		PsiElement psi = node.getPsi();
		return super.shouldCreateStub(node) && psi instanceof PerlPolyNamedElement && !((PerlPolyNamedElement) psi).getNamesList().isEmpty();
	}
}
