package com.squarespace.v6.template.less.exec;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.squarespace.v6.template.less.core.TypeRef;


public abstract class SymbolTable<V> {

  private Map<String, V> table;
  
  private TypeRef<V> typeRef;
  
  private boolean inUse = false;
  
  public SymbolTable(TypeRef<V> typeRef, int numBuckets) {
    this.table = new HashMap<>(numBuckets);
    this.typeRef = typeRef;
  }

  public void setInUse() {
    this.inUse = true;
  }
  
  public void register(Registry<V> source) {
    registerClass(source);
  }
  
  public V get(String symbol) {
    return table.get(symbol);
  }
  
  protected void put(String symbol, V value) {
    if (inUse) {
      throw new RuntimeException("Attempt to add a symbol after table in use.");
    }
    if (table.containsKey(symbol)) {
      throw new RuntimeException("A symbol named '" + symbol + "' is already registered!");
    }
    table.put(symbol, value);
  }
  
  public abstract void registerSymbol(Object impl);
  
  private void registerClass(Registry<V> source) {
    Field[] fields = source.getClass().getDeclaredFields();
    for (Field field : fields) {
      if (!Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      Class<?> type = field.getType();
      if (type.equals(typeRef.type())) {
        field.setAccessible(true);
        try {
          registerSymbol(field.get(source));
        } catch (IllegalAccessException e) {
          throw new RuntimeException("failed to register " + source, e);
        }
      }
    }
  }
}
