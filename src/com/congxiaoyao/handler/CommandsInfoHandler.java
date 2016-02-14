package com.congxiaoyao.handler;

import com.congxiaoyao.cmd.Analysable;
import com.congxiaoyao.cmd.Command;
import com.congxiaoyao.cmd.CommandName;

import java.util.List;

/**
 * Created by congxiaoyao on 2016/2/14.
 */
public class CommandsInfoHandler extends BaseHandler {

    private Command nowCommand;

    @CommandName("cmd_selc")
    public void selectCommand(String commandName, int paramCount, String delimiter) {
        Command temp = new Command(commandName, paramCount, delimiter, "");
        List<Command> commands = getAnalysable().getCommands();
        for (Command command : commands) {
            if (command.equals(temp)) {
                nowCommand = temp;
                System.out.println("select command->"+nowCommand);
                return;
            }
        }
        System.out.println("failed");
    }

    @Override
    BaseHandler registerCommands() {
        Analysable analysable = getAnalysable();
        analysable.addCommand(new Command("cmd_selc", 3, "通过name、paramCount、delimiter选择一个command"));
//        analysable.addCommand(new Command(ci));
        return this;
    }
}
