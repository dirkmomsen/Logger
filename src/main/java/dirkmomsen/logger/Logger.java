package dirkmomsen.logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Logger {
    
    public final static String PATH = "path";
    public final static String ACTIVE = "active"; // true/false
    public final static String CONSOLE = "console";
    
    private Properties config;
    private String fileName;
    private String last;
    
    @SuppressWarnings("unchecked")
    private Map<String, GenericLogger> loggers;
    
    public Logger() {
    }
    
    public Logger(Properties config) {
        this.config = config;
        this.loggers = new HashMap<>();
    }
    
    public Logger(Properties config, String fileName) {
        this.config = config;
        this.fileName = fileName;
        this.loggers = new HashMap<>();
    }
    
    public static Logger createLogger(Properties config) {
        Logger logger = new Logger(config);
        logger.empty();
        return logger;
    }
    
    public static Logger createLogger(Properties config, String fileName) {
        Logger logger = new Logger(config, fileName);
        logger.empty();
        return logger;
    }
    
    public Logger toFile(String fileName) {
        setFileName(fileName);
        
        loggers.forEach((k, v) -> v.setFileName(fileName));
        
        return this;
    }
    
    static final String EMPTY= "empty";
    public GenericLogger empty() {
        if (loggers.get(EMPTY) == null) {
            loggers.put(EMPTY, new GenericLogger(config, fileName, this) {
                @Override
                synchronized public GenericLogger log(Object item) {
                    return this;
                }
                
                @Override
                public GenericLogger console() {
                    return this;
                }
                
                @Override
                public GenericLogger console(Object item) {
                    return this;
                }
            });
        }
        return loggers.get(EMPTY);
    }
    
    public GenericLogger<Object> json() {
        if (!checkActive()) {
            return (GenericLogger<Object>)loggers.get(EMPTY);
        }
        
        final String name = "json";
        if (loggers.get(name) == null) {
            loggers.put(name, new GenericLogger<Object>(config, fileName, this) {
                @Override
                synchronized public GenericLogger<Object> log(Object item) {
                    if (!checkActive()) return this;
                    
                    last = logJson(item, this.getFilePath());
                    
                    return this;
                }
                
                @Override
                public GenericLogger<Object> console() {
                    printConsole(last);
                    return this;
                }
                
                @Override
                public GenericLogger<Object> console(Object item) {
                    if (!checkActive()) return this;
                    
                    last = getJsonString(item);
                    
                    printConsole(last);
                    
                    return this;
                }
            });
        }
        return loggers.get(name);
    }
    
    public static GenericLogger<Object> json(Properties config) {
        return createLogger(config).json();
    }
    
    public GenericLogger<String> string() {
        if (!checkActive()) {
            return (GenericLogger<String>)loggers.get(EMPTY);
        }
        
        final String name = "string";
        if (loggers.get(name) == null) {
            loggers.put(name, new GenericLogger<String> (config, fileName, this) {
                @Override
                synchronized public GenericLogger<String> log(String item) {
                    if (!checkActive()) return this;
                    
                    last = logString(item, this.getFilePath());
                    
                    return this;
                }
                
                @Override
                public GenericLogger<String> console() {
                    printConsole(last);
                    return this;
                }
                
                @Override
                public GenericLogger<String> console(String item) {
                    if (!checkActive()) return this;
                    
                    last = item;
                    
                    printConsole(last);
                    
                    return this;
                }
            });
        }
        return loggers.get(name);
    }
    
    public static GenericLogger<String> string(Properties config) {
        return createLogger(config).string();
    }
    
    public GenericLogger<HashMap<String, String>> stringMap() {
        if (!checkActive()) {
            return (GenericLogger<HashMap<String, String>>)loggers.get(EMPTY);
        }
        
        final String name = "stringMap";
        if (loggers.get(name) == null) {
            loggers.put(name, new GenericLogger<HashMap<String, String>> (config, fileName, this) {
                
                @Override
                synchronized public GenericLogger<HashMap<String, String>> log(HashMap<String, String> item) {
                    if (!checkActive()) return this;
                    
                    last = logStringMap(item, this.getFilePath());
                    
                    return this;
                }
                
                @Override
                public GenericLogger<HashMap<String, String>> console() {
                    printConsole(last);
                    return this;
                }
                
                @Override
                public GenericLogger<HashMap<String, String>> console(HashMap<String, String> item) {
                    if (!checkActive()) return this;
                    
                    last = getStringMapString(item);
                    
                    printConsole(last);
                    
                    return this;
                }
            });
        }
        return loggers.get(name);
    }
    
    public static GenericLogger<HashMap<String, String>> stringMap(Properties config) {
        return createLogger(config).stringMap();
    }
    
    public GenericLogger<Throwable> stacktrace() {
        if (!checkActive()) {
            return (GenericLogger<Throwable>)loggers.get(EMPTY);
        }
        
        final String name = "stacktrace";
        if (loggers.get(name) == null) {
            loggers.put(name, new GenericLogger<Throwable> (config, fileName, this) {
                
                @Override
                synchronized public GenericLogger<Throwable> log(Throwable item) {
                    if (!checkActive()) return this;
                    
                    last = logStacktrace(item, this.getFilePath());
                    
                    return this;
                }
                
                @Override
                public GenericLogger<Throwable> console() {
                    printConsole(last);
                    return this;
                }
                
                @Override
                public GenericLogger<Throwable> console(Throwable item) {
                    if (!checkActive()) return this;
                    
                    last = getStacktraceString(item);
                    
                    printConsole(last);
                    
                    return this;
                }
            });
        }
        return loggers.get(name);
    }
    
    public static GenericLogger<Throwable> stacktrace(Properties config) {
        return createLogger(config).stacktrace();
    }
    
    private static String logJson(Object object, String filePath) {
        String objectAsJson = getJsonString(object);

//        List<String> lines = Arrays.toList(LocalDateTime.now().toString(), objectAsJson);
        String lines = getFormattedString(objectAsJson) + System.lineSeparator();
        
        appendFile(filePath, lines);
        return objectAsJson;
    }
    
    private static String getJsonString(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String objectAsJson = "";
        try {
            objectAsJson = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return objectAsJson;
    }
    
    private static String logStringMap(Map<String, String> map, String filePath) {
        String returnString = getStringMapString(map);
//        List<String> lines = Arrays.toList(LocalDateTime.now().toString(), returnString);
        String lines = getFormattedString(returnString) + System.lineSeparator();
        
        appendFile(filePath, lines);
        
        return returnString;
    }
    
    private static String getStringMapString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        map.forEach((key, value) -> sb.append("Key: ").append(key).append(", Value: ").append(value).append(System.lineSeparator()));
        
        return sb.toString();
    }
    
    private static String logString(String line, String filePath) {
//        List<String> lines = Arrays.toList(LocalDateTime.now().toString(), line);
        String lines = getFormattedString(line) + System.lineSeparator();
        
        appendFile(filePath, lines);
        
        return line;
    }
    
    private static String logStacktrace(Throwable e, String filePath) {
        String returnString = getStacktraceString(e);
//        List<String> lines = Arrays.toList(LocalDateTime.now().toString(), returnString);
        String lines = getFormattedString(returnString) + System.lineSeparator();
        
        appendFile(filePath, lines);
        
        return returnString;
    }
    
    private static String getStacktraceString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Localized Message: " + e.getLocalizedMessage() + ". Message: " + e.getMessage() + ". Class: " + e.getClass() + System.lineSeparator());
        Arrays.stream(e.getStackTrace()).forEach(v -> sb.append(v.toString()).append(System.lineSeparator()));
        
        return sb.toString();
    }
    
    synchronized private static void appendFile(String filePath, List<String> lines) {
        try {
            Files.write(Paths.get(filePath), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    synchronized private static void appendFile(String filePath, String line) {
        try {
            Files.write(Paths.get(filePath), line.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void printConsole(String last) {
        if (config.getProperty(CONSOLE) != null && Boolean.parseBoolean(config.getProperty(CONSOLE))) {
            System.out.println(getFormattedString(last));
        }
    }
    
    private static String getFormattedString(String returnString) {
        if (returnString.contains(System.lineSeparator())) {
            StringBuilder sb = new StringBuilder();
            String[] splitString = returnString.split(System.lineSeparator());
            
            sb.append(splitString[0]).append(System.lineSeparator());
            Arrays.stream(Arrays.copyOfRange(splitString, 1, splitString.length - 1))
                    .forEach(s -> sb.append("                          -   ").append(s).append(System.lineSeparator()));
            sb.append("                          -   ").append(splitString[splitString.length - 1]);
            
            returnString = sb.toString();
        }
        
        return LocalDateTime.now().toString() + "   -   " + returnString;
    }
    
    private static String getFilePath(String fileName, Properties config) {
        String filePath;
        if (config == null) {
            config = new Properties();
            config.setProperty("log_path", Logger.class.getProtectionDomain().getCodeSource().getLocation().getPath().substring(1));
        }
        filePath =  getFilePathFormatted(config.getProperty("log_path"), fileName);
        return filePath;
    }
    
    private static String getFilePathFormatted(String path, String fileName) {
        return path + DateTimeFormatter.ofPattern("yyyy_MM_dd").format(LocalDateTime.now()) + "_" + fileName;
    }
    
    private boolean checkActive() {
        Boolean test = Boolean.parseBoolean(config.getProperty(ACTIVE));
        return test;
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
    
    public Map<String, GenericLogger> getLoggers() {
        return loggers;
    }
    
    public void setLoggers(Map<String, GenericLogger> loggers) {
        this.loggers = loggers;
    }
    
    public String getLast() {
        return last;
    }
}