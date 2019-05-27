package view.cli.conection.commands;

import common.RemoteController;

import static view.cli.Commands.*;

public class CommandFactory {
   private CommandFactory(){}
   public static CliCommand getCommand(String s, RemoteController controller, boolean gui){
         switch (s.toLowerCase().trim()){
            case MAP_LONG: case MAP_SHORT: case MAP_LONGER:
               return new ChooseMapCommand( controller, gui );
            case SKULLS_SHORT: case SKULLS_LONG: case SKULLS_LONGER:
               return new ChooseNumberOfSkullsCommand( controller, gui);
            case COLOUR_SHORT: case COLOUR_LONG: case COLOUR_LONGER:
               return new ChoosePcColourCommand(controller, gui);
            case SPAWN_SHORT: case SPAWN_LONG:
               return new DiscardAndSpawnCommand(controller, gui);
            case COMMENT_SHORT: case COMMENT_LONG:
               return new ShowCommentCommand(controller, gui);
            case RUN_LONG: case RUN_SHORT:
               return new RunAroundCommand(controller, gui);
            case GRAB_STUFF_SHORT: case GRAB_STUFF_LONG:
               return new GrabStuffCommand(controller, gui);
            case SHOOT_LONG: case SHOOT_SHORT:
               return new ShootCommand(controller, gui);
            case SQUARE_LONG: case SQUARE_SHORT:
               return new SelectSquareCommand(controller, gui);
            case GRAB_WEAPON_SHORT: case GRAB_WEAPON_LONG:
               return new GrabWeaponCommand(controller,gui);
            case CHOOSE_WEAPON_SHORT: case CHOOSE_WEAPON_LONG:
               return new ChooseWeaponCommand(controller,gui);
            case QUIT_LONG: case QUIT_SHORT:
               return new QuitCommand(controller, gui);
            case SKIP_SHORT: case SKIP_LONG:
               return new SkipCommand(controller,gui);
            default:
               System.out.println("Unsupported command");
               return new UselessCommand();
         }
      }
   
}
