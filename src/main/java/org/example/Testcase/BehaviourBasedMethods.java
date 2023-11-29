package org.example.Testcase;

import org.example.Log;
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
     * @param element WebElement
     */
    public void  onElementClick(WebElement element) {
        try{
            if (element.isDisplayed()){
                element.click();
                Log.out(4,  "Sending click action");
            }
        }catch (Exception ex){
            Log.out(4,  "Unable to locate clickable element");
        }
    }

    /**
     *  verifies element exists and is displayed on page
     * @param element HTML Element
     * @return boolean
     */
    public boolean verifyElement(WebElement element){
        return element != null && element.isDisplayed() ;
    }

    public boolean verifyElementByValue(WebElement element, String value){
        return element != null && element.getText().contains(value);
    }
}

