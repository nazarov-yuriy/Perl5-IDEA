/*
 * Copyright 2015-2017 Alexandr Evstigneev
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

package com.perl5.lang.perl.psi.references;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.perl5.lang.perl.psi.PerlNamespaceDefinitionElement;
import com.perl5.lang.perl.util.PerlPackageUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hurricup on 28.05.2015.
 */
public class PerlNamespaceReference extends PerlCachingReference<PsiElement> {
  public PerlNamespaceReference(PsiElement psiElement) {
    super(psiElement);
  }

  public PerlNamespaceReference(@NotNull PsiElement element, TextRange textRange) {
    super(element, textRange);
  }

  @Override
  protected ResolveResult[] resolveInner(boolean incompleteCode) {
    String referenceText = getRangeInElement().substring(myElement.getText());
    if (referenceText.isEmpty()) {
      referenceText = PerlPackageUtil.MAIN_PACKAGE;
    }

    Project project = myElement.getProject();
    List<ResolveResult> result = new ArrayList<>();

    for (PerlNamespaceDefinitionElement namespaceDefinition : PerlPackageUtil
      .getNamespaceDefinitions(project, PerlPackageUtil.getCanonicalPackageName(referenceText))) {
      result.add(new PsiElementResolveResult(namespaceDefinition));
    }

    return result.toArray(new ResolveResult[result.size()]);
  }
}
