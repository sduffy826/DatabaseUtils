import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class TestConnectionOldWay {

  public static void main(String[] args) {
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
      String url = "jdbc:mysql://192.168.1.11:3306/playground";                     
      Class.forName("com.mysql.cj.jdbc.Driver"); // Load driver
      conn = DriverManager.getConnection(url,args[0], args[1]);
      System.out.println("Connection established");
      
      stmt = conn.createStatement();
      String sqlSelect = "select * from corti.people";
      rs = stmt.executeQuery(sqlSelect);
      ResultSetMetaData rsMd = rs.getMetaData();
      int numberOfColumns = rsMd.getColumnCount();
      for (int i = 1; i <= numberOfColumns; i++) {
        System.out.print(rsMd.getColumnName(i) + ", ");
      }
      System.out.println(" "); // newline
      
      while (rs.next()) {
        for (int i = 1; i <= numberOfColumns; i++) {
          System.out.print(rs.getString(i) + ", ");          
        }
        System.out.println(" "); // newline
      }
      
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      if (rs != null) {
        try {
          rs.close();
          System.out.println("Result set closed");
        }
        catch (Exception e) { }
      }
      if (stmt != null) {
        try {
          stmt.close();
          System.out.println("Statement closed");
        }
        catch (Exception e) { }
      }
      if (conn != null) {
        try {
          conn.close();
          System.out.println("Connection closed");
        }
        catch (Exception e) { }
      }      
    }

  }

}
