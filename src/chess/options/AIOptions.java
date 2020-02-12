/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.options;

/**
 *
 * @author Phoenix
 */
public class AIOptions {
    
    private int searchDepth;
    private int quietSearchDepth;
    private int transpositionTableSize;
    private int maxEvPositions;
    private int maxEvPositionsQuiet;
    private boolean peterMode;
    
    private static final int DEFAULT_SEARCH_DEPTH = 6;
    private static final int  DEFAULT_QUIET_SEARCH_DEPTH = 20;
    private static final int  DEFAULT_TRANSPOSITION_TABLE_SIZE = 20000000;   
    
    private static final boolean DEFAULT_PETERMODE = false; 
    
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

    public void setMaxEvPositions(int maxEvPositions) {
        this.maxEvPositions = maxEvPositions;
    }

    public void setMaxEvPositionsQuiet(int maxEvPositionsQuiet) {
        this.maxEvPositionsQuiet = maxEvPositionsQuiet;
    }

    public void setPeterMode(boolean peterMode) {
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

    public int getMaxEvPositions() {
        return maxEvPositions;
    }

    public int getMaxEvPositionsQuiet() {
        return maxEvPositionsQuiet;
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
