package com.example.dispatchmain;

public class DataItem
{
    private String name;
    private String chat;
    private String time;
    private String key;

    public DataItem(String name, String chat, String time, String key)
    {
        this.name = name;
        this.chat = chat;
        this.time = time;
        this.key = key;
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
}
