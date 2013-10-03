package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.QUOTED;
import static com.squarespace.v6.template.less.parse.Parselets.SELECTOR;
import static com.squarespace.v6.template.less.parse.Parselets.VARIABLE;
import static com.squarespace.v6.template.less.parse.Parselets.VARIABLE_CURLY;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.CharClass;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Anonymous;
import com.squarespace.v6.template.less.model.AttributeElement;
import com.squarespace.v6.template.less.model.Combinator;
import com.squarespace.v6.template.less.model.Element;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Paren;
import com.squarespace.v6.template.less.model.TextElement;
import com.squarespace.v6.template.less.model.ValueElement;
import com.squarespace.v6.template.less.model.Variable;


/**
 * Parses pairs of combinators and elements.
 */
public class ElementParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Combinator comb = parseCombinator(stm);
    stm.skipWs();
    char ch = stm.peek();
    
    if (stm.matchElement0()) {
      return new TextElement(comb, stm.token());
      
    } else if (stm.matchElement1()) {
      return new TextElement(comb, stm.token());

    } else if (ch == Chars.ASTERISK || ch == Chars.AMPERSAND) {
      stm.seek1();
      return new TextElement(comb, "" + ch);
    }

    Node elem = parseAttribute(stm, comb);
    if (elem != null) {
      return elem;
    }
    
    if (stm.matchElement2()) {
      return new TextElement(comb, stm.token());

    } else if (stm.matchElement3()) {
      return new TextElement(comb, stm.token());
    
    } else {
      Node var = stm.parse(VARIABLE_CURLY);
      if (var != null) {
        return new ValueElement(comb, (Variable)var);
      }
    }

    if (elem == null) {
      elem = parseSub(stm);
      if (elem != null) {
        return new ValueElement(comb, elem);
      }
    }
    return null;
  }
  
  /**
   * For example input selector:  > p[class~="foo"]
   * This method parses the section between the '[' and ']' characters.
   */
  private Element parseAttribute(LessStream stm, Combinator comb) throws LessException {
    if (!stm.seekIf(Chars.LEFT_SQUARE_BRACKET)) {
      return null;
    }
    
    Node key = null;
    if (stm.matchAttributeKey()) {
      key = new Anonymous(stm.token());
    } else {
      key = stm.parse(Parselets.QUOTED);
    }
    if (key == null) {
      return null;
    }

    AttributeElement elem = new AttributeElement(comb);
    elem.add(key);
    if (stm.matchAttributeOp()) {
      Node oper = new Anonymous(stm.token());
      Node val = stm.parse(QUOTED);
      if (val == null && stm.matchIdentifier()) {
        val = new Anonymous(stm.token());
      }
      if (val != null) {
        elem.add(oper);
        elem.add(val);
      }
    }
    
    if (!stm.seekIf(Chars.RIGHT_SQUARE_BRACKET)) {
      return null;
    }
    return elem;
  }

  /**
   * Parse a single combinator character from the stream.
   */
  private Combinator parseCombinator(LessStream stm) throws LessException {
    char prev = stm.peek(-1);
    int skipped = stm.skipWs();
    char ch = stm.peek();
    if (CharClass.combinator(ch)) {
      stm.seek1();
      return Combinator.fromChar(ch);
      
    } else if (skipped > 0 || CharClass.whitespace(prev) || prev == Chars.EOF) {
      return Combinator.DESC;
    }
    return null;
  }
  
  private Node parseSub(LessStream stm) throws LessException {
    stm.skipWs();
    if (!stm.seekIf(Chars.LEFT_PARENTHESIS)) {
      return null;
    }
    Node value = stm.parse(VARIABLE_CURLY, VARIABLE, SELECTOR);
    stm.skipWs();
    if (value != null && stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      return new Paren(value);
    }
    return null;
  }
  
}
