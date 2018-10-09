package dirkmomsen.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public abstract class GenericLogger<T> {
    private Properties config;
    private String fileName;
    private Logger logger;
    
    private static final String PATH = "path";
    
    public GenericLogger(Properties config, String fileName, Logger logger) {
        this.config = config;
        this.fileName = fileName;
        this.logger = logger;
    }
    
    public GenericLogger(Properties config, String fileName) {
        this.config = config;
        this.fileName = fileName;
    }
    
    public GenericLogger(Properties config) {
        this.config = config;
    }
    
    public abstract GenericLogger<T> log(T item);
    
    public abstract GenericLogger<T> console();
    
    public abstract GenericLogger<T> console(T item);
    
    public Logger logger() {
        return logger;
    }
    
    public GenericLogger<T> toFile(String fileName) {
        setFileName(fileName);
        return this;
    }
    
    public String getFilePath() {
        String filePath;
        if (config == null) {
            config = new Properties();
            config.setProperty(PATH, GenericLogger.class.getProtectionDomain().getCodeSource().getLocation().getPath().substring(1));
        } else if (config.getProperty(PATH) == null) {
            config.setProperty(PATH, GenericLogger.class.getProtectionDomain().getCodeSource().getLocation().getPath().substring(1));
        }
        filePath =  formatFilePath();
        return filePath;
    }
    
    public String formatFilePath() {
        if (fileName == null) {
            fileName = ".txt";
        }
        return config.getProperty(Logger.PATH) + DateTimeFormatter.ofPattern("yyyy_MM_dd").format(LocalDateTime.now()) + "_" + fileName;
    }
    
    public Properties getConfig() {
        return config;
    }
    
    public void setConfig(Properties config) {
        this.config = config;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
