package com.github.invictum.panels;

import com.github.invictum.unified.data.provider.UnifiedDataProvider;
import com.github.invictum.unified.data.provider.UnifiedDataProviderFactory;
import com.github.invictum.unified.data.provider.UnifiedDataProviderUtil;
import net.serenitybdd.core.SerenitySystemProperties;
import net.serenitybdd.core.pages.WebElementFacade;
import net.serenitybdd.core.pages.WebElementFacadeImpl;
import net.thucydides.core.ThucydidesSystemProperty;
import net.thucydides.core.webdriver.ThucydidesWebDriverSupport;
import com.github.invictum.pages.AbstractPage;
import com.github.invictum.tricks.Visibility;
import com.github.invictum.tricks.Wait;
import com.github.invictum.tricks.core.AbstractTrick;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.List;

public class AbstractPanel {

    public static final String PANEL_LOCATOR_PREFIX = ".";
    public static final int timeout = SerenitySystemProperties.getProperties()
            .getIntegerValue(ThucydidesSystemProperty.WEBDRIVER_WAIT_FOR_TIMEOUT, 10000);
    protected WebElementFacade panel;
    private UnifiedDataProvider dataProvider;
    private AbstractPage parentPage;

    public AbstractPanel() {
        dataProvider = UnifiedDataProviderFactory.getInstance(this);
        final WebDriver driver = ThucydidesWebDriverSupport.getDriver();
        panel = WebElementFacadeImpl
                .wrapWebElement(driver, driver.findElement(By.xpath(dataProvider.getBase())), timeout);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public void initWith(final AbstractPage parentPage) {
        this.parentPage = parentPage;
        smartWait();
    }

    protected <T extends AbstractTrick> T getTrick(Class<T> trickClass) {
        return parentPage.getTrick(trickClass);
    }

    public WebElementFacade findBy(final String elementLocator) {
        return panel.findBy(elementLocator);
    }

    public List<WebElementFacade> findAll(final String elementLocator) {
        return panel.thenFindAll(elementLocator);
    }

    public WebDriver getDriver() {
        return parentPage.getDriver();
    }

    public Alert getAlert() {
        return parentPage.getAlert();
    }

    public void evaluateJavascript(final String scriptToExecute) {
        parentPage.evaluateJavascript(scriptToExecute);
    }

    protected String locator(final String locatorKey) {
        return locator(locatorKey, false);
    }

    private String locator(final String locatorKey, final boolean stripLocator) {
        String base = UnifiedDataProviderUtil.getLocatorByKey(locatorKey, dataProvider);
        if (!stripLocator) {
            base = PANEL_LOCATOR_PREFIX + base;
        }
        return base;
    }

    public WebElementFacade locate(final String locatorKey) {
        return findBy(locator(locatorKey));
    }

    public List<WebElementFacade> locateAll(final String locatorKey) {
        return findAll(locator(locatorKey));
    }

    protected String data(final String dataKey) {
        return UnifiedDataProviderUtil.getDataByKey(dataKey, dataProvider);
    }

    protected void waitABit(long milliseconds) {
        parentPage.waitABit(milliseconds);
    }

    protected void smartWait() {
        getTrick(Wait.class).waitForJquery(this);
    }

    public Actions withAction() {
        return parentPage.withAction();
    }

    public boolean isVisible(String locatorKey) {
        String fullLocator = dataProvider.getBase() + locator(locatorKey, true);
        return getTrick(Visibility.class).isElementVisible(fullLocator, panel);
    }

    public boolean isVisible(WebElementFacade element, String locatorKey) {
        String fullLocator = dataProvider.getBase() + locator(locatorKey, true);
        return getTrick(Visibility.class).isElementVisible(fullLocator, element);
    }

    public void sendKeys(CharSequence... charSequences) {
        panel.sendKeys(charSequences);
    }

    public WebElementFacade asWebElement() {
        return panel;
    }
}