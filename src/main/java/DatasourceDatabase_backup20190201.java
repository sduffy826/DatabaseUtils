import java.sql.Connection;
import java.sql.SQLException;

import org.apache.tomcat.jdbc.pool.DataSource;

import java.util.Properties;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * This isn't used... was developed when had issues with the poolproperties
 * version... issue was dumb... was related to properties file.  I left this
 * for reference.
 * 
 * This is similar to PooledDatabase, but this one doesn't use PoolProperties
 *   for the datasource... read more about pros/cons between the two  
 */
public class DatasourceDatabase_backup20190201 {
  // Array has the fields that must be defined in the properties file
  private static String[] requiredProperties = {"version", "name", "type",
                                                "driverClassName", "url", "username",
                                                "password" };

  private static final boolean DEBUGIT = true;
  
  // Object attributes  
  private String databaseAlias;
  private Properties databaseProperties;
  private DataSource dataSource;
  private boolean isValid;
     
  // Constructor
  public DatasourceDatabase_backup20190201(String databaseAlias) {
    this.databaseAlias = databaseAlias;
 
    // The property file name should be the databaseAlias + ".properties"
    String propertyName = databaseAlias + ".properties";
    databaseProperties = PropertyHelper.getPropertyObject(propertyName);    
    dataSource = null;
    
    // Validate the properties and if good then create a datasource object
    isValid = validateIt();
    
    if (DEBUGIT) {
      System.out.println("In DatabaseAttributes constructor, databaseAlias: " + databaseAlias +
          " isValid: " + isValid);
      System.out.println("Classpath:");
      showClassPath(); 
    }
         
    if (isValid) dataSource = getDataSource();
  }
  
  // This method returns a DataSource for the object
  private org.apache.tomcat.jdbc.pool.DataSource getDataSource() {
    dataSource = new DataSource();
    dataSource.setDriverClassName(databaseProperties.getProperty("driverClassName").trim());
    dataSource.setUrl(databaseProperties.getProperty("url").trim());
    dataSource.setUsername(databaseProperties.getProperty("username").trim());
    dataSource.setPassword(databaseProperties.getProperty("password").trim());
    
    dataSource.setInitialSize(5);
    dataSource.setMaxActive(10);
    dataSource.setMaxIdle(5);
    dataSource.setMinIdle(2);
    
    if (DEBUGIT) System.out.println("DatabaseAttributes.getDataSource, dataSource: " + dataSource.toString());
    return dataSource;
  }
  
  // Return a connection
  public Connection getConnection() {
    Connection theConn = null;
    if (isValid) {
      try {
        if (DEBUGIT) System.out.println("DatabaseAttributes.getConnection(), before getConnection, dataSource: " + dataSource.toString());
        theConn = dataSource.getConnection();
      } catch (SQLException e) {
        if (DEBUGIT) System.out.println("DatabaseAttributes.getConnection(), exception raised: " + e.toString());
      }
    }
    return theConn;
  }
  
  // Validate that the properties for this database alias are good
  private boolean validateIt() {
    boolean isGood = true;
    for (String aProperty : requiredProperties) {
      if (databaseProperties.getProperty(aProperty) == null) {
        if (isGood) System.out.println("** INVALID properties for: " + databaseAlias);
        System.out.println("Missing property: " + aProperty);
        isGood = false;
      }
    }    
    return isGood;
  }  
  
  // Show classpath
  public void showClassPath() {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    URL[] urls = ((URLClassLoader)cl).getURLs();
    for (URL url: urls) {
      System.out.println(url.getFile());
    }    
  }
  
  
}
