package client.controller.socket;

import common.enums.interfaces_names.RemoteViewEnum;
import common.remote_interfaces.RemoteLoginController;
import common.remote_interfaces.RemotePlayer;
import common.remote_interfaces.RemoteView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Queue;
import java.util.UUID;

import static common.enums.interfaces_names.SocketLoginEnum.*;
import static common.enums.interfaces_names.SocketPlayerEnum.*;

public class ClientSocketHandler implements Runnable, RemoteLoginController, RemotePlayer {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private RemoteView view;
    
    public ClientSocketHandler(Socket socket) throws IOException {
        this.socket = socket;
        out = new PrintWriter( socket.getOutputStream() );
        in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
    }
    
    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                String[] args = in.readLine().split( "," );
                parseRemoteView( args );
            }catch ( IOException | NullPointerException e ){ //NPE is thrown when socket is closed in the middle of the while body
                //TODO: manage disconnection
            }
        }
    }
    
    private void parseRemoteView(String[] args) throws IOException {
        try {
            switch (RemoteViewEnum.valueOf( args[0] )) {
                case ACK:
                    view.ack( args[1] );
            }
        }catch ( IllegalArgumentException ignored ){}//Other kinds of messages
    }
    
    //LoginController
    @Override
    public synchronized UUID register(String username) throws IOException {
        out.println( REGISTER.toString() + "," + username );
        out.flush();
        return UUID.fromString( in.readLine() );
    }
    
    
    @Override
    public synchronized RemotePlayer login(UUID token, RemoteView view) throws IOException {
        this.view = view;
        new Thread( this ).start();
        out.println( LOGIN.toString() + "," + token );
        out.flush();
        return this;
    }
    
    
    @Override
    public synchronized void joinLobby(UUID token) {
        out.println( JOIN_LOBBY.toString() + "," + token );
        out.flush();
    }
    
    //Player
    
    @Override
    public void chooseMap(int n) {
        out.print( CHOOSE_MAP.toString() + "," + n );
        out.flush();
    }
    
    
    @Override
    public void chooseNumberOfSkulls(int n) {
        out.println( CHOOSE_NUMBER_OF_SKULLS.toString() + "," + n );
        out.flush();
    }
    
    
    @Override
    public void choosePcColour(String colour) {
        out.println( CHOOSE_PC_COLOUR + "," + colour );
        out.flush();
    }
    
    
    @Override
    public void runAround() {
        out.println( RUN_AROUND );
        out.flush();
    }
    
    
    @Override
    public void grabStuff() {
        out.println( GRAB_STUFF );
        out.flush();
    }
    
    
    @Override
    public void usePowerUp() {
        out.println( USE_POWERUP );
        out.flush();
    }
    
    
    @Override
    public void shootPeople() {
        out.println( SHOOT_PEOPLE );
        out.flush();
    }
    
    
    @Override
    public void chooseSquare(int x, int y) {
        out.println( SELECT_SQUARE + "," + x + "," + y );
        out.flush();
    }
    
    
    @Override
    public void choosePowerUp(int index) {
        out.println( CHOOSE_POWERUP + "," + index );
        out.flush();
    }
    
    
    @Override
    public void chooseWeaponOnSpawnPoint(int n) {
        out.println( GRAB_WEAPON + "," + n );
        out.flush();
    }
    
    
    @Override
    public void chooseWeaponOfMine(int n) {
        out.println( CHOOSE_WEAPON + "," + n );
        out.flush();
    }
    
    
    @Override
    public void switchFireMode() {
        out.println( SWITCH_FIREMODE );
        out.flush();
    }
    
    
    @Override
    public void upgrade() {
        out.println( UPGRADE );
        out.flush();
    }
    
    
    @Override
    public void removeUpgrade() {
        out.println( REMOVE_UPGRADE );
        out.flush();
    }
    
    
    @Override
    public void chooseAsynchronousEffectOrder(boolean beforeBasicEffect) {
        out.println( CHOOSE_ASYNCH_EFFECT_ORDER + "," + beforeBasicEffect);
        out.flush();
    }
    
    
    @Override
    public void skip() {
        out.println( SKIP );
        out.flush();
    }
    
    
    @Override
    public void undo() {
        out.println( UNDO );
        out.flush();
    }
    
    
    @Override
    public void ok() {
        out.println( OK );
        out.flush();
    }
    
    
    @Override
    public void reload() {
        out.println( RELOAD );
        out.flush();
    }
    
    
    @Override
    public void pass() {
        out.println( PASS );
        out.flush();
    }
    
    
    @Override
    public void quit(){
        out.println( QUIT );
        try {
            out.close();
            in.close();
            socket.close();
        }catch ( IOException ignored ){} //if already closed it's ok
    }
}
