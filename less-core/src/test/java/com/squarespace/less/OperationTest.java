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

import static com.squarespace.less.model.Operator.ADD;
import static com.squarespace.less.model.Operator.DIVIDE;
import static com.squarespace.less.model.Operator.MULTIPLY;
import static com.squarespace.less.model.Operator.SUBTRACT;
import static com.squarespace.less.model.Units.CM;
import static com.squarespace.less.model.Units.DEG;
import static com.squarespace.less.model.Units.EM;
import static com.squarespace.less.model.Units.IN;
import static com.squarespace.less.model.Units.KHZ;
import static com.squarespace.less.model.Units.MS;
import static com.squarespace.less.model.Units.PERCENTAGE;
import static com.squarespace.less.model.Units.PX;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.model.Operator;
import com.squarespace.less.parse.Parselets;


public class OperationTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(oper(ADD, dim(1), dim(2)), oper(ADD, dim(1), dim(2)));

    assertNotEquals(oper(ADD, dim(1), dim(2)), oper(SUBTRACT, dim(1), dim(2)));
    assertNotEquals(oper(ADD, dim(1), dim(2)), oper(ADD, dim(1), dim(3)));
  }

  @Test
  public void testModelReprSafety() {
    oper(ADD, dim(1), dim(2)).toString();
  }

  @Test
  public void testColorMath() throws LessException {
    LessHarness h = new LessHarness(Parselets.ADDITION);

    // color math
    h.evalEquals("#000 + 10", color("#0a0a0a"));
    h.evalEquals("#fff - 1", color("#fefefe"));
    h.evalEquals("#222 * 2", color("#444444"));
    h.evalEquals("#888 - #555 - #111", color("#222"));
    h.evalEquals("1 + blue + #010101", color("#0202ff"));
    h.evalEquals("1 * #123 * 1", color("#123"));
    h.evalEquals("#000 + red + blue", color("#f0f"));
  }

  @Test
  public void testMath() throws LessException {
    GenericBlock defs = defs(
        def("@two", dim(2)),
        def("@ten", oper(Operator.MULTIPLY, var("@two"), dim(5))),
        def("@tenPX", dim(10, PX)),
        def("@tenIN", dim(10, IN))
    );

    LessHarness h = new LessHarness(Parselets.ADDITION, defs);

    // whitespace
    h.evalEquals("(1+2)/(3*4)", dim(0.25));
    h.evalEquals("\n ( \n 2 \n + \n 3 \n ) \n", dim(5));

    // basic math
    h.evalEquals(".2 * 5", dim(1.0));
    h.evalEquals("2 + 3.1", dim(5.1));
    h.evalEquals(".2 * 6", dim(.2 * 6));
    h.evalEquals("1 + (2 + (-17)) + 3.14", dim(-10.86));
    h.evalEquals("1 + 2 - 7", dim(-4));
    h.evalEquals("-7 - -3 - -1", dim(-3));
    h.evalEquals("12 / 3", dim(4));
    h.evalEquals("(-(10)) * (-(100)) * (-(1000))", dim(-1000000));

    // variables
    h.evalEquals("@ten * 2", dim(20));
    h.evalEquals("-10000 / @ten", dim(-1000));
    h.evalEquals("-@ten * 32", dim(-320));

    // implicit units
    h.evalEquals("2 * 1px", dim(2, PX));
    h.evalEquals("7 + 3em + 1em", dim(11, EM));
    h.evalEquals("3cm - 4 + 4", dim(3, CM));

    // unit conversions
    h.evalEquals(".5in + 48px", dim(1, IN));
    h.evalEquals("30ms + 1s + 200ms", dim(1230, MS));
    h.evalEquals("180deg - .25turn", dim(90, DEG));
    h.evalEquals("10khz - 9000hz", dim(1, KHZ));

    // unit conversions with variables
    h.evalEquals("1px + @tenIN + @tenPX", dim(971, PX));
    h.evalEquals("@tenIN + 48px", dim(10.5, IN));

    // incomplete, ignored trailing operators
    h.evalEquals("1+2*", dim(3));

    // percentages
    h.evalEquals("100% * 10px", dim(1000, PERCENTAGE));
    h.evalEquals("10% * 10px", dim(100, PERCENTAGE));
    h.evalEquals("10em * 50%", dim(500, EM));

    h.evalEquals("10px / 100%", dim(0.1, PX));
    h.evalEquals("100em / 50%", dim(2, EM));

    // adding/subtracting percentages
    h.evalEquals("10px + 10%", dim(20, PX));
    h.evalEquals("20px - 10%", dim(10, PX));
    h.evalEquals("100px / 100%", dim(1, PX));
    h.evalEquals("100px / 200%", dim(0.5, PX));
    h.evalEquals("100px / 50%", dim(2, PX));

    h.evalEquals("50% + 10%", dim(60, PERCENTAGE));
    h.evalEquals("50% - 10%", dim(40, PERCENTAGE));
    h.evalEquals("100% * 50%", dim(5000, PERCENTAGE));
    h.evalEquals("20% * 50%", dim(1000, PERCENTAGE));
    h.evalEquals("100% / 10%", dim(10, PERCENTAGE));

    h.evalEquals("30% + 10", dim(40, PERCENTAGE));
    h.evalEquals("30% - 10", dim(20, PERCENTAGE));
    h.evalEquals("30% / 10", dim(3, PERCENTAGE));
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.ADDITION);

    h.parseEquals("1 + -2", oper(ADD, dim(1), dim(-2)));
    h.parseEquals("1 - -2", oper(SUBTRACT, dim(1), dim(-2)));
    h.parseEquals("-@foo", oper(MULTIPLY, var("@foo"), dim(-1)));
    h.parseEquals("3.14 * 3.14", oper(MULTIPLY, dim(3.14), dim(3.14)));
    h.parseEquals("17 / 3", oper(DIVIDE, dim(17), dim(3)));
    h.parseEquals("3 * 4 - 5", oper(SUBTRACT, oper(MULTIPLY, dim(3), dim(4)), dim(5)));
    h.parseEquals("3 * (4 - 5)", oper(MULTIPLY, dim(3), oper(SUBTRACT, dim(4), dim(5))));
    h.parseEquals("(((1 - 2)))", oper(SUBTRACT, dim(1), dim(2)));
  }

  @Test
  public void testBadColorMath() throws LessException {
    String[] strings = new String[] {
      "#000 + 1px", "12em + #fff", "2 / #010101", "2 - #010101"
    };

    LessHarness h = new LessHarness(Parselets.ADDITION);

    // XXX: change to use evalFail
    for (String str : strings) {
      try {
        h.evaluate(str);
        fail("Expected operation to raise an exception: " + str);
      } catch (LessException e) {
        // fallthrough
      }
    }
  }

  @Test
  public void testBadMath() throws LessException {
    String[] strings = new String[] {
      "foo + 1",
      "1 * foo"
    };

    LessHarness h = new LessHarness(Parselets.ADDITION);

    // XXX: change to use evalFail
    for (String str : strings) {
      try {
        h.evaluate(str);
        fail("Expected operation to raise an exception: " + str);
      } catch (LessException e) {
        // fallthrough
      }
    }
  }

}
