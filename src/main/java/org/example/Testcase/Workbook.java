package org.example.Testcase;


import org.apache.poi.xssf.usermodel.*;


import javax.swing.border.Border;
import java.io.*;

public abstract class Workbook {

    private static XSSFWorkbook report;

    public static XSSFWorkbook getWorkBook(File file) throws IOException{
        return getWorkbook(file);
    }

    public static XSSFWorkbook getWorkbook(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        return workbook;
    }

    public static XSSFWorkbook createWorkbook(File file, XSSFWorkbook workbook)  {
        report = new XSSFWorkbook();

        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++){
            final XSSFSheet originSheet = workbook.getSheetAt(sheetIndex);
            final XSSFSheet newSheet = report.createSheet(originSheet.getSheetName());
            //set Height and Width
            newSheet.setDefaultRowHeight(originSheet.getDefaultRowHeight());
            newSheet.setDefaultColumnWidth(originSheet.getDefaultColumnWidth());

            for (int rowIndex = originSheet.getFirstRowNum(); rowIndex <= originSheet.getLastRowNum(); rowIndex++){
                final XSSFRow originRow = originSheet.getRow(rowIndex);
                if (originRow != null){
                    final XSSFRow newRow = newSheet.createRow(rowIndex);
                    newRow.setHeight(originRow.getHeight());

                    for (int colNumber = originRow.getFirstCellNum(); colNumber <= originRow.getLastCellNum() ; colNumber++){
                        newSheet.setColumnWidth(colNumber, originSheet.getColumnWidth(colNumber));
                        final XSSFCell originCell = originRow.getCell(colNumber);
                        if (originCell != null){
                            final XSSFCell newCell = newRow.createCell(colNumber);

                            /*
                             * Copy styling from template to report
                             * Add additional styling based on value condition
                             * set cell style to updated styling
                             */
                            XSSFCellStyle sourceStyle = report.createCellStyle();
                            sourceStyle.cloneStyleFrom(originCell.getCellStyle());
                            setCellValue(newCell, getValue(originCell));
                            newCell.setCellStyle(sourceStyle);
                        }
                    }
                }
            }
        }
        return report;
    }

    private static void setCellValue(final XSSFCell cell, final Object value) {
        if (value instanceof Boolean) {
            cell.setCellValue((boolean) value);
        } else if (value instanceof Byte) {
            cell.setCellValue((byte) value);
        } else if (value instanceof Double) {
            cell.setCellValue((double) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        }else if (value instanceof String) {
            if (((String) value).startsWith("=")) {
                //  Formula String
                cell.setCellFormula(value.toString().substring(1));
            } else {
                cell.setCellValue(value.toString());
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static String getValue(XSSFCell cell){
        switch (cell.getCellType()){
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BLANK:
                return "";
            default:
                return "none";
        }
    }
}
