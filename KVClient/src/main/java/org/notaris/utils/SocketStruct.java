package org.notaris.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketStruct {

    public Socket socket;
    public BufferedReader in;
    public PrintWriter out;

    public SocketStruct(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public String toString() {
        return "SocketStruct{" +
                "socket=" + socket +
                ", in=" + in +
                ", out=" + out +
                '}';
    }
}
