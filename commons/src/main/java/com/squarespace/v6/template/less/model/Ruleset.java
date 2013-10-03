package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;

import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.LessUtils;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.SelectorUtils;


public class Ruleset extends BlockNode {

  private Selectors selectors;

  private boolean evaluating;
  
  private List<List<String>> mixinPaths;
  
  public Ruleset() {
    this.selectors = new Selectors();
  }
  
  public Ruleset(Selectors selectors) {
    this(selectors, new Block());
  }
  
  public Ruleset(Selectors selectors, Block block) {
    super(block);
    this.selectors = selectors;
    addMixinPaths(selectors);
  }
  
  public Ruleset copy(ExecEnv env) throws LessException {
    Ruleset result = new Ruleset((Selectors) selectors.eval(env), block.copy());
    result.mixinPaths = mixinPaths;
    if (originalBlockNode != null) {
      result.originalBlockNode = originalBlockNode;
    }
    return result;
  }
  
  public Selectors selectors() {
    return selectors;
  }
  
  public List<List<String>> mixinPaths() {
    return LessUtils.safeList(mixinPaths);
  }

  public void enter() {
    evaluating = true;
  }
  
  public void exit() {
    evaluating = false;
  }

  public boolean evaluating() {
    return evaluating;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Ruleset) {
      Ruleset other = (Ruleset)obj;
      return safeEquals(selectors, other.selectors) && super.equals(obj);
    }
    return false;
  }
  
  @Override
  public void add(Node node) {
    if (node.is(NodeType.SELECTOR)) {
      Selector selector = (Selector)node;
      selectors.add(selector);
      addMixinPaths(selector);
      
    } else {
      super.add(node);
    }
  }

  @Override
  public NodeType type() {
    return NodeType.RULESET;
  }
  
  @Override
  public void repr(Buffer buf) {
    selectors.repr(buf);
    buf.append(" {\n");
    buf.incrIndent();
    block.repr(buf);
    buf.decrIndent();
    buf.indent().append("}\n");
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append('\n');
    buf.incrIndent().indent();
    selectors.modelRepr(buf);
//    buf.append('\n').indent();
    buf.append('\n');
    super.modelRepr(buf);
    buf.decrIndent().append('\n');
  }
  
  private void addMixinPaths(Selectors selectors) {
    for (Selector selector : selectors.selectors()) {
      addMixinPaths(selector);
    }
  }
  
  private void addMixinPaths(Selector selector) {
    List<String> path = SelectorUtils.renderMixinSelector(selector);
    if (path != null) {
      addMixinPath(path);
    }
  }
  
  private void addMixinPath(List<String> path) {
    mixinPaths = LessUtils.initList(mixinPaths, 2);
    mixinPaths.add(path);
  }
  
}  

