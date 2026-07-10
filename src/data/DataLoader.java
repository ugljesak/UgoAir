package data;

import exception.AppException;
import exception.DataAccessException;
import exception.DataFormatException;

import java.io.*;

public abstract class DataLoader {

    public final Dataset load(File file) throws AppException {
        if (!file.exists()) {
            throw new DataAccessException("File '" + file.getName() + "' doesn't exist.");
        }
        String content;
        try {
            content = readFile(file);
        } catch (IOException e) {
            throw new DataAccessException("Error reading file: " + e.getMessage());
        }
        return parse(content, file.getName());
    }

    public final void save(File file, Dataset data) throws AppException {
        String content = serialize(data);
        try {
            writeFile(file, content);
        } catch (IOException e) {
            throw new DataAccessException("Error writing file: " + e.getMessage());
        }
    }

    private String readFile(File file) throws IOException {
        Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
        try {
            StringBuilder sb = new StringBuilder();
            char[] chunk = new char[4096];
            int charsRead;
            while ((charsRead = reader.read(chunk)) != -1) {
                sb.append(chunk, 0, charsRead);
            }
            return sb.toString();
        } finally {
            reader.close();
        }
    }
    private void writeFile(File file, String content) throws IOException {
        OutputStream out = new FileOutputStream(file);
        try {
            out.write(content.getBytes("UTF-8"));
        } finally {
            out.close();
        }
    }

    protected abstract Dataset parse(String content, String filename) throws DataFormatException;
    protected abstract String serialize(Dataset data);

    public abstract String getFormatName();
}