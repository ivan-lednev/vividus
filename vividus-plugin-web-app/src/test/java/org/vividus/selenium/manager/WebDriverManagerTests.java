/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.selenium.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Rotatable;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.vividus.selenium.BrowserWindowSize;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.SauceLabsCapabilityType;
import org.vividus.selenium.WebDriverType;

import io.appium.java_client.MobileDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.remote.MobilePlatform;

@SuppressWarnings({ "checkstyle:MethodCount", "PMD.GodClass"})
@ExtendWith(MockitoExtension.class)
class WebDriverManagerTests
{
    private static final String DESIREDCAPABILITIES_KEY = "desired";
    private static final String BROWSER_TYPE = "browser type";
    private static final String NATIVE_APP_CONTEXT = "NATIVE_APP";
    private static final String WEBVIEW_CONTEXT = "WEBVIEW_1";

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IWebDriverManagerContext webDriverManagerContext;

    @Mock(extraInterfaces = HasCapabilities.class)
    private MobileDriver<MobileElement> mobileDriver;

    @InjectMocks
    private WebDriverManager webDriverManager;

    private WebDriver mockWebDriverHavingCapabilities()
    {
        WebDriver webDriver = mock(WebDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        when(webDriverProvider.get()).thenReturn(webDriver);
        return webDriver;
    }

    private WebDriver mockWebDriverHavingCapabilities(Map<String, Object> capabilities)
    {
        Capabilities capabilitiesMock = mock(Capabilities.class);
        capabilities.forEach((capabilityType, capabilityValue) ->
                setCapabilities(capabilitiesMock, capabilityType, capabilityValue));

        WebDriver webDriver = mockWebDriverHavingCapabilities();
        when(((HasCapabilities) webDriver).getCapabilities()).thenReturn(capabilitiesMock);
        return webDriver;
    }

    private void setCapabilities(Capabilities capabilitiesMock, String capabilityType, Object capabilityValue)
    {
        if (BROWSER_TYPE.equals(capabilityType))
        {
            when(capabilitiesMock.getBrowserName()).thenReturn((String) capabilityValue);
        }
        else
        {
            lenient().when(capabilitiesMock.getCapability(capabilityType)).thenReturn(capabilityValue);
        }
    }

    private Options mockOptions(WebDriver webDriver)
    {
        Options options = mock(Options.class);
        when(webDriver.manage()).thenReturn(options);
        return options;
    }

    private void mockMobileDriverContext(MobileDriver<?> mobileDriver, String returnContext,
            Set<String> returnContextHandles)
    {
        when(webDriverProvider.getUnwrapped(MobileDriver.class)).thenReturn(mobileDriver);
        when(mobileDriver.getContext()).thenReturn(returnContext);
        when(mobileDriver.getContextHandles()).thenReturn(returnContextHandles);
    }

    private WebDriverManager spyIsMobile(boolean returnValue)
    {
        WebDriverManager spy = Mockito.spy(webDriverManager);
        doReturn(returnValue).when(spy).isMobile();
        return spy;
    }

    @Test
    void testResizeWebDriverWithDesiredBrowserSize()
    {
        WebDriver webDriver = mockWebDriverHavingCapabilities(Map.of());
        BrowserWindowSize browserWindowSize = mock(BrowserWindowSize.class);
        Window window = mock(Window.class);
        when(mockOptions(webDriver).window()).thenReturn(window);
        Dimension dimension = mock(Dimension.class);
        when(browserWindowSize.toDimension()).thenReturn(dimension);
        webDriverManager.resize(browserWindowSize);
        verify(window).setSize(dimension);
    }

    @Test
    void testResizeWebDriverWithNullBrowserSize()
    {
        WebDriver webDriver = mockWebDriverHavingCapabilities();
        Window window = mock(Window.class);
        when(mockOptions(webDriver).window()).thenReturn(window);
        webDriverManager.resize(null);
        verify(window).maximize();
    }

    @Test
    void testResizeWebDriverWithNullBrowserSizeChrome()
    {
        WebDriverManager spy = Mockito.spy(webDriverManager);
        WebDriver webDriver = mockWebDriverHavingCapabilities();
        Window window = mock(Window.class);
        when(mockOptions(webDriver).window()).thenReturn(window);
        Dimension maxSize = new Dimension(1920, 1200);
        when(window.getSize()).thenReturn(maxSize);
        doReturn(true).when(spy).isBrowserAnyOf(BrowserType.CHROME);
        spy.resize(null);
        verify(window).maximize();
        verify(window).setSize(maxSize);
    }

    @Test
    void testResizeWebDriverWithNullBrowserSizeChromeAndroid()
    {
        WebDriverManager spy = Mockito.spy(webDriverManager);
        WebDriver webDriver = mockWebDriverHavingCapabilities();
        Window window = mock(Window.class);
        when(mockOptions(webDriver).window()).thenReturn(window);
        doReturn(true).when(spy).isBrowserAnyOf(BrowserType.CHROME);
        doReturn(true).when(spy).isAndroid();
        spy.resize(null);
        verify(window, never()).getSize();
        verify(window, never()).setSize(any(Dimension.class));
    }

    @Test
    void testResizeWebDriverDesiredBrowserSizeMobile()
    {
        WebDriver webDriver = mockWebDriverHavingCapabilities(Map.of(BROWSER_TYPE, BrowserType.ANDROID));
        BrowserWindowSize browserWindowSize = mock(BrowserWindowSize.class);
        webDriverManager.resize(browserWindowSize);
        verify(webDriver, never()).manage();
    }

    @Test
    void testGetSize()
    {
        MobileDriver<?> mobileDriver = mock(MobileDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        Set<String> contexts = new HashSet<>();
        contexts.add(WEBVIEW_CONTEXT);
        contexts.add(NATIVE_APP_CONTEXT);
        mockMobileDriverContext(mobileDriver, WEBVIEW_CONTEXT, contexts);
        lenient().when(webDriverManagerContext.getParameter(WebDriverManagerParameter.SCREEN_SIZE)).thenReturn(null);
        lenient().when(webDriverManagerContext.getParameter(WebDriverManagerParameter.ORIENTATION))
                .thenReturn(ScreenOrientation.PORTRAIT);
        WebDriverManager spy = spyIsMobile(true);
        Window window = mock(Window.class);
        when(mockOptions(mobileDriver).window()).thenReturn(window);
        when(window.getSize()).thenReturn(mock(Dimension.class));
        assertNotNull(spy.getSize());
    }

    @Test
    void testPerformActionInNativeContextNotMobile()
    {
        WebDriver webDriverConfigured = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriverConfigured);
        WebDriverManager spy = spyIsMobile(false);
        spy.performActionInNativeContext(webDriver -> assertEquals(webDriverConfigured, webDriver));
        verifyNoInteractions(webDriverConfigured);
    }

    @Test
    void testPerformActionInNativeContext()
    {
        MobileDriver<?> mobileDriver = mock(MobileDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        Set<String> contexts = new HashSet<>();
        contexts.add(WEBVIEW_CONTEXT);
        contexts.add(NATIVE_APP_CONTEXT);
        mockMobileDriverContext(mobileDriver, WEBVIEW_CONTEXT, contexts);
        WebDriverManager spy = spyIsMobile(true);
        spy.performActionInNativeContext(webDriver -> assertEquals(mobileDriver, webDriver));
        verify(mobileDriver).context(NATIVE_APP_CONTEXT);
        verify(mobileDriver).context(WEBVIEW_CONTEXT);
    }

    @Test
    void testPerformActionInNativeContextException()
    {
        MobileDriver<?> mobileDriver = mock(MobileDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        mockMobileDriverContext(mobileDriver, WEBVIEW_CONTEXT, new HashSet<>());
        WebDriverManager spy = spyIsMobile(true);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> spy.performActionInNativeContext(webDriver -> assertEquals(mobileDriver, webDriver)));
        assertEquals("MobileDriver doesn't have context: " + NATIVE_APP_CONTEXT, exception.getMessage());
    }

    @Test
    void testPerformActionInNativeContextSwitchNotNeeded()
    {
        when(webDriverProvider.getUnwrapped(MobileDriver.class)).thenReturn(mobileDriver);
        when(mobileDriver.getContext()).thenReturn(NATIVE_APP_CONTEXT);
        WebDriverManager spy = spyIsMobile(true);
        spy.performActionInNativeContext(webDriver -> assertEquals(mobileDriver, webDriver));
        verify(mobileDriver, never()).context(NATIVE_APP_CONTEXT);
        verify(mobileDriver, never()).getContextHandles();
    }

    @Test
    void testIsMobileAnyDevice()
    {
        mockWebDriverHavingCapabilities(Map.of(CapabilityType.PLATFORM_NAME, MobilePlatform.IOS));
        assertTrue(webDriverManager.isMobile());
    }

    @Test
    void testIsIOSDeviceWhenPlatformNameIsString()
    {
        mockWebDriverHavingCapabilities(Map.of(CapabilityType.PLATFORM_NAME, MobilePlatform.IOS));
        assertTrue(webDriverManager.isIOS());
    }

    @Test
    void testIsIOSDeviceWhenPlatformNameIsEnum()
    {
        mockWebDriverHavingCapabilities(Map.of(CapabilityType.PLATFORM_NAME, Platform.IOS));
        assertTrue(webDriverManager.isIOS());
    }

    @Test
    void testIsNotIOSDeviceWhenPlatformNameIsEnum()
    {
        mockWebDriverHavingCapabilities(Map.of(CapabilityType.PLATFORM_NAME, Platform.ANDROID));
        assertFalse(webDriverManager.isIOS());
    }

    @Test
    void testIsIpadIOSDevice()
    {
        Map<String, Object> capabilities = Map.of(
                CapabilityType.PLATFORM_NAME, Platform.MAC.toString(),
                BROWSER_TYPE, BrowserType.IPAD
        );
        mockWebDriverHavingCapabilities(capabilities);
        assertTrue(webDriverManager.isIOS());
    }

    @Test
    void testIsIphoneIOSDevice()
    {
        Map<String, Object> capabilities = Map.of(
                CapabilityType.PLATFORM_NAME, Platform.MAC.toString(),
                BROWSER_TYPE, BrowserType.IPHONE
        );
        mockWebDriverHavingCapabilities(capabilities);
        assertTrue(webDriverManager.isIOS());
    }

    @Test
    void testIsIOSDeviceFalse()
    {
        mockWebDriverHavingCapabilities(Map.of(CapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID));
        assertFalse(webDriverManager.isIOS());
    }

    @Test
    void testIsIOSDeviceFromInnerCapabilities()
    {
        Map<String, String> map = Map.of(SauceLabsCapabilityType.DEVICE, BrowserType.IPHONE);
        mockWebDriverHavingCapabilities(Map.of(SauceLabsCapabilityType.CAPABILITIES, map));
        assertTrue(webDriverManager.isIOS());
    }

    @Test
    void testIsIOSUsingDeviceType()
    {
        mockWebDriverHavingCapabilities(Map.of(SauceLabsCapabilityType.DEVICE, BrowserType.IPHONE));
        assertTrue(webDriverManager.isIOS());
    }

    @Test
    void testIsAndroidByPlatformName()
    {
        mockWebDriverHavingCapabilities(Map.of(CapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID));
        assertTrue(webDriverManager.isAndroid());
    }

    @Test
    void testIsMobileAndroidPlatform()
    {
        mockWebDriverHavingCapabilities(Map.of(CapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID));
        assertTrue(webDriverManager.isMobile());
    }

    @Test
    void testIsMobileAndroidBrowser()
    {
        mockWebDriverHavingCapabilities(Map.of(BROWSER_TYPE, BrowserType.ANDROID));
        assertTrue(webDriverManager.isMobile());
    }

    @Test
    void testIsMobileAndroidCapabilitiesBrowser()
    {
        mockWebDriverHavingCapabilities(Map.of(BROWSER_TYPE, BrowserType.ANDROID));
        assertTrue(webDriverManager.isAndroid());
    }

    @Test
    void testIsMobileAndroidCapabilitiesMobilePlatform()
    {
        Map<String, Object> capabilities = Map.of(
                DESIREDCAPABILITIES_KEY, Collections.emptyMap(),
                CapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID
        );
        mockWebDriverHavingCapabilities(capabilities);
        assertTrue(webDriverManager.isAndroid());
    }

    @Test
    void testIsMobileAndroidDesiredCapabilitiesMobilePlatform()
    {
        Map<String, Object> capabilities = Map.of(
                DESIREDCAPABILITIES_KEY, Map.of(CapabilityType.PLATFORM_NAME, "android"),
                CapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID
        );
        mockWebDriverHavingCapabilities(capabilities);
        assertTrue(webDriverManager.isAndroid());
    }

    @Test
    void testIsMobileAndroidIncorrectDesiredCapabilitiesMobilePlatform()
    {
        Map<String, Object> capabilities = Map.of(
                DESIREDCAPABILITIES_KEY, "DesiredAndroid",
                CapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID
        );
        mockWebDriverHavingCapabilities(capabilities);
        assertTrue(webDriverManager.isAndroid());
    }

    @Test
    void testIsAndroidFalse()
    {
        mockWebDriverHavingCapabilities(Map.of(BROWSER_TYPE, BrowserType.CHROME));
        assertFalse(webDriverManager.isAndroid());
    }

    @Test
    void testIsMobileFalse()
    {
        Map<String, Object> capabilities = Map.of(
                CapabilityType.PLATFORM_NAME, Platform.LINUX.toString(),
                BROWSER_TYPE, BrowserType.CHROME
        );
        mockWebDriverHavingCapabilities(capabilities);
        assertFalse(webDriverManager.isMobile());
    }

    // CHECKSTYLE:OFF
    static Stream<Arguments> webDriverTypeChecks()
    {
        return Stream.of(
            Arguments.of(BrowserType.FIREFOX,         WebDriverType.FIREFOX,      true   ),
            Arguments.of(BrowserType.FIREFOX_CHROME,  WebDriverType.FIREFOX,      false  ),
            Arguments.of(BrowserType.CHROME,          WebDriverType.CHROME,       true   ),
            Arguments.of(BrowserType.FIREFOX_CHROME,  WebDriverType.CHROME,       false  ),
            Arguments.of(BrowserType.EDGE,            WebDriverType.EDGE,         true   ),
            Arguments.of(BrowserType.IE,              WebDriverType.EDGE,         false  ),
            Arguments.of(BrowserType.IEXPLORE,        WebDriverType.IEXPLORE,     true   ),
            Arguments.of(BrowserType.IE_HTA,          WebDriverType.IEXPLORE,     false  ),
            Arguments.of(BrowserType.SAFARI,          WebDriverType.SAFARI,       true   ),
            Arguments.of(BrowserType.SAFARI_PROXY,    WebDriverType.SAFARI,       true   ),
            Arguments.of(BrowserType.IPHONE,          WebDriverType.SAFARI,       false  )
        );
    }

    static Stream<Arguments> webDriverTypeDetections()
    {
        return Stream.of(
            Arguments.of(BrowserType.FIREFOX,         WebDriverType.FIREFOX   ),
            Arguments.of(BrowserType.FIREFOX_CHROME,  null                    ),
            Arguments.of(BrowserType.CHROME,          WebDriverType.CHROME    ),
            Arguments.of(BrowserType.FIREFOX_CHROME,  null                    ),
            Arguments.of(BrowserType.EDGE,            WebDriverType.EDGE      ),
            Arguments.of(BrowserType.IE,              WebDriverType.IEXPLORE  ),
            Arguments.of(BrowserType.IEXPLORE,        WebDriverType.IEXPLORE  ),
            Arguments.of(BrowserType.IE_HTA,          null                    ),
            Arguments.of(BrowserType.SAFARI,          WebDriverType.SAFARI    ),
            Arguments.of(BrowserType.SAFARI_PROXY,    WebDriverType.SAFARI    ),
            Arguments.of(BrowserType.IPHONE,          null                    )
        );
    }
    // CHECKSTYLE:ON

    @ParameterizedTest
    @MethodSource("webDriverTypeChecks")
    void testIsTypeAnyOf(String browserName, WebDriverType webDriverType, boolean result)
    {
        mockWebDriverHavingCapabilities(Map.of(BROWSER_TYPE, browserName));
        assertEquals(result, webDriverManager.isTypeAnyOf(webDriverType));
    }

    @ParameterizedTest
    @MethodSource("webDriverTypeDetections")
    void testDetectType(String browserName, WebDriverType webDriverType)
    {
        mockWebDriverHavingCapabilities(Map.of(BROWSER_TYPE, browserName));
        assertEquals(webDriverType, webDriverManager.detectType());
    }

    @Test
    void testIsBrowser()
    {
        String browserName = "chrome";
        mockWebDriverHavingCapabilities(Map.of(BROWSER_TYPE, browserName));
        assertTrue(webDriverManager.isBrowserAnyOf(BrowserType.CHROME));
    }

    @Test
    void testIsBrowserFalse()
    {
        String browserName = "browser";
        mockWebDriverHavingCapabilities(Map.of(BROWSER_TYPE, browserName));
        assertFalse(webDriverManager.isBrowserAnyOf(BrowserType.CHROME));
    }

    @Test
    void shouldBeAndroidNativeApp()
    {
        webDriverManager.setNativeApp(true);
        mockWebDriverHavingCapabilities(Map.of(CapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID));
        assertTrue(webDriverManager.isAndroidNativeApp());
    }

    @Test
    void shouldNotBeAndroidNativeApp()
    {
        webDriverManager.setNativeApp(false);
        assertFalse(webDriverManager.isAndroidNativeApp());
    }

    @Test
    void shouldNotBeAndroidNativeAppAsItIsIOS()
    {
        webDriverManager.setNativeApp(true);
        mockWebDriverHavingCapabilities(Map.of(CapabilityType.PLATFORM_NAME, MobilePlatform.IOS));
        assertFalse(webDriverManager.isAndroidNativeApp());
    }

    @Test
    void shouldBeIOSNativeApp()
    {
        webDriverManager.setNativeApp(true);
        mockWebDriverHavingCapabilities(Map.of(CapabilityType.PLATFORM_NAME, MobilePlatform.IOS));
        assertTrue(webDriverManager.isIOSNativeApp());
    }

    @Test
    void shouldNotBeIOSNativeApp()
    {
        webDriverManager.setNativeApp(false);
        assertFalse(webDriverManager.isIOSNativeApp());
    }

    @Test
    void shouldNotBeIOSNativeAppAsItIsAndroid()
    {
        webDriverManager.setNativeApp(true);
        mockWebDriverHavingCapabilities(Map.of(CapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID));
        assertFalse(webDriverManager.isIOSNativeApp());
    }

    @ParameterizedTest
    @EnumSource(ScreenOrientation.class)
    void shouldNotDetectOrientationForDesktop(ScreenOrientation orientation)
    {
        mockWebDriverHavingCapabilities(Map.of(CapabilityType.PLATFORM_NAME, Platform.WIN10.toString()));
        assertFalse(webDriverManager.isOrientation(orientation));
    }

    static Stream<Arguments> orientationProvider()
    {
        return Stream.of(
            Arguments.of(ScreenOrientation.LANDSCAPE, ScreenOrientation.PORTRAIT,  MobilePlatform.ANDROID),
            Arguments.of(ScreenOrientation.PORTRAIT,  ScreenOrientation.PORTRAIT,  MobilePlatform.ANDROID),
            Arguments.of(ScreenOrientation.LANDSCAPE, ScreenOrientation.PORTRAIT,  MobilePlatform.IOS),
            Arguments.of(ScreenOrientation.PORTRAIT,  ScreenOrientation.PORTRAIT,  MobilePlatform.IOS),
            Arguments.of(ScreenOrientation.LANDSCAPE, ScreenOrientation.LANDSCAPE, MobilePlatform.ANDROID),
            Arguments.of(ScreenOrientation.PORTRAIT,  ScreenOrientation.LANDSCAPE, MobilePlatform.ANDROID),
            Arguments.of(ScreenOrientation.LANDSCAPE, ScreenOrientation.LANDSCAPE, MobilePlatform.IOS),
            Arguments.of(ScreenOrientation.PORTRAIT,  ScreenOrientation.LANDSCAPE, MobilePlatform.IOS)
        );
    }

    @ParameterizedTest
    @MethodSource("orientationProvider")
    void testIsOrientation(ScreenOrientation actualOrientation, ScreenOrientation orientationToCheck, String platform)
    {
        when(webDriverProvider.getUnwrapped(Rotatable.class)).thenReturn(mobileDriver);
        when(webDriverManagerContext.getParameter(WebDriverManagerParameter.ORIENTATION)).thenReturn(null);
        when(mobileDriver.getOrientation()).thenReturn(actualOrientation);
        mockWebDriverHavingCapabilities(Map.of(CapabilityType.PLATFORM_NAME, platform));
        assertEquals(actualOrientation == orientationToCheck, webDriverManager.isOrientation(orientationToCheck));
        verify(webDriverManagerContext).putParameter(WebDriverManagerParameter.ORIENTATION, actualOrientation);
    }

    @Test
    void testOrientationCached()
    {
        mockWebDriverHavingCapabilities(Map.of(CapabilityType.PLATFORM_NAME, MobilePlatform.IOS));
        when(webDriverManagerContext.getParameter(WebDriverManagerParameter.ORIENTATION))
                .thenReturn(ScreenOrientation.PORTRAIT);
        assertTrue(webDriverManager.isOrientation(ScreenOrientation.PORTRAIT));
        verifyNoInteractions(mobileDriver);
    }

    static Stream<Arguments> nativeApplicationViewportProvider()
    {
        return Stream.of(
            Arguments.of(ScreenOrientation.LANDSCAPE, new Dimension(375, 667), new Dimension(667, 375)),
            Arguments.of(ScreenOrientation.PORTRAIT,  new Dimension(375, 667), new Dimension(375, 667))
        );
    }

    @ParameterizedTest
    @MethodSource("nativeApplicationViewportProvider")
    void testGetScreenSizeForPortraitOrientation(ScreenOrientation orientation, Dimension dimension,
            Dimension viewport)
    {
        lenient().when(webDriverProvider.getUnwrapped(MobileDriver.class)).thenReturn(mobileDriver);
        lenient().when(webDriverProvider.getUnwrapped(Rotatable.class)).thenReturn(mobileDriver);
        when(mobileDriver.getOrientation()).thenReturn(orientation);
        when(mobileDriver.getContext()).thenReturn(NATIVE_APP_CONTEXT);
        WebDriverManager spy = spyIsMobile(true);
        Options options = mock(Options.class);
        when(mobileDriver.manage()).thenReturn(options);
        Window window = mock(Window.class);
        when(options.window()).thenReturn(window);
        when(window.getSize()).thenReturn(dimension);
        assertEquals(viewport, spy.getSize());
        verify(webDriverManagerContext).putParameter(WebDriverManagerParameter.SCREEN_SIZE, dimension);
    }

    @Test
    void testGetNativeApplicationViewportCached()
    {
        mockWebDriverHavingCapabilities(Map.of(CapabilityType.PLATFORM_NAME, MobilePlatform.IOS));
        when(webDriverProvider.getUnwrapped(Rotatable.class)).thenReturn(mobileDriver);
        lenient().when(webDriverManagerContext.getParameter(WebDriverManagerParameter.ORIENTATION)).thenReturn(null);
        when(mobileDriver.getOrientation()).thenReturn(ScreenOrientation.PORTRAIT);
        Dimension dimension = new Dimension(375, 667);
        lenient().when(webDriverManagerContext.getParameter(WebDriverManagerParameter.SCREEN_SIZE))
                .thenReturn(dimension);
        Dimension actualDimension = webDriverManager.getSize();
        assertEquals(dimension.getHeight(), actualDimension.getHeight());
        assertEquals(dimension.getWidth(), actualDimension.getWidth());
        verify(this.mobileDriver, never()).manage();
    }
}
