package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.example.Testcase.DataDrivenEvents;
import org.example.Testcase.EventInterface;
import org.example.Testcase.SpreadsheetReaderPOI;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLOutput;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class Main{
    public static ChromeDriver driver;

    static String baseDIR = Config.getSetting("basedir");

    public static void main(String[] args) {
        try{

            String sessionFolderName = generateSessionFolder();
            Log.openLog(sessionFolderName + "/logfile " + new SimpleDateFormat("yyyy-MM-dd HHmmss").format(new java.util.Date()) + ".txt");

            driver = getChromeDriver();

            DataDrivenEvents events = new DataDrivenEvents(driver);
            events.test(sessionFolderName);


        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private static String generateSessionFolder() throws FileNotFoundException {
        SpreadsheetReaderPOI poi = new SpreadsheetReaderPOI();
        return poi.createLogDirectory(baseDIR);
    }

    private static ChromeDriver getChromeDriver() {
        System.setProperty("webdriver.chrome.driver", "src/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.setBinary("E:\\Automation\\chromium\\chrome.exe");
        return new ChromeDriver(options);
    }
}