package org.selyu.obf.core.transformer;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;

public interface ISimpleTransformer extends ITransformer {
    /**
     * Transforms a classNode
     *
     * @param classNode The classNode to transform
     */
    void transform(@NotNull ClassNode classNode);
}
