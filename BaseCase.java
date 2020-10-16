package com.lemon.cases;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

import com.alibaba.fastjson.JSONObject;
import com.lemon.page.Page;
import com.lemon.page.UIElement;

public class BaseCase {
	//定义页面url常量，方便维护
	public static final String LOGIN_PAGE = "http://test.lemonban.com/lmcanon_web_auto/mng/login.html";
	public static final String REGISTER_PAGE = "http://test.lemonban.com/lmcanon_web_auto/mng/register.html";
	public static final String HOME_PAGE = "http://test.lemonban.com/lmcanon_web_auto/mng/index.html";
	public static List<Page> pages = new ArrayList<Page>();
	
	public static WebDriver driver = null;
	
	public static Logger log = Logger.getLogger(BaseCase.class);
	
	@BeforeSuite
	@Parameters("driverType")
	public void init(String driverType) throws IOException {
		log.info("=======================套件开始========================");
		//选择浏览器驱动
		chooseDriver(driverType);
		//加载OP对象
		loadPages();
	}

	public void chooseDriver(String driverType) {
		log.info("=======================选择浏览器驱动========================");
		if("chrome".equals(driverType)) {
			// 设置浏览器驱动位置
			System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver.exe");
			// 创建driver对象
			driver = new ChromeDriver();
		}else if("ie".equals(driverType)) {
			//设置浏览器驱动位置
			System.setProperty("webdriver.ie.driver", "src/test/resources/IEDriverServer.exe");
			//创建Driver对象
			DesiredCapabilities capabilities = new DesiredCapabilities();
			//忽略缩放大小比例
			capabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
			//忽略保护模式
			capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			driver = new InternetExplorerDriver(capabilities);
		}else if("firefox".equals(driverType)) {
			//设置浏览器驱动位置
			System.setProperty("webdriver.gecko.driver","src/test/resources/geckodriver.exe");
			//创建driver对象
			driver = new FirefoxDriver();
		}
	}
	
	public static void loadPages() throws IOException {
		log.info("=======================加载UI库========================");
		String json1 = FileUtils.readFileToString(new File("src/test/resources/UILibrary.json"), "UTF-8");
		pages = JSONObject.parseArray(json1,Page.class);
	}
	
	public static WebElement getElement(String pageName,String elementName) {
		WebElement findElement = null;
		//从page中获取所有页面元素
		for (Page page : pages) {
			//是同一个页面
			if(pageName.equals(page.getPageName())) {
				List<UIElement> uiElements = page.getUiElements();
				for (UIElement element : uiElements) {
					//找到指定的元素
					if(elementName.equals(element.getName())) {
						String methodName = element.getBy();
						String value = element.getValue();
						//反射处理定位方法
						//反射第一步：获取字节码对象
						try {
							Method method = By.class.getMethod(methodName, String.class);
							//因为是静态方法，不需要对象直接使用类名即可调用。
							By by = (By)method.invoke(null, value);
							//加显示等待
							WebDriverWait wait = new WebDriverWait(driver,3);
							findElement = wait.until(ExpectedConditions.
									visibilityOfElementLocated(by));
						} catch (Exception e) {
							log.info(e.toString());
//							e.printStackTrace();
						}
						return findElement;
					}
				}
			}
		}
		return null;
	}
	
	@AfterSuite
	public void teardwon() throws Exception {
		//main睡眠3秒钟
		Thread.sleep(3000);
		//退出浏览器驱动
		driver.quit();
		log.info("=======================套件结束========================");
	}
	
	public void open(String url) {
		driver.get(url);
	}
	
	public void input(String pageName,String elementName,String content) {
		WebElement element = getElement(pageName, elementName);
		if(element != null) {
			element.sendKeys(content);
		}else {
//			System.out.println("元素定位失败【" + pageName+"页面】【"+elementName+"】");
			log.info("元素定位失败【" + pageName+"页面】【"+elementName+"】");
		}
	}
	
	public void click(String pageName,String elementName) {
		try {
			WebElement element = getElement(pageName, elementName);
			if(element != null) {
				//把鼠标移动到元素上
				Actions actions = new Actions(driver);
				actions.moveToElement(element).perform();
				element.click();
			}else {
//			System.out.println("元素定位失败【"+pageName+"页面】【"+elementName+"】");
				log.info("元素定位失败【" + pageName+"页面】【"+elementName+"】");
			}
		} catch (Exception e) {
			log.info(e.toString());
			e.printStackTrace();
		}
	}
	 
	public String getText(String pageName,String elementName) {
		WebElement element = getElement(pageName, elementName);
		if(element != null) {
			return element.getText();
		}else {
//			System.out.println("元素定位失败【"+pageName+"页面】【"+elementName+"】");
			log.info("元素定位失败【" + pageName+"页面】【"+elementName+"】");
			return null;
		}
	}
	
	public void assertActualExpected(String actual,String expected) {
		Assert.assertEquals(actual, expected);
	}
	
	public void assertURl(String page) {
		WebDriverWait wait = new WebDriverWait(driver, 5);
		Boolean flag = wait.until(ExpectedConditions.urlContains(page));
		Assert.assertTrue(flag);
	}
	
	public void swtichToFrame(String pageName,String elementName) {
		WebElement element = getElement(pageName, elementName);
		if(element != null) {
			driver.switchTo().frame(element);
		}else {
//			System.out.println("元素定位失败【"+pageName+"页面】【"+elementName+"】");
			log.info("元素定位失败【" + pageName+"页面】【"+elementName+"】");
		}
//		WebDriverWait wait = new WebDriverWait(driver,5);
//		//页面加载完毕之后，在进行下面的操作
//		Object until = wait.until(ExpectedConditions.jsReturnsValue("return document.readyState == 'complete'"));
//		System.out.println(until + "====================");
	}
	
	public void selectByText(String pageName,String elementName,String text) {
		WebElement element = getElement(pageName, elementName);
		if(element != null) {
			Select select = new Select(element);
			if(StringUtils.isNotBlank(text)) {
				select.selectByVisibleText(text);
			}
		}else {
//			System.out.println("元素定位失败【"+pageName+"页面】【"+elementName+"】");
			log.info("元素定位失败【" + pageName+"页面】【"+elementName+"】");
		}
	}
	
	public void jsWaitClick(String pageName,String elementName) {
//		WebDriverWait wait = new WebDriverWait(driver,5);
//		//页面加载完毕之后，在进行下面的操作
//		wait.until(ExpectedConditions.jsReturnsValue("return document.readyState == 'complete'"));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		WebElement element = getElement(pageName, elementName);
		if(element != null) {
			//把鼠标移动到元素上
			Actions actions = new Actions(driver);
			actions.moveToElement(element).perform();
			element.click();
		}else {
//			System.out.println("元素定位失败【"+pageName+"页面】【"+elementName+"】");
			log.info("元素定位失败【" + pageName+"页面】【"+elementName+"】");
		}
	}
	
	public void assertContains(String className,String expected) {
		WebDriverWait wait = new WebDriverWait(driver,5);
		WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(className)));
		String actual = element.getText();
		//如果实际值包含期望值，就断言成功
		boolean flag = actual.contains(expected);
		Assert.assertTrue(flag);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

}
