// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.testing.UnitTests;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.openqa.selenium.By.ByClassName;
import static org.openqa.selenium.By.ByCssSelector;
import static org.openqa.selenium.By.ById;
import static org.openqa.selenium.By.ByLinkText;
import static org.openqa.selenium.By.ByName;
import static org.openqa.selenium.By.ByPartialLinkText;
import static org.openqa.selenium.By.ByTagName;
import static org.openqa.selenium.By.ByXPath;
import static org.openqa.selenium.json.Json.MAP_TYPE;

@Category(UnitTests.class)
public class ByTest {

  @Test
  public void shouldUseFindsByNameToLocateElementsByName() {
    final AllDriver driver = mock(AllDriver.class);

    By.cssSelector("cheese").findElement(driver);
    By.cssSelector("peas").findElements(driver);

    verify(driver).findElement(By.cssSelector("cheese"));
    verify(driver).findElements(By.cssSelector("peas"));
    verifyNoMoreInteractions(driver);
  }

  @Test
  public void shouldUseXpathLocateElementsByXpath() {
    AllDriver driver = mock(AllDriver.class);

    By.xpath(".//*[@name = 'cheese']").findElement(driver);
    By.xpath(".//*[@name = 'peas']").findElements(driver);

    verify(driver).findElement(By.xpath(".//*[@name = 'cheese']"));
    verify(driver).findElements(By.xpath(".//*[@name = 'peas']"));
    verifyNoMoreInteractions(driver);
  }

  @Test
  public void searchesByTagNameIfSupported() {
    AllDriver context = mock(AllDriver.class);

    By.tagName("foo").findElement(context);
    By.tagName("bar").findElements(context);

    verify(context).findElement(By.tagName("foo"));
    verify(context).findElements(By.tagName("bar"));
    verifyNoMoreInteractions(context);
  }

  @Test
  public void innerClassesArePublicSoThatTheyCanBeReusedElsewhere() {
    assertThat(new ByXPath("a").toString()).isEqualTo("By.xpath: a");
    assertThat(new ById("a").toString()).isEqualTo("By.id: a");
    assertThat(new ByClassName("a").toString()).isEqualTo("By.className: a");
    assertThat(new ByLinkText("a").toString()).isEqualTo("By.linkText: a");
    assertThat(new ByName("a").toString()).isEqualTo("By.name: a");
    assertThat(new ByTagName("a").toString()).isEqualTo("By.tagName: a");
    assertThat(new ByCssSelector("a").toString()).isEqualTo("By.cssSelector: a");
    assertThat(new ByPartialLinkText("a").toString()).isEqualTo("By.partialLinkText: a");
  }

  // See https://github.com/SeleniumHQ/selenium-google-code-issue-archive/issues/2917
  @Test
  public void testHashCodeDoesNotFallIntoEndlessRecursion() {
    By locator = new By() {
      @Override
      public List<WebElement> findElements(SearchContext context) {
        return null;
      }
    };
    locator.hashCode();
  }

  @Test
  public void ensureMultipleClassNamesAreNotAccepted() {
    assertThatExceptionOfType(InvalidSelectorException.class).isThrownBy(() -> By.className("one two"));
  }

  @Test
  public void ensureIdIsSerializedProperly() {
    // Although it's not legal, make sure we handle the case where people use spaces.
    By by = By.id("one two");

    Json json = new Json();
    Map<String, Object> blob = json.toType(json.toJson(by), MAP_TYPE);

    assertThat(blob.get("using")).isEqualTo("css selector");
    assertThat(blob.get("value")).isEqualTo("#one\\ two");
  }

  private interface AllDriver extends SearchContext {
    // Place holder
  }

}
