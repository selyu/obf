package org.selyu.obf.core.transformer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Transformer type where the transformer can replace the current map of classNodes
 */
public interface IMapTransformer extends ITransformer {
    /**
     * @param classNodeMap The Name->ClassNode map
     * @return Null if the transformer doesn't change the map else the modified map
     */
    @Nullable
    HashMap<String, ClassNode> transform(@NotNull Map<String, ClassNode> classNodeMap);
}
