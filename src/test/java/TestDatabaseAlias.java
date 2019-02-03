import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Little mainline program to test database connection (with pooling)
 *
 */
public class TestDatabaseAlias {
  
  static final boolean DEBUGIT = true;
  
  // Mainline, just for testing a connection
  public static void main(String[] args) {
    // Vars for testing
    String databaseAlias = "playground";
    
    java.sql.Connection dbConn = GetDatabaseConnection.getConnection(databaseAlias);    
    System.out.println("Connection returned: " + dbConn);
   
    // If you want to get a connection using a userid and password you'd do like the code below :)
    // java.sql.Connection dbConn2 = GetDatabaseConnection.getConnection(databaseAlias, "testuser", "testpassword");    
    
    // Just for demo show all the property files in the current path.... could instantiate for each one if wanted :)
    System.out.println("\n\nAll property files in current path");
    ArrayList<String> allProps = getAllPropertyFiles();
    for (String propertyFile: allProps) {
      System.out.println("Property file: " + propertyFile);
    }    
  }

  // Return an array with all the property files in the current path
  public static ArrayList<String> getAllPropertyFiles() {
    String thePath = PropertyHelper.getPath();
   
    if (DEBUGIT) System.out.println("TestDatabaseAlias.getAllPropertyFiles(), thePath: " + thePath);
    ArrayList<String> theList = new ArrayList<>();
    try {
       DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(thePath), "*.properties");       
       
       for (Path entry : stream) {
         if (Files.isDirectory(entry) == false) {
           theList.add(entry.toString());           
         }
       }
    } catch(Exception e) { e.printStackTrace(); }
    return theList;
  }
  
}
