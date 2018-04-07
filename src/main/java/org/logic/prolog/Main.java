package org.logic.prolog;

import java.io.InputStreamReader;
import org.logic.prolog.builtins.Builtins;
import org.logic.prolog.fluents.DataBase;

/**
   Minimal command line only Prolog main entry point
*/
public class Main {
  public static int init() {
    if(!Init.startProlog())
      return 0;
    Init.builtinDict=new Builtins();
    DataBase.streamToProg(new InputStreamReader(Init.class.getResourceAsStream(Init.default_lib)), true);
    return 1;
  }
  
  public static void main(String args[]) {
    if(0==init())
      return;
    if(!Init.run(args))
      return;
    Init.standardTop(); // interactive
  }
}
