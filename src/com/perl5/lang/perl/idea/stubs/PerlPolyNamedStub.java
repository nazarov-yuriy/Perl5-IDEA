package com.perl5.lang.perl.idea.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.perl5.lang.perl.psi.PerlPolyNamedStubBasedElement;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Container stub for poly-declarative psi elements, like constants, moose elements etc
 *
 * @param <Psi>
 */
public class PerlPolyNamedStub<Psi extends PerlPolyNamedStubBasedElement> extends StubBase<Psi>
{
	@NotNull
	private final Map<String, StubElementWrapper> myStubsMap;

	public PerlPolyNamedStub(StubElement parent, IStubElementType elementType, @NotNull Map<String, StubElementWrapper> stubsMap)
	{
		super(parent, elementType);
		myStubsMap = stubsMap;
	}

	/**
	 * Returns list of names for this Stub element
	 */
	@NotNull
	public Map<String, StubElementWrapper> getStubsMap()
	{
		return myStubsMap;
	}
}
