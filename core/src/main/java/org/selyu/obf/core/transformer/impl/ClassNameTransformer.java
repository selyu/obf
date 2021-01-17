package org.selyu.obf.core.transformer.impl;

import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.selyu.obf.core.transformer.IMapTransformer;
import org.selyu.obf.core.transformer.IResourceTransformer;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class ClassNameTransformer implements IMapTransformer, IResourceTransformer {
    private static final char[] ALPHABET;

    private static final String[] RESOURCE_EXTENSIONS = {
            ".mf",
            ".yml"
    };

    static {
        char[] original = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        ALPHABET = new char[original.length * 2];
        for (int i = 0; i < original.length; i++) {
            ALPHABET[i] = original[i];
            ALPHABET[i + 1] = Character.toUpperCase(original[i]);
        }
    }

    private final Map<String, String> classNames = new HashMap<>(); // old name -> new name

    @Override
    public @NotNull HashMap<String, ClassNode> transformClasses(@NotNull Map<String, ClassNode> classNodeMap) {
        for (var classNode : classNodeMap.values()) {
            classNames.put(classNode.name, RandomStringUtils.random(10, ALPHABET));
        }

        logger().debug("Transforming classes {");
        for (var entry : classNames.entrySet()) {
            var oldName = entry.getKey();
            var newName = entry.getValue();
            if (oldName.equals(newName))
                continue;

            logger().debug("  > {}->{}", oldName, newName);
        }
        logger().debug("}");

        var remapper = new SimpleRemapper(classNames);
        var newMap = new HashMap<String, ClassNode>();
        for (ClassNode classNode : classNodeMap.values()) {
            var n = classNode.name;
            var newNode = new ClassNode();
            var classRemapper = new ClassRemapper(newNode, remapper);
            classNode.accept(classRemapper);

            newMap.put(n, newNode);
        }

        if (classNames.size() > 0) {
            logger().info("Remapped {} classe{}", classNames.size(), classNames.size() > 1 ? "s" : "");
        }

        return newMap;
    }

    @Override
    public @NotNull HashMap<String, byte[]> transformResources(@NotNull Map<String, byte[]> resourceMap) {
        var newMap = new HashMap<String, byte[]>();
        for (var entry : resourceMap.entrySet()) {
            var name = entry.getKey();
            var bytes = entry.getValue();

            var valid = false;
            for (String extension : RESOURCE_EXTENSIONS) {
                if (name.toLowerCase().endsWith(extension)) {
                    valid = true;
                    break;
                }
            }

            if (!valid) {
                newMap.put(name, bytes);
                continue;
            }

            var fileContents = new String(bytes, StandardCharsets.UTF_8);
            if (fileContents.isEmpty() || fileContents.isBlank())
                continue;

            for (var nameEntry : classNames.entrySet()) {
                var oldName = nameEntry.getKey();
                var newName = nameEntry.getValue();

                fileContents = fileContents.replaceAll(oldName, newName);
                fileContents = fileContents.replaceAll(oldName.replace('/', '.'), newName.replace('/', '.'));
            }

            newMap.put(name, fileContents.getBytes());
        }

        return newMap;
    }

    @Override
    public @NotNull String getName() {
        return "ClassName";
    }
}
