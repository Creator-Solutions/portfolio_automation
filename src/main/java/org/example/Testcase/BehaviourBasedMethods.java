package org.example.Testcase;

import org.example.Exceptions.InvalidElementException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * Class that handles all behaviours during execution.
 *
 * @author Owen Burns
 */
public class BehaviourBasedMethods {

    /**
     * Method that enters value into fields
     * @param el - Web Element
     * @param value - Value to be entered
     */
    public void enterValue(WebElement el, String value){
        el.sendKeys(value);
    }

    /**
     * Method to grab elements text value
     * @param el - Given element
     * @return string
     */
    public String getElementValue(WebElement el){
        return el.getText();
    }

    /**
     * Retrieves element Value by xPath
     * @param driver - Chrome Driver instance
     * @param expression - xPath expression
     * @return String
     */
    public String getElementValueByXPath(ChromeDriver driver, String expression){
        WebElement el = driver.findElement(By.xpath(expression));

        return el.getText();
    }

    /**
     * Handles onclick events for elements
     * @param driver - Chrome Driver Instance
     * @param xPathExpression - xPath Expression
     * @throws InvalidElementException - Throws an exception if the element could not be found or is not displayed
     */
    public void onElementClick(ChromeDriver driver, String xPathExpression) throws InvalidElementException {
        WebElement el = driver.findElement(By.xpath(xPathExpression));

        try{
            if (el.isDisplayed()){
                el.click();
            }else{
                throw new InvalidElementException("Clickable element could not be located by xPath");
            }
        }catch (InvalidElementException ex){
            ex.fillInStackTrace();
        }
    }
}
