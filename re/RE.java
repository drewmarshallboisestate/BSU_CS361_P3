package re;

import java.io.FilterInputStream;

import fa.State;
import fa.nfa.NFA;
import fa.nfa.NFAState;

/**
 * December 9, 2022
 * This class parses through a regular expression and
 * creates a standard NFA. It uses recursive decent to
 * parse the regular expression and build the NFA
 * state by state. 
 * @author Drew Marshall    
 * @author Steven Lineses
 *
 */

public class RE implements REInterface{

    String regEx;  //String representation of the regular expression passed in from the file 
    int stateCount;  //Used to create new states just like we would in JFlap by clicking to add a state


    /**
     * Constructor to create and evaluate our regular expression
     * @param regEx - String representation of the regular expression passed in from the test files
     */
    public RE(String regEx) {
        this.regEx = regEx;
        stateCount = 0;  //Initialize our statecount to zero to begin before adding states
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
     * 
     * @return - the NFA representation of the regular expression 
    **/
    @Override
    public NFA getNFA() {

        return regex();
    }

    
    
    /**
     * Returns the next item of input without consuming it
     * @return - first item of the regular expression currently 
     */
    public char peek() {
        char peekABoo = regEx.charAt(0);
        return peekABoo;
    }

    /**
     * Returns the next item of input and consumes it
     * @return - character returned from peek()
     */
    public char next() {
        char retChar = peek();
        eat(retChar);
        return retChar;
    }

    /**
     * Consumes the next item of input, failing if not equal to item
     * @param item - character to be removed from the string
     */
    public void eat(char item) {
        //First check if item is equal to first char in our regEx string
        if (peek() == item) {
            this.regEx = this.regEx.substring(1);
        } else {  //If it's not a matching char then there is an error in our parsing
            throw new RuntimeException("Expected: " + item + "; got: " + peek());
        }
    }


    /**
     * Check if there are any more character in the regex string to be parsed
     * @return - boolean of if the string is empty or not
     */
    private boolean anyMore() {
        return (!regEx.isEmpty());
    }

    
    /**
     * NFA representation of the regular expression
     * Used to parse the regex
     * Regex can either go to a term '|' and another regex OR just go to a term
     * @return the NFA term representation of the regular expression
     */
    private NFA regex() {
        NFA NFATerm = term();
       
        if (anyMore() && peek() == '|') {  //If the regEX is not empty and the next char is a '|' 
            eat('|');  //Eat the '|' symbol which signifies we need a union/choice 
            return Choice(NFATerm, regex());  // '|' denotes the union so a choice of either this or that
        } else {
            return NFATerm;
        }
    }



    /**
     * Term goes to a factor
     * Is possibly an empty sequence of factors
     * Term has to check that is has not reached the boundary of a term
     * or the end of the input
     * 
     * @return - the NFA factor block of our parsing of the regular expression
     */
    private NFA term() {
        NFA NFAFactor = new NFA();  //Create a new factor as an NFA

        String helper = Integer.toString(stateCount);  //Create a string rep of a state created from our state count
        stateCount ++;   //Incremement state count as we are going to add a new state
        NFAFactor.addStartState(helper);  //Add the new string rep of state count as the start state
        NFAFactor.addFinalState(helper);  //Also add it as the final state 

        while (anyMore() && peek() != ')' && peek() != '|') {  //While our regEx isn't empty and the next chars don't equal ')' or '|'
            NFA nextFactor = factor();  //We know a term will create a factor and since there is more it must be joined by a following factor such as aa or ab
            NFAFactor = Sequence(NFAFactor, nextFactor);  //Amend to NFAFactor the concatenation of the next factor 
        }
    
        return NFAFactor;
    }

    /**
     * Sequence represents concatenating two NFA's together 
     * @param NFAFactor - our current factor 
     * @param nextFactor - the factor to be concatenated to the current factor 
     * @return - the complete NFA factor after concatenation 
     */
    private NFA Sequence(NFA NFAFactor, NFA nextFactor) {
        NFAFactor.addAbc(nextFactor.getABC());  //Add the alphabet from the next factor to our current factor 
        NFAState nextStarts = (NFAState) nextFactor.getStartState();  //The start state of next factor 
        
        //Simple for loop to iterate through the final states of our current factor 
        for (State state: NFAFactor.getFinalStates()) {
            ((NFAState) state).setNonFinal();  //Because of concatenation they will no longer be final states as they are going to transition to the next factor's start state
            ((NFAState) state).addTransition('e', nextStarts);  //Add an epsilon transition from current factors final states to the next factor's start state
        }

        NFAFactor.addNFAStates(nextFactor.getStates());  //Add all of the states from the next factor to our current factor 

        return NFAFactor;  //Now our current factor will have the following factor concatenated to it 
    }

 
    /**
     * Factor goes to a base followed by a '*'
     * Used to produce repetition
     * @return - the base block of our parsing of the regular expression
     */
    private NFA factor() {
        NFA baseNFA = base();  //Create a base version of the regular expression

        while (anyMore() && peek() == '*') {  //While there are more chars in the regEx and the next char equals a '*'
            eat('*');  //Remove the *
            baseNFA = Repetition(baseNFA);  //The kleene star then signals to us there is the repetition which means a self loop
        }

        return baseNFA;  //Base representation of our parsing of the regular expression
    }

    
    /**
     * Representation of * in a regular expression
     * @param baseNFA - base representation of our parsing of the regEx
     * @return - the baseNFA that will have the self loop
     */
    private NFA Repetition(NFA baseNFA) {
        NFAState internal = (NFAState) baseNFA.getStartState();  //Create a NFAState from the start state of the base 
        String transTo = internal.getName();  // the name of the state to be transitioned to later

        //Iterate through every final state in the base NFA 
        for (State s: baseNFA.getFinalStates()) {  
            String transFrom = s.getName();
            baseNFA.addTransition(transFrom, 'e', transTo);  //Add epsilon transition 
        }

        String newState = Integer.toString(stateCount);  //Create a new state 
        stateCount++;  //Update state count

        baseNFA.addStartState(newState);  
        baseNFA.addFinalState(newState);
        baseNFA.addTransition(newState, 'e', transTo);

        return baseNFA;
    }

    
    /**
     * Union of two NFA's
     * @param first - First NFA 
     * @param second - Second NFA 
     * @return - NFA that is a union of the two NFA's passed in
     */
    public NFA Choice(NFA first, NFA second) {  //AKA Union 
        NFAState firstNFA = (NFAState) first.getStartState();
        NFAState secondNFA = (NFAState) second.getStartState();

        first.addNFAStates(second.getStates());  //Add all states from the second NFA to the first NFA
        first.addAbc(second.getABC());  //Add the alphabet from the second NFA to the first NFA

        String newStart = Integer.toString(stateCount);
        stateCount ++;
        
        //To union NFA's you need to add an intial state with an e-transition
        //to each of the initial start states of the original NFA's 
        first.addStartState(newStart);
        first.addTransition(newStart, 'e', firstNFA.getName());
        first.addTransition(newStart, 'e', secondNFA.getName());;

        String toBeFinal = Integer.toString(stateCount);
        stateCount ++;
        first.addFinalState(toBeFinal);

        for (State state: second.getFinalStates()) {
            ((NFAState) state).setNonFinal();
            first.addTransition(((NFAState) state).getName(), 'e', toBeFinal);
        }

        return first;  //First NFA will now be the union of the first and second NFA
    }

    
    /**
     * Base goes to a char OR a '\' char OR a '(' regex ')'
     * @return the NFA representation of the regular expression
     */
    private NFA base() {

        char baseChar = peek();  //The char from the regEx 

        //BaseChar will either be a character or a ()
        switch(baseChar) {
            case ('('):
                eat('(');
                NFA expression = regex();  //Go back to regex for parsing 
                eat(')');
                return expression;
            default:
                char nextToParse = next();
                return Primitive(nextToParse);  //The next char to be parsed 
        }     
    }

    /**
     * Last case of regex will hold an individual character
     * @param next - character from regEx to be parsed 
     * @return - the NFA of the char
     */
    private NFA Primitive(char next) {

        NFA baseHelper = new NFA();  //Create an NFA
        
        String charState = Integer.toString(stateCount);  //New state to be added
        stateCount ++;
        baseHelper.addStartState(charState);  //Add our new state to the NFA

        String secondChar = Integer.toString(stateCount);  //Next state to be added
        stateCount ++;
        baseHelper.addFinalState(secondChar);  //Add the second state as a final state
        baseHelper.addTransition(charState, next, secondChar);  //Create the transition from the character parsed from the regular expression

        return baseHelper;  //The NFA that will now contain the transition from the regular expression 
    }

}
