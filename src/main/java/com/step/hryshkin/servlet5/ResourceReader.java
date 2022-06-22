package com.step.hryshkin.servlet5;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class ResourceReader {
    private ResourceReader() {
    }

    public static String webPageContentToString(String fileName) throws Exception {
        return Files.readString(Paths.get(Objects
                .requireNonNull(ResourceReader.class
                        .getResource(String.format("/%s", fileName))).toURI()));
    }
}
