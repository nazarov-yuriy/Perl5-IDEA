package com.perl5.lang.perl.lexer.elements;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.perl5.lang.perl.highlighter.PerlSyntaxHighlighter;
import com.perl5.utils.SelfStyled;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by hurricup on 19.04.2015.
 */
public class PerlHash extends PerlVariable implements SelfStyled
{
	private static final TextAttributesKey[] attributesKeys = new TextAttributesKey[]{PerlSyntaxHighlighter.PERL_HASH};

	public PerlHash(PerlVariableScope scope, boolean isBuiltIn) {
		super("PERL_HASH", scope, isBuiltIn);
	}

	@Override
	public TextAttributesKey[] getTextAttributesKey()
	{
		return attributesKeys;
	}

	public static final ArrayList<String> BUILT_IN = new ArrayList<String>( Arrays.asList(
			"%!",
			"%+",
			"%-",
			"%^H",
			"%ENV",
			"%INC",
			"%OVERLOAD",
			"%SIG"
	));
}
