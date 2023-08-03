package openAF.OpenAF;

/*
 *Copyright 2023 Imperial College London
 *Redistribution and use in source and binary forms, with or without
 *modification, are permitted provided that the following conditions are met:
 *
 *1. Redistributions of source code must retain the above copyright notice, this
 *list of conditions and the following disclaimer.
 *2. Redistributions in binary form must reproduce the above copyright notice, this 
 *list of conditions and the following disclaimer in the documentation and/or
 *other materials provided with the distribution.
 *
 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 *CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES,
 *INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 *MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
 *CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 *NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 *CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 *STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 *ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 *ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 *
 * @author Jonathan Lightley
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Control {
    MainAF parent_ = null;
    
    Thread sent = null;
    Thread receive = null;
    Socket socket = null;
    Thread buttonListner = null;
    Thread backgroundSent = null;
    Thread backgroundReceive = null;
    
    public volatile boolean flagReceive = false;
    public volatile boolean flagSent = false;
    public volatile boolean flagRead = false;
    public volatile boolean flagBackgroundSent = false;
    public volatile boolean flagBackgroundFirstSent = false;
    public volatile boolean flagNoiseBackgroundSent = false;
    public volatile boolean flagBackgroundReceive = false;
    public volatile String pyZ = "0";  
    public volatile String pyZ2 = "0";
    public volatile String avgInt = "0";
    public volatile String back = "0";  
    
    Control(MainAF parent_in){
        parent_ = parent_in;
        socket = parent_.getSocket();
    }
    
    public boolean flagRecieve(){
        return flagReceive;
    }
    
    public boolean flagSent(){
        return flagSent;
    }

    public boolean flagRead(){
        return flagRead;
    }
        
    public String pyZ(){
        return pyZ;
    }
    
    public void set_flagReceive(boolean val){
        flagReceive = val;
    }
    
    public void set_flagSent(boolean val){
        flagSent = val;
    }

    public void set_flagRead(boolean val){
        flagRead = val;
    }

    public void set_pyZ(String val){
        pyZ = val;
    }   
    
    public void setSocket(Socket s){
        System.out.println("Socket is "+s.toString());
        socket = s;
    }
    
    public Socket getSocket(){
        return socket;
    }
    
    public void init_control_threads(){
        sent = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    while(true){
                        synchronized(parent_.control_){
                            if(parent_.control_.flagSent){
                                System.out.println("Loop sent");
                                out.write("call"+"\r\n");
                                out.flush();
                                parent_.control_.flagSent = false;
                                parent_.control_.flagReceive = true;
                            }
                        }
                    }    
                } catch (IOException e) {
                    System.out.println("Could not send to socket");
                }
            }            
        });
        
        receive = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    InputStreamReader stdIn =new InputStreamReader(socket.getInputStream());
                    BufferedReader in =new BufferedReader(stdIn);
                    String val_tmp = "0";
                    
                    while(true){
                        synchronized(parent_.control_){
                            if(parent_.control_.flagReceive){ 
                                String inni = in.readLine();
                                String[] projections = inni.split(",");
                                val_tmp = parent_.control_.pyZ;
                                parent_.control_.pyZ = projections[0];
                                parent_.control_.pyZ2 = projections[1];
                                parent_.control_.avgInt = projections[2];
                                System.out.println(inni);
                                parent_.control_.flagReceive = false;                            
                                parent_.control_.flagRead = true;
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Could not read socket");
                }        
            }
        });
        
        backgroundSent = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintWriter outB = new PrintWriter(socket.getOutputStream(), true);
                    while(true){
                        synchronized(parent_.control_){
                            if(parent_.control_.flagBackgroundSent){
                                System.out.println("Loop sent");
                                outB.write("background_call"+"\r\n");
                                outB.flush();
                                parent_.control_.flagBackgroundSent = false;
                                parent_.control_.flagBackgroundReceive = true;
                            }
                            else if(parent_.control_.flagBackgroundFirstSent){
                                System.out.println("Loop sent");
                                outB.write("background_first_call"+"\r\n");
                                outB.flush();
                                parent_.control_.flagBackgroundFirstSent = false;
                                parent_.control_.flagBackgroundReceive = true;
                            }
                            else if(parent_.control_.flagNoiseBackgroundSent){
                                System.out.println("Loop sent");
                                outB.write("noise_background_call"+"\r\n");
                                outB.flush();
                                parent_.control_.flagNoiseBackgroundSent = false;
                                parent_.control_.flagBackgroundReceive = true;
                            }
                        }
                    }    
                } catch (IOException e) {
                    System.out.println("Could not send to socket");
                }
            }            
        });
        
        backgroundReceive = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStreamReader stdIn_bg =new InputStreamReader(socket.getInputStream());
                    BufferedReader in_bg =new BufferedReader(stdIn_bg);
                    String val_tmp = "0";

                    while(true){
                        synchronized(parent_.control_){
                            if(parent_.control_.flagBackgroundReceive){ 
                                String inni = in_bg.readLine();
                                val_tmp = parent_.control_.back;
                                parent_.control_.back = inni;
                                System.out.println(inni);
                                parent_.control_.flagBackgroundReceive = false;                            
                                parent_.control_.flagRead = true;
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Could not read socket");
                }        
            }
        });
    }
}


