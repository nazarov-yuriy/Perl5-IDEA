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

package com.perl5.lang.perl.idea.structureView.elements;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.perl5.lang.perl.extensions.PerlHierarchyViewElementsProvider;
import com.perl5.lang.perl.extensions.packageprocessor.PerlExportDescriptor;
import com.perl5.lang.perl.idea.highlighter.PerlSyntaxHighlighter;
import com.perl5.lang.perl.idea.presentations.PerlItemPresentationBase;
import com.perl5.lang.perl.idea.presentations.PerlItemPresentationSimple;
import com.perl5.lang.perl.psi.*;
import com.perl5.lang.perl.psi.mixins.PerlConstantDefinitionMixin;
import com.perl5.lang.perl.psi.mro.PerlMro;
import com.perl5.lang.perl.psi.properties.PerlIdentifierOwner;
import com.perl5.lang.perl.util.*;
import com.perl5.lang.pod.PodLanguage;
import com.perl5.lang.pod.idea.structureView.PodStructureViewElement;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by hurricup on 15.08.2015.
 */
public class PerlStructureViewElement implements StructureViewTreeElement, SortableTreeElement {
  protected PsiElement myElement;
  protected boolean isInherited;
  protected boolean isImported;

  public PerlStructureViewElement(PsiElement element) {
    myElement = element;
  }

  public PerlStructureViewElement setInherited() {
    this.isInherited = true;
    return this;
  }

  public PerlStructureViewElement setImported() {
    this.isImported = true;
    return this;
  }

  public boolean isInherited() {
    return isInherited;
  }

  public boolean isImported() {
    return isImported;
  }

  @Override
  public Object getValue() {
    return myElement;
  }

  @Override
  public void navigate(boolean requestFocus) {
    if (myElement instanceof NavigationItem) {
      ((NavigationItem)myElement).navigate(requestFocus);
    }
  }

  @Override
  public boolean canNavigate() {
    return myElement instanceof NavigationItem &&
           ((NavigationItem)myElement).canNavigate();
  }

  @Override
  public boolean canNavigateToSource() {
    return myElement instanceof NavigationItem &&
           ((NavigationItem)myElement).canNavigateToSource();
  }

  @NotNull
  @Override
  public String getAlphaSortKey() {
    assert myElement instanceof PsiNamedElement;
    String name = ((PsiNamedElement)myElement).getName();
    if (name == null) {
      name = "Empty named " + myElement;
    }
    return name;
  }

  @NotNull
  @Override
  public ItemPresentation getPresentation() {

    ItemPresentation itemPresentation = createPresentation();

    if ((isInherited() || isImported()) && itemPresentation instanceof PerlItemPresentationBase) {
      if (getValue() instanceof PerlDeprecatable && ((PerlDeprecatable)getValue()).isDeprecated()) {
        ((PerlItemPresentationBase)itemPresentation).setAttributesKey(PerlSyntaxHighlighter.UNUSED_DEPRECATED);
      }
      else {
        ((PerlItemPresentationBase)itemPresentation).setAttributesKey(CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES);
      }
    }
    return itemPresentation;
  }

  protected ItemPresentation createPresentation() {
    if (myElement instanceof NavigationItem) {
      return ((NavigationItem)myElement).getPresentation();
    }
    else {
      return new PerlItemPresentationSimple(myElement, "FIXME");
    }
  }


  @NotNull
  @Override
  public TreeElement[] getChildren() {
    List<TreeElement> result = new ArrayList<TreeElement>();

    Set<String> implementedMethods = new HashSet<String>();

    if (myElement instanceof PerlFile) {
      PsiFile podFile = ((PerlFile)myElement).getViewProvider().getPsi(PodLanguage.INSTANCE);
      if (podFile != null) {
        result.add(new PodStructureViewElement(podFile));
      }

      // namespaces
      for (PerlNamespaceDefinitionElement child : PsiTreeUtil.findChildrenOfType(myElement, PerlNamespaceDefinitionElement.class)) {
        result.add(new PerlStructureViewElement(child));
      }
    }

    if (myElement instanceof PerlNamespaceDefinitionElement) {
      // global variables
      for (PerlVariableDeclarationElement child : PsiTreeUtil.findChildrenOfType(myElement, PerlVariableDeclarationElement.class)) {
        if (child.isGlobalDeclaration() && myElement.isEquivalentTo(PerlPackageUtil.getNamespaceContainerForElement(child))) {
          result.add(new PerlVariableDeclarationStructureViewElement(child));
        }
      }

      Project project = myElement.getProject();
      GlobalSearchScope projectScope = GlobalSearchScope.projectScope(project);

      // imported scalars
      for (PerlExportDescriptor exportDescritptor : ((PerlNamespaceDefinition)myElement).getImportedScalarDescriptors()) {
        String canonicalName = exportDescritptor.getTargetCanonicalName();

        Collection<PerlVariableDeclarationElement> variables = PerlScalarUtil.getGlobalScalarDefinitions(project, canonicalName);

        for (PerlVariableDeclarationElement variable : variables) {
          result.add(new PerlVariableDeclarationStructureViewElement(variable).setImported());
        }

        // globs
        Collection<PsiPerlGlobVariable> items = PerlGlobUtil.getGlobsDefinitions(project, canonicalName, projectScope);
        if (items.isEmpty()) {
          items = PerlGlobUtil.getGlobsDefinitions(project, canonicalName);
        }

        for (PerlGlobVariable item : items) {
          result.add(new PerlGlobStructureViewElement(item).setImported());
        }
      }

      // imported arrays
      for (PerlExportDescriptor exportDescritptor : ((PerlNamespaceDefinition)myElement).getImportedArrayDescriptors()) {
        String canonicalName = exportDescritptor.getTargetCanonicalName();

        Collection<PerlVariableDeclarationElement> variables = PerlArrayUtil.getGlobalArrayDefinitions(project, canonicalName);

        for (PerlVariableDeclarationElement variable : variables) {
          result.add(new PerlVariableDeclarationStructureViewElement(variable).setImported());
        }

        // globs
        Collection<PsiPerlGlobVariable> items = PerlGlobUtil.getGlobsDefinitions(project, canonicalName, projectScope);
        if (items.isEmpty()) {
          items = PerlGlobUtil.getGlobsDefinitions(project, canonicalName);
        }

        for (PerlGlobVariable item : items) {
          result.add(new PerlGlobStructureViewElement(item).setImported());
        }
      }

      // imported hashes
      for (PerlExportDescriptor exportDescritptor : ((PerlNamespaceDefinition)myElement).getImportedHashDescriptors()) {
        String canonicalName = exportDescritptor.getTargetCanonicalName();

        Collection<PerlVariableDeclarationElement> variables = PerlHashUtil.getGlobalHashDefinitions(project, canonicalName);

        for (PerlVariableDeclarationElement variable : variables) {
          result.add(new PerlVariableDeclarationStructureViewElement(variable).setImported());
        }

        // globs
        Collection<PsiPerlGlobVariable> items = PerlGlobUtil.getGlobsDefinitions(project, canonicalName, projectScope);
        if (items.isEmpty()) {
          items = PerlGlobUtil.getGlobsDefinitions(project, canonicalName);
        }

        for (PerlGlobVariable item : items) {
          result.add(new PerlGlobStructureViewElement(item).setImported());
        }
      }

      // Imported subs
      for (PerlExportDescriptor exportDescritptor : ((PerlNamespaceDefinition)myElement).getImportedSubsDescriptors()) {
        String canonicalName = exportDescritptor.getTargetCanonicalName();

        // declarations
        Collection<PerlSubDeclarationElement> subDeclarations = PerlSubUtil.getSubDeclarations(project, canonicalName, projectScope);
        if (subDeclarations.isEmpty()) {
          subDeclarations = PerlSubUtil.getSubDeclarations(project, canonicalName);
        }

        for (PerlSubDeclarationElement item : subDeclarations) {
          result.add(new PerlSubStructureViewElement(item).setImported());
        }

        // definitions
        Collection<PerlSubDefinitionElement> subDefinitions = PerlSubUtil.getSubDefinitions(project, canonicalName, projectScope);
        if (subDefinitions.isEmpty()) {
          subDefinitions = PerlSubUtil.getSubDefinitions(project, canonicalName);
        }

        for (PerlSubDefinitionElement item : subDefinitions) {
          if (item instanceof PerlConstantDefinitionMixin) {
            result.add(new PerlConstantStructureViewElement((PerlConstantDefinitionMixin)item).setImported());
          }
          else {
            result.add(new PerlSubStructureViewElement(item).setImported());
          }
        }

        // globs
        Collection<PsiPerlGlobVariable> items = PerlGlobUtil.getGlobsDefinitions(project, canonicalName, projectScope);
        if (items.isEmpty()) {
          items = PerlGlobUtil.getGlobsDefinitions(project, canonicalName);
        }

        for (PerlGlobVariable item : items) {
          result.add(new PerlGlobStructureViewElement(item).setImported());
        }
      }

      // containing globs
      for (PerlGlobVariable child : PsiTreeUtil.findChildrenOfType(myElement, PerlGlobVariable.class)) {
        if (child.isLeftSideOfAssignment() && myElement.isEquivalentTo(PerlPackageUtil.getNamespaceContainerForElement(child))) {
          implementedMethods.add(child.getName());
          result.add(new PerlGlobStructureViewElement(child));
        }
      }

      // containing subs declarations
      for (PerlSubDeclarationElement child : PsiTreeUtil.findChildrenOfType(myElement, PerlSubDeclarationElement.class)) {
        if (myElement.isEquivalentTo(PerlPackageUtil.getNamespaceContainerForElement(child))) {
          result.add(new PerlSubStructureViewElement(child));
        }
      }

      // containing subs definitions, currently only supports PerlHierarchyViewElementsProvider
      for (PerlSubDefinitionElement child : PsiTreeUtil.findChildrenOfType(myElement, PerlSubDefinitionElement.class)) {
        if (myElement.isEquivalentTo(PerlPackageUtil.getNamespaceContainerForElement(child))) {
          if (child instanceof PerlHierarchyViewElementsProvider) {
            ((PerlHierarchyViewElementsProvider)child).fillHierarchyViewElements(result, implementedMethods, false, false);
          }
          else {
            implementedMethods.add(child.getName());

            if (child instanceof PerlConstantDefinitionMixin) {
              result.add(new PerlConstantStructureViewElement((PerlConstantDefinitionMixin)child));
            }
            else {
              result.add(new PerlSubStructureViewElement(child));
            }
          }
        }
      }
    }

    // inherited elements
    if (myElement instanceof PerlNamespaceDefinitionWithIdentifier) {
      List<TreeElement> inheritedResult = new ArrayList<TreeElement>();

      String packageName = ((PerlNamespaceDefinitionElement)myElement).getPackageName();

      if (packageName != null) {
        for (PsiElement element : PerlMro.getVariants(myElement.getProject(), packageName, true)) {
          if (element instanceof PerlHierarchyViewElementsProvider) {
            ((PerlHierarchyViewElementsProvider)element).fillHierarchyViewElements(inheritedResult, implementedMethods, true, false);
          }
          else if (element instanceof PerlIdentifierOwner && !implementedMethods.contains(((PerlIdentifierOwner)element).getName())) {
            if (element instanceof PerlConstantDefinitionMixin && ((PerlConstantDefinitionMixin)element).getName() != null) {
              inheritedResult.add(new PerlConstantStructureViewElement((PerlConstantDefinitionMixin)element).setInherited());
            }
            else if (element instanceof PerlSubDefinitionElement) {
              inheritedResult.add(new PerlSubStructureViewElement((PerlSubDefinitionElement)element).setInherited());
            }
            else if (element instanceof PerlSubDeclarationElement) {
              inheritedResult.add(new PerlSubStructureViewElement((PerlSubDeclarationElement)element).setInherited());
            }
            else if (element instanceof PerlGlobVariable &&
                     ((PerlGlobVariable)element).isLeftSideOfAssignment() &&
                     ((PerlGlobVariable)element).getName() != null) {
              inheritedResult.add(new PerlGlobStructureViewElement((PerlGlobVariable)element).setInherited());
            }
          }
        }
      }

      if (!inheritedResult.isEmpty()) {
        result.addAll(0, inheritedResult);
      }
    }

    return result.toArray(new TreeElement[result.size()]);
  }
}
