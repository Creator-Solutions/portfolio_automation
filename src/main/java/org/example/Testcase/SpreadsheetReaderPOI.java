package org.example.Testcase;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.*;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.poi.xssf.usermodel.*;
import org.example.Config;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;


/**
 * Class to read Excel files
 * Contains extra method not for Production use
 * -> for debugging purposes only.
 *
 * @author owen
 */
public class SpreadsheetReaderPOI{
    /**
     * Class properties
     */
    private XSSFWorkbook workbook;
    private XSSFWorkbook report;
    private XSSFSheet template;

    private FileInputStream inputStream;
    private File file;

    public String getSheetName(int templateIndex){
        return workbook.getSheetName(templateIndex);
    }

    /**
     * Handles reading template Excel file
     * Handles creating a new Excel File for Reporting
     * @param input Template file.
     * @param output Report File
     */
    public void readExcel(File input, File output){
        try{
            // Get primary workbook
            inputStream = new FileInputStream(input);
            workbook = new XSSFWorkbook(inputStream);
            report = Workbook.createWorkbook(output, workbook);
            file = output;
        }catch (IOException ex){
            System.out.println("IOEXCEPTION: " + ex.getMessage());
        }
    }

    /**
     * Finds all files & folders
     * -> only uses files
     * @param path Path of .xlsx files
     * @param ext Specific extension for Excel Files
     * @return List
     */
    public List<String> getSpreadSheetsByApacheCommonLib(Path path, String ext) throws IOException{
        if (!Files.isDirectory(path)){
            throw new IllegalArgumentException("Path must be a directory");
        }

        List<String> paths = new ArrayList<>();

        File testDocs = new File(path.toUri());
        File[] files = testDocs.listFiles();

        if (files != null){
            for (File f: files){
                if (!f.isDirectory() && (f.getAbsolutePath().endsWith(ext))){
                    //if file is not a directory, add to list
                    paths.add(f.getName());
                }
            }
        }

        return new ArrayList<>(
                new LinkedHashSet<>(paths)
        );
    }

    /**
     * Returns the current active sheet row count
     * @param sheetIndex e.g => 0 = Login
     * @return int
     */
    public int getRowCount(int sheetIndex ){
        // Fix made for SEL-38
        return workbook.getSheetAt(sheetIndex).getPhysicalNumberOfRows();
    }


    /**
     * Returns number of sheets
     * from test case workbook
     * @return int
     */
    public int getSheetCount(){
        return workbook.getNumberOfSheets();
    }


    /**
     * Set value into specific cell
     * -> (rowNumber, colNumber) => data
     * @param sheetIndex index of current sheet
     * @param colNumber number of Column in Row
     * @param rowNumber Specific row in sheet
     * @param data String value based on action result
     */
    public void setValueIntoCell(int sheetIndex, int colNumber, int rowNumber, String data, boolean... isLastRetry) {
        try{
            XSSFSheet writable = report.getSheetAt(sheetIndex);
            XSSFRow row = writable.getRow(rowNumber);
            XSSFCell cell = row.getCell(colNumber);

            if (cell == null){
                cell = row.createCell(colNumber);
            }
            XSSFCellStyle style = cell.getCellStyle();
            XSSFFont newFont = style.getFont();
            newFont.setColor(IndexedColors.BLACK.getIndex());
            style.setFont(newFont);

            if ("FAILURE".equals(data) && isLastRetry[0]){
                XSSFCellStyle newStyle = combineStyles(style, report);
                cell.setCellStyle(newStyle);
            }
            setCellValue(cell,data);
        }catch (Exception ex){
            ex.fillInStackTrace();
        }
    }

    /**
     * Main call to enter value into cell
     * @param cell Excel cell at (x, y) position
     * @param value String value for insert
     */
    private static void setCellValue(XSSFCell cell, String value) {
        cell.setCellType(CellType.STRING);
        cell.setCellValue(value);
    }

    /**
     * Read Value from Specific Cell
     * @param sheetIndex index of current sheet
     * @param colNumber number of Column in Row
     * @param rowNumber Specific row in sheet
     * @return String
     */
    public String getValueFromCell(int sheetIndex, int colNumber, int rowNumber) {
        try{
            template =  workbook.getSheetAt(sheetIndex);
            XSSFRow row = template.getRow(rowNumber);
            XSSFCell cell = row.getCell(colNumber, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            return getValue(cell);
        }catch (Exception ex){
            if (ex instanceof NullPointerException){
                System.out.println("Value cannot be null");
            }
            return ex.getMessage();
        }
    }

    /**
     * Retrieve specific data type from cell
     * @param cell Excel Cell
     * @return String
     */
    public static String getValue(XSSFCell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) ((double) cell.getNumericCellValue()));
            case BLANK:
                return "";
            default:
                return "None";
        }
    }

    /**
     * Checks for basic styling on stylesheet
     * -> and combines all styling together
     * @param originStyle Style cloned from template
     * @param workbook Workbook used to add styling to
     * @return XSSFCellStyle
     */
    public static XSSFCellStyle combineStyles(XSSFCellStyle originStyle, XSSFWorkbook workbook){
        XSSFCellStyle combinedStyle = workbook.createCellStyle();
        combinedStyle.cloneStyleFrom(originStyle);
        XSSFFont newFont = originStyle.getFont();

        // Copy all border styles from the original style
        combinedStyle.setBorderTop(BorderStyle.valueOf(originStyle.getBorderTop().getCode()));
        combinedStyle.setBorderRight(BorderStyle.valueOf(originStyle.getBorderRight().getCode()));
        combinedStyle.setBorderBottom(BorderStyle.valueOf(originStyle.getBorderBottom().getCode()));
        combinedStyle.setBorderLeft(BorderStyle.valueOf(originStyle.getBorderLeft().getCode()));


        if (originStyle.getFillPattern() == FillPatternType.NO_FILL) {
            newFont.setFontHeightInPoints(originStyle.getFont().getFontHeightInPoints());
            combinedStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            combinedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            combinedStyle.setFont(newFont);
        }

        if (combinedStyle.getFillPattern() == FillPatternType.SOLID_FOREGROUND){
            newFont.setColor(IndexedColors.BLACK.getIndex());
        }

        return combinedStyle;
    }

    /**
     * Checks if row is empty
     * For debugging purposes only.
     * @param row current row in the sheet
     * @return boolean
     */
    public boolean isRowEmpty(XSSFRow row){
        boolean isEmpty = true;
        DataFormatter dataFormatter = new DataFormatter();

        if (row != null){
            for (Cell cell: row){
                if (dataFormatter.formatCellValue(cell).trim().isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }

    /**
     * Takes a screenshot of the webpage where failures occur
     *
     * @param rowIndex current row of the template
     * @param action current action that is failing
     * @param field field that is failing
     */
    public void takeSnapshot(ChromeDriver driver, int rowIndex, String action, String field, String dir){
        try {

            //Date Formats
            LocalDateTime dateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String formattedDateTime = dateTime.format(formatter);

            /*
              Parent and Child names
              Parent = Snapshot
              Child = Snapshot - datetime
             */
            String parentFolderName = dir + "/Snapshots/";
            //Append parent path to child
            String folderName = parentFolderName + "SnapShot-" + formattedDateTime;
            System.out.println(folderName);
            //Object to check parent directory
            File parentFolder = new File(parentFolderName);

            if (OnParentExistsCallback(parentFolder.exists(), parentFolderName)){
                File childFolder = new File(folderName);
                if (!childFolder.exists()){
                    boolean isChildCreated = childFolder.mkdir();
                    if (isChildCreated){
                        //Generate file for screenshot
                        final File outputSnapShot = new File(folderName + "/" + generateFileName(rowIndex, action, field) + ".png").getAbsoluteFile();
                        //Save screenshot in File object
                        File sourceFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                        FileUtils.copyFile(sourceFile, outputSnapShot);

                        /*
                            If temp file exists in other directory delete
                            screenshot after copy
                         */
                        if (outputSnapShot.exists()){
                            boolean isTempFileDeleted = sourceFile.delete();
                            if (isTempFileDeleted){
                                System.out.println("Took Screenshot for Row: " + rowIndex + "- Action: " + action);
                                System.out.println("Snapshot stored at: " + outputSnapShot.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Callback function to check for directories
     * Will attempt to create folder on runtime without duplicating code
     * @param arg condition if parent folder exists
     * @param name parent folder name, to create if not exists
     * @return boolean
     */
    private boolean OnParentExistsCallback(boolean arg, String name){
        if (arg){
            return true;
        }else{
            File parentFolder = new File(name);
            boolean callBackCondition = recursiveCondition(parentFolder.mkdir());
            OnParentExistsCallback(callBackCondition, name);
        }
        return false;
    }

    /**
     * Method to trigger recursion dependent on condition
     * @param condition callback trigger
     * @return boolean
     */
    public boolean recursiveCondition(boolean condition) {
        return !condition;
    }

    /**
     * Generate a file name for the screenshot
     * @param rowIndex current row of the template
     * @param action current action that is failing
     * @param field field that is failing
     * @return String
     */
    private static String generateFileName(int rowIndex, String action, String field){

        //Generate TimeStamp
        Date date = new Date();
        long time = date.getTime();
        Timestamp tmp = new Timestamp(time);

        String tst = tmp.toString();
        try{
            tst = tst.substring(0, tst.length() - 4);
            tst = tst.replace("-", "");
            tst = tst.replace(" ", "");
            tst = tst.replace(":", "");
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }

        return String.format("rowIndex-%d-%s-%s-%s", rowIndex, action, field, tst);
    }

    public static String generateFileName(){
        Date date = new Date();
        long time = date.getTime();
        Timestamp tmp = new Timestamp(time);

        String tst = tmp.toString();
        try{
            tst = tst.substring(0, tst.length() - 4);
            tst = tst.replace("-", "");
            tst = tst.replace(" ", "");
            tst = tst.replace(":", "");
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }

        return String.format("Session %s", tst);
    }

    /**
     * Handles the creation of log folders
     * Catering for when multiple logs are run for readability.
     * @param rootDIR Main directory
     * @return String
     */
    public static String createLogDirectory(String rootDIR) throws FileNotFoundException{
        Date date = new Date();
        long time = date.getTime();
        Timestamp tmp = new Timestamp(time);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyy");
        String formattedDate = simpleDateFormat.format(date);
        String timestamp = tmp.toString();

        String sessionFolderName = String.format(rootDIR + "/Logs/" + generateFileName() + "/");

        File sessionFolder = new File(sessionFolderName);
        if (!sessionFolder.exists()) sessionFolder.mkdir();

        if (sessionFolder.exists() && sessionFolder.isDirectory()){
            return sessionFolder.getAbsolutePath();
        }

        throw new FileNotFoundException(String.format("Folder does not exist at %s", sessionFolder.getAbsolutePath()));
    }


    /**
     * Closes workbook and report file
     */
    public void closeFile(){
        try{
            workbook.close();
            report.write(new FileOutputStream(file));
        }catch (IOException  ex){
            System.out.println("IOException: " + ex.getMessage());
        }
    }

    private boolean isDouble(String str){
        try{
            Double.valueOf(str);
        }catch (Exception ex){
            return false;
        }

        return true;
    }

    /**
     * Main method for debugging purposes only.
     * @param args arguments
     */
    public static void main(String[] args){
        SpreadsheetReaderPOI xls = new SpreadsheetReaderPOI();
        System.setProperty("webdriver.chrome.driver", "src\\chromedriver.exe");
        String[] content = {"SUCCESS!", "FAILURE"};
        Random random = new Random();
        String dir = Config.getSetting("basedir");
        ChromeOptions opt = new ChromeOptions();
        opt.setBinary("E:\\Automation\\chromium\\chrome.exe");
        opt.addArguments("--remote-allow-origins=*");
        ChromeDriver driver = new ChromeDriver(opt);

        try{
            List<String>  files = xls.getSpreadSheetsByApacheCommonLib(Paths.get(dir), "xlsx");

            for (String xlsFile: files){
                File output = new File(createLogDirectory(dir) + "/Logs/" + xlsFile.replace(".xlsx", "") + " " + new SimpleDateFormat("yyyy-MM-dd HHmmss").format(new java.util.Date()) + ".xlsx");
                File template = new File(dir + "/" + xlsFile);
                xls.readExcel(template, output);

                int sheetCount = xls.report.getNumberOfSheets();

                for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++){
                    XSSFSheet origin = xls.report.getSheetAt(sheetIndex);
                    int rowCount = xls.report.getSheetAt(sheetIndex).getPhysicalNumberOfRows();
                    System.out.println("Sheet: " + origin.getSheetName() + " " + "Row Count: " + rowCount);
                    for (int rowIndex = origin.getFirstRowNum(); rowIndex <= rowCount; rowIndex++){
                        int randomNumber=random.nextInt(content.length);
                        xls.takeSnapshot(driver,rowIndex, "", "",  dir);
                        xls.setValueIntoCell(sheetIndex, 5, rowIndex, content[randomNumber]);
                    }

                }
                xls.closeFile();
                System.out.println(createLogDirectory(dir));
            }
        }catch (Exception ex){
            //Replaced printStackTrace
            // displays information about current state of stack frames
            // for the current thread.
            ex.fillInStackTrace();

            if (ex instanceof FileNotFoundException){
                System.out.println("[IO Exception]: " + ex);
            }
        }
    }
}