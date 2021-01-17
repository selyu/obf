package org.selyu.obf.core.transformer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public interface IResourceTransformer extends ITransformer {
    /**
     * @param resourceMap A copy of the resourceMap
     * @return Null if the transformer makes no modification to the map else the modified map
     */
    @Nullable
    HashMap<String, byte[]> transformResources(@NotNull Map<String, byte[]> resourceMap);
}
