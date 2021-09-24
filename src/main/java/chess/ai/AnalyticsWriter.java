package chess.ai;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a writer for AI analytics to the text file "AI analytics". Only used
 * in testing. 
 */
public class AnalyticsWriter {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    private BufferedWriter writer;    
    
    /**
     * Class constructor.
     */
    public AnalyticsWriter() {
        try {
            this.writer = new BufferedWriter(new FileWriter("AI analytics.txt", 
                    true));
        } catch (IOException ex) {

        }
    } 
    
    /**
     * Marks start of game and writes relevant information including timestamp.
     */
    public void writeNewGame() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            writer.newLine();
            writer.write("Start of game: ");
            writer.write(dateFormat.format(Calendar.getInstance().getTime()));
            writer.newLine();
            writer.newLine();
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(AnalyticsWriter.class.getName()).log(Level.SEVERE, 
                    null, ex);
        }
    }

    /**
     * Marks end of game and closes stream.
     */
    public void endGame() {
        try {        
            writer.write("End of game");
            writer.newLine();
            writer.newLine();
            writer.flush();
            
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(AnalyticsWriter.class.getName()).log(Level.SEVERE, 
                    null, ex);
        }
    }
    
    /**
     * Provides a method to write the analytics data of a turn.
     * 
     * @param currentLabel the analytics data to be written 
     */
    public void writeAnalyticsTurn(String currentLabel) {
        
        try {
            if(currentLabel != null) writer.write(currentLabel);
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(AnalyticsWriter.class.getName()).log(Level.SEVERE, 
                    null, ex);
        }
    }
    
    /**
     * Provides a method to write the move played by a player.
     * 
     * @param move the move to be written
     */
    public void movePlayed(String move) {
        try {
            writer.write("Move played: "+ move);
            writer.newLine();
            writer.newLine();
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(AnalyticsWriter.class.getName()).log(Level.SEVERE, 
                    null, ex);
        }
    }
    
}
