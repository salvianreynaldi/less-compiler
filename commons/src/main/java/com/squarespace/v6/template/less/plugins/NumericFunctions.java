package com.squarespace.v6.template.less.plugins;

import static com.squarespace.v6.template.less.SyntaxErrorType.UNKNOWN_UNIT;
import static com.squarespace.v6.template.less.core.ErrorUtils.error;

import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.Function;
import com.squarespace.v6.template.less.exec.Registry;
import com.squarespace.v6.template.less.exec.SymbolTable;
import com.squarespace.v6.template.less.model.Dimension;
import com.squarespace.v6.template.less.model.Keyword;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Unit;


public class NumericFunctions implements Registry<Function> {

  public static final Function CEIL = new Function("ceil", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      return new Dimension(Math.ceil(dim.value()), dim.unit());
    }
  };

  public static final Function FLOOR = new Function("floor", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      return new Dimension(Math.floor(dim.value()), dim.unit());
    }
  };

  public static final Function PERCENTAGE = new Function("percentage", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      return new Dimension(dim.value() * 100, Unit.PERCENTAGE);
    }
  };

  public static final Function ROUND = new Function("round", "d:n") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double places = 0.0;
      int size = args.size();
      Dimension dim = (Dimension)args.get(0);
      if (size == 2) {
        places = Math.max(((Dimension)args.get(1)).value(), 0);
      }
      double scale = Math.pow(10, places);
      return new Dimension(Math.round(dim.value() * scale) / scale, dim.unit());
    }
  };

  public static final Function UNIT = new Function("unit", "d:k") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      Unit unit = null;
      if (args.size() >= 2) {
        Keyword word = (Keyword)args.get(1);
        unit = Unit.get(word.value());
        if (unit == null) {
          throw new LessException(error(UNKNOWN_UNIT).arg0(word));
        }
      }
      return new Dimension(dim.value(), unit);
    }
  };
  
  @Override
  public void registerTo(SymbolTable<Function> table) {
   // NO-OP
  }

}
