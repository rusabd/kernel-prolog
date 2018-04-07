package org.logic.prolog.fluents;

import org.logic.prolog.terms.Const;
import org.logic.prolog.terms.Copier;
import org.logic.prolog.terms.Prog;

/**
  Builds an iterator from a list
*/
public class ListSource extends JavaSource {
  public ListSource(Const Xs,Prog p){
    super(Copier.ConsToVector(Xs),p);
  }
}
