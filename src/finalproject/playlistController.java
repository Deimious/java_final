package djbzf5finalproject;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;

public class playlistController extends superController {
    
    //some local variables that correspond to the playlist view
    @FXML public ListView<String> mediaList;    
    @FXML private AnchorPane anchorPane;    
    @FXML public MenuItem playlistItem;    
    @FXML public MenuItem visualizerItem;
    @FXML private Slider timeSlider;
    @FXML private Button playButton;
    @FXML private Slider volumeSlider;
    @FXML private Label songTitle;
    
    //handle opening a new file in a new playlist
    @FXML private void handleNew(Event event){
        
        //open a  file chooser to pick a music file
        Stage primaryStage = (Stage)anchorPane.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(primaryStage);
        
        //if the file is there
        if(file != null){
            
            //if the model is not empty
            if(model.isNotEmpty())
                //if it's playing or paused
                if(model.getStatus() == Status.PLAYING || model.getStatus() == Status.PAUSED)
                    //seek to the beginning of the trackc
                    model.seek(Duration.ZERO);
            
            //reset the whole playlist
            model.reset();
            //add the file to the playlist
            model.addTrack(file);
            //clear the media list
            mediaList.getItems().clear();
            //add the file path to the media list
            mediaList.getItems().addAll(file.getPath());
            //open that media
            openMedia();            
        }
    }    
    //handle adding a track to the playlist
    @FXML private void handleAdd(Event event){
        
        //open a file chooser
        Stage primaryStage = (Stage)anchorPane.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(primaryStage);
        
        //if the selected file is there
        if(file != null){
            
            //see whether the playlist was empty or not
            boolean wasEmpty = model.isEmpty();
            //add the file to the playlist
            model.addTrack(file);
            //add the file path to the medialist
            mediaList.getItems().addAll(file.getPath());
            
            //try to set the track handlers
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
            
            //if the playlist was empty, play the track and set the song title and set the play button
            if(wasEmpty){
                model.play();
                songTitle.setText(model.getName());
                playButton.setText("Pause");
            }
        }
    }    
    //handle playing/pausing the current track
    @FXML private void handlePlay(ActionEvent event){
        
        //if the text is pause, pause the track and set the text to play
        if(playButton.getText().equals("Pause")){
            model.pause();
            playButton.setText("Play");
        }
        
        //if it's play, play the track and set the text to pause
        else if(playButton.getText().equals("Play")){
            model.play();
            songTitle.setText(model.getName());
            playButton.setText("Pause");
        }
        
    }    
    //handle pressing the slider mouse button
    @FXML private void handleSliderMousePressed(Event event){
        //if the model is not empty, pause the track
        if(model.isNotEmpty()){
            model.pause();
        }
    }    
    //handle releasing the slider mouse button
    @FXML private void handleSliderMouseReleased(Event event){
        //if the model is not empty, seek to the new position and play at the new position
        if(model.isNotEmpty()){
            model.seek(new Duration(timeSlider.getValue()));
            model.play();
            playButton.setText("Pause");
        }
    }    
    //handle the next track button
    @FXML private void handleNext(ActionEvent event){
        //just end the media, it's easier
        handleEndOfMedia();
        
    }    
    //handle the previous track button
    @FXML private void handlePrev(ActionEvent event){
        
        //if the model is not empty
        if(model.isNotEmpty()){
            
            //and it is at the beginning of the track
            if(model.isAtBeginning()){
                
                //reset the track time
                model.seek(Duration.ZERO);
                //and the time slider
                timeSlider.setValue(0);
                
                //and loop from the top
                model.loopFromTop();
               
                //then set the max value of the time slider to the new track max value
                timeSlider.setMax(model.getTotalDuration());
                //change the song title
                songTitle.setText(model.getName());
                //set the play button
                playButton.setText("Pause");
                //and select the appropriate index in the medialist
                mediaList.getSelectionModel().select(model.getIndex());
                
            } //if it's anywhere except the beginning
              else {
                
                //reset the track time
                model.seek(Duration.ZERO);
                //play the previous track
                model.playPrev();
                
                //reset the time slider and change its max value
                timeSlider.setValue(0);
                timeSlider.setMax(model.getTotalDuration());                                
                
                //change the song title, play button text, and select the appropriate index in the medialist
                songTitle.setText(model.getName());
                playButton.setText("Pause");
                mediaList.getSelectionModel().select(model.getIndex());
                
            }
            
        }
        
    }    
    //handle selecing an item in the list
    private void handleListSelection(MouseEvent event){
        
        //get the current index
        int index = mediaList.getSelectionModel().getSelectedIndex();

        //try to select a value in the list
        try{
            
            //reset the track time, set the new current track, and seek to zero again
            model.seek(Duration.ZERO);            
            model.setIndex(index);
            model.seek(Duration.ZERO);
            
            //resets time slider and changes its max value to the new track's max duration
            timeSlider.setValue(0);
            timeSlider.setMax(model.getTotalDuration());
           
            //play the current track
            model.play();
            
            //change the song title, set the play button to pause, and select the new index in the media list
            songTitle.setText(model.getName());
            playButton.setText("Pause");
            mediaList.getSelectionModel().select(model.getIndex());
        } //and catch a generic exception
          catch(Exception ex){
            System.out.println(ex.toString());
        }
    }    
    //handle clearing the playlist
    public void handleClear(){
        
        //if the playlist is not empty
        if(model.isNotEmpty())
            //stop it
            model.stop();
        
        //clear the media list, reset the playlist, reset the time slider, and set the song title text to null
        mediaList.getItems().clear();
        timeSlider.setValue(0);
        songTitle.setText("");
        model.reset();
        
    }
    //handle switching to playlist, doesn't do anything
    @FXML public void switchScenesPlaylist(ActionEvent event){
        
        switchTo("playlist");
        getControllersByName("playlist").handleChange();

    }    
    //handle switching to the visualizer
    @FXML public void switchScenesVisualizer(ActionEvent event){
        
        //switch to it and handle the change to it
        switchTo("visualizer");
        getControllersByName("visualizer").handleChange();

    }
    
    //initialize handler, doesn't do anything
    @Override public void initialize(URL url, ResourceBundle rb) {}        
    //handle the change to this view
    @Override public void handleChange(){
        
        //clear the items in the list
        mediaList.getItems().clear();
        
        //get the file list and add each file in the playlist to the media list, essentially refreshing it
        model.getFiles().forEach((file) -> {
            mediaList.getItems().add(file.getAbsolutePath());
        });
        
        //if the playlist isn't empty
        if(model.isNotEmpty()){
            
            //reset the time slider, change the max value of it, and seek to the current time
            timeSlider.setMin(0);
            timeSlider.setMax(model.getTotalDuration());
            timeSlider.setValue(model.getCurrentTime());
            
            //set the audio spectrum listener, band number, and update interval
            model.getCurrentTrack().setAudioSpectrumNumBands(numBands);
            model.getCurrentTrack().setAudioSpectrumInterval(updateInterval);
            model.getCurrentTrack().setAudioSpectrumListener((double timestamp, double duration, float[] magnitudes, float[] phases) ->{
                handleUpdate(timestamp, duration, magnitudes, phases);
            });
            
            //start the visualizer
            visualizer.start(numBands, visualizerPane);
            //set the volume slider to the current volume
            volumeSlider.setValue(model.getVolume() * 100);
            //select the current index in the media list
            mediaList.getSelectionModel().select(model.getIndex());
            //set the song title to the new song title
            songTitle.setText(model.getName());
            
            //if we're playing something, set the button to pause
            if(model.getStatus() == Status.PLAYING){
                playButton.setText("Pause");
            } //or play if we aren't
              else {
                playButton.setText("Play");
            }
        }
        
    }
    //handle the end of media functions
    @Override public void handleEndOfMedia(){
        
        //reset the time of the track and time slider
        model.seek(Duration.ZERO);
        timeSlider.setValue(0);
        
        //get the current track isn't at the max value
        if(model.getIndex() != model.getMaxIndex()){
            //play the next one and reset the timeslider, then set its maximum value regarding the new track
            model.playNext();            
            timeSlider.setValue(0);
            timeSlider.setMax(model.getTotalDuration());
            
            //change the song title, the play button to pause, the selection in the media list
            songTitle.setText(model.getName());
            playButton.setText("Pause");
            mediaList.getSelectionModel().select(model.getIndex());
        } //if it's the last track
          else {
            //loop from the bottom
            model.loopFromBottom();
            //reset the time slider and change its max
            timeSlider.setValue(0);
            timeSlider.setMax(model.getTotalDuration());            
            //change the name of the song title to the new one, and the play/pause to pause
            songTitle.setText(model.getName());
            playButton.setText("Pause");
            //then select the track in the medialist
            mediaList.getSelectionModel().select(model.getIndex());
        }
    } 
    
    //open a media file
    private void openMedia(){
        
        //try to set the handlers and the spectrum values
        try{
            model.getCurrentTrack().setOnReady(() -> {
                handleReady();
            });            
            model.getCurrentTrack().setOnEndOfMedia(() -> {
                handleEndOfMedia();
            });            
            model.getCurrentTrack().setAudioSpectrumNumBands(numBands);
            model.getCurrentTrack().setAudioSpectrumInterval(updateInterval);
            model.getCurrentTrack().setAudioSpectrumListener((double timestamp, double duration, float[] magnitudes, float[] phases) -> {                        
                handleUpdate(timestamp, duration, magnitudes, phases);
            });
            
            //set it to autoplay
            model.setAutoPlay(true);
            //change the song title to the new one
            songTitle.setText(model.getName());
            //select the corresponding item in the  media list
            mediaList.getSelectionModel().select(model.getIndex());
            //set the play/pause button to pause
            playButton.setText("Pause");
            
        } //and catch a generic exception
          catch (Exception ex){
            System.out.println(ex.toString());
        }
    }
     
}
