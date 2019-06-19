package client.cli;

import client.ClientView;
import client.InputReader;
import client.socket.ClientSocketHandler;
import common.enums.ConnectionsEnum;
import common.remote_interfaces.RemoteLoginController;

import java.io.IOException;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

class CliView extends UnicastRemoteObject implements ClientView {
    private transient InputReader inputReader = new CliInputReader();
    
    protected CliView() throws RemoteException {
    }
    
    @Override
    public RemoteLoginController acquireConnection(){
        ConnectionsEnum cmd = null;
        do {
            try {
                cmd = ConnectionsEnum.parseString( inputReader.requestString( "Choose a connection method:" + System.lineSeparator() + " - (s)ocket" + System.lineSeparator() + " - (r)mi" + System.lineSeparator() ).toLowerCase());
            }catch ( IllegalArgumentException e ){
                System.out.println(e.getMessage());
            }
        } while (cmd == null);
        switch (cmd){
            case Socket:
                try {
                    Socket socket = new Socket( HOST, SOCKET_PORT );
                    ClientSocketHandler handler = new ClientSocketHandler( socket );
                    new Thread( handler ).start();
                    return handler;
                } catch (IOException e ) {
                    displayErrorAndExit( "Server unreachable" );
                }
            case Rmi:
                try {
                    Registry registry = LocateRegistry.getRegistry( HOST, RMI_PORT );
                    return ( RemoteLoginController ) registry.lookup( "LoginController" );
                } catch ( RemoteException | NotBoundException e ) {
                    displayErrorAndExit( "Server unreachable" );
                }
            default:
                    throw new IllegalArgumentException( cmd + "is not supported yet" );
        }
    }
    
    @Override
    public boolean wantsRegister(){
        HashSet<String> yesAnswers = new HashSet<>( Arrays.asList("y", "yes"));
        HashSet<String> noAnswers = new HashSet<>(Arrays.asList("n", "no"));
        String cmd;
        do{
            cmd = inputReader.requestString("Do you have a login token?"+System.lineSeparator()).toLowerCase();
        }while (!yesAnswers.contains( cmd ) && ! noAnswers.contains( cmd ));
        return noAnswers.contains( cmd );
    }
    
    @Override
    public String acquireUsername() {
        return inputReader.requestString("Insert an username");
    }
    
    @Override
    public UUID acquireToken() {
        return UUID.fromString(inputReader.requestString("Insert your token"));
    }
    
    @Override
    public String requestString(String message) {
        return inputReader.requestString( message );
    }
    
    @Override
    public void displayErrorAndExit(String msg) {
        inputReader.requestString( msg + System.lineSeparator() + "Press enter to exit." );
        System.exit( 1 );
    }
    
    @Override
    public void ack(String message) {
        System.out.println(message);
    }
    
}
