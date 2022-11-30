package re;

import fa.nfa.NFA;

public class RE implements REInterface{

    public RE(String regEx) {
        
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

}
