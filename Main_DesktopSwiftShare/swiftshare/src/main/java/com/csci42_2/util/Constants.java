package com.csci42_2.util;

public class Constants {
    public static final int DISCOVERY_PORT = 8888;
    public static final int TCP_PORT = 9999;
    public static final int DISCOVERY_TIMEOUT_MS = 5000;

    public static final String DISCOVERY_MESSAGE = "HELLO_FILESHARE";
    public static final String ACK_MESSAGE = "ACK_FILESHARE";
    public static final String RECEIVE_DIR = System.getProperty("user.home") + "/Downloads/SwiftShare_ReceivedFiles";

}
