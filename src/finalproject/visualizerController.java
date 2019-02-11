package djbzf5finalproject;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;

public class visualizerController extends superController {
    
    //some local variables
    @FXML private AnchorPane anchorPane;    
    @FXML public MenuItem playlistItem;    
    @FXML public MenuItem visualizerItem;
    @FXML
    private Slider timeSlider;
    @FXML
    private Button playButton;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Label songTitle;
    @FXML
    private AnchorPane visualizerPane;
    
    //new playlist handler
    @FXML private void handleNew(Event event){
        
        //open a file chooser
        Stage primaryStage = (Stage)anchorPane.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(primaryStage);
        
        //if the selected file isn't null
        if(file != null){
            
            //and the playlist isn't empty
            if(model.isNotEmpty()){
                
                //and it's playing
                if(model.getStatus() == MediaPlayer.Status.PLAYING){
                    //seek to the beginning
                    model.seek(Duration.ZERO);
                } //if it's paused
                  else if (model.getStatus() == MediaPlayer.Status.PAUSED) {
                    //do the same
                    model.seek(Duration.ZERO);
                }
            }
            
            //clear the playlist
            model.reset();
            //add the file
            model.addTrack(file);
            //set the handlers, etc
            openMedia();
            handleChange();
        }
    }    
    //add another track handler
    @FXML private void handleAdd(Event event){
        
        //open a file chooser
        Stage primaryStage = (Stage)anchorPane.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(primaryStage);
        
        //if it's there
        if(file != null){
            
            //see if the playlist was empty
            boolean wasEmpty = model.isEmpty();
            
            //add the file to the playlist
            model.addTrack(file);
            
            //set some handlers and the audiospectrum listener/update interval
            try{                
                model.getMaxTrack().setOnReady(() -> {
                    handleReady();
                });            
                model.getMaxTrack().setOnEndOfMedia(() -> {
                    handleEndOfMedia();
                });            
                model.getMaxTrack().setAudioSpectrumInterval(updateInterval);                
                model.getMaxTrack().setAudioSpectrumListener((double timestamp, double duration, float[] magnitudes, float[] phases) -> {
                    handleUpdate(timestamp, duration, magnitudes, phases);
                });            
            } //catch a generic exception
              catch (Exception ex){
                System.out.println(ex.toString());
              }
            
            //if the playlist was empty, play the new track and set the song title/play button
            if(wasEmpty){
                model.play();
                songTitle.setText(model.getName());
                playButton.setText("Pause");
            }
        }
    }        
    //handle the play/pause button
    @FXML private void handlePlay(ActionEvent event){
        
        //if it's pause, set it to play and pause the track
        if(playButton.getText().equals("Pause")){
            model.pause();
            playButton.setText("Play");
        }
        //if it's play, play the track and set it to pause
        else if(playButton.getText().equals("Play")){
            model.play();
            songTitle.setText(model.getName());
            playButton.setText("Pause");
        }
        
    }    
    //handle the pressing of the slider
    @FXML private void handleSliderMousePressed(Event event){
        
        if(model.getCurrentTrack() != null){
            model.getCurrentTrack().pause();
        }
        
    }    
    //and the release of the slide
    @FXML private void handleSliderMouseReleased(Event event){
        
        if(model.getCurrentTrack() != null){
            model.seek(new Duration(timeSlider.getValue()));
            model.play();
            playButton.setText("Pause");
        }
        
    }    
    //handle the next button
    @FXML private void handleNext(ActionEvent event){
        
        //just end the media, it's easier
        handleEndOfMedia();
        
    }    
    //handle the previous button
    @FXML private void handlePrev(ActionEvent event){
        
        //if the playlist is not empty
        if(model.isNotEmpty()){
            
            //and the current track is the first one
            if(model.isAtBeginning()){
                
                //seek to zero
                model.seek(Duration.ZERO);
                //and set the time slider to zero
                timeSlider.setValue(0);
                
                //loop from the top to the bottom, set the new time slider max,  and song title
                model.loopFromTop();                
                timeSlider.setMax(model.getTotalDuration());
                songTitle.setText(model.getName());                
                //set the play button to pause
                playButton.setText("Pause");
                
            } //if it's not at the beginning
              else {
                
                //seek to zero
                model.seek(Duration.ZERO);
                //play the previous track
                model.playPrev();
                
                //set the time slider to zero, and the max to the new max
                timeSlider.setValue(0);
                timeSlider.setMax(model.getTotalDuration()); 
                //change the song title to the new one and set the play button to pause
                songTitle.setText(model.getName());
                playButton.setText("Pause");
                
            }
            
        }

        
    }
    //switch to the playlist view
    @FXML public void switchScenesPlaylist(ActionEvent event){
        
        //switch to the playlist and handle the change to it
        switchTo("playlist");
        getControllersByName("playlist").handleChange();
        
            
    }     
    //switch to the visualizer
    @FXML public void switchScenesVisualizer(ActionEvent event){
        
        //switch to the visualizer and handle its change
        switchTo("visualizer");
        getControllersByName("visualizer").handleChange();

    }    
    //handle clearing the playlist
    public void handleClear(){
        
        //if the playlist is not empty
        if(model.isNotEmpty())
            //stop the current track
            model.stop();
        
        //reset the playlist
        model.reset();
        //and the time slider
        timeSlider.setValue(0);
        //and change the song title to the new one
        songTitle.setText("");
    }
    
    //initialize the visualizer
    @Override public void initialize(URL url, ResourceBundle rb) {
        
        //if the playlist is not empty, set some handlers
        if(model.isNotEmpty()){
            openMedia();
        }        
    }    
    //handle the change to here
    @Override public void handleChange(){
    
        //if the playlist is not empty
        if(model.isNotEmpty()){
            
            //set the min, max, and current value of the time slider to reflect the current track
            timeSlider.setMin(0);
            timeSlider.setMax(model.getTotalDuration());
            timeSlider.setValue(model.getCurrentTime());
            
            //set the visualizer number of bands, update interval, and listener
            model.getCurrentTrack().setAudioSpectrumNumBands(numBands);
            model.getCurrentTrack().setAudioSpectrumInterval(updateInterval);
            model.getCurrentTrack().setAudioSpectrumListener((double timestamp, double duration, float[] magnitudes, float[] phases) ->{                       
                handleUpdate(timestamp, duration, magnitudes, phases);
            });
            
            //change the song title
            songTitle.setText(model.getName());
            //start the visualizer
            visualizer.start(numBands, getControllersByName("visualizer").visualizerPane);            
            //set the volume slider to the current volume level
            volumeSlider.setValue(model.getVolume() * 100);
            
            //if it's playing
            if(model.getStatus() == Status.PLAYING){
                //set the button to pause
                playButton.setText("Pause");
            } //or play if it's not playing
              else {
                playButton.setText("Play");
            }
        }
                
    }
    
    //set some handlers for the media
    private void openMedia(){
        
        //try to set some handlers, the audio spectrum numbands, update interval, and listener
        try{
            model.getCurrentTrack().setOnReady(() -> {
               handleReady(); 
            });            
            model.getCurrentTrack().setOnEndOfMedia(() -> {
                handleEndOfMedia();
            });            
            model.getCurrentTrack().setAudioSpectrumNumBands(numBands);
            model.getCurrentTrack().setAudioSpectrumInterval(updateInterval);
            model.getCurrentTrack().setAudioSpectrumListener((double timestamp, double duration, float[] magnitudes, float[] phases) ->{                        
                handleUpdate(timestamp, duration, magnitudes, phases);
            });
            
            //auto play the track
            model.setAutoPlay(true);
            //set the new song title
            songTitle.setText(model.getName());
            //and change the play button to pause
            playButton.setText("Pause");
            
        } //catch a generic exception
          catch (Exception ex){
            System.out.println(ex.toString());
        }
    }

    
}
