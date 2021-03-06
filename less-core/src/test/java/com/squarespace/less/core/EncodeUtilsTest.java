/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.less.core;

import static com.squarespace.less.core.EncodeUtils.encodeURI;
import static com.squarespace.less.core.EncodeUtils.encodeURIComponent;
import static com.squarespace.less.core.EncodeUtils.escape;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;


public class EncodeUtilsTest {

  private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";

  private static final String ALPHANUM = LOWERCASE + LOWERCASE.toUpperCase() + "0123456789";

  @Test
  public void testEncodeURI() {

    // Alphanumerics
    assertEquals(encodeURI(ALPHANUM), ALPHANUM);

    // Whitelist characters
    assertEquals(encodeURI("!#$&'()*+,-./:;=?@_~"), "!#$&'()*+,-./:;=?@_~");

    // Single character escapes
    assertEquals(encodeURI("http://foo.com/"), "http://foo.com/");
    assertEquals(encodeURI(" . . ."), "%20.%20.%20.");
    assertEquals(encodeURI("%abc"), "%25abc");
    assertEquals(encodeURI("\u2018.\u2019.\u201a.\u201b"), "%e2%80%98.%e2%80%99.%e2%80%9a.%e2%80%9b");

    // Unicode surrogate pair escapes.
    assertEquals(encodeURI("\ud800\udc00"), "%f0%90%80%80");
    assertEquals(encodeURI("\ud900\udc01"), "%f1%90%80%81");
  }

  @Test
  public void testEncodeURIComponent() {

    // Alphanumerics
    assertEquals(encodeURI(ALPHANUM), ALPHANUM);

    // Whitelist characters
    assertEquals(encodeURIComponent("!'()*-._~"), "!'()*-._~");

    // encodeURI whitelist characters
    assertEquals(encodeURIComponent("#$&+,/:;?@"), "%23%24%26%2b%2c%2f%3a%3b%3f%40");
  }

  @Test
  public void testEscape() {

    // This function escapes some characters that encodeURI allows through
    assertEquals(escape("#():;="), "%23%28%29%3a%3b%3d");
  }

}
