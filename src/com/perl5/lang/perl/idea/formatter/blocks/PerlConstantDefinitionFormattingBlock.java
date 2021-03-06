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

package com.perl5.lang.perl.idea.formatter.blocks;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.perl5.lang.perl.idea.formatter.PerlFormattingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hurricup on 07.11.2015.
 */
public class PerlConstantDefinitionFormattingBlock extends PerlFormattingBlock {
  protected final Alignment arrowAlignment;

  public PerlConstantDefinitionFormattingBlock(@NotNull ASTNode node,
                                               @Nullable Wrap wrap,
                                               @Nullable Alignment alignment,
                                               @NotNull PerlFormattingContext context
  ) {
    super(node, wrap, null, context);
    arrowAlignment = alignment;
  }

  @Override
  protected List<Block> buildSubBlocks() {
    final List<Block> blocks = new ArrayList<Block>();

    for (ASTNode child = myNode.getFirstChildNode(); child != null; child = child.getTreeNext()) {
      if (!shouldCreateBlockFor(child)) {
        continue;
      }
      blocks.add(createChildBlock(
        child,
        null,
        child.getElementType() == FAT_COMMA ? arrowAlignment : null
      ));
    }

    return blocks;
  }
}
