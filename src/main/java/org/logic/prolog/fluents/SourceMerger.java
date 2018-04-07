package org.logic.prolog.fluents;

import org.logic.prolog.terms.Const;
import org.logic.prolog.terms.Copier;
import org.logic.prolog.terms.Prog;
import org.logic.prolog.terms.Source;
import org.logic.prolog.terms.Term;

/**
  Merges a List of Sources into a new Source which
  (fairly) iterates over them breadth first.
*/
public class SourceMerger extends JavaSource {
  public SourceMerger(Const Xs,Prog p){
    super(p);
    this.Q=new Queue(Copier.ConsToVector(Xs));
  }
  
  private Queue Q;
  
  public Term getElement() {
    if(null==Q)
      return null;
    while(!Q.isEmpty()) {
      Source current=(Source)Q.deq();
      if(null==current)
        continue;
      Term T=current.getElement();
      if(null==T)
        continue;
      Q.enq(current);
      return T;
    }
    return null;
  }
}
