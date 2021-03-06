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

package formatter;

import base.PerlLightCodeInsightFixtureTestCase;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.perl5.lang.perl.PerlLanguage;

public abstract class PerlFormatterTestCase extends PerlLightCodeInsightFixtureTestCase {
  CommonCodeStyleSettings myPerlSettings;

  boolean mySpaceBeforeIfParenthes;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myPerlSettings = CodeStyleSettingsManager.getSettings(getProject()).getCommonSettings(PerlLanguage.INSTANCE);
    mySpaceBeforeIfParenthes = myPerlSettings.SPACE_BEFORE_IF_PARENTHESES;
  }

  @Override
  protected void tearDown() throws Exception {
    myPerlSettings.SPACE_BEFORE_IF_PARENTHESES = mySpaceBeforeIfParenthes;
    super.tearDown();
  }
}
