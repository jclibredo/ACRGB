/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method.resourcefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author ACR_GB
 */
@RequestScoped
public class ConfigReader {

    public ConfigReader() {
    }

    public Properties getProperties() {
        Properties prop = new Properties();
        try {
            FileInputStream fp = new FileInputStream("D:\\Java Swing\\ACRGB\\src\\main\\java\\Configuration\\config.properties");
            prop.load(fp);
            return prop;
        } catch (FileNotFoundException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(ConfigReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(ConfigReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return prop;
    }

    public String getSchemaName() {
        return getProperties().getProperty("SCHEMA_NAME");
    }

    public String getJndiName() {
        return getProperties().getProperty("JNDI_NAME");
    }

    public String getDBPass() {
        return getProperties().getProperty("DBPASS");
    }

    public String getDBUser() {
        return getProperties().getProperty("DBUSER");
    }

    public String GetAbsolutePath() {
//        InputStream in = new InputStreamReader(FileLoaderServlet.class.getClassLoader().getResourceAsStream("file.txt") );
        return new File(ConfigReader.class.toString()).getPath();
    }

}
