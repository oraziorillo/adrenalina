package server.controller.socket;

import common.enums.ControllerMethodsEnum;
import common.remote_interfaces.RemotePlayer;
import common.remote_interfaces.RemoteView;
import server.controller.LoginController;
import common.exceptions.PlayerAlreadyLoggedInException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;

import static common.Constants.*;

public class ServerSocketHandler implements Runnable {
    private final Socket socket;
    private final PrintWriter out;
    private final Scanner in;
    private LoginController loginController;
    private RemotePlayer player;
    private RemoteView view;


    public ServerSocketHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream());
        this.in = new Scanner(socket.getInputStream());
        this.loginController = LoginController.getInstance();
        this.view = new ViewSocketProxy(socket);
    }


    @Override
    public void run() {
        while (!socket.isClosed()) {
            String[] args = in.nextLine().split(ARGS_SEPARATOR);
            try {
                handle(args);
            } catch (IOException e) {
                try {
                    socket.close();
                    out.close();
                    in.close();
                    if (player != null) player.quit();
                } catch (IOException ignored) {
                }
            } catch (IllegalArgumentException ignored) {
            } //only one of the "handle..." method will not throw it
        }
    }


    /**
     * Parser for socket methods
     *
     * @param args the strings to parse
     * @throws IOException              if client is unreachable
     * @throws IllegalArgumentException if the method name (args[0]) is not in ControllerMethodsEnum
     * @see ControllerMethodsEnum
     */
    private void handle(String[] args) throws IOException {
        int argInt;
        switch (ControllerMethodsEnum.valueOf(args[0])) {
            case SIGN_UP:
                out.println(loginController.register(args[1], view));
                out.flush();
                break;
            case LOG_IN:
                try {
                    this.player = loginController.login(UUID.fromString(args[1]), view);
                    out.println(SUCCESS);
                    out.flush();
                } catch (PlayerAlreadyLoggedInException e) {
                    out.println(FAIL);
                    out.flush();
                }
                break;
            case JOIN_LOBBY:
                loginController.joinLobby(UUID.fromString(args[1]));
                break;
            case CHOOSE_MAP:
                argInt = Integer.parseInt(args[1]);
                player.chooseMap(argInt);
                break;
            case CHOOSE_NUMBER_OF_SKULLS:
                argInt = Integer.parseInt(args[1]);
                player.chooseNumberOfSkulls(argInt);
                break;
            case CHOOSE_PC_COLOUR:
                player.choosePcColour(args[1]);
                break;
            case RUN_AROUND:
                player.runAround();
                break;
            case GRAB_STUFF:
                player.grabStuff();
                break;
            case SHOOT_PEOPLE:
                player.shootPeople();
                break;
            case USE_POWER_UP:
                player.usePowerUp();
                break;
            case CHOOSE_SQUARE:
                argInt = Integer.parseInt(args[1]);
                int argInt2 = Integer.parseInt(args[2]);
                player.chooseSquare(argInt, argInt2);
                break;
            case CHOOSE_POWER_UP:
                argInt = Integer.parseInt(args[1]);
                player.choosePowerUp(argInt);
                break;
            case CHOOSE_WEAPON_ON_SPAWN_POINT:
                argInt = Integer.parseInt(args[1]);
                player.chooseWeaponOnSpawnPoint(argInt);
                break;
            case CHOOSE_WEAPON_OF_MINE:
                argInt = Integer.parseInt(args[1]);
                player.chooseWeaponOfMine(argInt);
                break;
            case SWITCH_FIRE_MODE:
                player.switchFireMode();
                break;
            case CHOOSE_UPGRADE:
                argInt = Integer.parseInt(args[1]);
                player.chooseUpgrade( argInt );
                break;
            case CHOOSE_ASYNCHRONOUS_EFFECT_ORDER:
                if (args[1].equals("true"))
                    player.chooseAsynchronousEffectOrder(true);
                else if (args[1].equals("false"))
                    player.chooseAsynchronousEffectOrder(false);
                break;
            case CHOOSE_TARGET:
                player.chooseTarget( args[1] );
                break;
            case CHOOSE_AMMO:
                player.chooseAmmo(args[1]);
                break;
            case CHOOSE_DIRECTION:
                argInt = Integer.parseInt(args[1]);
                player.chooseDirection( argInt );
                break;
            case RELOAD:
                player.reload();
                break;
            case UNDO:
                player.undo();
                break;
            case OK:
                player.ok();
                break;
            case RESPONSE:
                player.response(args[1]);
                break;
            case PASS:
                player.pass();
                break;
            case SEND_MESSAGE:
                StringBuilder builder = new StringBuilder();
                for(String s: Arrays.copyOfRange(args,1,args.length - 1))
                    builder.append(s).append(" ");
                builder.append(args[args.length - 1]);
                player.sendMessage( builder.toString() );
                break;
            case SKIP:
                player.skip();
                break;
            case QUIT:
                player.quit();
                break;
            case HELP:
                player.help();
                break;
            default:
                break;
        }
    }
}
