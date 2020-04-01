package chess.options;

/**
 *
 * Provides a collection of options for a chess AI.
 */
public class AIOptions {
    
    /* maximal search depth */
    private int searchDepth;
    /* maximal search depth of quiescence search */
    private int quietSearchDepth;
    /* size of transposition table */
    private int transpositionTableSize;
    /* play in creator mode */
    private boolean peterMode;
    
    /* default values */
    private static final int DEFAULT_SEARCH_DEPTH = 6;
    private static final int  DEFAULT_QUIET_SEARCH_DEPTH = 20;
    private static final int  DEFAULT_TRANSPOSITION_TABLE_SIZE = 20000000;   
    private static final boolean DEFAULT_PETERMODE = false; 
    
    /**
     * Class constructor. Options will be initialized with default values.
     */
    public AIOptions() {
        
        searchDepth = DEFAULT_SEARCH_DEPTH;
        quietSearchDepth = DEFAULT_QUIET_SEARCH_DEPTH;
        transpositionTableSize = DEFAULT_TRANSPOSITION_TABLE_SIZE;
        peterMode = DEFAULT_PETERMODE;    
    }

    public void setSearchDepth(int searchDepth) {
        this.searchDepth = searchDepth;
    }

    public void setQuietSearchDepth(int quietSearchDepth) {
        this.quietSearchDepth = quietSearchDepth;
    }

    public void setTranspositionTableSize(int transpositionTableSize) {
        this.transpositionTableSize = transpositionTableSize;
    }

    public void setCreatorMode(boolean peterMode) {
        this.peterMode = peterMode;
    }

    public int getSearchDepth() {
        return searchDepth;
    }

    public int getQuietSearchDepth() {
        return quietSearchDepth;
    }

    public int getTranspositionTableSize() {
        return transpositionTableSize;
    }

    public boolean isPeterMode() {
        return peterMode;
    }

    public int getDefaultSearchDepth() {
        return DEFAULT_SEARCH_DEPTH;
    }

    public int getDefaultQuietSearchDepth() {
        return DEFAULT_QUIET_SEARCH_DEPTH;
    }

    public static int getDEFAULT_TRANSPOSITION_TABLE_SIZE() {
        return DEFAULT_TRANSPOSITION_TABLE_SIZE;
    }
    
}
