package org.logic.prolog.fluents;

import java.util.HashMap;

/**
 General purpose dictionary
*/
public class HashDict extends HashMap {
  public String name() {
    return getClass().getName()+hashCode();
  }
  
  public String stat() {
    return "BlackBoard: "+size();
  }
}
