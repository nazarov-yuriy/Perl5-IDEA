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

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
import com.perl5.lang.perl.extensions.packageprocessor.PerlExportDescriptor;
import com.perl5.lang.perl.lexer.PerlElementTypes;
import com.perl5.lang.perl.psi.PerlVariableDeclarationElement;
import com.perl5.lang.perl.psi.stubs.variables.PerlVariablesStubIndex;
import com.perl5.lang.perl.util.processors.PerlHashImportsCollector;
import com.perl5.lang.perl.util.processors.PerlImportsCollector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by hurricup on 19.04.2015.
 */
public class PerlHashUtil implements PerlElementTypes {
  public static final HashSet<String> BUILT_IN = new HashSet<String>(Arrays.asList(
    "!",
    "+",
    "-",
    "^H",
    "ENV",
    "INC",
    "OVERLOAD",
    "SIG",
    "LAST_PAREN_MATCH",
    "LAST_MATCH_START",
    "^CAPTURE",
    "^CAPTURE_ALL",
    "OS_ERROR",
    "ERRNO"
  ));

  public static boolean isBuiltIn(String variable) {
    return BUILT_IN.contains(variable);
  }

  /**
   * Searching project files for global hash definitions by specific package and variable name
   *
   * @param project       project to search in
   * @param canonicalName canonical variable name package::name
   * @return Collection of found definitions
   */
  public static Collection<PerlVariableDeclarationElement> getGlobalHashDefinitions(Project project, String canonicalName) {
    return getGlobalHashDefinitions(project, canonicalName, GlobalSearchScope.allScope(project));
  }

  public static Collection<PerlVariableDeclarationElement> getGlobalHashDefinitions(Project project,
                                                                                    String canonicalName,
                                                                                    GlobalSearchScope scope) {
    if (canonicalName == null) {
      return Collections.emptyList();
    }
    return StubIndex.getElements(
      PerlVariablesStubIndex.KEY_HASH,
      canonicalName,
      project,
      scope,
      PerlVariableDeclarationElement.class
    );
  }


  /**
   * Returns list of defined global hashes
   *
   * @param project project to search in
   * @return collection of variable canonical names
   */
  public static Collection<String> getDefinedGlobalHashNames(Project project) {
    return PerlUtil.getIndexKeysWithoutInternals(PerlVariablesStubIndex.KEY_HASH, project);
  }

  /**
   * Processes all global hashes names with specific processor
   *
   * @param project   project to search in
   * @param processor string processor for suitable strings
   * @return collection of constants names
   */
  public static boolean processDefinedGlobalHashes(@NotNull Project project,
                                                   @NotNull GlobalSearchScope scope,
                                                   @NotNull Processor<PerlVariableDeclarationElement> processor) {
    return PerlScalarUtil.processDefinedGlobalVariables(PerlVariablesStubIndex.KEY_HASH, project, scope, processor);
  }

  /**
   * Returns a map of imported hashes names
   *
   * @param rootElement element to start looking from
   * @return result map
   */
  @NotNull
  public static List<PerlExportDescriptor> getImportedHashesDescriptors(@NotNull PsiElement rootElement) {
    PerlImportsCollector collector = new PerlHashImportsCollector();
    PerlUtil.processImportedEntities(rootElement, collector);
    return collector.getResult();
  }
}
