package org.selyu.obf.core.transformer;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ITransformer {
    @NotNull
    String getName();

    // This can be called over and over again fine because it just fetches from a map if it already exists
    @NotNull
    default Logger logger() {
        return LoggerFactory.getLogger(getName() + "Transformer");
    }
}
