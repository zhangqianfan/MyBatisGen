package org.connection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;
import java.util.Properties;

public class DBConnection {

    private final String driver;
    private final String url;
    private final String username;
    private final String password;

    public DBConnection(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        Properties properties = new Properties();
        FileReader reader = new FileReader(file);
        properties.load(reader);
        this.driver = (String) properties.get("driver");
        this.url = (String) properties.get("url");
        this.username = (String) properties.get("username");
        this.password = (String) properties.get("password");
    }

    public DBConnection(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        Properties properties = new Properties();
        FileReader reader = new FileReader(file);
        properties.load(reader);
        this.driver = (String) properties.get("driver");
        this.url = (String) properties.get("url");
        this.username = (String) properties.get("username");
        this.password = (String) properties.get("password");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DBConnection that = (DBConnection) obj;
        return Objects.equals(driver, that.driver) &&
                Objects.equals(url, that.url) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driver, url, username, password);
    }

    @Override
    public String toString() {
        return "DBConnection{" +
                "driver='" + driver + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public final Connection getConnection() {
        if (driver != null && url != null && username != null && password != null) {
            try {
                Class.forName(driver);
                return DriverManager.getConnection(url, username, password);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                return null;
            }
        }
        return null;
    }

}
