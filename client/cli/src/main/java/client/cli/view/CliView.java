package client.cli.view;

import client.controller.socket.LoginControllerSocketProxy;
import client.view.AbstractView;
import client.view.InputReader;
import common.dto_model.GameDTO;
import common.dto_model.PcDTO;
import common.enums.ConnectionMethodEnum;
import common.enums.ControllerMethodsEnum;
import common.events.ModelEventListener;
import common.events.game_board_events.GameBoardEvent;
import common.events.kill_shot_track_events.KillShotTrackEvent;
import common.events.lobby_events.LobbyEvent;
import common.events.pc_board_events.PcBoardEvent;
import common.events.pc_events.PcEvent;
import common.events.requests.Request;
import common.events.square_events.SquareEvent;
import common.remote_interfaces.RemoteLoginController;

import java.io.IOException;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.UUID;

import static common.Constants.WRONG_TIME;

public class CliView extends AbstractView {

    private transient InputReader inputReader;
    private PcDTO pc;


    public CliView() throws RemoteException {
        super();
        this.inputReader = new CliInputReader();
    }


    public void showPcStatus() {
        if (pc != null)
            printMessage(pc.getPcStatus());
        else
            printMessage("You haven't still selected your character");
    }


    @Override
    public String nextCommand() {
        return inputReader.requestCommand("").toLowerCase();
    }


    @Override
    public void printMessage(String msg) {
        if(msg.length() != 0)
            System.out.println(msg);
    }


    @Override
    public ConnectionMethodEnum acquireConnectionMethod() {
        ConnectionMethodEnum cme = null;
        do {
            try {
                cme = ConnectionMethodEnum.parseString(inputReader.requestString(
                        "Please, provide a connection method:" + System.lineSeparator() +
                                "\t> s\t\tSocket" + System.lineSeparator() +
                                "\t> r\t\tRmi")
                        .toLowerCase());
            } catch (IllegalArgumentException e) {
                printMessage(e.getMessage());
            }
        } while (cme == null);
        return cme;
    }


    @Override
    public RemoteLoginController acquireConnection(ConnectionMethodEnum cme) {
        try {
            switch (cme) {
                case SOCKET:
                    Socket socket = new Socket(HOST, SOCKET_PORT);
                    return new LoginControllerSocketProxy(socket, this);
                case RMI:
                    System.setProperty( "java.rmi.server.hostname",HOST );
                    Registry registry = LocateRegistry.getRegistry(HOST, RMI_PORT);
                    RemoteLoginController returned = (RemoteLoginController) registry.lookup("LoginController");
                    return returned;
                default:
                    throw new IllegalArgumentException(WRONG_TIME);
            }
        } catch (IOException | NotBoundException e) {
            printMessage("Server unreachable");
            return null;
        }
    }


    @Override
    public ControllerMethodsEnum authMethod() {
        ControllerMethodsEnum cmd = null;
        do {
            try {
                cmd = ControllerMethodsEnum.parseString(inputReader.requestString(
                        "Are you new?" + System.lineSeparator() +
                                "\t> s\t\tSign up" + System.lineSeparator() +
                                "\t> l\t\tLog in")
                        .toLowerCase());
            } catch (IllegalArgumentException e) {
                printMessage(e.getMessage());
            }
        } while (cmd == null);

        return cmd;
    }


    @Override
    public String acquireUsername() {
        return inputReader.requestString("Please, provide a username");
    }


    @Override
    public UUID acquireToken() {
        try {
            return UUID.fromString(inputReader.requestString("Insert your token"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    @Override
    public synchronized void error(String msg){
        inputReader.requestString(msg + System.lineSeparator() + "Press enter to exit.");
        System.exit(1);
    }


    @Override
    public synchronized void ack(String message) throws RemoteException {
        if (message.length() != 0) {
            printMessage(message);
        }
    }

    @Override
    public void chatMessage(String message) throws RemoteException {
        printMessage(message);
    }



    @Override
    public synchronized void notifyEvent(LobbyEvent event) throws RemoteException {
        printMessage(event.toString());
    }


    @Override
    public void request(Request request) throws RemoteException {
        printMessage(request.toString());
    }


    @Override
    public synchronized ModelEventListener getListener() throws RemoteException{
        return this;
    }
    
    
    @Override
    public void resumeGame(GameDTO game) throws RemoteException {
        printMessage(game.toString());
    }


    public void winners(List<String> winners) throws RemoteException {
        printMessage(winners.size() == 1
                ? System.lineSeparator() + "It seems we have a winner. And he/she is..." + System.lineSeparator() + winners.get(0)
                : "We have a draw between " + winners.get(0) + " and " + winners.get(1));
    }

    
    public void close() throws RemoteException {

    }
    
    
    @Override
    public synchronized void onGameBoardUpdate(GameBoardEvent event) throws RemoteException {
        printMessage(event.toString());
        event.getDTO().getSquares().forEach(s -> printMessage( System.lineSeparator() + s.description()));
    }


    @Override
    public synchronized void onKillShotTrackUpdate(KillShotTrackEvent event) throws RemoteException {
        printMessage(event.toString());
    }


    @Override
    public synchronized void onPcBoardUpdate(PcBoardEvent event) throws RemoteException {
        printMessage(event.toString());
    }


    @Override
    public synchronized void onPcUpdate(PcEvent event) throws RemoteException {
        printMessage(event.toString());
        if (!event.isCensored())
            this.pc = event.getDTO();
    }


    @Override
    public synchronized void onSquareUpdate(SquareEvent event) throws RemoteException {
        printMessage(event.toString());
    }
}
