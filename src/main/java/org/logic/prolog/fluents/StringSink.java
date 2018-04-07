package org.logic.prolog.fluents;

import org.logic.prolog.terms.Const;
import org.logic.prolog.terms.Prog;
import org.logic.prolog.terms.Sink;
import org.logic.prolog.terms.Term;

/**
  Builds  Fluents from Java
  Streams
*/
public class StringSink extends Sink {
  protected StringBuffer buffer;
  
  public StringSink(Prog p){
    super(p);
    this.buffer=new StringBuffer();
  }
  
  public int putElement(Term t) {
    buffer.append(t.toUnquoted());
    return 1;
  }
  
  public void stop() {
    buffer=null;
  }
  
  public Term collect() {
    return new Const(buffer.toString());
  }
}
