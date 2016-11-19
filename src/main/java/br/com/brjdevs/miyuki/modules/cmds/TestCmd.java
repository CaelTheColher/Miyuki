package br.com.brjdevs.miyuki.modules.cmds;

import br.com.brjdevs.miyuki.commands.Commands;
import br.com.brjdevs.miyuki.commands.ICommand;
import br.com.brjdevs.miyuki.loader.Module;
import br.com.brjdevs.miyuki.loader.Module.Command;

@Module(name = "cmds.test")
public class TestCmd {
    @Command("test")
    private static ICommand testCommand() {
        return Commands.buildSimple().setAction((event) ->
            event.getChannel().sendMessage("test").queue()).build();
    }
}
