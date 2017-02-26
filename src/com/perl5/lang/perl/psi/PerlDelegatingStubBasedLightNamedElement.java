package com.perl5.lang.perl.psi;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.psi.stubs.StubElement;
import com.perl5.lang.perl.idea.stubs.PerlPolyNamedStub;
import com.perl5.lang.perl.idea.stubs.StubElementTypeWrapper;
import com.perl5.lang.perl.idea.stubs.StubElementWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PerlDelegatingStubBasedLightNamedElement<Delegate extends PerlPolyNamedStubBasedElement, Stub extends StubElement>
		extends PerlDelegatingLightNamedElement<Delegate>
{
	@NotNull
	private final StubElementTypeWrapper myStubElementTypeWrapper;

	public PerlDelegatingStubBasedLightNamedElement(
			@NotNull Delegate delegate,
			@NotNull String name,
			@NotNull StubElementTypeWrapper stubElementTypeWrapper)
	{
		super(delegate, name);
		myStubElementTypeWrapper = stubElementTypeWrapper;
	}

	@Nullable
	public Stub getStub()
	{
		return getRealStub(getDelegate().getStub());
	}

	private Stub getRealStub(StubElement delegateStub)
	{
		if (delegateStub == null)
		{
			return null;
		}

		assert delegateStub instanceof PerlPolyNamedStub;

		//noinspection unchecked
		Object stubWrapper = ((PerlPolyNamedStub) delegateStub).getStubsMap().get(getName());
		if (stubWrapper == null)
		{
			return null;
		}
		assert stubWrapper instanceof StubElementWrapper;
		//noinspection unchecked
		return (Stub) ((StubElementWrapper) stubWrapper).getRealStub();
	}

	@Nullable
	public Stub getGreenStub()
	{
		Delegate delegate = getDelegate();
		if (!(delegate instanceof StubBasedPsiElementBase))
		{
			return null;
		}
		return getRealStub(((StubBasedPsiElementBase) delegate).getGreenStub());
	}

	@NotNull
	public StubElementTypeWrapper getStubElementTypeWrapper()
	{
		return myStubElementTypeWrapper;
	}
}
