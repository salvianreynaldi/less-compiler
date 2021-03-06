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

package com.squarespace.less.exec;

import java.util.List;

import com.squarespace.less.LessContext;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.CharClass;
import com.squarespace.less.core.Chars;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.model.Alpha;
import com.squarespace.less.model.Assignment;
import com.squarespace.less.model.AttributeElement;
import com.squarespace.less.model.BaseColor;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.CombinatorType;
import com.squarespace.less.model.Comment;
import com.squarespace.less.model.CompositeProperty;
import com.squarespace.less.model.Directive;
import com.squarespace.less.model.Expression;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.Feature;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.FunctionCall;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Operation;
import com.squarespace.less.model.Paren;
import com.squarespace.less.model.Property;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.Rule;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.SelectorPart;
import com.squarespace.less.model.Selectors;
import com.squarespace.less.model.Shorthand;
import com.squarespace.less.model.TextElement;
import com.squarespace.less.model.Url;
import com.squarespace.less.model.ValueElement;
import com.squarespace.less.model.WildcardElement;


/**
 * Renders Node instances into strings using reusable buffers.
 */
public class NodeRenderer {

  private NodeRenderer() {
  }

  public static String render(LessContext ctx, Node node) {
    Buffer buf = ctx.acquireBuffer();
    render(buf, node);
    ctx.returnBuffer();
    return buf.toString();
  }

  public static void render(Buffer buf, Node node) {
    if (node == null) {
      return;
    }

    // Nodes which are composed of other nodes are output here.
    switch (node.type()) {

      case ALPHA:
        renderImpl(buf, (Alpha)node);
        break;

      case ANONYMOUS:
      case DIMENSION:
      case FALSE:
      case KEYWORD:
      case RATIO:
      case TRUE:
      case UNICODE_RANGE:
        node.repr(buf);
        break;

      case ASSIGNMENT:
        renderImpl(buf, (Assignment)node);
        break;

      case COLOR:
        ((BaseColor)node).toRGB().repr(buf);
        break;

      case COMMENT:
        renderImpl(buf, (Comment)node);
        break;

      case COMPOSITE_PROPERTY:
        renderImpl(buf, (CompositeProperty)node);
        break;

      case DIRECTIVE:
        renderImpl(buf, (Directive)node);
        break;

      case EXPRESSION:
        renderImpl(buf, (Expression)node);
        break;

      case EXPRESSION_LIST:
        renderImpl(buf, (ExpressionList)node);
        break;

      case FEATURE:
        renderImpl(buf, (Feature)node);
        break;

      case FEATURES:
        renderImpl(buf, (Features)node);
        break;

      case FUNCTION_CALL:
        renderImpl(buf, (FunctionCall)node);
        break;

      case OPERATION:
        renderImpl(buf, (Operation)node);
        break;

      case PAREN:
        renderImpl(buf, (Paren)node);
        break;

      case PROPERTY:
        renderImpl(buf, (Property)node);
        break;

      case QUOTED:
        renderImpl(buf, (Quoted)node);
        break;

      case RULE:
        renderImpl(buf, (Rule)node);
        break;

      case SELECTORS:
        for (Selector selector : ((Selectors)node).selectors()) {
          renderImpl(buf, selector);
        }
        break;

      case SELECTOR:
        renderImpl(buf, (Selector)node);
        break;

      case SHORTHAND:
        renderImpl(buf, (Shorthand)node);
        break;

      case URL:
        renderImpl(buf, (Url)node);
        break;

      default:
        throw new LessInternalException("Don't know how to render a node of type " + node.type());
    }
  }

  private static void renderImpl(Buffer buf, Alpha alpha) {
    buf.append("alpha(opacity=");
    render(buf, alpha.value());
    buf.append(')');
  }

  private static void renderImpl(Buffer buf, Assignment assign) {
    buf.append(assign.name()).append('=');
    render(buf, assign.value());
  }

  private static void renderImpl(Buffer buf, Comment comment) {
    String body = comment.body();
    if (comment.block()) {
      buf.append("/*").append(body).append("*/");
    } else {
      buf.append("//").append(body);
    }
    if (comment.newline()) {
      buf.append(Chars.LINE_FEED);
    }
  }

  private static void renderImpl(Buffer buf, CompositeProperty property) {
    for (Node node : property.segments()) {
      buf.append(node.repr());
    }
  }

  private static void renderImpl(Buffer buf, Directive directive) {
    buf.append(directive.name());
    Node value = directive.value();
    if (value != null) {
      buf.append(' ');
      render(buf, value);
    }
  }

  private static void renderImpl(Buffer buf, Expression expn) {
    renderList(buf, expn.values(), " ");
  }

  private static void renderImpl(Buffer buf, ExpressionList list) {
    renderList(buf, list.expressions(), buf.compress() ? "," : ", ");
  }

  private static void renderList(Buffer buf, List<Node> nodes, String sep) {
    int size = nodes.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(sep);
      }
      render(buf, nodes.get(i));
    }
  }

  public static void renderImpl(Buffer buf, Feature feature) {
    render(buf, feature.property());
    buf.ruleSep();
    render(buf, feature.value());
  }

  private static void renderImpl(Buffer buf, Features features) {
    List<Node> nodes = features.features();
    int size = nodes.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.listSep();
      }
      render(buf, nodes.get(i));
    }
  }

  private static void renderImpl(Buffer buf, FunctionCall call) {
    String name = call.name();
    buf.append(name).append('(');

    List<Node> args = call.args();
    int size = args.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.listSep();
      }
      render(buf, args.get(i));
    }
    buf.append(')');
  }

  private static void renderImpl(Buffer buf, Operation operation) {
    render(buf, operation.left());
    buf.append(operation.operator().toString());
    render(buf, operation.right());
  }

  /** Render a PAREN node. */
  private static void renderImpl(Buffer buf, Paren paren) {
    buf.append('(');
    render(buf, paren.value());
    buf.append(')');
  }

  private static void renderImpl(Buffer buf, Property property) {
    buf.append(property.name());
  }

  /**
   * Render a QUOTED node.
   */
  private static void renderImpl(Buffer buf, Quoted quoted) {
    renderQuoted(buf, quoted, quoted.delimiter());
  }

  /**
   * Render a QUOTED node, forcing the given delimiter to be used.
   * This is useful when comparing the contents of 2 strings, ignoring
   * their delimiters.
   */
  public static void renderQuoted(Buffer buf, Quoted quoted, char delim) {
    boolean escaped = quoted.escaped();
    delim = escaped ? Chars.NULL : delim;
    boolean emitDelim = !buf.inEscape();
    if (emitDelim) {
      if (!escaped) {
        buf.append(delim);
      }
      buf.startDelim(delim);
    }

    List<Node> parts = quoted.parts();
    int size = (parts == null) ? 0 : parts.size();
    for (int i = 0; i < size; i++) {
      render(buf, parts.get(i));
    }

    if (emitDelim) {
      buf.endDelim();
      if (!escaped) {
        buf.append(delim);
      }
    }
  }

  /** Render a RULE node. */
  private static void renderImpl(Buffer buf, Rule rule) {
    render(buf, rule.property());
    buf.ruleSep();
    render(buf, rule.value());
    if (rule.important()) {
      buf.append(" !important");
    }
  }

  /** Render a SHORTHAND node. */
  private static void renderImpl(Buffer buf, Shorthand shorthand) {
    render(buf, shorthand.left());
    buf.append('/');
    render(buf, shorthand.right());
  }

  /** Render a SELECTOR node. */
  private static void renderImpl(Buffer buf, Selector selector) {
    List<SelectorPart> parts = selector.parts();
    int size = parts.size();
    int last = size - 1;
    for (int i = 0; i < size; i++) {
      SelectorPart part = parts.get(i);
      renderSelectorPart(buf, part, i == 0, i == last);
    }
  }

  /**
   * Renders part of a selector, which may be a combinator or element.
   */
  public static void renderSelectorPart(Buffer buf, SelectorPart part, boolean isFirst, boolean isLast) {
    if (part instanceof Combinator) {
      CombinatorType type = ((Combinator)part).combinatorType();
      boolean isDescendant = type == CombinatorType.DESC;
      if (isFirst) {
        if (!isDescendant) {
          buf.append(type.repr());
        }

      } else {
        if (!buf.compress() && !isDescendant) {
          buf.append(' ');
        }
        if (!isDescendant || !CharClass.whitespace(buf.prevChar())) {
          buf.append(type.repr());
        }
      }
      if (!buf.compress() && !isLast && !CharClass.whitespace(buf.prevChar())) {
        buf.append(' ');
      }

    } else {
      renderElement(buf, part);
    }
  }

  /**
   * Render CSS for an element.
   */
  public static void renderElement(Buffer buf, SelectorPart current) {
    if (current instanceof WildcardElement) {
      return;
    }

    if (current instanceof TextElement) {
      ((TextElement)current).repr(buf);

    } else if (current instanceof ValueElement) {
      render(buf, ((ValueElement)current).value());

    } else if (current instanceof AttributeElement) {
      buf.append('[');
      AttributeElement attrElem = (AttributeElement)current;
      List<Node> parts = attrElem.parts();
      int size = parts.size();
      for (int i = 0; i < size; i++) {
        render(buf, parts.get(i));
      }
      buf.append(']');
    }
  }

  /** Render a URL node. */
  private static void renderImpl(Buffer buf, Url url) {
    buf.append("url(");
    render(buf, url.value());
    buf.append(Chars.RIGHT_PARENTHESIS);
  }

}
