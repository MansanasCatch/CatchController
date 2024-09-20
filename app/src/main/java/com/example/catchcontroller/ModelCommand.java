package com.example.catchcontroller;

public class ModelCommand {
    String command;
    String key;

    ModelCommand(){

    }

    public ModelCommand(String command, String key) {
        this.command = command;
        this.key = key;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}