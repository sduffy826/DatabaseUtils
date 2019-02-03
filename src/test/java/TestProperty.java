import java.util.Properties;

public class TestProperty {

  public static void main(String[] args) {
    // Vars for testing
    String propertyFile = "databaseUtils.properties";
    String property = "name";
    
    Properties databaseProperties = PropertyHelper.getPropertyObject(propertyFile);
    PropertyHelper.dumpAllProperties(databaseProperties);
    
    System.out.println("Value for " + propertyFile + "/" + property + " is: " + 
      PropertyHelper.getProperty(propertyFile,property));
  }

}
