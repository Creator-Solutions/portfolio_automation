package org.example;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

public abstract class Config {

    public static String getSetting(String setting){
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("src/config.properties")) {
            properties.load(fileInputStream);
            return properties.getProperty(setting);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getPropertyName(String propertyName){
        Properties properties = loadPropertiesFile();
        if (properties != null){
            Set<Object> props = properties.keySet();

            for (Object obj : props){
                String key = obj.toString();
                if (key.equals(propertyName)){
                    return key;
                }
            }
        }else{
            return null;
        }

        return null;
    }

    private static Properties loadPropertiesFile(){
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("src/config.properties")) {
            properties.load(fileInputStream);
            return properties;
        } catch (IOException e) {
            e.fillInStackTrace();
            return null; // Handle the exception as needed
        }
    }
}
