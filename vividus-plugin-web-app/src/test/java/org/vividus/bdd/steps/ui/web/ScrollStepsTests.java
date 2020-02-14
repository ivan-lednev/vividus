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

package org.vividus.bdd.steps.ui.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.IJavascriptActions;
import org.vividus.ui.web.context.IWebUiContext;

@ExtendWith(MockitoExtension.class)
class ScrollStepsTests
{
    @Mock
    private IWebUiContext webUiContext;
    @Mock
    private IJavascriptActions javascriptActions;

    @InjectMocks
    private ScrollSteps scrollSteps;

    @Test
    void shouldScrollContextInDownDirectionWhenContextIsPage()
    {
        WebDriver driver = mock(WebDriver.class);
        when(webUiContext.getSearchContext()).thenReturn(driver);
        scrollSteps.scrollContextIn(ScrollDirection.BOTTOM);
        verify(javascriptActions).scrollToEndOfPage();
    }

    @Test
    void shouldScrollContextInDownDirectionWhenContextIsElement()
    {
        WebElement webElement = mock(WebElement.class);
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        scrollSteps.scrollContextIn(ScrollDirection.BOTTOM);
        verify(javascriptActions).scrollToEndOf(webElement);
    }

    @Test
    void shouldScrollContextInUpDirectionWhenContextIsPage()
    {
        WebDriver driver = mock(WebDriver.class);
        when(webUiContext.getSearchContext()).thenReturn(driver);
        scrollSteps.scrollContextIn(ScrollDirection.TOP);
        verify(javascriptActions).scrollToStartOfPage();
    }

    @Test
    void shouldScrollContextInUpDirectionWhenContextIsElement()
    {
        WebElement webElement = mock(WebElement.class);
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        scrollSteps.scrollContextIn(ScrollDirection.TOP);
        verify(javascriptActions).scrollToStartOf(webElement);
    }

    @Test
    void shouldScrollContextInLeftDirectionWhenContextIsElement()
    {
        WebElement webElement = mock(WebElement.class);
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        scrollSteps.scrollContextIn(ScrollDirection.LEFT);
        verify(javascriptActions).scrollToLeftOf(webElement);
    }

    @Test
    void shouldScrollContextInRightDirectionWhenContextIsElement()
    {
        WebElement webElement = mock(WebElement.class);
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        scrollSteps.scrollContextIn(ScrollDirection.RIGHT);
        verify(javascriptActions).scrollToRightOf(webElement);
    }

    @Test
    void shouldThorowExceptionForScrollContextInLeftDirectionWhenContextIsPage()
    {
        WebDriver driver = mock(WebDriver.class);
        when(webUiContext.getSearchContext()).thenReturn(driver);
        verifyUnsupportedScroll(() -> scrollSteps.scrollContextIn(ScrollDirection.LEFT));
    }

    @Test
    void shouldThorowExceptionForScrollContextInRightDirectionWhenContextIsPage()
    {
        WebDriver driver = mock(WebDriver.class);
        when(webUiContext.getSearchContext()).thenReturn(driver);
        verifyUnsupportedScroll(() -> scrollSteps.scrollContextIn(ScrollDirection.RIGHT));
    }

    @Test
    void testScrollToTheEndOfThePage()
    {
        scrollSteps.scrollToTheEndOfThePage();
        verify(javascriptActions).scrollToEndOfPage();
    }

    @Test
    void testScrollToTheStartOfThePage()
    {
        scrollSteps.scrollToTheStartOfThePage();
        verify(javascriptActions).scrollToStartOfPage();
    }

    private void verifyUnsupportedScroll(Executable toTest)
    {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, toTest);
        assertEquals("Horizontal scroll of the page not supported", exception.getMessage());
    }
}