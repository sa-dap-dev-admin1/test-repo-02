import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationManager {
    private static final String CONFIG_FILE = "config.properties";
    private Properties properties;

    public ConfigurationManager() {
        properties = new Properties();
        loadConfiguration();
    }

    private void loadConfiguration() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            // In a production environment, we might want to throw a custom exception or use a logging framework
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("Invalid integer property: " + key);
                // In a production environment, we might want to log this error
            }
        }
        return defaultValue;
    }

    public double getDoubleProperty(String key, double defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                System.err.println("Invalid double property: " + key);
                // In a production environment, we might want to log this error
            }
        }
        return defaultValue;
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public void removeProperty(String key) {
        properties.remove(key);
    }

    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    public void reloadConfiguration() {
        loadConfiguration();
    }
}