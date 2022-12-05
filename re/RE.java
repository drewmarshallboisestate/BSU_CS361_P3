package re;

import java.io.FilterInputStream;

import fa.State;
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

        return regex();
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


    //Check if there are any more character in the regex string to be parsed
    private boolean anyMore() {
        return regEx.length() > 0;
    }

    //NFa representation of the regular expression
    //Used to parse the regex
    //Regex can either go to a term '|' and another regex OR just go to a term
    private NFA regex() {
        NFA NFATerm = term();
       
        if (anyMore() && (peek() == '|')) {
            eat('|');
            NFA retNFA = regex();
            return Choice(NFATerm, retNFA);  // '|' denotes the union so a choice of either this or that
        } else {
            return NFATerm;
        }
    }


    //Term goes to a factor
    //Is possibly an empty sequence of factors 
    //Term has to check that is has not reached the boundary of a term
    //or the end of the input
    private NFA term() {
        NFA NFAFactor = new NFA();

        NFAFactor.addStartState(Integer.toString(stateCount++));
        NFAFactor.addStartState(regEx);
        NFAFactor.addFinalState(regEx);

        while (anyMore() && peek() != ')' && peek() != '|') {
            NFA nextFactor = factor();
            NFAFactor = Sequence(NFAFactor, nextFactor);
        }
    
        return NFAFactor;
    }

    private NFA Sequence(NFA nFAFactor, NFA nextFactor) {
        
        return null;
    }

    //Factor goes to a base followed by a '*'
    //Used to produce repetition
    private NFA factor() {
        NFA baseNFA = base();

        while (anyMore() && peek() == '*') {
            eat('*');
            baseNFA = Repetition(baseNFA);
        }
        return baseNFA;
    }

    //Representation of *
    private NFA Repetition(NFA baseNFA) {
        NFAState internal = (NFAState) baseNFA.getStartState();
        String transTo = internal.getName();

        for (State s: baseNFA.getFinalStates()) {
            String transFrom = s.getName();
            baseNFA.addTransition(transFrom, 'e', transTo);
        }

        String newState = Integer.toString(stateCount++);

        baseNFA.addStartState(newState);
        baseNFA.addFinalState(newState);
        baseNFA.addTransition(newState, 'e', transTo);

        return baseNFA;
    }

    //Union of two NFA's
    public NFA Choice(NFA first, NFA second) {  //AKA Union 
        NFAState firstNFA = (NFAState) first.getStartState();
        NFAState secondNFA = (NFAState) second.getStartState();

        //To union NFA's you need to add an intial state with an e-transition
        //to each of the initial start states of the original NFA's 
        first.addNFAStates(second.getStates());
        first.addAbc(second.getABC());  

        String newStart = Integer.toString(stateCount++);

        first.addStartState(newStart);
        
        first.addTransition(newStart, 'e', firstNFA.getName());
        first.addTransition(newStart, 'e', secondNFA.getName());;

        return first;    
    }

    //Base goes to a char OR a '\' char OR a '(' regex ')'
    private NFA base() {

        char baseChar = peek();

        switch(baseChar) {
            case ('('):
                eat('(');
                NFA expression = regex();
                eat(')');
                return expression;
            default:
                return Primitive(next());
        }     
    }

    //Last case of regex will hold an individual character
    private NFA Primitive(char next) {

        NFA baseHelper = new NFA();
        
        String charState = Integer.toString(stateCount++);
        baseHelper.addStartState(charState);

        String secondChar = Integer.toString(stateCount++);
        baseHelper.addFinalState(secondChar);
        baseHelper.addTransition(charState, next, secondChar);

        return null;
    }

}
