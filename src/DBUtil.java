import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/game_zone?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=BST";
    private static final String username = "your_db_user";
    private static final String password = "your_db_password";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.DBUtil");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, username,  password);
    }
}