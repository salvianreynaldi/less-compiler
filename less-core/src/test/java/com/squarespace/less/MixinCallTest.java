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

package com.squarespace.less;

import static com.squarespace.less.model.CombinatorType.CHILD;
import static com.squarespace.less.model.Units.PX;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.MixinCallArgs;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Selector;
import com.squarespace.less.parse.Parselets;


public class MixinCallTest extends LessTestBase {

  @Test
  public void testEquals() {
    Selector selXY = selector(element(".x"), element(".y"));
    Selector selXZ = selector(element(".x"), element(".z"));
    MixinCallArgs args = args(';', arg("@a", anon("b")));

    assertEquals(mixincall(selXY), mixincall(selXY));
    assertEquals(mixincall(selXY, args), mixincall(selXY, args));
    assertEquals(mixincall(selXY, args, true), mixincall(selXY, args, true));

    assertNotEquals(mixincall(selXY), null);
    assertNotEquals(mixincall(selXY), mixincall(selXZ));
    assertNotEquals(mixincall(selXY), mixincall(selXY, args));
    assertNotEquals(mixincall(selXY, args), mixincall(selXY, args, true));
  }

  @Test
  public void testModelReprSafety() {
    mixincall(selector(element(".x")), args(';', arg("@a", anon("b"))), true).toString();
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.MIXIN_CALL);

    Node exp = mixincall(selector(element(".x")));
    h.parseEquals(".x;", exp);

    exp = mixincall(selector(element(".x")), args(','));
    h.parseEquals(".x();", exp);

    exp = mixincall(selector(element(".mixin")), args(',', arg(var("@a")), arg(var("@b"))));
    h.parseEquals(".mixin(@a, @b)", exp);

    exp = mixincall(selector(element(".x")), args(',', arg("@a", dim(1))));
    h.parseEquals(".x(@a: 1)", exp);

    exp = mixincall(selector(element("#ns"), comb(CHILD), element(".mixin")));
    h.parseEquals("#ns > .mixin", exp);
  }

  @Test
  public void testParseBlockArgs() throws LessException {
    LessHarness h = new LessHarness(Parselets.MIXIN_CALL);

    Node rs1 = ruleset(rule(prop("color"), color("red")));
    Node rs2 = ruleset(rule(prop("font-size"), dim(1, PX)));
    Node exp = mixincall(selector(element(".mixin")), args(',', arg(dim(1)), arg(rs1), arg(rs2)));
    h.parseEquals(".mixin(1, {color: red}, {font-size: 1px})", exp);
  }

  @Test
  public void testUnevaluatedRuleset() throws LessException {
    LessHarness h = new LessHarness(Parselets.STYLESHEET);
    LessOptions opts = new LessOptions(true);

    String source = "@a: 1; .parent; .parent when (@a = 1) { .foo { color: red; }} .parent;";
    String expected = ".parent .foo{color:red}";
    assertEquals(h.execute(source, opts), expected);
  }

}
