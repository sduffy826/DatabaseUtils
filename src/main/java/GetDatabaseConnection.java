import java.util.HashMap;
import java.util.Map;
//import java.util.Properties;

/**
 * Class really just a factory to get database connections; most of the work for doing
 * that is in the DatabaseAttributes class
 * 
 * @author sduffy
 */
public class GetDatabaseConnection {
  
  private static Map<String, PooledDatabase> databaseMapping = new HashMap<>(); 
    
  private static PooledDatabase getConnectionHelper(String databaseAlias) {
    // Get the pooled database object for the alias passed in, if it doesn't
    //   exist we'll instantiat it.
    PooledDatabase pooledDB = databaseMapping.get(databaseAlias);
    if (pooledDB == null) {
      databaseMapping.put(databaseAlias, new PooledDatabase(databaseAlias));
      pooledDB = databaseMapping.get(databaseAlias);
    }
    return pooledDB;
  }
  
  // Return connection, it'll use the userid/pw in the properties file
  public static java.sql.Connection getConnection(String databaseAlias) {
    PooledDatabase pooledDB = getConnectionHelper(databaseAlias);    
    return pooledDB.getConnection();    
  }
  
  // Return connection, this will use the userid/pw passed in
  public static java.sql.Connection getConnection(String databaseAlias, String uid, String pw) {
    PooledDatabase pooledDB = getConnectionHelper(databaseAlias);    
    return pooledDB.getConnection(uid, pw);    
  }
}
