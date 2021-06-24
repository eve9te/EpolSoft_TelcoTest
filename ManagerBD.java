import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class ManagerBD{

    public static Connection connectBD(String host, String user, String password, String database) {
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(host + database, user, password);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return conn;
    }
    
    public static void createTable(Connection conn, String nameTable) {
        String commandSQL = "CREATE TABLE IF NOT EXISTS " + nameTable +
        "(Id INT PRIMARY KEY AUTO_INCREMENT, Name VARCHAR(255), Size VARCHAR(30), CreatedAt VARCHAR(30))";
        runCommandSQL(conn, commandSQL);
    }
    
    public static void addElement(Connection conn, String nameTable, String nameFile, String sizeFile, String currentTime) {
        String commandSQL = "INSERT "+ nameTable +"(Name, Size, CreatedAt) VALUES (?,?,?)";
        try {
            PreparedStatement statement = conn.prepareStatement(commandSQL);
            statement.setString(1, nameFile);
            statement.setString(2, sizeFile);
            statement.setString(3, currentTime);
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void outputTable(Connection conn, String nameTable) {
        try {
            java.sql.Statement st = conn.createStatement();
            ResultSet resultSet = st.executeQuery("SELECT * FROM " + nameTable);
            while(resultSet.next()){
                    
                int id = resultSet.getInt(1);
                String Name = resultSet.getString(2);
                String SizeFile = resultSet.getString(3);
                String CreatedAt = resultSet.getString(4);
                System.out.printf("%d. %s (%s) - %s \n", id, Name, SizeFile, CreatedAt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }  
    }

    public static void runCommandSQL (Connection conn, String commandSQL) {
        try {
            java.sql.Statement st = conn.createStatement();
            st.executeUpdate(commandSQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}