package org.example.model;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TagDatabase {
    private final Path dbPath;
    private final Connection connection;

    public TagDatabase() {
        try {
            dbPath = getPersistentDbPath(); // Получаем путь с проверкой целостности
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toString());
            initializeDatabase(); // Создаём таблицу, если её нет

            Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        } catch (IOException | SQLException | URISyntaxException e) {
            throw new RuntimeException("Ошибка при подключении к базе данных", e);
        }
    }

    private Path getPersistentDbPath() throws IOException, URISyntaxException {
        Path userHome = Paths.get(System.getProperty("user.home"));
        Path appDir = userHome.resolve("DocCraft");

        // Выводим путь к папке DocCraft
        System.out.println("Путь к папке DocCraft: " + appDir);

        // Проверяем, существует ли папка DocCraft
        if (!Files.exists(appDir)) {
            System.out.println("Папка DocCraft не существует. Будет создана.");
        }

        // Создаём директорию, если её нет
        Files.createDirectories(appDir);

        Path dbFile = appDir.resolve("tags.db");

        // Выводим путь к файлу базы данных
        System.out.println("Путь к файлу базы данных: " + dbFile);

        boolean needInitialization = !Files.exists(dbFile);

        if (Files.exists(dbFile)) {
            // Проверяем целостность существующей базы
            if (!isDatabaseValid(dbFile)) {
                System.out.println("База данных повреждена. Будет удалена и создана заново.");
                Files.delete(dbFile); // Удаляем повреждённый файл
                needInitialization = true;
            }
        }

        if (needInitialization) {
            // Пытаемся скопировать из ресурсов
            try (InputStream inputStream = getClass().getResourceAsStream("/tags.db")) {
                if (inputStream != null) {
                    System.out.println("Копируем базу данных из ресурсов...");
                    Files.copy(inputStream, dbFile, StandardCopyOption.REPLACE_EXISTING);
                    // Проверяем после копирования
                    if (!isDatabaseValid(dbFile)) {
                        System.out.println("Скопированная база данных повреждена. Будет создана новая.");
                        Files.delete(dbFile);
                        createNewDatabase(dbFile);
                    }
                } else {
                    System.out.println("Ресурс tags.db не найден. Будет создана новая база данных.");
                    // Ресурс не найден, создаём новую базу
                    createNewDatabase(dbFile);
                }
            }
        }

        return dbFile;
    }

    private boolean isDatabaseValid(Path dbFile) {
        final String checkQuery = "SELECT 1 FROM sqlite_master LIMIT 1";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toString());
             Statement stmt = conn.createStatement()) {
            stmt.executeQuery(checkQuery); // Простой запрос для проверки
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private void createNewDatabase(Path dbFile) throws IOException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toString());
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE tags (" +
                    "tag TEXT PRIMARY KEY, " +
                    "placeholder TEXT NOT NULL, " +
                    "placeholder_long TEXT)");
        } catch (SQLException e) {
            throw new IOException("Не удалось создать новую базу данных", e);
        }
    }

    private void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {
            // Создаём таблицу, если она не существует
            stmt.execute("CREATE TABLE IF NOT EXISTS tags (" +
                    "tag TEXT PRIMARY KEY, " +
                    "placeholder TEXT NOT NULL, " +
                    "placeholder_long TEXT)");

            // Проверяем, существует ли столбец placeholder_long
            boolean columnExists = false;
            try (ResultSet rs = connection.getMetaData().getColumns(null, null, "tags", "placeholder_long")) {
                if (rs.next()) {
                    columnExists = true;
                }
            }

            // Если столбец отсутствует, добавляем его
            if (!columnExists) {
                stmt.execute("ALTER TABLE tags ADD COLUMN placeholder_long TEXT");
                System.out.println("Столбец placeholder_long добавлен в таблицу tags.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при создании или изменении таблицы", e);
        }
    }
    public void saveTagWithLongPlaceholder(String tag, String placeholder, String placeholderLong) {
        String sql = "INSERT OR REPLACE INTO tags(tag, placeholder, placeholder_long) VALUES(?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, tag);
            pstmt.setString(2, placeholder);
            pstmt.setString(3, placeholderLong);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении тега с длинной подсказкой", e);
        }
    }

    public String getPlaceholderLong(String tag) {
        String sql = "SELECT placeholder_long FROM tags WHERE tag = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, tag);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("placeholder_long");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении длинной подсказки", e);
        }
        return null; // Если значение не найдено
    }

    public void saveTag(String tag, String placeholder) {
        String sql = "INSERT OR REPLACE INTO tags(tag, placeholder) VALUES(?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, tag);
            pstmt.setString(2, placeholder);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении тега", e);
        }
    }

    public void saveTag(String tag) {
        saveTag(tag, tag);
    }

    public String getPlaceholder(String tag) {
        String sql = "SELECT placeholder FROM tags WHERE tag = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, tag);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("placeholder");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении подсказки", e);
        }
        saveTag(tag);
        return tag;
    }

    public List<String> getAllTags() {
        List<String> tags = new ArrayList<>();
        String sql = "SELECT tag FROM tags";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                tags.add(rs.getString("tag"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении списка тегов", e);
        }
        return tags;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Соединение с базой данных закрыто.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при закрытии соединения", e);
        }
    }

}
