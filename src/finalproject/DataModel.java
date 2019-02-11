/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package djbzf5finalproject;

import java.io.File;
import java.util.LinkedList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;

/**
 *
 * @author daymoose
 */
public class DataModel {
    
    private LinkedList<File> files = new LinkedList<>();
    private LinkedList<MediaPlayer> playlist = new LinkedList<>();
    
    private int currentTrack = 0;
    private int maxTracks = 0;
    
    public LinkedList<File> getFiles(){
        return files;
    } 
    public LinkedList<MediaPlayer> getTracks(){
        return playlist;
    }
    public int getIndex(){
        return currentTrack;
    }
    public int getMaxIndex(){
        return maxTracks - 1;
    }    
    
    public MediaPlayer getCurrentTrack(){
        if(!playlist.isEmpty()){
            return playlist.get(currentTrack);
        } else return null;
    }
    public MediaPlayer getMaxTrack(){
        return playlist.get(maxTracks - 1);
    }
    public File getCurrentFile(){
        if(!files.isEmpty()){
            return files.get(currentTrack);
        } else return null;
    } 
    public double getTotalDuration(){
        return getCurrentTrack().getTotalDuration().toMillis();
    }    
    public double getCurrentTime(){
        return getCurrentTrack().getCurrentTime().toMillis();
    }    
    public double getVolume(){
        return getCurrentTrack().getVolume();
    }
    public String getName(){
        return getCurrentFile().getName();
    }
    
    public void play(){
        getCurrentTrack().play();
    }    
    public void pause(){
        getCurrentTrack().pause();
    }    
    public void stop(){
        playlist.get(currentTrack).stop();
    } 
    public void seek(Duration duration){
        getCurrentTrack().pause();
        getCurrentTrack().seek(duration);
    }
    public void loopFromTop(){
        
        currentTrack = maxTracks - 1;
        seek(Duration.ZERO);
        play();
        
    }    
    public void loopFromBottom(){
        currentTrack = 0;
        seek(Duration.ZERO);
        play();
    }    
    public void playPrev(){
        currentTrack--;
        seek(Duration.ZERO);
        play();
    }    
    public void playNext(){
        currentTrack++;
        seek(Duration.ZERO);
        play();
    }
    public Status getStatus(){
        return playlist.get(currentTrack).getStatus();
    } 
    
    public boolean isEmpty(){
        return playlist.isEmpty();
    }    
    public boolean isNotEmpty(){
        return !playlist.isEmpty();
    }    
    public boolean isAtBeginning(){
        if(currentTrack == 0){
            return true;
        }else return false;
    }
    
    public void addTrack(File file){
        playlist.add(new MediaPlayer(new Media(file.toURI().toString())));
        files.add(file);
        maxTracks++;
    }        
    public void setIndex(int index){
        currentTrack = index;
    }
    public void reset(){
        
        if(isNotEmpty())
            playlist.get(currentTrack).stop();
        
        files.clear();
        playlist.clear();
        
        currentTrack = 0;
        maxTracks = 0;
        
    }    
    public void setAutoPlay(boolean bool){
        getCurrentTrack().setAutoPlay(bool);
    }    
    public void setVolume(double val){
        getCurrentTrack().setVolume(val);
    }    
                
    
    
}
