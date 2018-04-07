package org.logic.prolog;

import java.io.InputStreamReader;
import org.logic.prolog.builtins.Builtins;
import org.logic.prolog.fluents.DataBase;

/**
   Minimal command line only Prolog main entry point
*/
public class Main {
  private static DataBase db;
  public static int init() {
    db = Init.startProlog();
    Init.builtinDict=new Builtins(db);
    db.streamToProg(new InputStreamReader(Init.class.getResourceAsStream(Init.default_lib)), true);
    return 1;
  }
  
  public static void main(String args[]) {
    if(0==init())
      return;
    if(!Init.run(args))
      return;
    Init.standardTop(db); // interactive
  }
}
