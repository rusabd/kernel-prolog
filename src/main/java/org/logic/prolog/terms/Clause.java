package org.logic.prolog.terms;

import java.util.Iterator;

import org.logic.prolog.fluents.HashDict;
import org.logic.prolog.io.IO;
import org.logic.prolog.io.Parser;

//!depends
/**
* Datatype for a Prolog clause (H:-B) having a head H and a body b
*/
public class Clause extends Fun {
  /**
   *  Builds a clause given its head and its body
   */
  public Clause(Term head,Term body){
    super(":-",head,body);
  }
  
  /**
  * Constructs a clause by parsing its string representation.
  * Note the building of a dictionary of variables, allowing
  * listing of the clause with its original variable names.
  */
  public Clause(String s){
    super(":-");
    Clause C=clauseFromString(s);
    // IO.mes("CLAUSE:"+C.pprint()+"\nDICT:"+C.dict);
    this.args=C.args;
    this.dict=C.dict;
    this.ground=C.ground;
  }
  
  /**
  * Extracts a clause from its String representation.
  */
  
  public static Clause clauseFromString(String s) {
    return Parser.clsFromString(s);
  }
  
  /**
    Reads a goal as a clause containing a dummy header with
    all variables in it
  */
  
  public Clause toGoal() {
    Clause G=new Clause(varsOf(),getHead());
    G.dict=dict;
    G.checkIfGround();
    IO.trace("conversion from clause to goal ignores body of: "+pprint());
    return G;
  }
  
  public static Clause goalFromString(String line) {
    IO.trace("read string: <"+line+">");
    
    if(null==line)
      line=Const.anEof.name();
    else if(0==line.length())
      return null;
    
    Clause C=clauseFromString(line);
    if(null==C) {
      IO.errmes("warning (null Clause):"+line);
      return null;
    }
    
    // IO.trace("got goal:\n"+C.toGoal()); //OK
    return C.toGoal();
  }
  
  /**
  * Detects that a clause is ground (i.e. has no variables)
  */
  final void checkIfGround() {
    ground=varsOf().getArity()==0;
  }
  
  /**
    Variable dictionary
  */
  public HashDict dict=null;
  
  /**
    Remembers if a clause is ground.
  */
  public boolean ground=false;
  
  /**
    File name and line where sources start and end (if applicable)
  */
  
  public String fname=null;
  
  public int begins_at=0;
  
  public int ends_at=0;
  
  public void setFile(String fname,int begins_at,int ends_at) {
    this.fname=fname.intern();
    this.begins_at=begins_at;
    this.ends_at=ends_at;
  }
  
  /**
    Checks if a Clause has been proven ground after
    beeing read in or created.
  */
  final boolean provenGround() {
    return ground;
  }
  
  /**
  * Prints out a clause as Head:-Body
  */
  private String Clause2String(Clause c) {
    Term h=c.getHead();
    Term t=c.getBody();
    if(t instanceof Conj)
      return h+":-"+((Conj)t).conjToString();
    return h+":-"+t;
  }
  
  // uncomment if you want this to be the default toString
  // procedure - it might create read-back problems, though
  // public String toString() {
  // return Clause2String(this);
  // }
  
  /**
  * Pretty prints a clause after replacing ugly variable names
  */
  public String pprint() {
    return pprint(false);
  }
  
  /**
  * Pretty prints a clause after replacing ugly variable names
  */
  public String pprint(boolean replaceAnonymous) {
    String s=Clause2String(this.cnumbervars(replaceAnonymous));
    // if(fname!=null) s="%% "+fname+":"+begins_at+"-"+ends_at+"\n"+s;
    return s;
  }
  
  /**
    Clause to Term converter: the joy of strong typing:-)
  */
  public Clause toClause() { // overrides toClause in Term
    return this;
  }
  
  /**
  * Replaces varibles with nice looking upper case
  * constants for printing purposes
  */
  // synchronized
  public Clause cnumbervars(boolean replaceAnonymous) {
    if(dict==null)
      return (Clause)numbervars();
    if(provenGround())
      return this;
    Trail trail=new Trail();
    Iterator e=dict.keySet().iterator();
    
    while(e.hasNext()) {
      Object X=e.next();
      if(X instanceof String) {
        Var V=(Var)dict.get(X);
        long occNb=((Int)dict.get(V)).longValue();
        String s=(occNb<2&&replaceAnonymous)?"_":(String)X;
        // bug: occNb not accurate when adding artif. '[]' head
        V.unify(new PseudoVar(s),trail);
      }
    }
    Clause NewC=(Clause)numbervars();
    trail.unwind(0);
    return NewC;
  }
  
  /**
    Converts a clause to a term.
    Note that Head:-true will convert to the term Head.
  */
  public final Term toTerm() {
    if(getBody() instanceof true_)
      return getHead();
    return this;
  }
  
  /**
    Creates a copy of the clause with variables standardized
    apart, i.e. something like f(s(X),Y,X) becomes f(s(X1),Y1,X1))
    with X1,Y1 variables garantted not to occurring in the
    the current resolvant.
  */
  final Clause ccopy() {
    if(ground)
      return this;
    Clause C=(Clause)copy();
    C.dict=null;
    C.ground=ground;
    return C;
  }
  
  /**
    Extracts the head of a clause (a Term).
  */
  public final Term getHead() {
    return args[0].ref();
  }
  
  /**
    Extracts the body of a clause
  */
  public final Term getBody() {
    return args[1].ref();
  }
  
  /**
    Gets the leftmost (first) goal in the body of a clause,
    i.e. from H:-B1,B2,...,Bn it will extract B1.
  */
  final Term getFirst() {
    Term body=getBody();
    if(body instanceof Conj)
      return ((Conj)body).args[0].ref();
    else if(body instanceof true_)
      return null;
    else
      return body;
  }
  
  /**
    Gets all but the leftmost goal in the body of a clause,
    i.e. from H:-B1,B2,...,Bn it will extract B2,...,Bn.
    Note that the returned Term is either Conj or True,
    the last one meaning an empty body.

    @see True
    @see Conj
  */
  final Term getRest() {
    Term body=getBody();
    if(body instanceof Conj)
      return ((Conj)body).args[1].ref();
    else
      return Const.aTrue;
  }
  
  /**
    Concatenates 2 Conjunctions
    @see Clause#unfold
  */
  static final Term appendConj(Term x,Term y) {
    y=y.ref();
    if(x instanceof true_)
      return y;
    if(y instanceof true_)
      return x; // comment out if using getState
    if(x instanceof Conj) {
      Term curr=((Conj)x).args[0].ref();
      Term cont=appendConj(((Conj)x).args[1],y);
      // curr.getState(this,cont);
      return new Conj(curr,cont);
    } else
      return new Conj(x,y);
  }
  
  /**
    Algebraic composition operation of 2 Clauses, doing
    the basic resolution step Prolog is based on.
    From A0:-A1,A2...An and B0:-B1...Bm it builds
    (A0:-B1,..Bm,A2,...An) mgu(A1,B0). Note that it returns
    null if A1 and B0 do not unify.

    @see Term#unify()

  */
  private final Clause unfold(Clause that,Trail trail) {
    Clause result=null;
    Term first=getFirst();
    
    if(first!=null&&that.getHead().matches(first,trail)) {
      
      that=that.ccopy();
      
      that.getHead().unify(first,trail);
      
      Term cont=appendConj(that.getBody(),getRest());
      result=new Clause(getHead(),cont);
    }
    return result;
  }
  
  // synchronized
  final Clause unfold_with_goal(Clause goal,Trail trail) {
    return goal.unfold(this,trail);
  }
  
  /*
  // synchronized
  final Clause unfoldedCopy(Clause that,Trail trail) {
    int oldtop=trail.size();
    Clause result=unfold(that,trail);
    if(result==null)
      return null;
    result=result.ccopy();
    trail.unwind(oldtop);
    return result;
  }
  */
  
  /**
    Returns a key based on the principal functor of the
    head of the clause and its arity.
  */
  final public String getKey() {
    return getHead().getKey();
  }
  
  final boolean isClause() {
    return true;
  }
}
