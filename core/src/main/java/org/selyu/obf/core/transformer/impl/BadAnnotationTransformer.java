package org.selyu.obf.core.transformer.impl;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.selyu.obf.core.transformer.ISimpleTransformer;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

public final class BadAnnotationTransformer implements ISimpleTransformer {
    // 65535 is the max this can be according to org.objectweb.asm.ByteVector#putUTF8
    private static final AnnotationNode ANNOTATION = new AnnotationNode("\n".repeat(65535));

    @Override
    public void transform(@NotNull ClassNode classNode) {
        if (Modifier.isInterface(classNode.access))
            return;
        for (MethodNode method : classNode.methods) {
            if (method.invisibleAnnotations == null)
                method.invisibleAnnotations = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                method.invisibleAnnotations.add(ANNOTATION);
            }
        }
    }

    @Override
    public @NotNull String getName() {
        return "BadAnnotation";
    }
}
