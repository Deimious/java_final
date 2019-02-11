package djbzf5finalproject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public abstract class superController implements Initializable{
    
    //ui elements
    @FXML public Label songTitle;
    @FXML public Button playButton;
    @FXML public Button prevButton;    
    @FXML public Button nextButton;    
    @FXML public Slider timeSlider;
    @FXML public Slider volumeSlider;
    @FXML public AnchorPane visualizerPane = new AnchorPane();
   
    //data model variables
    public final Double updateInterval = 0.05;
    public final Integer numBands = 100;
    public static DataModel model = new DataModel();
    
    //scene variables
    public static Scene scene;
    private Parent root;
    
    //controllers to switch between
    public static final HashMap<String, superController> controllers = new HashMap<>();
    
    //visualizer that has to be maintained, but not shown
    public static Djbzf5Visualizer visualizer = new Djbzf5Viz();
    
    //some ui handlers that are common between all controllers
    @FXML public void handleSave(){
        
        //opens a file chooser for the user to select a file to save to (automatically creates one if none selected)
        FileChooser fileChooser = new FileChooser();
        File userSelection = fileChooser.showSaveDialog(playButton.getScene().getWindow());
        
        //tries to open the file to write, catches a null pointer exception
        try{
            
            //a writer to write to the file
            BufferedWriter writer = new BufferedWriter(new FileWriter(userSelection.getAbsolutePath()));
        
            //writes to the file if the playlist is not empty
            if(model.isNotEmpty()){
                for(File file : model.getFiles()){
                    writer.write(file.getAbsolutePath());
                    writer.newLine();
                }
            }
            
            //close the writer
            writer.close();
            
        } //catches the i/o exception and dumps the log
          catch (IOException ex) {
            Logger.getLogger(superController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML public void handleOpen(){
        
        //open a playlist file (file.txt containing the absolute path of a music file to open per line)
        FileChooser chooser = new FileChooser();
        File userSelection = chooser.showOpenDialog(playButton.getScene().getWindow());
        
        //tries to open the file and catches an i/o exception
        try {
            
            //new file reader
            BufferedReader reader = new BufferedReader(new FileReader(userSelection.getAbsolutePath()));
            
            //some variables to grab lines
            String line;
            File file;
            
            //reset the playlist to empty
            model.reset();
            
            //read each line
            while((line = reader.readLine()) != null){
                
                //create the file
                file = new File(line);
                
                //if that file is real
                if(file != null)
                    //add the track to the playlist
                    model.addTrack(file);
            }
            
            //for each track in the playlist
            for(MediaPlayer mediaPlayer : model.getTracks()){
                
                //try to handle them and catch a generic exception
                try{
                    
                    //set the media events and audio spectrum output interval/listener
                    mediaPlayer.setOnReady(() -> {
                        handleReady();
                    });                 
                    mediaPlayer.setOnEndOfMedia(() -> {
                        handleEndOfMedia();
                    });
                    mediaPlayer.setAudioSpectrumInterval(updateInterval);
                    mediaPlayer.setAudioSpectrumListener((double timestamp, double duration, float[] magnitudes, float[] phases) -> {
                        handleUpdate(timestamp, duration, magnitudes, phases);                    
                    });
                    
                } //catches a generic exception and prints it
                  catch(Exception ex){
                    System.out.println(ex.toString());
                }
            }
            
        } //catches some specific and a generic exception
          catch (FileNotFoundException ex) {
            Logger.getLogger(superController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(superController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex){
            System.out.println(ex.toString());
        }
        
        //switches to the playlist view as a stock view and handles the changing functions associated with displaying this view
        switchTo("playlist");
        getControllersByName("playlist").handleChange();
        
    }
    @FXML public void handleVolumeReleased(Event event){
        
        //for each media player, change the volume to the selected amount
        for(MediaPlayer mediaPlayer : model.getTracks()){
            mediaPlayer.setVolume(volumeSlider.getValue() / 100);
            model.setVolume(volumeSlider.getValue() / 100);
            volumeSlider.setValue(volumeSlider.getValue());
        }
        
    }
    @FXML public void handleAbout(ActionEvent event){
        
        //just switch to the about view
        switchTo("about");
        
    }
    @FXML public void handleClose(ActionEvent event){
        
        //close the program without saving a file
        System.exit(0);
    }
    
    //some functions to handle getting controllers from and adding controllers to the hashmap
    public static superController add(String name){
        
        //new instance of this superclass
        superController controller;
        
        //new controller, retrieved by name
        controller = controllers.get(name);
        
        //if that controller is present
        if(controller == null){
            
            //try to load it's view
            try {
                
                //load the controller for the view
                FXMLLoader loader = new FXMLLoader(superController.class.getResource(name + ".fxml"));
                
                //set the root
                Parent root = loader.load();
                
                //get the controller
                controller = loader.getController();
                
                //set the root controller
                controller.setRoot(root);
                
                //add the controller to the hasmap
                controllers.put(name, controller);
                
            } //catch a specific and generic exception
              catch (IOException ex) {
                Logger.getLogger(superController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex){
                System.out.println("Error loading " + name + ".fxml \n\n" + ex);
                controller = null;
            }
        }
        
        return controller;
    }    
    public static void switchTo(String name){
        
        //get the controller from the hashmap, store in local variable
        superController controller = controllers.get(name);
        
        //if that controller isn't present in the hashmap
        if(controller == null){
            //add the controller to it
            controller = add(name);
        }
        
        //if the controller is present in the hashmap
        if(controller != null){
            //if there isn't a scene
            if(scene != null){
                //set the scene to the controller's
                scene.setRoot(controller.getRoot());
            }
        }
    }    
    public void setRoot(Parent root){
        //just set the controller's root
        this.root = root;
    }    
    public Parent getRoot(){
        //return the root of the controller
        return root;
    }    
    public static superController getControllersByName(String name){
        //return the controller with the name of the variable
        return controllers.get(name);
    }
    
    //some functions to handle adding media into the playlist data model
    public void handleReady(){
        //start the visualizer
        visualizer.start(numBands, visualizerPane);
        //set the min value of the time slider to zero
        timeSlider.setMin(0);
        //set the maximum value of the time slider to the maximum duration of the current track
        timeSlider.setMax(model.getTotalDuration());
    }
    public void handleEndOfMedia(){
        
        //seek to the beginning of the media for the next playthrough
        model.seek(Duration.ZERO);
        
        //seek the time slider to the zero position
        timeSlider.setValue(0);
        
        //if the current track is not the max track
        if(model.getIndex() != model.getMaxIndex()){
            
            //play the next track
            model.playNext();
            
            //reset the time slider again
            timeSlider.setValue(0);
            
            //set the max of the time slider to the current track's max duration
            timeSlider.setMax(model.getTotalDuration());
                                    
            //set the song title to the new song's name
            songTitle.setText(model.getName());
            
            //set the play/pause button to pause
            playButton.setText("Pause");
        } //if it's the max track
          else {
            
            //loop from the bottmo of the playlist to the top
            model.loopFromBottom();
            
            //reset time slider and set the new max
            timeSlider.setValue(0);
            timeSlider.setMax(model.getTotalDuration());                        
            
            
            //chang the song title to the new name
            songTitle.setText(model.getName());
            //and the play button to pause
            playButton.setText("Pause");
        }
    }  
    public void handleUpdate(double timestamp, double duration, float[] magnitudes, float[] phases){
        
        //if the playlist is not empty
        if(model.isNotEmpty()){
            //set the time slider to the current time and update the visualizer
            timeSlider.setValue(model.getCurrentTime());        
            visualizer.update(timestamp, duration, magnitudes, phases);
        } else {
            //set the timeslider to zero if it is empty and end the visualizer
            timeSlider.setValue(0);
            //visualizer.end();
        }
    }
    public abstract void handleChange();
    
}
