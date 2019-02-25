package es.uam.eps.bmi.search;

import es.uam.eps.bmi.search.index.Index;

/**
 *
 * @author pablo
 */
public abstract class AbstractEngine implements SearchEngine {
    protected Index index;
    
    public AbstractEngine(Index idx) {
        index = idx;
    }
    
    public Index getIndex() {
        return index;
    }

    public String[] parse(String query) {
        return query.toLowerCase().split("\\P{Alpha}+");
    }
}
