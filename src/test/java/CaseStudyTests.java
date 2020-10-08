package test.java;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaseStudyTests {
    private final String xeUrl = "https://www.xe.gr/property/";
    private final String driverPath = "./geckodriver.exe";
    private FirefoxDriver driver ;


    @Test(priority=1)
    public void testIfPricesAreaAreWithinSelectedRange() throws InterruptedException {
        SoftAssert softAssert = new SoftAssert();

        launchBrowser();
        initializeSearch();
        applyFilters();

        while(true) {
            List<WebElement> articleList = getArticleList();
            int size = articleList.size();

            for (int i =0; i<size;i++) {
                List<WebElement> articleListInLoop = getArticleList();

                WebElement innerDiv = articleListInLoop.get(i).findElement(By.tagName("h1"));
                String[] areaAndPriceSplit = innerDiv.getText().split("\\|");
                int areaExtracted = Integer.parseInt(extractDigitsFromString(areaAndPriceSplit[0]));
                int priceExtracted = Integer.parseInt(extractDigitsFromString(areaAndPriceSplit[1]));

                softAssert.assertTrue(priceExtracted >= 200 && priceExtracted <= 700,"Price is not within 200 and 700 euros");
                softAssert.assertTrue(areaExtracted >=75 && areaExtracted <= 150,"Area is not within 75 and 150 square meters");

                Thread.sleep(1000);
            }

            ((JavascriptExecutor) driver)
                    .executeScript("window.scrollTo(0, document.body.scrollHeight)");

            Thread.sleep(2000);

            if (!nextPageArrowExists()) break;
        }
        driver.close();
    }

    @Test(priority=2)
    public void testImageCarouselNotOverloaded() throws InterruptedException{
        SoftAssert softAssert = new SoftAssert();

        launchBrowser();
        initializeSearch();
        applyFilters();

        while(true) {
            List<WebElement> articleList = getArticleList();
            int size = articleList.size();

            for (int i =0; i<size;i++) {
                List<WebElement> articleListInLoop = getArticleList();

                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", articleListInLoop.get(i));

                int imageCounter = 0;
                while(true) {
                    try {
                        WebElement carouselRightArrow = articleListInLoop.get(i).findElement(By.className("xeIcons-pagination_arrow_right"));
                        if(carouselRightArrow.getAttribute("class").contains("slick-disabled")) break;
                        carouselRightArrow.click();
                        imageCounter++;
                    } catch (org.openqa.selenium.NoSuchElementException e){
                        break;
                    }
                }
                softAssert.assertTrue(imageCounter <= 10,"Images in Carousel Are Over 10");
                Thread.sleep(1000);
            }

            ((JavascriptExecutor) driver)
                    .executeScript("window.scrollTo(0, document.body.scrollHeight)");

            Thread.sleep(2000);
            if (!nextPageArrowExists()) break;
        }
        driver.close();
    }

    @Test(priority=3)
    public void testSortingIsCorrectWhenSortDesc() throws InterruptedException{
        SoftAssert softAssert = new SoftAssert();

        launchBrowser();
        initializeSearch();
        applyFilters();
        applyDescSorting();

        List<Integer> priceList = new ArrayList<>();

        while(true) {
            List<WebElement> articleList = getArticleList();
            int size = articleList.size();

            for (int i =0; i<size;i++) {
                List<WebElement> articleListInLoop = getArticleList();

                //check the prices and area
                WebElement innerDiv = articleListInLoop.get(i).findElement(By.tagName("h1"));
                String[] areaAndPriceSplit = innerDiv.getText().split("\\|");
                Integer priceExtracted = Integer.parseInt(extractDigitsFromString(areaAndPriceSplit[1]));
                priceList.add(priceExtracted);

                Thread.sleep(1000);
            }

            ((JavascriptExecutor) driver)
                    .executeScript("window.scrollTo(0, document.body.scrollHeight)");

            Thread.sleep(2000);

            if (!nextPageArrowExists()){
                softAssert.assertTrue(isSorted(priceList),"Prices Are Sorted Desc When Desc Filters Are Applied");
                break;
            }
        }
        driver.close();
    }

    @Test(priority=4)
    public void testContactPhoneNotVisible() throws InterruptedException{
        SoftAssert softAssert = new SoftAssert();

        launchBrowser();
        initializeSearch();
        applyFilters();

        while(true) {
            List<WebElement> articleList = getArticleList();
            int size = articleList.size();

            for (int i =0; i<size;i++) {
                List<WebElement> articleListInLoop = getArticleList();

                articleListInLoop.get(i).click();

                WebElement showPhoneButton = driver.findElementById("to-form");
                showPhoneButton.getText();
                softAssert.assertTrue(!showPhoneButton.getText().matches("[0-9]"),"Contact phone button contains digits, probably a phone!");
                softAssert.assertTrue(showPhoneButton.getText().equalsIgnoreCase("Προβολή Τηλεφώνου"),"Contact phone button label is not correct");
                showPhoneButton.click();

                WebElement hiddenPhone = driver.findElementByXPath("//div[@class='phone-area']/div[@class='hidden-info']/span[@class='tel']/a");
                Pattern pattern = Pattern.compile("^(\\d{3}[- .]?){2}\\d{4}$");
                Matcher matcher = pattern.matcher(hiddenPhone.getText());
                softAssert.assertTrue(matcher.matches(),"Hidden phone does not contain a valid phone number");

                driver.navigate().back();
            }

            ((JavascriptExecutor) driver)
                    .executeScript("window.scrollTo(0, document.body.scrollHeight)");

            Thread.sleep(2000);

            if (!nextPageArrowExists()) break;

        }
        driver.close();
    }


    private void initializeSearch() {
        WebElement selectionArrow = driver.findElementByClassName("select2-selection__arrow");
        selectionArrow.click();

        WebElement rentalSelection = driver.findElementByXPath("//li[contains(@id,'117541')]");
        rentalSelection.click();

        WebElement areaTextField = driver.findElementById("imf_locations");
        areaTextField.click();
        WebElement areaSelection = driver.findElementById("imf_locations_input");
        areaSelection.sendKeys("Παγκράτι");
        WebElement areaList = driver.findElementByXPath("//*[@class='autocomplete_box']/ul");

        List<WebElement> allListElements = areaList.findElements(By.tagName("li"));
        for(WebElement elem : allListElements) {
            WebElement innerDiv = elem.findElement(By.className("add_to_search"));
            innerDiv.click();
        }
        WebElement searchButton = driver.findElementById("submit_search");
        searchButton.click();
    }

    public void launchBrowser() {
        System.out.println("launching firefox browser");
        System.setProperty("webdriver.gecko.driver", driverPath);
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get(xeUrl);
    }


    private void applyDescSorting() {
        WebElement sortingLink = driver.findElementByXPath("//a[@id='r_sorting_link']/span");
        sortingLink.click();
        WebElement descPriceSort = driver.findElementByXPath("//div[@class='select-sort']/div[@id='sorting_selection']/ul/li[2]/a");
        descPriceSort.click();
    }

    private void applyFilters() {
        WebElement advancedSearchFiltersContainer = driver.findElementById("advanced_filters_container");
        advancedSearchFiltersContainer.click();

        WebElement priceRangeContainer = driver.findElementById("price_range_container");
        priceRangeContainer.click();

        WebElement priceFrom = driver.findElementById("priceFrom");
        priceFrom.click();
        priceFrom.sendKeys("200");
        WebElement priceTo = driver.findElementById("priceTo");
        priceTo.click();
        priceTo.sendKeys("700");

        WebElement closeButton = driver.findElementByClassName("closeButton");
        closeButton.click();

        WebElement areaRangeContainer = driver.findElementById("area_range_container");
        areaRangeContainer.click();

        WebElement areaFrom = driver.findElementById("areaFrom");
        areaFrom.click();
        areaFrom.sendKeys("75");
        WebElement areaTo = driver.findElementById("areaTo");
        areaTo.click();
        areaTo.sendKeys("150");

        WebElement repeatSearch = driver.findElementById("submit_search");
        repeatSearch.click();
    }

    private List<WebElement> getArticleList() {
        WebElement resultItems = driver.findElementByXPath("//*[@class='resultItems']");
        return resultItems.findElements(By.tagName("article"));
    }

    private boolean nextPageArrowExists() {
        try {
            WebElement pagesRightArrow = driver.findElementByXPath("//a[@class='page_btn']/span[@class='xeIcons-pagination_arrow_right']");
            pagesRightArrow.click();
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    public static String extractDigitsFromString(String value) {
        return value.trim().replaceAll("[^\\d]", "");
    }

    private static <T extends Comparable<? super T>> boolean isSorted(List<T> list){
        for (int i = 0; i < list.size()-1; i++) {
            if(list.get(i).compareTo(list.get(i+1))> 0){
                return false;
            }
        }
        return true;
    }
}


