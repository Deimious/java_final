/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package djbzf5finalproject;

import static djbzf5finalproject.superController.getControllersByName;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 * FXML Controller class
 *
 * @author daymoose
 */
public class aboutController extends superController {

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @Override
    public void handleChange() {
    }
    
    @FXML public void switchScenesPlaylist(ActionEvent event){
        
        superController.switchTo("playlist");
        getControllersByName("playlist").handleChange();
        
            
    }     
    @FXML public void switchScenesVisualizer(ActionEvent event){
        
        superController.switchTo("visualizer");
        getControllersByName("visualizer").handleChange();
        superController.switchTo("visualizer");
        getControllersByName("visualizer").handleChange();

    } 
    
}
