package client.view.gui.javafx_controllers.components;

import common.dto_model.PcDTO;
import common.dto_model.SquareDTO;
import common.enums.PcColourEnum;
import common.remote_interfaces.RemotePlayer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.util.EnumMap;


public class Map implements MapChangeListener<PcColourEnum,PcDTO>{
   @FXML
   GridPane grid;
   private RemotePlayer player;
   
   public final MapChangeListener<PcColourEnum,PcDTO> playerObserver = change -> {
      //TODO: automatizza la gestione dei pc nella mappa
   };
   
   public final MapChangeListener<SquareDTO,SquareDTO> squareObserver = change -> {
      //TODO: automatizza la gestione degli square nella mappa
   };
   
   private Pane[][] squares = new Pane[3][4];
   private EnumMap<PcColourEnum,Circle> pcCircles = new EnumMap<>( PcColourEnum.class );
   
   private static final int ROWS = 3;
   private static final int COLS = 4;
   
   public void initialize() {
      //force columns to stay same sized
      for(int c=0;c<COLS;c++){
         ColumnConstraints cc = new ColumnConstraints();
         cc.setPercentWidth( 100/COLS );
         cc.setHgrow( Priority.ALWAYS );
         grid.getColumnConstraints().add( cc );
      }
      for(int r=0;r<ROWS;r++){
         //force rows to stay same sized
         RowConstraints rc = new RowConstraints();
         rc.setPercentHeight( 100/ROWS );
         rc.setVgrow( Priority.ALWAYS );
         grid.getRowConstraints().add( rc );
         //setup cells
         for(int c=0;c<COLS;c++){
            int row=r,column=c;
            
            FlowPane cellContents = new FlowPane( Orientation.VERTICAL );
            cellContents.setAlignment( Pos.CENTER );
            cellContents.setMaxSize( Double.MAX_VALUE,Double.MAX_VALUE );
            cellContents.setOnMouseClicked( e->{
               try {
                  player.chooseSquare( row,column );
               } catch ( IOException ex ) {
                  //TODO: chiama error in MainGui
               }
            } );
   
            StackPane stackPane = new StackPane( cellContents );
            stackPane.setMaxSize( Double.MAX_VALUE,Double.MAX_VALUE );
            
            squares[r][c]=cellContents;
            grid.add( stackPane,c,r);
         }
      }
   }
   
   public void setMap(int mapIndex){
      //load map image
      Image image = new Image( "/images/maps/map_"+mapIndex+".png" );
      BackgroundImage bi = new BackgroundImage( image,BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.CENTER,new BackgroundSize( 1,1,false,false,true,false ) );
      //setup image and map to preserve aspect ratio
      double ratio = image.getWidth()/image.getHeight();
      grid.setBackground( new Background( bi ) );
      grid.maxWidthProperty().bind( Bindings.multiply( ratio,grid.heightProperty() ) );
      grid.minWidthProperty().bind( Bindings.multiply( ratio,grid.heightProperty() ) );
   
   }
   
   public void setPlayer(RemotePlayer player) {
      this.player = player;
   }
   
   @Override
   public void onChanged(Change<? extends PcColourEnum, ? extends PcDTO> change) {
      if(change.wasRemoved()){
         pcCircles.get( change.getValueRemoved()
                 .getColour() ).setVisible( false );
      }
      if(change.wasAdded()){
         int playersNumb = change.getMap().size();
         double gridSide = Math.ceil( Math.sqrt( playersNumb + 1));
         PcColourEnum colour = change.getValueAdded().getColour();
         Pane square = squares[change.getValueAdded().getCurrSquare().getRow()][change.getValueAdded().getCurrSquare().getCol()];
         Circle circle = pcCircles.getOrDefault( colour,new Circle() );
         circle.setFill( Color.valueOf( colour.toString() ) );
         NumberBinding radius = Bindings.subtract( Bindings.divide(Bindings.min( square.widthProperty(),square.heightProperty()),2*gridSide),1);
         circle.radiusProperty().bind( radius );
         for(Circle c:pcCircles.values()){
            c.radiusProperty().unbind();
            c.radiusProperty().bind( radius );
         }
         square.getChildren().add( circle );
         square.setMinSize( 0,0 );
   
         pcCircles.put( colour,circle );
         circle.setVisible( true );
      }
   }
   
}
