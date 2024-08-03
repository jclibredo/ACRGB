/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method.resourcefile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class ConfigReader {

    public ConfigReader() {
    }

    public Properties getProperties() throws FileNotFoundException, IOException {
        FileInputStream fp = new FileInputStream("D:\\Java Swing\\ACRGB\\src\\main\\java\\Configuration\\config.properties");
        Properties prop = new Properties();
        prop.load(fp);
        return prop;
    }

    public String getSchemaName() throws IOException {
        return getProperties().getProperty("SCHEMA_NAME");
    }

    public String getJndiName() throws IOException {
        return getProperties().getProperty("JNDI_NAME");
    }

    public String getDBPass() throws IOException {
        return getProperties().getProperty("DBPASS");
    }

    public String getDBUser() throws IOException {
        return getProperties().getProperty("DBUSER");
    }
}
