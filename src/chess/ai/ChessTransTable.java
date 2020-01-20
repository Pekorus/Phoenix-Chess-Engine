/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.ai;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 *
 * @author Phoenix
 */
public class ChessTransTable {
    
    private final Int2ObjectOpenHashMap<TransTableEntry> hashMap;
    private final int mapSize;
    private int hashCollisions;
    
    public ChessTransTable(int entryCount) {
        this.mapSize = entryCount+1;
        this.hashMap = new Int2ObjectOpenHashMap(mapSize, 1.0f); 
        this.hashCollisions = 0;
    }
    
    public void insertEntry(TransTableEntry entry){
        //TODO: store in buckets if collision happens        
        int key = zobrisToHashMapKey(entry.getZobristKey());
        
        /* position is already in table */
        if(hashMap.containsKey(key)){
            TransTableEntry auxEntry = hashMap.get(key);
            if(auxEntry.getZobristKey() == entry.getZobristKey()) 
                auxEntry.setOldFlag(false);
            if(auxEntry.getOldFlag() || auxEntry.getDepth()>= entry.getDepth())
                hashMap.replace(key, entry);    
        }
        else hashMap.put(key, entry);
    }

    /* returns the table entry correlating to the given zobris key. Returns null
        if there is no entry with this zobris key. 
    */
    public TransTableEntry getEntryByZobrisKey(long zobris){
        
        int key = zobrisToHashMapKey(zobris);
        if(hashMap.containsKey(key)){
            TransTableEntry auxEntry = hashMap.get(key);
            if(auxEntry.getZobristKey() == zobris) return auxEntry;
        }            
        return null;
    }

    private int zobrisToHashMapKey(long zobris){
        return (int) (zobris %mapSize);
    } 

    public int getHashCollisions() {
        return hashCollisions;
    }

    public int getHashFilled(){
        return hashMap.size();
    }
    
    public void resetCollisionCounter(){
        hashCollisions = 0;
    }
    void clear() {
        hashMap.clear();
    }
}
