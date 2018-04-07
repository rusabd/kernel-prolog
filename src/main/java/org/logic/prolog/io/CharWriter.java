package org.logic.prolog.io;

import java.io.IOException;
import java.io.Writer;

import org.logic.prolog.terms.Int;
import org.logic.prolog.terms.Prog;
import org.logic.prolog.terms.Sink;
import org.logic.prolog.terms.Term;


/**
  Writer
*/
public class CharWriter extends Sink {
  protected Writer writer;
  
  public CharWriter(String f,Prog p){
    super(p);
    this.writer=IO.toFileWriter(f);
  }
  
  public CharWriter(Prog p){
    super(p);
    this.writer=IO.output;
  }
  
  public int putElement(Term t) {
    if(null==writer)
      return 0;
    try {
      char c=(char)((Int)t).intValue();
      writer.write(c);
    } catch(IOException e) {
      return 0;
    }
    return 1;
  }
  
  public void stop() {
    if(null!=writer&&IO.output!=writer) {
      try {
        writer.close();
      } catch(IOException e) {
      }
      writer=null;
    }
  }
}
