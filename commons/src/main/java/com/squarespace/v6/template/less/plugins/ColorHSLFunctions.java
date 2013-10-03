package com.squarespace.v6.template.less.plugins;

import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.Function;
import com.squarespace.v6.template.less.exec.Registry;
import com.squarespace.v6.template.less.exec.SymbolTable;
import com.squarespace.v6.template.less.model.Dimension;
import com.squarespace.v6.template.less.model.HSLColor;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.RGBColor;
import com.squarespace.v6.template.less.model.Unit;


public class ColorHSLFunctions implements Registry<Function> {

  public static final Function HSL = new Function("hsl", "ppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double hue = number(args.get(0));
      double saturation = number(args.get(1));
      double lightness = number(args.get(2));
      return new HSLColor(hue, saturation, lightness, 1.0);
    }
  };

  public static final Function HSLA = new Function("hsla", "pppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double hue = number(args.get(0));
      double saturation = number(args.get(1));
      double lightness = number(args.get(2));
      double alpha = number(args.get(3));
      return new HSLColor(hue, saturation, lightness, alpha);
    }
  };

  public static final Function HSV = new Function("hsv", "ppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double hue = number(args.get(0));
      double saturation = number(args.get(1));
      double value = number(args.get(2));
      return RGBColor.fromHSVA(hue, saturation, value, 1.0);
    }
  };

  public static final Function HSVA = new Function("hsva", "pppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double hue = number(args.get(0));
      double saturation = number(args.get(1));
      double value = number(args.get(2));
      double alpha = number(args.get(3));
      return RGBColor.fromHSVA(hue, saturation, value, alpha);
    }
  };
  
  public static final Function HUE = new Function("hue", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return new Dimension(hsl(args.get(0)).hue());
    }
  };

  public static final Function SATURATION = new Function("saturation", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return new Dimension(Math.round(hsl(args.get(0)).saturation() * 100.0), Unit.PERCENTAGE);
    }
  };

  public static final Function LIGHTNESS = new Function("lightness", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return new Dimension(Math.round(hsl(args.get(0)).lightness() * 100.0), Unit.PERCENTAGE);
    }
  };

  public static final Function LUMA = new Function("luma", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return new Dimension(Math.round(rgb(args.get(0)).luma() * 100.0), Unit.PERCENTAGE);
    }
  };

  /*
   return new(tree.Dimension)(Math.round((0.2126 * (color.rgb[0]/255) +
            0.7152 * (color.rgb[1]/255) +
            0.0722 * (color.rgb[2]/255)) *
            color.alpha * 100), '%');
    
   */
  
  @Override
  public void registerTo(SymbolTable<Function> table) {
    // NO-OP
  }

}
