package org.selyu.obf.core;

import ch.qos.logback.classic.Level;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.selyu.obf.core.transformer.IMapTransformer;
import org.selyu.obf.core.transformer.ISimpleTransformer;
import org.selyu.obf.core.transformer.ITransformer;
import org.selyu.obf.core.transformer.impl.DebugTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public final class Obfuscator {
    private static final ITransformer[] TRANSFORMERS = {
            new DebugTransformer()
    };

    private final Logger logger = LoggerFactory.getLogger("main");
    private final Timer timer = new Timer();

    /**
     * @param inputFile       The jar file to be obfuscated
     * @param outputDirectory The directory the file will be output to
     * @throws IllegalArgumentException If outputDirectory is not a directory or the outputFile exists
     */
    public Obfuscator(@NotNull File inputFile, @NotNull File outputDirectory, boolean overwriteExisting) throws IOException {
        Objects.requireNonNull(inputFile, "The inputFile may not be null!");
        Objects.requireNonNull(outputDirectory, "The outputDirectory may not be null!");

        // https://stackoverflow.com/a/10849560
        ((ch.qos.logback.classic.Logger) logger).setLevel(Level.ALL);

        if (!inputFile.isFile())
            throw new IllegalArgumentException("inputFile is not a file!");
        if (!outputDirectory.isDirectory())
            throw new IllegalArgumentException("outputDirectory is not a directory!");

        var jarFile = new JarFile(inputFile);
        File outputFile = new File(outputDirectory, "obf-" + inputFile.getName());
        if (outputFile.exists() && !overwriteExisting)
            throw new IllegalArgumentException("File '" + outputFile.getAbsolutePath() + "' already exists!");
        logger.info("Obfuscating input file '{}'", inputFile.getName());

        timer.start("read input jar file");

        var classNodeMap = new HashMap<String, ClassNode>();
        var entries = jarFile.entries();
        while (entries.hasMoreElements()) { // loop through files
            var entry = entries.nextElement();
            if (!entry.getName().endsWith(".class"))
                continue;

            byte[] bytes;

            // auto disposal
            try (
                    var inputStream = jarFile.getInputStream(entry);
                    var outputStream = new ByteArrayOutputStream()
            ) {
                // https://www.baeldung.com/convert-input-stream-to-array-of-bytes
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    outputStream.write(data, 0, nRead);
                }

                bytes = outputStream.toByteArray();
            }

            var classNode = new ClassNode();
            var classReader = new ClassReader(bytes);
            classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

            classNodeMap.put(entry.getName(), classNode);
        }
        timer.end("read input jar file");

        timer.start("obfuscation");

        timer.start("run transformers");
        for (ITransformer transformer : TRANSFORMERS) {
            timer.start("run " + transformer.getName() + "Transformer");
            if (transformer instanceof ISimpleTransformer) {
                for (ClassNode classNode : classNodeMap.values()) {
                    ((ISimpleTransformer) transformer).transform(classNode);
                }
            } else if (transformer instanceof IMapTransformer) {
                var newMap = ((IMapTransformer) transformer).transform(new HashMap<>(classNodeMap));
                if (newMap != null) {
                    classNodeMap = newMap;
                }
            }
            timer.end("run " + transformer.getName() + "Transformer");
        }
        timer.end("run transformers");

        timer.start("file save");
        try (var outputStream = new JarOutputStream(Files.newOutputStream(outputFile.toPath()))) {
            logger.info("Copying transformed classes to new jar file");
            for (var classNode : classNodeMap.values()) {
                var entry = new JarEntry(classNode.name + ".class");
                outputStream.putNextEntry(entry);

                var classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(classWriter);

                outputStream.write(classWriter.toByteArray());
                outputStream.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        timer.end("file save");

        timer.end("obfuscation");
    }

    // This code is relative to my machine and will be removed when I introduce the CLI/GUI extensions
    public static void main(String[] args) throws IOException {
        new Obfuscator(new File("/home/lillian/Desktop/to-obf.jar"), new File("/home/lillian/Desktop"), true);
    }

    private class Timer {
        private final Map<String, Long> tasks = new HashMap<>();

        public void start(String task) {
            tasks.put(task, System.currentTimeMillis());
            logger.info("Starting task '{}'", task);
        }

        public void end(String task) {
            long start = tasks.getOrDefault(task, 0L);
            logger.info("Task '{}' took {}ms", task, System.currentTimeMillis() - start);
        }
    }
}
