package org.example.Testcase;

import org.example.Config;
import org.example.Exceptions.InvalidPropertyException;
import org.example.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.io.Files.getFileExtension;

/**
 * Handles all data driven execution
 * Data read from excel spreadsheet
 *
 * @author Owen Burns
 */
public class DataDrivenEvents  extends EventInterface {

    private static final SpreadsheetReaderPOI xls = new SpreadsheetReaderPOI();
    private static final BehaviourBasedMethods bbm = new BehaviourBasedMethods();

    private final String  baseDIR = Config.getSetting("basedir");

    private static long startTime;
    private final List<String> xlsFiles = new ArrayList<>();

    public DataDrivenEvents(WebDriver driver){
        super(driver);
    }

    @Override
    public boolean test(String sessionFolderName){
        try{
            File directory = new File(baseDIR + "/Logs");

            if (!directory.exists()) {
                boolean isCreated = directory.mkdir();
                Log.out(2, "Log Directory Created: " + isCreated);
            }
            directory = new File(baseDIR + "/Attachments");
            if (!directory.exists()) {
                boolean isCreated = directory.mkdir();
                Log.out(2, "Attachment Directory Created: " + isCreated);
            }

            File[] files = new File(baseDIR + "/").listFiles();

            assert files != null;
            for (File file : files) {
                if (file.isFile()) {
                    if (getFileExtension(file.getName()).equalsIgnoreCase("xlsx")) {
                        xlsFiles.add(file.getName());
                    }
                }
            }

            for (String xlsFile : xlsFiles) {
                File output = new File(sessionFolderName + "/" + xlsFile.replace(".xlsx", "") + " " + new SimpleDateFormat("yyyy-MM-dd HHmmss").format(new java.util.Date()) + ".xlsx");
                File template = new File(baseDIR + "/" + xlsFile);
                Log.out(2, "Starting test for " + xlsFile);
                xls.readExcel(template, output);
                int sheetCount = xls.getSheetCount();

                for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
                    startTime = System.currentTimeMillis();
                    int rows = xls.getRowCount(sheetIndex);

                    for (int rowIndex = 0; rowIndex < rows; rowIndex++){
                        String action = xls.getValueFromCell(sheetIndex, 0, rowIndex).toLowerCase();
                        String field = xls.getValueFromCell(sheetIndex, 1, rowIndex);
                        String value = xls.getValueFromCell(sheetIndex, 2, rowIndex);


                        if (!action.equals("")) {
                            Log.reset();
                            Log.out(1, "");
                            Log.out(1, "Action: " + action + ",  Field: " + field + ", Value: " + value + ", Sheet Number: " + (sheetIndex + 1) + ", Row Number: " + (rowIndex + 1));
                            Log.inc();
                        }
                        try {
                            execSteps(action, field, value, sheetIndex , rowIndex , xls, driver);
                            Thread.sleep(1000);
                        } catch (Exception ex) {
                            xls.setValueIntoCell(sheetIndex, 3, rowIndex, "FAILURE");
                        }
                    }
                }
                xls.closeFile();
            }
            Log.closeLog();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return true;
    }

    public static void execSteps(String action, String field, String value, int sheetIndex, int rowIndex, SpreadsheetReaderPOI xls, WebDriver driver) throws InvalidPropertyException {
        String xPath = "";
        WebElement element;

        switch (action){
            case "navigate":
                if (!value.isEmpty()){
                    Log.out(4, "searching for navigation element");
                    driver.get(value);
                    Log.out(4, "Navigating to url");
                    success(sheetIndex, rowIndex);
                }else{
                    failure(sheetIndex, rowIndex);
                    throw new InvalidPropertyException("Could not find Url");
                }
                break;

            case "waitForAction":
                if (value != null){
                    long waitDuration = Long.parseLong(value);
                    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(waitDuration));
                    success(sheetIndex, rowIndex);
                }else{
                    Log.out(4, "No wait time set");
                    failure(sheetIndex, rowIndex);
                }
                break;


            case "set text":
                Log.out(4, "Searching for usable field");

                try{
                    xPath = "//div[contains(@class, form_group)]//input[@id='"+ field +"']";
                    element = driver.findElement(By.xpath(xPath));

                    Log.out(4, "Searching for usable field");
                    Log.out(4, "xPath Value: " + xPath);

                    if (element != null){
                        bbm.enterValue(element, value);
                        success(sheetIndex, rowIndex);
                    }

                }catch (Exception ex){
                    try{
                        if (field.contains("-")){
                            field = field.replace("-", "");

                            xPath = "//div[contains(@class, form_group)]//input[@data-test='"+ field +"']";
                            Log.out(4, "xPath Value: " + xPath);

                            element = driver.findElement(By.xpath(xPath));

                            if (bbm.verifyElement(element)){
                                Log.out(4, "Found relative field by xpath");
                                bbm.enterValue(element, value);
                                success(sheetIndex, rowIndex);
                            }else{
                                Log.out(4, "Could not find relative field by xPath");
                                failure(sheetIndex, rowIndex);
                            }
                        }
                    }catch (NoSuchElementException e){
                        failure(sheetIndex, rowIndex);
                    }
                }

                break;

            case "click":
                try{
                    xPath = "//div[contains(@class, form_group)]//input[@type='submit']|//div[contains(@class, form_group)]//button[@data-test='"+ value +"']";
                    Log.out(4, "Searching for clickable element");
                    Log.out(4, "xPath Value: " + xPath);
                    element = driver.findElement(By.xpath(xPath));

                    if (bbm.verifyElement(element)){
                        Log.out(4, "Sending Click Event");
                        bbm.onElementClick(element);
                        success(sheetIndex, rowIndex);
                    }
                }catch (NoSuchElementException ex) {
                    Log.out(4, "Could not find clickable element");
                    try {
                        Log.out(4, "Searching for clickable element using secondary xPath");
                        xPath = "//div[contains(@class, form_group)]//input[contains(@data-test, '" + replaceSpaces(value) + "')]";
                        Log.out(4, "xPath Value: " + xPath);

                        element = driver.findElement(By.xpath(xPath));

                        if (bbm.verifyElement(element)) {
                            Log.out(4, "Sending Click Event");
                            bbm.onElementClick(element);
                            success(sheetIndex, rowIndex);
                        }
                    } catch (NoSuchElementException exc) {
                        Log.out(4, "Could not find clickable element");

                        try {
                            Log.out(4, "Final Element Search Attempt");
                            xPath = "//*[contains(text(),'"+ value + "')]/following::div/button[@id='add-to-cart-" + replaceSpaces(value) + "']";

                            element = driver.findElement(By.xpath(xPath));

                            if (bbm.verifyElement(element)) {
                                Log.out(4, "Sending Click Event");
                                bbm.onElementClick(element);
                                success(sheetIndex, rowIndex);
                            }

                        } catch (NoSuchElementException elementException) {
                            failure(sheetIndex, rowIndex);
                        }

                    }
                }
                break;

            case "click icon":
                if (!value.isEmpty()){
                    try{
                        Log.out(4, "Searching for Clickable Cart Icon");
                        xPath = "//div[contains(@id, 'shopping_cart_container')]//a[contains(@class, '"+ concatenate(value) +"_link')]";

                        Log.out(4, "Locating Icon...");
                        Log.out(4, "xPath: " + xPath);
                        element = driver.findElement(By.xpath(xPath));

                        if (bbm.verifyElement(element)){
                            Log.out(4, "submitting Click Event");
                            bbm.onElementClick(element);
                        }
                    }catch (NoSuchElementException ex){
                        try{
                            Log.out(4, "Searching for Clickable Cart Icon using secondary xPath");
                            xPath = "//div[contains(@class, 'primary_header')]//div[@id='shopping_cart_container']//a[contains(@class, '"+ concatenate(value) +"_link')]";

                            Log.out(4, "xPath: " + xPath);

                            element = driver.findElement(By.xpath(xPath));

                            if (bbm.verifyElement(element)){
                                Log.out(4, "submitting Secondary Click Event");
                                bbm.onElementClick(element);
                                success(sheetIndex, rowIndex);
                            }
                        }catch (NoSuchElementException e){
                            Log.out(4, "Could not find Clickable Element");
                            failure(sheetIndex, rowIndex);
                        }
                    }
                }
                break;


            case "verify":
                if (!field.isEmpty() || !value.isEmpty()){
                    try{
                        xPath = "//div[@class='cart_item']//div[@class='inventory_item_name' and normalize-space()='"+ field +"']/following::div[@class='item_pricebar']//div[@class='inventory_item_price' and contains(normalize-space(), '"+ value +"')]";
                        element = driver.findElement(By.xpath(xPath));

                        Log.out(4, "Locating element using xPath: " + xPath);

                        if (bbm.verifyElement(element)){
                            Log.out(4, "Element found using primary  path");
                            success(sheetIndex, rowIndex);
                        }else{
                            Log.out(4, "Could not find element");
                            failure(sheetIndex, rowIndex);
                        }
                    }catch (NoSuchElementException ex){
                        Log.out(4, "Failed to locate label for verification");

                        try{
                            Log.out(4, "Attempting secondary xPath ");
                            xPath = "//div[contains(@class, 'summary_info')]//div[@class='summary_info_label' and normalize-space()='"+ field +"']/following::div[@class='summary_value_label' and normalize-space()='"+ value +"']";

                            element = driver.findElement(By.xpath(xPath));
                            if (bbm.verifyElement(element)) {
                                Log.out(4, "Found element using Secondary Path: " + xPath);
                                success(sheetIndex, rowIndex);
                            }
                        }catch (NoSuchElementException e){
                            try{
                                Log.out(4, "Locating value label");
                                xPath = "//div[contains(@class, 'summary_info')]//div[@class='summary_info_label' ]/following::div[contains(@class, 'summary_"+ removeSpace(field) +"_label')]";

                                Log.out(4, "Searching for label: " + xPath);
                                element = driver.findElement(By.xpath(xPath));

                                Log.out(4, "Verifying label conditions");

                                if (bbm.verifyElementByValue(element, value)){
                                    Log.out(4, "Label value verified successfully");
                                    success(sheetIndex, rowIndex);
                                }else{
                                    Log.out(4, "Could not verify Label value  successfully");
                                    failure(sheetIndex, rowIndex);
                                }
                            }catch (NoSuchElementException elementException){
                                try{
                                    Log.out(4, "Locating secondary  value label");
                                    xPath = "//div[@class='header_secondary_container']//span[contains(text(), '"+ value +"')]";

                                    element = driver.findElement(By.xpath(xPath));

                                    if (bbm.verifyElement(element)){
                                        Log.out(4, "Found verified element");
                                        success(sheetIndex, rowIndex);
                                    }
                                }catch (NoSuchElementException e1){
                                    try{
                                        xPath = "//div[contains(@class, 'checkout_complete_container')]//h2[contains(text(),'"+ value +"')]";

                                        element = driver.findElement(By.xpath(xPath));

                                        if (bbm.verifyElement(element)){
                                            Log.out(4, "Found verified element");
                                            success(sheetIndex, rowIndex);
                                        }
                                    }catch (NoSuchElementException exception){
                                        Log.out(4, "Could not verify using xPath: " + xPath);
                                        failure(sheetIndex, rowIndex);
                                    }
                                }
                            }
                        }
                    }
                }
                break;

            case "totaltimeexecution":
                Log.out(4, "Total time Execution: " +  compareTime(startTime) + "elapsed");
                xls.setValueIntoCell(sheetIndex, 3, rowIndex, compareTime(startTime));
            break;
    }
}

    private static String replaceSpaces(String value){
        return value.replace(" ", "-").toLowerCase();
    }

    private static String concatenate(String value){
        return value.replace(" ", "_").toLowerCase();
    }

    private static String removeSpace(String value){
        return value.replace(" ", "");
    }

    private static void success(int sheetIndex, int rowIndex){
        xls.setValueIntoCell(sheetIndex, 3, rowIndex,  "SUCCESS", true);
    }

    private static void failure(int sheetIndex, int rowIndex){
        xls.setValueIntoCell(sheetIndex, 3, rowIndex,  "FAILURE", true);
    }

    private static String compareTime(long startTime) {
        long currentTime = System.currentTimeMillis();
        long totalTime = currentTime - startTime;

        return String.format("%dh:%dm:%ds",
                TimeUnit.MILLISECONDS.toHours(totalTime),
                TimeUnit.MILLISECONDS.toMinutes(totalTime),
                TimeUnit.MILLISECONDS.toSeconds(totalTime));
    }
}
