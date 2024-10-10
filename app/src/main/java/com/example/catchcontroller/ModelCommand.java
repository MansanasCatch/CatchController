package com.example.catchcontroller;

public class ModelCommand {
    String command;
    String commandType;
    String key;

    ModelCommand(){

    }

    public ModelCommand(String command,String commandType, String key) {
        this.command = command;
        this.commandType = commandType;
        this.key = key;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}