package org.logic.prolog.terms;



abstract public class Sink extends Fluent {
  
  public Sink(Prog p){
    super(p);
  }
  
  abstract public int putElement(Term T);
  
  public Term collect() {
    return null;
  }
}
