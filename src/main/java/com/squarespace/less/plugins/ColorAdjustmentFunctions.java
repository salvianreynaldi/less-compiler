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

package com.squarespace.less.plugins;

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.Function;
import com.squarespace.less.exec.Registry;
import com.squarespace.less.exec.SymbolTable;
import com.squarespace.less.model.Colors;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.HSLColor;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.RGBColor;
import com.squarespace.less.model.Unit;


public class ColorAdjustmentFunctions implements Registry<Function> {

  public static final Function CONTRAST = new Function("contrast", "*:ccp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Node arg = args.get(0);
      if (!arg.is(NodeType.COLOR)) {
        return null;
      }
      RGBColor color = rgb(arg);
      int size = args.size();
      RGBColor dark = size >= 2 ? rgb(args.get(1)) : Colors.BLACK;
      RGBColor light = size >= 3 ? rgb(args.get(2)) : Colors.WHITE;
      double threshold = size >= 4 ? number(args.get(3)) : 0.43;
      double value = (0.2126 * (color.red() / 255.0)
          + 0.7152 * (color.green() / 255.0)
          + 0.0722 * (color.blue() / 255.0)) * color.alpha();
      if (value < threshold) {
        return light;
      }
      return dark;
    }
  };

  public static final Function DARKEN = new Function("darken", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      HSLColor hsl = hsl(args.get(0));
      double value = number(args.get(1)) * 0.01;
      return new HSLColor(hsl.hue() / 360.0, hsl.saturation(), hsl.lightness() - value, hsl.alpha());
    }
  };

  public static final Function DESATURATE = new Function("desaturate", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      HSLColor hsl = hsl(args.get(0));
      double value = number(args.get(1)) * 0.01;
      return new HSLColor(hsl.hue() / 360.0, hsl.saturation() - value, hsl.lightness(), hsl.alpha());
    }
  };

  public static final Function FADE = new Function("fade", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor rgb = rgb(args.get(0));
      double alpha = number(args.get(1)) * 0.01;
      return new RGBColor(rgb.red(), rgb.green(), rgb.blue(), alpha);
    }
  };

  public static final Function FADEIN = new Function("fadein", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor rgb = rgb(args.get(0));
      double amount = number(args.get(1)) * 0.01;
      return new RGBColor(rgb.red(), rgb.green(), rgb.blue(), rgb.alpha() + amount);
    }
  };

  public static final Function FADEOUT = new Function("fadeout", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor rgb = rgb(args.get(0));
      double amount = number(args.get(1)) * 0.01;
      return new RGBColor(rgb.red(), rgb.green(), rgb.blue(), rgb.alpha() - amount);
    }
  };

  public static final Function GREYSCALE = new Function("greyscale", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      HSLColor hsl = hsl(args.get(0));
      return new HSLColor(hsl.hue() / 360.0, 0, hsl.lightness(), hsl.alpha());
    }
  };

  public static final Function LIGHTEN = new Function("lighten", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      HSLColor hsl = hsl(args.get(0));
      double value = number(args.get(1)) * 0.01;
      return new HSLColor(hsl.hue() / 360.0, hsl.saturation(), hsl.lightness() + value, hsl.alpha());
    }
  };

  public static final Function SATURATE = new Function("saturate", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      HSLColor hsl = hsl(args.get(0));
      double value = number(args.get(1)) * 0.01;
      return new HSLColor(hsl.hue() / 360.0, hsl.saturation() + value, hsl.lightness(), hsl.alpha());
    }
  };

  public static final Function SPIN = new Function("spin", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      HSLColor hsl = hsl(args.get(0));
      Dimension amount = (Dimension)args.get(1);
      double value = amount.value();
      if (amount.unit() == Unit.PERCENTAGE) {
        value = (value / 100.0) * 360;
      }
      double hue = (hsl.hue() + value) % 360;
      hue = hue < 0 ? 360 + hue : hue;
      Node result = new HSLColor(hue / 360.0, hsl.saturation(), hsl.lightness(), hsl.alpha());
      return result;
    }
  };

  @Override
  public void registerTo(SymbolTable<Function> table) {
    // TO-DO
  }

}
