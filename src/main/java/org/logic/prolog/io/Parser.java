package org.logic.prolog.io;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;

import org.logic.prolog.builtins.Builtins;
import org.logic.prolog.fluents.HashDict;
import org.logic.prolog.terms.Clause;
import org.logic.prolog.terms.Conj;
import org.logic.prolog.terms.Cons;
import org.logic.prolog.terms.Const;
import org.logic.prolog.terms.Fun;
import org.logic.prolog.terms.Int;
import org.logic.prolog.terms.Nonvar;
import org.logic.prolog.terms.Real;
import org.logic.prolog.terms.Term;
import org.logic.prolog.terms.Var;


/**
  Lexicographic analyser reading from a stream
*/
class Lexer extends StreamTokenizer {
  protected Reader input;
  
  public Lexer(Reader I) throws IOException{
    super(I);
    this.input=I;
    parseNumbers();
    eolIsSignificant(true);
    ordinaryChar('.');
    ordinaryChar('-'); // creates problems with -1 etc.
    ordinaryChar('/');
    quoteChar('\'');
    quoteChar('\"');
    wordChar('$');
    wordChar('_');
    slashStarComments(true);
    commentChar('%');
    dict=new HashDict();
  }
  
  /**
     Path+File name based constructor
     Used in prolog2java
  */
  
  public Lexer(String path,String s) throws IOException{
    this(IO.url_or_file(path+s)); // stream
  }
  
  /**
     String based constructor.
     Used in queries ended by \n + prolog2java.
  */
  
  public Lexer(String s) throws Exception{
    this(IO.string_to_stream(s));
  }
  
  public Lexer() throws IOException{
    this(IO.input);
  }
  
  private final static String anonymous="_".intern();
  
  private final static String char2string(int c) {
    return ""+(char)c;
  }
  
  private boolean inClause=false;
  
  public boolean atEOF() {
    boolean yes=(TT_EOF==ttype);
    if(yes)
      try {
        input.close();
      } catch(IOException e) {
        IO.trace("unable to close atEOF");
      }
    return yes;
  }
  
  boolean atEOC() {
    return !inClause;
  }
  
  protected final static Term make_const(String s) {
    return new constToken(s);
  }
  
  private final static Term make_fun(String s) {
    return new funToken(s);
  }
  
  private final static Term make_int(double n) {
    return new intToken((int)n);
  }
  
  private final static Term make_real(double n) {
    return new realToken(n);
  }
  
  private final static Term make_number(double nval) {
    Term T;
    if(Math.floor(nval)==nval)
      T=make_int(nval);
    else
      T=make_real(nval);
    return T;
  }
  
  private final Term make_var(String s) {
    s=s.intern();
    Var X;
    long occ;
    if(s==anonymous) {
      occ=0;
      X=new Var();
      s=X.toString();
    } else {
      X=(Var)dict.get(s);
      if(X==null) {
        occ=1;
        X=new Var();
      } else {
        occ=((Int)dict.get(X)).longValue();
        occ++;
      }
    }
    Int I=new Int(occ);
    dict.put(X,I);
    dict.put(s,X);
    return new varToken(X,new Const(s),I);
  }
  
  private final void wordChar(char c) {
    wordChars(c,c);
  }
  
  HashDict dict;
  
  private Term getWord(boolean quoted) throws IOException {
    Term T;
    if(quoted&&0==sval.length())
      T=make_const("");
    /* DO NOT DO THIS: quoting is meant to prevent it!!!
    else if("()[]|".indexOf(sval.charAt(0))>=0) {
      switch(sval.charAt(0)) {
        case '(':
           T=new lparToken();
          break;
        case ')':
           T=new rparToken();
          break;
        case '[':
           T=new lbraToken();
          break;
        case ']':
           T=new rbraToken();
          break;
        case '|':
           T=new barToken();
          break;
      }
    }
    */
    else {
      char c=sval.charAt(0);
      if(!quoted&&(Character.isUpperCase(c)||'_'==c))
        T=make_var(sval);
      else { // nonvar
        String s=sval;
        int nt=nextToken();
        pushBack();
        T=('('==nt)?make_fun(s):make_const(s);
      }
    }
    return T;
  }
  
  protected Term next() throws IOException {
    int n=nextToken();
    inClause=true;
    Term T;
    switch(n) {
      case TT_WORD:
        T=getWord(false);
      break;
      
      case '\'':
        T=getWord(true);
      break;
      
      case TT_NUMBER:
        T=make_number(nval);
      break;
      
      case TT_EOF:
        T=new eofToken();
        inClause=false;
      break;
      
      case TT_EOL:
        T=next();
      break;
      
      case '-':
        if(TT_NUMBER==nextToken()) {
          T=make_number(-nval);
        } else {
          pushBack();
          T=make_const(char2string(n));
        }
      
      break;
      
      case ':':
        if('-'==nextToken()) {
          T=new iffToken(":-");
        } else {
          pushBack();
          T=make_const(char2string(n));
        }
      break;
      
      case '.':
        int c=nextToken();
        if(TT_EOL==c||TT_EOF==c) {
          inClause=false;
          // dict.clear(); ///!!!: this looses Var names
          T=new eocToken();
        } else {
          pushBack();
          T=make_const(char2string(n)); // !!!: sval is gone
        }
      break;
      
      case '\"':
        T=new stringToken((constToken)getWord(true));
      break;
      
      case '(':
        T=new lparToken();
      break;
      case ')':
        T=new rparToken();
      break;
      case '[':
        T=new lbraToken();
      break;
      case ']':
        T=new rbraToken();
      break;
      case '|':
        T=new barToken();
      break;
      
      case ',':
        T=new commaToken();
      break;
      default:
        T=make_const(char2string(n));
    }
    // IO.mes("TOKEN:"+T);
    return T;
  }
}

class varToken extends Fun {
  public varToken(Var X,Const C,Int I){
    super("varToken",3);
    args[0]=X;
    args[1]=C;
    args[2]=I;
  }
}

class intToken extends Fun {
  public intToken(int i){
    super("intToken",new Int(i));
  }
}

class realToken extends Fun {
  public realToken(double i){
    super("realToken",new Real(i));
  }
}

class constToken extends Fun {
  public constToken(Const c){
    super("constToken",c);
    args[0]=Builtins.toConstBuiltin(c);
  }
  
  public constToken(String s){
    this(new Const(s));
  }
}

class stringToken extends Fun {
  public stringToken(constToken c){
    super("stringToken",(c.args[0]));
  }
}

class funToken extends Fun {
  public funToken(String s){
    super("funToken",new Fun(s));
  }
}

class eocToken extends Fun {
  public eocToken(){
    super("eocToken",new Const("end_of_clause"));
  }
}

class eofToken extends Fun {
  public eofToken(){
    super("eofToken",Const.anEof);
  }
}

class iffToken extends Fun {
  public iffToken(String s){
    super("iffToken",new Const(s));
  }
}

class Token extends Const {
  Token(String s){
    super(s);
  }
}

class lparToken extends Token {
  public lparToken(){
    super("(");
  }
}

class rparToken extends Token {
  public rparToken(){
    super(")");
  }
}

class lbraToken extends Token {
  public lbraToken(){
    super("[");
  }
}

class rbraToken extends Token {
  public rbraToken(){
    super("]");
  }
}

class barToken extends Token {
  public barToken(){
    super("|");
  }
}

class commaToken extends Token {
  public commaToken(){
    super(",");
  }
}

/**
  Simplified Prolog parser:
  Synatax supported:
  a0:-a1,...,an.
  - no operators ( except toplevel :- and ,)
  - use quotes to create special symbol names, i.e.
  compute('+',1,2, Result) and  write(':-'(a,','(b,c)))
*/

public class Parser extends Lexer {
  
  public Parser(Reader I) throws IOException{
    super(I);
  }
  
  /*
    used in prolog2java
  */
  public Parser(String p,String s) throws IOException{
    super(p,s);
  }
  
  public Parser(String s) throws Exception{
    super(s);
  }
  
  /**
    Main Parser interface: reads a clause together
    with variable name information
  */
  public Clause readClause() {
    Clause t=null;
    boolean verbose=false;
    try {
      t=readClauseOrEOF();
      // IO.mes("GOT Clause:"+t);
    }
    /**
      catch built exception clauses which are defined
      in lib.pro - allowing to recover or be quiet about
      such errors.
    */
    catch(ParserException e) {
      t=errorClause(e,"syntax_error",lineno(),verbose);
      try {
        while(!atEOC()&&!atEOF())
          next();
      } catch(IOException toIgnore) {
      }
    } catch(IOException e) {
      t=errorClause(e,"io_exception",lineno(),verbose);
    } catch(Exception e) {
      t=errorClause(e,"unexpected_syntax_exception",lineno(),true);
    }
    return t;
  }
  
  static final Clause errorClause(Exception e,String type,int line,
      boolean verbose) {
    
    String mes=e.getMessage();
    if(null==mes)
      mes="unknown_error";
    Fun f=new Fun("error",new Const(type),new Const(mes),new Fun("line",
        new Int(line)));
    Clause C=new Clause(f,Const.aTrue);
    if(verbose) {
      IO.errmes(type+" error at line:"+line);
      IO.errmes(C.pprint(),e);
    }
    return C;
  }
  
  static public final boolean isError(Clause C) {
    Term H=C.getHead();
    if(H instanceof Fun&&"error".equals(((Fun)H).name())&&H.getArity()==3
        &&!(((Fun)H).args[0].ref() instanceof Var))
      return true;
    return false;
  }
  
  static public final void showError(Clause C) {
    IO.errmes("*** "+C);
  }
  
  static protected final Clause toClause(Term T,HashDict dict) {
    Clause C=T.toClause(); // adds ...:-true if missing
    C.dict=dict;
    return C;
  }
  
  private Clause readClauseOrEOF() throws IOException {
    
    dict=new HashDict();
    
    Term n=next();
    
    // IO.mes("readClauseOrEOF 0:"+n);
    
    if(n instanceof eofToken)
      return null; // $$toClause(n.token(),dict);
      
    if(n instanceof iffToken) {
      n=next();
      Term t=getTerm(n);
      Term bs=getConjCont(t);
      Clause C=new Clause(new Const("init"),bs);
      C.dict=dict;
      return C;
    }
    
    Term h=getTerm(n);
    
    // IO.mes("readClauseOrEOF 1:"+h);
    
    n=next();
    
    // IO.mes("readClauseOrEOF 2:"+n);
    
    if(n instanceof eocToken||n instanceof eofToken)
      return toClause(h,dict);
    
    // IO.mes("readClauseOrEOF 3:"+b);
    
    Clause C=null;
    if(n instanceof iffToken) {
      Term t=getTerm();
      Term bs=getConjCont(t);
      C=new Clause(h,bs);
      C.dict=dict;
    } else if(n instanceof commaToken) {
      Term b=getTerm();
      Term bs=getConjCont(b);
      C=toClause(new Conj(h,bs),dict);
    } else {
      throw new ParserException("':-' or '.' or ','","bad body element",n);
    }
    return C;
  }
  
  private final Term getConjCont(Term curr) throws IOException {
    
    Term n=next();
    Term t=null;
    if(n instanceof eocToken)
      t=curr;
    else if(n instanceof commaToken) {
      Term other=getTerm();
      t=new Conj(curr,getConjCont(other));
    }
    if(null==t) {
      throw new ParserException("'.'","bad body element",n);
    }
    return t;
  }
  
  protected final Term getTerm(Term n) throws IOException {
    Term t=n.token();
    if(n instanceof varToken||n instanceof intToken||n instanceof realToken
        ||n instanceof constToken) {
      // is just OK as it is
    } else if(n instanceof stringToken) {
      t=((Nonvar)((stringToken)n).args[0]).toChars();
      // IO.mes("getTerm:stringToken-->"+t);
      
    } else if(n instanceof lbraToken) {
      t=getList();
    } else if(n instanceof funToken) {
      Fun f=(Fun)t;
      f.args=getArgs();
      t=Builtins.toFunBuiltin(f);
    } else
      throw new ParserException("var,int,real,constant,'[' or functor",
          "bad term",n);
    return t;
  }
  
  protected Term getTerm() throws IOException {
    Term n=next();
    return getTerm(n);
  }
  
  private final Term[] getArgs() throws IOException {
    Term n=next();
    if(!(n instanceof lparToken))
      throw new ParserException("'('","in getArgs",n);
    ArrayList v=new ArrayList();
    Term t=getTerm();
    v.add(t);
    for(;;) {
      n=next();
      if(n instanceof rparToken) {
        int l=v.size();
        Term args[]=new Term[l];
        // v.copyInto(args);
        Object[] as=v.toArray();
        for(int i=0;i<l;i++) {
          args[i]=(Term)as[i];
        }
        return args;
      } else if(n instanceof commaToken) {
        t=getTerm();
        v.add(t);
      } else {
        throw new ParserException("',' or ')'","bad arg",n);
      }
    }
  }
  
  private final Term getList() throws IOException {
    Term n=next();
    if(n instanceof rbraToken)
      return Const.aNil;
    Term t=getTerm(n);
    return getListCont(t);
  }
  
  private final Term getListCont(Term curr) throws IOException {
    // IO.trace("curr: "+curr);
    Term n=next();
    Term t=null;
    if(n instanceof rbraToken)
      t=new Cons(curr,Const.aNil);
    else if(n instanceof barToken) {
      t=new Cons(curr,getTerm());
      n=next();
      if(!(n instanceof rbraToken)) {
        throw new ParserException("']'","bad list end after '|'",n);
      }
    } else if(n instanceof commaToken) {
      Term other=getTerm();
      t=new Cons(curr,getListCont(other));
    }
    if(t==null)
      throw new ParserException("| or ]","bad list continuation",n);
    return t;
  }
  
  private static final String patchEOFString(String s) {
    if(!(s.lastIndexOf(".")>=s.length()-2))
      s=s+".";
    return s;
  }
  
  public static Clause clsFromString(String s) {
    if(null==s)
      return null;
    s=patchEOFString(s);
    Clause t=null;
    try {
      Parser p;
      p=new Parser(s);
      t=p.readClause();
    } catch(Exception e) { // nothing expected to catch
      IO.errmes("unexpected parsing error",e);
    }
    if(t.dict==null)
      t.ground=false;
    else
      t.ground=t.dict.isEmpty();
    return t;
  }
  
}

class ParserException extends IOException {
  public ParserException(String e,String f,Term n){
    super("expected: "+e+", found: "+f+"'"+n+"'");
  }
}
