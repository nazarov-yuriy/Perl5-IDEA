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

package com.perl5.lang.perl.util;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.perl5.lang.perl.PerlScopes;
import com.perl5.lang.perl.extensions.packageprocessor.PerlLibProvider;
import com.perl5.lang.perl.extensions.packageprocessor.PerlPackageProcessor;
import com.perl5.lang.perl.fileTypes.PerlFileTypePackage;
import com.perl5.lang.perl.idea.configuration.settings.PerlSharedSettings;
import com.perl5.lang.perl.idea.manipulators.PerlNamespaceElementManipulator;
import com.perl5.lang.perl.idea.refactoring.rename.RenameRefactoringQueue;
import com.perl5.lang.perl.internals.PerlVersion;
import com.perl5.lang.perl.lexer.PerlElementTypes;
import com.perl5.lang.perl.psi.PerlNamespaceDefinitionElement;
import com.perl5.lang.perl.psi.PerlSubDefinitionElement;
import com.perl5.lang.perl.psi.PerlSubElement;
import com.perl5.lang.perl.psi.PerlUseStatement;
import com.perl5.lang.perl.psi.impl.PerlFileImpl;
import com.perl5.lang.perl.psi.stubs.PerlSubStub;
import com.perl5.lang.perl.psi.stubs.namespaces.PerlLightNamespaceDirectIndex;
import com.perl5.lang.perl.psi.stubs.namespaces.PerlLightNamespaceReverseIndex;
import com.perl5.lang.perl.psi.stubs.namespaces.PerlNamespaceDefinitionDirectIndex;
import com.perl5.lang.perl.psi.stubs.namespaces.PerlNamespaceDefinitionReverseIndex;
import com.perl5.lang.perl.psi.utils.PerlPsiUtil;
import gnu.trove.THashSet;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by hurricup on 24.04.2015.
 */
public class PerlPackageUtil implements PerlElementTypes, PerlBuiltInNamespaces {
  public static final String PACKAGE_SEPARATOR = "::";
  public static final String PACKAGE_DEREFERENCE = "->";
  public static final char PACKAGE_SEPARATOR_LEGACY = '\'';
  public static final Pattern PACKAGE_SEPARATOR_RE = Pattern.compile(PACKAGE_SEPARATOR + "|" + PACKAGE_SEPARATOR_LEGACY);
  public static final Pattern PACKAGE_SEPARATOR_TAIL_RE = Pattern.compile("(" + PACKAGE_SEPARATOR + "|" + PACKAGE_SEPARATOR_LEGACY + ")$");

  public static final Set<String> BUILT_IN_ALL = new THashSet<>();

  public static final String SUPER_PACKAGE = "SUPER";
  public static final String SUPER_PACKAGE_FULL = SUPER_PACKAGE + PACKAGE_SEPARATOR;

  public static final String MAIN_PACKAGE = "main";
  public static final String MAIN_PACKAGE_FULL = MAIN_PACKAGE + PACKAGE_SEPARATOR;
  public static final String MAIN_PACKAGE_SHORT = PACKAGE_SEPARATOR;

  public static final String UNIVERSAL_PACKAGE = "UNIVERSAL";

  public static final String CORE_PACKAGE = "CORE";
  public static final String CORE_PACKAGE_FULL = CORE_PACKAGE + PACKAGE_SEPARATOR;

  private static final Set<String> INTERNAL_PACKAGES = new THashSet<>();

  private static final Map<String, String> CANONICAL_NAMES_CACHE = new ConcurrentHashMap<>();
  private static final Map<String, String> myFilePathsToPackageNameMap = new ConcurrentHashMap<>();

  static {
    BUILT_IN_ALL.addAll(BUILT_IN);
    BUILT_IN_ALL.addAll(BUILT_IN_PRAGMA);
    BUILT_IN_ALL.addAll(BUILT_IN_DEPRECATED);

    INTERNAL_PACKAGES.add(SUPER_PACKAGE);
    INTERNAL_PACKAGES.add(MAIN_PACKAGE);
    INTERNAL_PACKAGES.add(UNIVERSAL_PACKAGE);
    INTERNAL_PACKAGES.add(CORE_PACKAGE);
  }

  /**
   * Checks if package is built in
   *
   * @param pacakgeName package name
   * @return result
   */
  public static boolean isBuiltIn(String pacakgeName) {
    return BUILT_IN_ALL.contains(getCanonicalPackageName(pacakgeName));
  }

  /**
   * Checks if package is pragma
   *
   * @param pacakgeName package name
   * @return result
   */
  public static boolean isPragma(String pacakgeName) {
    return BUILT_IN_PRAGMA.contains(getCanonicalPackageName(pacakgeName));
  }

  /**
   * Checks if package is deprecated
   *
   * @param packageName package name
   * @return result
   */
  public static boolean isDeprecated(Project project, String packageName) {
    if (INTERNAL_PACKAGES.contains(packageName)) {
      return false;
    }

    for (PerlNamespaceDefinitionElement definition : PerlPackageUtil.getNamespaceDefinitions(project, packageName)) {
      if (definition.isDeprecated()) {
        return true;
      }
    }

    return BUILT_IN_DEPRECATED.contains(getCanonicalPackageName(packageName));
  }

  public static boolean isSUPER(String packageName) {
    return PerlPackageUtil.SUPER_PACKAGE.equals(packageName);
  }

  public static boolean isMain(String packageName) {
    return PerlPackageUtil.MAIN_PACKAGE.equals(packageName);
  }

  public static boolean isCORE(String packageName) {
    return PerlPackageUtil.CORE_PACKAGE.equals(packageName);
  }

  public static boolean isUNIVERSAL(String packageName) {
    return PerlPackageUtil.UNIVERSAL_PACKAGE.equals(packageName);
  }


  /**
   * Make canonical package name.
   *
   * @param name package name
   * @return canonical package name
   */
  public static String getCanonicalPackageName(@NotNull String name) {
    String canonicalName = getCanonicalName(name);
    return StringUtils.startsWith(canonicalName, MAIN_PACKAGE_FULL) ?
           canonicalName.substring(MAIN_PACKAGE_FULL.length()) : canonicalName;
  }

  public static String getCanonicalName(@NotNull String name) {
    String newName;

    if ((newName = CANONICAL_NAMES_CACHE.get(name)) != null) {
      return newName;
    }

    String originalName = name;

    name = PACKAGE_SEPARATOR_TAIL_RE.matcher(name).replaceFirst("");

    String[] chunks = PACKAGE_SEPARATOR_RE.split(name, -1);

    if (chunks.length > 0 && chunks[0].isEmpty())    // implicit main
    {
      chunks[0] = PerlPackageUtil.MAIN_PACKAGE;
    }

    newName = StringUtils.join(chunks, "::");

    CANONICAL_NAMES_CACHE.put(originalName, newName);

    return newName;
  }

  /**
   * Searching of namespace element is in. If no explicit namespaces defined, main is returned
   *
   * @param element psi element to find definition for
   * @return canonical package name
   */
  @Nullable
  public static String getContextPackageName(PsiElement element) {
    PerlNamespaceDefinitionElement namespaceDefinition = getContainingNamespace(element);

    if (namespaceDefinition != null && namespaceDefinition.getPackageName() != null) // checking that definition is valid and got namespace
    {
      String name = namespaceDefinition.getPackageName();
      assert name != null;
      return name;
    }

    // default value
    PsiFile file = element.getContainingFile();
    if (file instanceof PerlFileImpl) {
      PsiElement contextParent = file.getContext();
      PsiElement realParent = file.getParent();

      if (contextParent != null && !contextParent.equals(realParent)) {
        return getContextPackageName(contextParent);
      }

      return ((PerlFileImpl)file).getPackageName();
    }
    else {
      return PerlPackageUtil.MAIN_PACKAGE;
    }
  }

  // fixme shouldn't we have recursion protection here?
  @Nullable
  public static PerlNamespaceDefinitionElement getNamespaceContainerForElement(@Nullable PsiElement element) {
    if (element == null) {
      return null;
    }

    PerlNamespaceDefinitionElement namespaceContainer = PsiTreeUtil.getParentOfType(element, PerlNamespaceDefinitionElement.class);

    if (namespaceContainer instanceof PerlFileImpl) {
      PsiElement contextParent = namespaceContainer.getContext();
      if (contextParent != null && !contextParent.equals(namespaceContainer.getParent())) {
        return getNamespaceContainerForElement(contextParent);
      }
    }
    return namespaceContainer;
  }

  // fixme take fileContext in account?
  public static PerlNamespaceDefinitionElement getContainingNamespace(PsiElement element) {
    return PsiTreeUtil.getStubOrPsiParentOfType(element, PerlNamespaceDefinitionElement.class);
  }

  @NotNull
  public static List<PerlNamespaceDefinitionElement> collectNamespaceDefinitions(@NotNull Project project,
                                                                                 @NotNull List<String> packageNames) {
    ArrayList<PerlNamespaceDefinitionElement> namespaceDefinitions = new ArrayList<>();
    for (String packageName : packageNames) {
      Collection<PerlNamespaceDefinitionElement> list =
        getNamespaceDefinitions(project, packageName, GlobalSearchScope.projectScope(project));

      if (list.isEmpty()) {
        list = getNamespaceDefinitions(project, packageName, PerlScopes.getProjectAndLibrariesScope(project));
      }

      namespaceDefinitions.addAll(list);
    }
    return namespaceDefinitions;
  }

  /**
   * Searching project files for namespace definitions by specific package name
   *
   * @param project     project to search in
   * @param packageName canonical package name (without tailing ::)
   * @return collection of found definitions
   */
  public static Collection<PerlNamespaceDefinitionElement> getNamespaceDefinitions(Project project, @NotNull String packageName) {
    return getNamespaceDefinitions(project, packageName, PerlScopes.getProjectAndLibrariesScope(project));
  }

  public static Collection<PerlNamespaceDefinitionElement> getNamespaceDefinitions(Project project,
                                                                                   @NotNull String packageName,
                                                                                   GlobalSearchScope scope) {
    Collection<PerlNamespaceDefinitionElement> elements =
      StubIndex.getElements(PerlNamespaceDefinitionDirectIndex.KEY, packageName, project, scope, PerlNamespaceDefinitionElement.class);
    PerlLightNamespaceDirectIndex.processNamespaces(project, packageName, scope, namespace -> {
      elements.add(namespace);
      return true;
    });
    return elements;
  }

  /**
   * Returns list of defined package names
   *
   * @param project project to search in
   * @return collection of package names
   */
  public static Collection<String> getDefinedPackageNames(Project project) {
    Collection<String> keys = StubIndex.getInstance().getAllKeys(PerlNamespaceDefinitionDirectIndex.KEY, project);
    keys.addAll(StubIndex.getInstance().getAllKeys(PerlLightNamespaceDirectIndex.KEY, project));
    return keys;
  }

  /**
   * Processes all global packages names with specific processor
   *
   * @param scope     search scope
   * @param processor string processor for suitable strings
   * @return collection of constants names
   */
  public static boolean processPackages(@NotNull String name,
                                        @NotNull Project project,
                                        GlobalSearchScope scope,
                                        Processor<PerlNamespaceDefinitionElement> processor) {
    return StubIndex.getInstance().processElements(PerlNamespaceDefinitionDirectIndex.KEY,
                                                   name,
                                                   project,
                                                   scope,
                                                   PerlNamespaceDefinitionElement.class,
                                                   processor) &&
           PerlLightNamespaceDirectIndex.processNamespaces(project, name, scope, processor);
  }

  /**
   * Returns list of derived classes
   *
   * @param project project to search in
   * @return collection of definitions
   */
  @NotNull
  public static List<PerlNamespaceDefinitionElement> getDerivedNamespaceDefinitions(@NotNull Project project,
                                                                                    @Nullable String packageName) {
    if (StringUtil.isEmpty(packageName)) {
      return Collections.emptyList();
    }
    List<PerlNamespaceDefinitionElement> list =
      getDerivedNamespaceDefinitions(project, packageName, GlobalSearchScope.projectScope(project));
    if (list.isEmpty()) {
      list = getDerivedNamespaceDefinitions(project, packageName, PerlScopes.getProjectAndLibrariesScope(project));
    }
    return list;
  }

  public static List<PerlNamespaceDefinitionElement> getDerivedNamespaceDefinitions(@NotNull Project project,
                                                                                    @NotNull String packageName,
                                                                                    @NotNull GlobalSearchScope scope) {
    ArrayList<PerlNamespaceDefinitionElement> elements = new ArrayList<>(
      StubIndex.getElements(PerlNamespaceDefinitionReverseIndex.KEY, packageName, project, scope, PerlNamespaceDefinitionElement.class)
    );
    PerlLightNamespaceReverseIndex.processNamespaces(project, packageName, scope, namespace -> {
      elements.add(namespace);
      return true;
    });
    return elements;
  }

  /**
   * Builds package path from packageName Foo::Bar => Foo/Bar.pm
   *
   * @param packageName canonical package name
   * @return package path
   */
  public static String getPackagePathByName(String packageName) {
    return StringUtils.join(packageName.split(":+"), '/') + ".pm";
  }

  /**
   * Translates package relative name to the package name Foo/Bar.pm => Foo::Bar
   *
   * @param packagePath package relative path
   * @return canonical package name
   */
  public static String getPackageNameByPath(final String packagePath) {
    String result = myFilePathsToPackageNameMap.get(packagePath);

    if (result == null) {
      String path = packagePath.replaceAll("\\\\", "/");
      result = getCanonicalPackageName(StringUtils.join(path.replaceFirst("\\.pm$", "").split("/"), PACKAGE_SEPARATOR));
      myFilePathsToPackageNameMap.put(packagePath, result);
    }
    return result;
  }

  /**
   * Adds to queue netsted namespaces, which names should be adjusted to the new package name/path
   *
   * @param queue   - RenameRefactoringQueue
   * @param file    - file has been moved
   * @param oldPath - previous filepath
   */
  public static void collectNestedPackageDefinitionsFromFile(@NotNull RenameRefactoringQueue queue, VirtualFile file, String oldPath) {
    Project project = queue.getProject();
    VirtualFile newInnermostRoot = PerlUtil.getFileClassRoot(project, file);

    if (newInnermostRoot != null) {
      String newRelativePath = VfsUtil.getRelativePath(file, newInnermostRoot);
      String newPackageName = PerlPackageUtil.getPackageNameByPath(newRelativePath);

      VirtualFile oldInnermostRoot = PerlUtil.getFileClassRoot(project, oldPath);

      if (oldInnermostRoot != null) {
        String oldRelativePath = oldPath.substring(oldInnermostRoot.getPath().length());
        String oldPackageName = PerlPackageUtil.getPackageNameByPath(oldRelativePath);

        if (!oldPackageName.equals(newPackageName)) {
          PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
          if (psiFile != null) {
            for (PerlNamespaceDefinitionElement namespaceDefinition : PsiTreeUtil
              .findChildrenOfType(psiFile, PerlNamespaceDefinitionElement.class)) {
              if (oldPackageName.equals(namespaceDefinition.getPackageName())) {
                queue.addElement(namespaceDefinition, newPackageName);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Searches for all pm files and add renaming of nested package definitions to the queue. Invoked after renaming
   *
   * @param queue     RenameRefactoringQueue object
   * @param directory VirtualFile of renamed directory
   * @param oldPath   old directory path
   */
  public static void collectNestedPackageDefinitions(RenameRefactoringQueue queue, VirtualFile directory, String oldPath) {
    Project project = queue.getProject();
    VirtualFile directorySourceRoot = PerlUtil.getFileClassRoot(project, directory);

    if (directorySourceRoot != null) {
      for (VirtualFile file : VfsUtil.collectChildrenRecursively(directory)) {
        if (!file.isDirectory() &&
            file.getFileType() == PerlFileTypePackage.INSTANCE &&
            directorySourceRoot.equals(PerlUtil.getFileClassRoot(project, file))) {
          String relativePath = VfsUtil.getRelativePath(file, directory);
          String oldFilePath = oldPath + "/" + relativePath;
          collectNestedPackageDefinitionsFromFile(queue, file, oldFilePath);
        }
      }
    }
  }

  /**
   * Searches for all pm files in directory to be renamed/moved, searches for references to those packages and add them to the renaming queue
   *
   * @param project   Project to be renamed
   * @param directory VirtualFile of directory to be renamed
   * @param newPath   new directory path
   */
  public static void adjustNestedFiles(Project project, VirtualFile directory, String newPath) {
    VirtualFile oldDirectorySourceRoot = PerlUtil.getFileClassRoot(project, directory);
    PsiManager psiManager = PsiManager.getInstance(project);

    if (oldDirectorySourceRoot != null) {
      for (VirtualFile file : VfsUtil.collectChildrenRecursively(directory)) {
        if (!file.isDirectory() &&
            file.getFileType() == PerlFileTypePackage.INSTANCE &&
            oldDirectorySourceRoot.equals(PerlUtil.getFileClassRoot(project, file))) {
          PsiFile psiFile = psiManager.findFile(file);

          if (psiFile != null) {
            for (PsiReference inboundReference : ReferencesSearch.search(psiFile)) {
              String newPackagePath = newPath + "/" + VfsUtil.getRelativePath(file, directory);
              VirtualFile newInnermostRoot = PerlUtil.getFileClassRoot(project, newPackagePath);
              if (newInnermostRoot != null) {
                String newRelativePath = newPackagePath.substring(newInnermostRoot.getPath().length());
                String newPackageName = PerlPackageUtil.getPackageNameByPath(newRelativePath);

                PerlPsiUtil.renameFileReferencee(inboundReference.getElement(), newPackageName);
              }
            }
          }
        }
      }
    }
  }

  public static void processPackageFilesForPsiElement(@NotNull PsiElement element, @NotNull Processor<String> processor) {
    processIncFilesForPsiElement(
      element,
      (file, classRoot) -> {
        String relativePath = VfsUtil.getRelativePath(file, classRoot);
        String packageName = PerlPackageUtil.getPackageNameByPath(relativePath);
        return processor.process(packageName);
      },
      PerlFileTypePackage.INSTANCE)
    ;
  }

  public static boolean processIncFilesForPsiElement(@NotNull PsiElement element,
                                                     @NotNull ClassRootVirtualFileProcessor processor,
                                                     @NotNull FileType fileType) {
    for (VirtualFile classRoot : getIncDirsForPsiElement(element)) {
      if (!FileTypeIndex.processFiles(fileType,
                                      virtualFile -> processor.process(virtualFile, classRoot),
                                      GlobalSearchScopesCore.directoryScope(element.getProject(), classRoot, true))
        ) {
        return false;
      }
    }
    return true;
  }

  public static void processNotOverridedMethods(final PerlNamespaceDefinitionElement namespaceDefinition,
                                                Processor<PerlSubElement> processor) {
    if (namespaceDefinition != null) {
      PsiFile containingFile = namespaceDefinition.getContainingFile();
      String packageName = namespaceDefinition.getPackageName();
      if (packageName == null) {
        return;
      }

      Set<String> namesSet = new THashSet<>();
      // collecting overrided
      for (PerlSubDefinitionElement subDefinitionBase : PsiTreeUtil.findChildrenOfType(containingFile, PerlSubDefinitionElement.class)) {
        if (subDefinitionBase.isValid() && StringUtil.equals(packageName, subDefinitionBase.getPackageName())) {
          namesSet.add(subDefinitionBase.getSubName());
        }
      }

      processParentClassesSubs(
        namespaceDefinition,
        namesSet,
        new THashSet<>(),
        processor
      );
    }
  }

  public static void processParentClassesSubs(PerlNamespaceDefinitionElement childClass,
                                              Set<String> processedSubsNames,
                                              Set<PerlNamespaceDefinitionElement> recursionMap,
                                              Processor<PerlSubElement> processor
  ) {
    if (childClass == null || recursionMap.contains(childClass)) {
      return;
    }
    recursionMap.add(childClass);

    for (PerlNamespaceDefinitionElement parentNamespace : childClass.getParentNamespaceDefinitions()) {
      for (PsiElement subDefinitionBase : collectNamespaceSubs(parentNamespace)) {
        String subName = ((PerlSubElement)subDefinitionBase).getSubName();
        if (subDefinitionBase.isValid() &&
            ((PerlSubElement)subDefinitionBase).isMethod() &&
            !processedSubsNames.contains(subName)
          ) {
          processedSubsNames.add(subName);
          processor.process(((PerlSubElement)subDefinitionBase));
        }
      }
      processParentClassesSubs(
        parentNamespace,
        processedSubsNames,
        recursionMap,
        processor
      );
    }
  }

  public static List<PsiElement> collectNamespaceSubs(@NotNull final PsiElement namespace) {
    return CachedValuesManager.getCachedValue(
      namespace,
      () -> CachedValueProvider.Result
        .create(PerlPsiUtil.collectNamespaceMembers(namespace, PerlSubStub.class, PerlSubElement.class), namespace));
  }

  public static void processChildNamespacesSubs(@NotNull PerlNamespaceDefinitionElement namespaceDefinition,
                                                @Nullable Set<PerlNamespaceDefinitionElement> recursionSet,
                                                Processor<PerlSubElement> processor) {
    if (recursionSet == null) {
      recursionSet = new THashSet<>();
    }

    recursionSet.add(namespaceDefinition);

    for (PerlNamespaceDefinitionElement childNamespace : namespaceDefinition.getChildNamespaceDefinitions()) {
      if (!recursionSet.contains(childNamespace)) {
        boolean processSubclasses = true;

        for (PsiElement subBase : collectNamespaceSubs(childNamespace)) {
          processSubclasses = processor.process((PerlSubElement)subBase);
        }

        if (processSubclasses) {
          processChildNamespacesSubs(childNamespace, recursionSet, processor);
        }
      }
    }
  }

  @Nullable
  public static PsiFile getPackagePsiFileByPackageName(Project project, String packageName) {
    VirtualFile packageVirtualFile = getPackageVirtualFileByPackageName(project, packageName);

    if (packageVirtualFile != null) {
      return PsiManager.getInstance(project).findFile(packageVirtualFile);
    }

    return null;
  }

  @Nullable
  public static VirtualFile getPackageVirtualFileByPackageName(Project project, String packageName) {
    if (StringUtil.isEmpty(packageName)) {
      return null;
    }

    String packagePath = getPackagePathByName(packageName);
    VirtualFile[] classRoots = ProjectRootManager.getInstance(project).orderEntries().getClassesRoots();

    for (VirtualFile classRoot : classRoots) {
      VirtualFile targetFile = classRoot.findFileByRelativePath(packagePath);
      if (targetFile != null) {
        return targetFile;
      }
    }
    return null;
  }

  /**
   * Resolving canonical package name to a psi file
   *
   * @param psiFile              base file
   * @param canonicalPackageName package name in canonical form
   * @return vartual file
   */
  @Nullable
  public static PsiFile resolvePackageNameToPsi(@NotNull PsiFile psiFile, String canonicalPackageName) {
    // resolves to a psi file
    return resolveRelativePathToPsi(psiFile, PerlPackageUtil.getPackagePathByName(canonicalPackageName));
  }

  /**
   * Resolving canonical package to a virtual file
   *
   * @param psiFile              base file
   * @param canonicalPackageName package name in canonical form
   * @return vartual file
   */
  @Nullable
  public static VirtualFile resolvePackageNameToVirtualFile(@NotNull PsiFile psiFile, String canonicalPackageName) {
    // resolves to a psi file
    return resolveRelativePathToVirtualFile(psiFile, PerlPackageUtil.getPackagePathByName(canonicalPackageName));
  }

  /**
   * Resolving relative path to a psi file
   *
   * @param psiFile      base file
   * @param relativePath relative path
   * @return vartual file
   */
  @Nullable
  public static PsiFile resolveRelativePathToPsi(@NotNull PsiFile psiFile, String relativePath) {
    VirtualFile targetFile = resolveRelativePathToVirtualFile(psiFile, relativePath);

    if (targetFile != null && targetFile.exists()) {
      PsiFile targetPsiFile = PsiManager.getInstance(psiFile.getProject()).findFile(targetFile);
      if (targetPsiFile != null) {
        return targetPsiFile;
      }
    }

    return null;
  }

  /**
   * Resolving relative path to a virtual file
   *
   * @param psiFile      base file
   * @param relativePath relative path
   * @return vartual file
   */
  @Nullable
  public static VirtualFile resolveRelativePathToVirtualFile(@NotNull PsiFile psiFile, String relativePath) {
    if (relativePath != null) {
      for (VirtualFile classRoot : getIncDirsForPsiElement(psiFile)) {
        if (classRoot != null) {
          VirtualFile targetFile = classRoot.findFileByRelativePath(relativePath);
          if (targetFile != null) {
            String foundRelativePath = VfsUtil.getRelativePath(targetFile, classRoot);

            if (StringUtil.isNotEmpty(foundRelativePath) && StringUtil.equals(foundRelativePath, relativePath)) {
              return targetFile;
            }
          }
        }
      }
    }

    return null;
  }

  /**
   * Returns List of lib directories including class roots, current directory and use lib ones
   *
   * @param psiElement to resolve for
   * @return list of lib dirs
   */
  @NotNull
  public static List<VirtualFile> getIncDirsForPsiElement(@NotNull PsiElement psiElement) {
    PsiFile psiFile = psiElement.getContainingFile().getOriginalFile();
    List<VirtualFile> result = new ArrayList<>();

    // libdirs providers fixme we need to use stubs or psi here
    for (PerlUseStatement useStatement : PsiTreeUtil.findChildrenOfType(psiFile, PerlUseStatement.class)) {
      PerlPackageProcessor packageProcessor = useStatement.getPackageProcessor();
      if (packageProcessor instanceof PerlLibProvider) {
        ((PerlLibProvider)packageProcessor).addLibDirs(useStatement, result);
      }
    }

    // classpath
    result.addAll(Arrays.asList(ProjectRootManager.getInstance(psiFile.getProject()).orderEntries().getClassesRoots()));

    // current dir
    if (PerlSharedSettings.getInstance(psiFile.getProject()).getTargetPerlVersion().lesserThan(PerlVersion.V5_26)) {
      VirtualFile virtualFile = psiFile.getVirtualFile();
      if (virtualFile != null) {
        result.add(virtualFile.getParent());
      }
    }

    return result;
  }

  /**
   * Checks if sequence looks like a fqn
   *
   * @param text sequence to check
   * @return true if it's foo::bar
   */
  public static boolean isFullQualifiedName(String text) {
    return text.length() > 1 && StringUtil.containsAnyChar(text, ":'");
  }

  /**
   * Returns qualified ranges for identifier, like variable name or sub_name_qualified
   *
   * @param text token text
   * @return pair of two ranges; first will be null if it's not qualified name
   */
  @NotNull
  public static Pair<TextRange, TextRange> getQualifiedRanges(@NotNull CharSequence text) {

    int lastSeparatorOffset = StringUtil.lastIndexOfAny(text, ":'");

    if (lastSeparatorOffset < 0) {
      return Pair.create(null, TextRange.create(0, text.length()));
    }

    TextRange packageRange = PerlNamespaceElementManipulator.getRangeInString(text.subSequence(0, lastSeparatorOffset));

    TextRange nameRange;

    if (++lastSeparatorOffset < text.length()) {
      nameRange = TextRange.create(lastSeparatorOffset, text.length());
    }
    else {
      nameRange = TextRange.EMPTY_RANGE;
    }
    return Pair.create(packageRange, nameRange);
  }

  public interface ClassRootVirtualFileProcessor {
    boolean process(VirtualFile file, VirtualFile classRoot);
  }
}
