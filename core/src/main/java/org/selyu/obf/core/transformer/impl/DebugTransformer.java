package org.selyu.obf.core.transformer.impl;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.selyu.obf.core.transformer.ISimpleTransformer;

public final class DebugTransformer implements ISimpleTransformer {
    private static final String LINE = "-".repeat(15);

    @Override
    public void transform(@NotNull ClassNode classNode) {
        if (classNode.methods != null && !classNode.methods.isEmpty()) {
            logger().debug(LINE);
            logger().debug("Methods for class '{}'", classNode.name);
            for (MethodNode method : classNode.methods) {
                logger().debug("  > '{}'", method.name);
            }
            logger().debug(LINE);
        }

        if (classNode.fields != null && !classNode.fields.isEmpty()) {
            logger().debug(LINE);
            logger().debug("Fields for class '{}'", classNode.name);
            for (FieldNode field : classNode.fields) {
                logger().debug("  > '{}'", field.name);
            }
            logger().debug(LINE);
        }
    }

    @Override
    public @NotNull String getName() {
        return "Debug";
    }
}
