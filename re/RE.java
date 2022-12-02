package re;

import fa.nfa.NFA;
import fa.nfa.NFAState;

public class RE implements REInterface{

    String regEx;
    int stateCount;

    public RE(String regEx) {
        this.regEx = regEx;
        stateCount = 0;
    }

    /**
     * Returns the equivalent NFA for the RegEx used to instantiate RE object
     * Should parse a RegEX and build an NFA from the leaves up of the corresponding parse tree
     * 
     * NOTE: Don't build a parse tree, build an NFA as follows 
     *  private NFA factor(){
     *  NFA baseNFA = base();
     *  ...
     *  return baseNFA;
     *  }
    **/
    @Override
    public NFA getNFA() {

        return null;
    }

    
    //Returns the next item of input without consuming it
    public char peek() {
        char peekABoo = regEx.charAt(0);
        return peekABoo;
    }

    //Returns the next item of input and consumes it
    public char next() {
        char retChar = peek();
        eat(retChar);
        return retChar;
    }

    //Consumes the next item of input, failing if not equal to item
    public void eat(char item) {
        //First check if item is equal to first char in our regEx string
        if (peek() == item) {
            this.regEx = this.regEx.substring(1);
        } else {
            throw new RuntimeException("Expected: " + item + "; got: " + peek());
        }
    }

    private boolean anyMore() {
        return regEx.length() > 0;
    }

    private NFA base() {
        return null;
        
    }

    private NFA regex() {
        NFA NFATerm = term();
       
        if (anyMore() && (peek() == '|')) {
            eat('|');
            NFA retNFA = regex();
            return choice(NFATerm,retNFA);
        } else {
            return NFATerm;
        }
       
    }

    private NFA term() {
        NFA NFAFactor = new NFA();

        NFAFactor.addStartState(Integer.toString(stateCount));
        stateCount ++;

        
        return null;
    }

    private NFA factor() {
        NFA baseNFA = base();

        return baseNFA;
    }

    public NFA choice(NFA first, NFA second) {
        NFAState firstNFA = (NFAState) first.getStartState();
        NFAState secondNFA = (NFAState) second.getStartState();
        
        return second;    
    }

}
