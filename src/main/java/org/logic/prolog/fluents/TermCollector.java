package org.logic.prolog.fluents;

import java.util.ArrayList;

import org.logic.prolog.terms.Prog;
import org.logic.prolog.terms.Sink;
import org.logic.prolog.terms.Term;

/**
  Builds  Fluents from Java
  Streams
*/
public class TermCollector extends Sink {
  protected ArrayList buffer;
  
  private Prog p;
  
  public TermCollector(Prog p){
    super(p);
    this.p=p;
    this.buffer=new ArrayList();
  }
  
  public int putElement(Term T) {
    buffer.add(T);
    return 1;
  }
  
  public void stop() {
    buffer=null;
  }
  
  public Term collect() {
    return new JavaSource(buffer,p);
  }
}
