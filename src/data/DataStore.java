package data;

import exception.AppException;
import exception.DataAccessException;
import exception.DataFormatException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public abstract class DataStore {

    public final Dataset load(File file) throws AppException {
        if (!file.exists()) {
            throw new DataAccessException("File '" + file.getName() + "' doesn't exist.");
        }
        String content;
        try {
            content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataAccessException("Error reading file: " + e.getMessage());
        }
        return parse(content);
    }

    public final void save(File file, Dataset data) throws AppException {
        String content = serialize(data);
        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataAccessException("Error writing file: " + e.getMessage());
        }
    }

    protected abstract Dataset parse(String content) throws DataFormatException;
    protected abstract String serialize(Dataset data);
}