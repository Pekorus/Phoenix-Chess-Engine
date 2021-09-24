package chess.options;

import static chess.options.CalcType.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
    /* time to restrict length of an ai move in milliseconds*/     
    private long turnTime; 
    /* type of restriction on move calculation of ai */
    private CalcType calcType;
    
    /* default values */
    private final int DEFAULT_SEARCH_DEPTH = 6;
    private final int  DEFAULT_QUIET_SEARCH_DEPTH = 20;
    private final int  DEFAULT_TRANSPOSITION_TABLE_SIZE = 20000000;   
    private final boolean DEFAULT_PETERMODE = false; 
    private final long DEFAULT_TURN_TIME = 6000;
    private final CalcType DEFAULT_CALC_TYPE = DEPTH;
    
    /**
     * Class constructor. Options will be initialized with default values.
     */
    public AIOptions() {
        
        resetDefaultOptions();
    
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

    public void setTurnTime(long turnTime) {
        this.turnTime = turnTime;
    }

    public long getTurnTime() {
        return turnTime;
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

    public void setCalcType(CalcType calcType) {
        this.calcType = calcType;
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

    public long getDefaultTurnTime() {
        return DEFAULT_TURN_TIME;
    }

    public CalcType getCalcType() {
        return calcType;
    }

    /**
     * Writes Options to the file "Options.txt" to save them for future program
     * starts. Every field of this class is written to a line in the same order
     * as declared.
     */
    public void saveOptions () {
        
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(
                                                     "Options.txt", false));) {
       
            writer.write(Integer.toString(searchDepth));
            writer.newLine();
            writer.write(Integer.toString(quietSearchDepth));        
            writer.newLine();
            writer.write(Integer.toString(transpositionTableSize));
            writer.newLine();
            writer.write(Boolean.toString(peterMode));    
            writer.newLine();
            writer.write(Long.toString(turnTime));        
            writer.newLine();
            /* Save calcType as int: 0 is DEPTH, 1 is TIME */
            if(calcType == DEPTH) writer.write("0");         
            else writer.write("1");
            
            writer.flush();
            writer.close();
        
        } catch (IOException ex) {
            
        }        
    }
    
    /**
     * Loads and sets the fields of this class according to the "Options.txt" 
     * file. Sets all fields to default values if file doesn't exist or can't be
     * read.
     */
    public void loadOptions(){
        
        try {
            
            BufferedReader reader = new BufferedReader (new FileReader(
                                                               "Options.txt"));
        
            searchDepth = Integer.parseInt(reader.readLine());
            quietSearchDepth = Integer.parseInt(reader.readLine());
            transpositionTableSize = Integer.parseInt(reader.readLine());
            peterMode = Boolean.parseBoolean(reader.readLine());
            turnTime = Long.parseLong(reader.readLine());
            /* calculation type is saved as int, 0 = depth, 1 = time */
            if(Integer.parseInt(reader.readLine()) == 0)
                calcType = DEPTH;
            else calcType = TIME;
            
            reader.close();
            
        } catch (IOException ex) {
            resetDefaultOptions();
        }
        
    }

    /**
     * Resets all fields of this class to its default values.
     */
    private void resetDefaultOptions() {
        
        searchDepth = DEFAULT_SEARCH_DEPTH;
        quietSearchDepth = DEFAULT_QUIET_SEARCH_DEPTH;
        transpositionTableSize = DEFAULT_TRANSPOSITION_TABLE_SIZE;
        peterMode = DEFAULT_PETERMODE;    
        turnTime = DEFAULT_TURN_TIME;
        calcType = DEFAULT_CALC_TYPE;
    }
}
