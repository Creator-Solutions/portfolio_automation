package org.example.Testcase;

import org.apache.poi.hssf.record.Record;
import org.openqa.selenium.WebDriver;

public abstract class EventInterface {

    public WebDriver driver;
    public EventInterface(WebDriver driver) {
        this.driver = driver;
    }

    public abstract boolean test(String folderName);

    public boolean testTab(Record record) {
        return true;
    }

}
