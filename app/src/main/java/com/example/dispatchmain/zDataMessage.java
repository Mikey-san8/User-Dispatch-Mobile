package com.example.dispatchmain;

public class zDataMessage
{
    private String name, chat, time, key, sender;

    public zDataMessage(String name, String chat, String time, String key, String sender)
    {
        this.name = name;
        this.chat = chat;
        this.time = time;
        this.key = key;
        this.sender = sender;
    }

    public String getName() {
        return name;
    }

    public String getChat() {
        return chat;
    }

    public String getTime() {
        return time;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getKey()
    {
        return key;
    }

    public String getSender() {
        return sender;
    }
}
