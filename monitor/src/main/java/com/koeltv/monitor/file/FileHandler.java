package com.koeltv.monitor.file;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;

public class FileHandler {
    private static final String FILE_DIRECTORY = "./logs";

    private final Path filePath;

    public FileHandler(String fileName) throws IOException {
        Path directoryPath = Paths.get(FILE_DIRECTORY);
        Files.createDirectories(directoryPath);
        this.filePath = directoryPath.resolve(fileName);
        try {
            Files.createFile(filePath);
        } catch (FileAlreadyExistsException ignored) {
            // If the file already exists we do nothing
        }
    }

    public void appendLineToFile(String line) {
        try {
            Files.writeString(filePath, line + "\n", StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't append to file " + filePath, e);
        }
    }

    public List<String> readLinesFromFile() {
        try {
            return Files.readAllLines(filePath);
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
