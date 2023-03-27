package lk.ijse.dep10.db;

import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static DBConnection dbConnection;
    private final Connection connection;
    private DBConnection(){
        File configFile = new File("application.properties");
        Properties configurations = new Properties();
        try {
            FileReader fr = new FileReader(configFile);
            configurations.load(fr);
            fr.close();

            String host = configurations.getProperty("mysql.host", "localhost");
            String port = configurations.getProperty("mysql.port", "3306");
            String database = configurations.getProperty("mysql.database", "dep10_student_attendance_system");
            String username = configurations.getProperty("mysql.username", "root");
            String password = configurations.getProperty("mysql.password", "Nuwan");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?createDatabaseIfNotExist=true&allowMultiQueries=true";
            connection = DriverManager.getConnection(url,username,password);
        }
        catch (FileNotFoundException e){
            new Alert(Alert.AlertType.ERROR,"Configuration file doesn't exists, try again").showAndWait();
            throw new RuntimeException();
        }
        catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,"Failed to read configurations").showAndWait();
            throw new RuntimeException(e);
        }
        catch (SQLException e){
            new Alert(Alert.AlertType.ERROR,"Failed to establish the database connection, try again. If the problem persists please contact technical officer").showAndWait();
            throw new RuntimeException(e);
        }
    }
    public static DBConnection getInstance(){
        return (dbConnection == null)? dbConnection = new DBConnection(): dbConnection;
    }
    public Connection getConnection(){
        return connection;
    }
}
