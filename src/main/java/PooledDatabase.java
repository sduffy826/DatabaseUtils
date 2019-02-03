import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import java.net.URL;
import java.net.URLClassLoader;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * This represents a pooled database, it should be instantiated with the
 *   name of the database alias (note: the properties file 'alias2PropertyFileName'
 *   can map the alias to the property filename to use; if there isn't a mapping
 *   then the alias is used as the property filename).
 * The main purpose is to return a connection to the database so it exposes
 *   the getConnection method to do that, there are two flavors of it,
 *   one to return a connection using the userid/pw from the properties
 *   file and another where those args are passed in from the caller.
 * 
 * Some info for developer... basic logic here is:
 *   object is instantiated with a string which represents database alias
 *     we look for a properties file called 'alias.properties' and read it
 *   we validate that the property file has the required attributes... this tells
 *     us info about connection 
 *   we then create a Datasource object by using the values in the properties file
 *     
 * There's quite a bit of info output to the console if the DEBUGIT constants
 *   is on... useful if have issues.
 *   
 * @author sduffy
 *
 */
public class PooledDatabase {
  private static final boolean DEBUGIT = true;
  
  // Properties file to remap alias to databaseProperties file
  private static String alias2PropertyFileName = "alias2PropertyFileName.properties";
  private static Properties aliasProperties = null;
  
  // Array has the fields that must be defined in the properties file
  private static String[] requiredProperties = {"version", "name", "type",
                                                "driverClassName", "url", "username",
                                                "password" };

  // Object attributes  
  private String databaseAlias;
  private Properties databaseProperties;
  private org.apache.tomcat.jdbc.pool.DataSource dataSource;
  private boolean isValid;
     
  // Constructor, pass in the database alias
  public PooledDatabase(String databaseAlias) {
    this.databaseAlias = databaseAlias;
 
    // Get the name of the property file for this database alias
    String propertyFileName = getPropertyForAlias(databaseAlias);
    
    databaseProperties = PropertyHelper.getPropertyObject(propertyFileName);    
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
    PoolProperties p = new PoolProperties();
    
    // Set pool based on property file values, read more about the PoolProperty
    //   class and it's attributes 
    p.setUrl(databaseProperties.getProperty("url").trim());
    p.setDriverClassName(databaseProperties.getProperty("driverClassName").trim()); 
    p.setUsername(databaseProperties.getProperty("username").trim());
    p.setPassword(databaseProperties.getProperty("password").trim());
    
    // If property file has 'readOnly' value then use it
    String readOnly = databaseProperties.getProperty("readOnly","notDefined").trim();
    if (readOnly.equalsIgnoreCase("true") | readOnly.equalsIgnoreCase("false")) {
      p.setDefaultReadOnly(Boolean.parseBoolean(readOnly));
    }
    
    // Same for autoCommit, if defined use it
    String autoCommit = databaseProperties.getProperty("autoCommit","notDefined").trim();
    if (autoCommit.equalsIgnoreCase("true") | autoCommit.equalsIgnoreCase("false")) {
      p.setDefaultAutoCommit(Boolean.parseBoolean(autoCommit));
    }
    
    p.setAlternateUsernameAllowed(true);  // Allow method getConnection(userid,pw) to be used 
    
    if (DEBUGIT) {
      System.out.println("DatabaseAttributes.getDataSource(), PoolProperties: " + p.toString());
    
      // Password is not visible in 'p.' so show here
      System.out.println("Readable pw: " + databaseProperties.getProperty("password"));
    }  
    
    p.setJmxEnabled(true);      // if true, pool creates ConnectionPoolMBean that can be registered with JMX    
    p.setTestWhileIdle(false);  // sets whether query validation takes place on idle connections
    p.setTestOnBorrow(true);    // if true, objects are validated before being borrowed from pool
    p.setValidationQuery("SELECT 1");  // the sql statement to use for validation
    p.setTestOnReturn(false);          // if true then it validates objects being returned to pool
    p.setValidationInterval(30000);    // avoid excess validation, validate at most this interval - milliseconds
    p.setTimeBetweenEvictionRunsMillis(30000);  // milliseconds to sleep between idle connection validations
    p.setMaxActive(100);        // maximum active connections at one time 
    p.setInitialSize(10);       // number of connectsions established when pool is started
    p.setMaxWait(10000);        // max milliseconds pool will wait trying to get a connection, throws exception if timeout
    p.setRemoveAbandonedTimeout(60);  // time in seconds before a connection is considered abandoned
    p.setMinEvictableIdleTimeMillis(30000);  // min milliseconds an object must be idle before considered for eviction
    p.setMinIdle(5);            // min number of connections to keep in the pool at all times
    p.setLogAbandoned(true);    // if true stack traces should be logged when connection is abandoned
    p.setRemoveAbandoned(true); // if true removes abandoned connections after 'removeAbandonedTimeout' reached
    p.setJdbcInterceptors(      // classnames that extend the JdbcInterceptor class
      "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
      "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
    
    dataSource = new DataSource();
    dataSource.setPoolProperties(p);
    if (DEBUGIT) System.out.println("DatabaseAttributes.getDataSource, dataSource: " + dataSource.toString());
    return dataSource;
  }
  
  // Return a connection
  public Connection getConnection() {
    if (DEBUGIT) {
      if (dataSource != null) {
        System.out.println("DatabaseAttributes.getConnection(), before getConnection, dataSource: " + dataSource.toString());
      }
      else {
        System.out.println("dataSource is null");
      }
    }
    
    Connection theConn = null;
    if (isValid) {
      try {
        theConn = dataSource.getConnection();
      } catch (SQLException e) {
        if (DEBUGIT) System.out.println("DatabaseAttributes.getConnection(), exception raised: " + e.toString());
      }
    }
    return theConn;
  }
  
  //Return a connection, this one passes in the connect userid and pw
  public Connection getConnection(String uid, String pw) {
    if (DEBUGIT) {
      System.out.print("DatabaseAttributes.getConnection, uid: " + uid + " pw: " + pw + " ");
      if (dataSource != null) {
        System.out.println("dataSource: " + dataSource.toString());
      }
      else {
        System.out.println("dataSource is null");
      }
    }
   
    Connection theConn = null;
    if (isValid) {
      try {
        theConn = dataSource.getConnection(uid, pw);
      } catch (SQLException e) {
        if (DEBUGIT) System.out.println("DatabaseAttributes.getConnection(), exception raised: " + e.toString());
      }
    }
    return theConn;
  }
   
  // Validate that the properties for this database are good, basically ensure it has all the properties
  // in the 'requiredProperties' array
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
  
  // Show what the classpath is, mainly for debugging
  public void showClassPath() {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    URL[] urls = ((URLClassLoader)cl).getURLs();
    for (URL url: urls) {
      System.out.println(url.getFile());
    }    
  }

  // Get the property for the alias passed in, if it's not defined in the
  //   properties file then we'll return the alias as the name.. the
  //   properties file is really to override values
  private String getPropertyForAlias(String databaseAlias) {
    if (aliasProperties == null) {
      aliasProperties = PropertyHelper.getPropertyObject(alias2PropertyFileName);
      if (DEBUGIT) System.out.println("PooledDatabase.getPropertyForAlias, property object created");
    }
    
    // Get property value for alias, if not found then just use the alias + .properties
    String prop2Use = aliasProperties.getProperty(databaseAlias, databaseAlias+".properties");
    if (DEBUGIT) System.out.println("PooledDatabase.getPropertyForAlias, mapped: " + databaseAlias + " to:" + prop2Use);
    return prop2Use;
  }  
}
