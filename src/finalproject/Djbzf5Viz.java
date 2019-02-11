/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package djbzf5finalproject;

import static java.lang.Integer.min;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Djbzf5Viz implements Djbzf5Visualizer {
    
    private final String name = "Djbzf5Viz";
    private Integer numBands;
    private AnchorPane vizPane;
    private final Double bandHeightPercentage = 1.3;
    private final double minRectangleWidth = 10.0;
    private Double width = 0.0;
    private Double height = 0.0;
    private Double bandWidth = 0.0;
    private Double bandHeight = 0.0;
    private Double halfBandHeight = 0.0;
    private final Double startHue = 260.0;
    private double diff;
    private String defStyle;
    private Rectangle[] rectangles;
    private Rectangle rectangle;
    
    public Djbzf5Viz(){
    }
    
    @Override public void start(Integer numBands, AnchorPane vizPane){
        end();
        
        this.numBands = numBands;
        this.vizPane = vizPane;
        
        height = vizPane.getHeight();
        width = vizPane.getWidth();
        
        bandWidth = width / numBands;
        bandHeight = height * bandHeightPercentage;
        halfBandHeight = bandHeight / 2;
        rectangles = new Rectangle[numBands];
        
        for (int i = 0; i < numBands; i++) {
            Rectangle rectangle = new Rectangle();
            rectangle.setLayoutX(bandWidth / 2 + bandWidth * i);
            rectangle.setLayoutY(height / 2);
            rectangle.setWidth(bandWidth / 2);
            rectangle.setHeight(minRectangleWidth);
            rectangle.setArcWidth(0);
            rectangle.setFill(Color.hsb(startHue, 1.0, 1.0, 1.0));
            vizPane.getChildren().add(rectangle);
            rectangles[i] = rectangle;
        }
        
        rectangle = new Rectangle();
        defStyle = vizPane.getStyle();
    }
    @Override public void end(){
        if (rectangles != null) {
             for (Rectangle rectangle : rectangles) {
                 vizPane.getChildren().remove(rectangle);
             }
            rectangles = null;
        } 
    }
    @Override public String getName(){
            return name;
    }
    @Override public void update(double timestamp, double duration, float[] magnitudes, float[] phases) {
         if (rectangles == null) {
            return;
        }
         
        Integer num = min(rectangles.length, magnitudes.length);

        //implements a fall-down effect on the bars similar to the Monstercat style of visualizer. Instead of dropping really quickly upon a lower frequency, it drops until it's raised again.
        for (int i = 0; i < num; i++) {
            rectangle.setScaleY(((60.0 + magnitudes[i])/350.0) * halfBandHeight + 3);
            if(rectangle.getScaleY() < rectangles[i].getScaleY()){
                rectangles[i].setScaleY(rectangles[i].getScaleY() * 0.95);
            }
            else{
                rectangles[i].setScaleY(((60.0 + magnitudes[i])/350.0) * halfBandHeight + 3);
            }
            rectangles[i].setFill(Color.WHITE);
        }
        
        //smoothing pass for the bands. Looks at the height of each band and compares them to the
        //one before it. When a band grows, it forces the other band behind it to grow to 1/10 of the 
        //height difference. Makes it seem fuller and more active when visualizing.
        //This is incredibly cpu intensive.
        //Also this is is used to send a sort of "ripple" effect through the bars so the extremely high
        //frequencies bands are falsely activated to make it more "alive"
        for(int i = 0; i < num; i++){
            for(int j = 0; j < 100000; j++){
                
            }
            if(i == 0 && rectangles[i+1].getScaleY() > rectangles[i].getScaleY()){
                diff = rectangles[i+1].getScaleY() - rectangles[i].getScaleY();
                rectangles[i].setScaleY(rectangles[i].getScaleY() + (diff / 5));
            }
            if(i != 0 && i != num-1 && rectangles[i-1].getScaleY() < rectangles[i].getScaleY()){
                diff = rectangles[i].getScaleY() - rectangles[i-1].getScaleY();
                rectangles[i-1].setScaleY(rectangles[i-1].getScaleY() + (diff / 5));
            }
            if(i != 0 && i != num-1 && rectangles[i-1].getScaleY() > rectangles[i].getScaleY()){
                diff = rectangles[i-1].getScaleY() - rectangles[i].getScaleY();
                rectangles[i].setScaleY(rectangles[i].getScaleY() + (diff / 5));
            }
        }
    }
}
