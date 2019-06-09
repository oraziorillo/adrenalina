package client.view.cli.commands;

import server.controller.RemotePlayer;
import java.io.IOException;

class ChooseNumberOfSkullsCommand extends CliCommand {
    ChooseNumberOfSkullsCommand(RemotePlayer controller) {
        super(controller );
    }

    @Override
    public void execute() throws IOException {
        controller.chooseNumberOfSkulls( inputRequire.requestInt( "Select number of skulls" ) );
    }
}
