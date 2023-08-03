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

import java.util.logging.Level;
import java.util.logging.Logger;
import openAF.OpenAF.AF_measurement;
import java.awt.Toolkit;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.micromanager.internal.utils.MMException;

public class AF_loop_thread implements Runnable{
    boolean OpenAF_not_dead = true;
    MainAF parent_ = null;
    private static final String SET_NEW_OFFSET = "Set new offset";
    private static final String SET_REF = "Set reference";
    String action = SET_REF;
    double AF_value = 0;
    String meas_type = "LR_MEAS";
    long measurement_time = 0;
    double offset = 0;
    double compensated_value = 0;
    int num_measurements_to_store = 1;
    String returned = "";
    AF_measurement[] measured_points = new AF_measurement[num_measurements_to_store];
    int measurement_counter = 0;
    int wait_time_ms = 100;
    boolean SocketConnect = false;
    Socket socket;
    boolean first_time = true;
    boolean socketStart = false;
    
    
    AF_loop_thread(MainAF parent_in){
        parent_ = parent_in;
        socket = null;      
    }
    
    @Override
    public void run() {
        initSocket();
        parent_.setSocket(socket);
        parent_.control_.setSocket(socket);
        parent_.control_.init_control_threads();
        parent_.control_.sent.start();    
        parent_.control_.receive.start();        
        parent_.control_.backgroundSent.start();
        parent_.control_.backgroundReceive.start();  
        if(parent_.initialised){
            parent_.af_ = new AFclass(parent_);
        }
        while(true){
            if(parent_.initialised){
                parent_.settings_applied = false;
                parent_.applySettings();
                if(parent_.settings_applied){
                    try {
                        if(!parent_.noiseBg_.equals("Off")){
                            if(parent_.noiseBg_.equals("Set noise BG")){
                                System.out.println("Set noise BG");                            
                                parent_.setPropertyValue("Noise Background", "Off");
                                parent_.af_.noise_background(false);
                            }
                            else if(parent_.noiseBg_.equals("Set BG")){
                                System.out.println("Set BG");
                                parent_.setPropertyValue("Noise Background", "Off");
                                parent_.af_.background(false);
                            }
                        }
                        else if(parent_.calib_.equals("On")){
                            System.out.println("Calibration");
                            parent_.setPropertyValue("Calibration", "Off");
                            parent_.af_.calib(true);
                        }
                        else if(parent_.defineFocus_.equals("On")){
                            System.out.println("Define Focus");
                            parent_.setPropertyValue("Define Focus", "Off"); 
                            parent_.af_.defineAFFocus_Proj();
                        }
                        else if(parent_.do_single_shot){
                            System.out.println("Single Shot");
                            parent_.do_single_shot = false;
                            parent_.af_.goToFocus_Projections();
                        }
                        else if(parent_.contFocus_.equals("On")){
                            System.out.println("Cont Focus On!");
                            parent_.af_.goToFocus_Projections();
                        } else {
                            //Not needed
                        }
                    } catch (MMException ex) {
                    Logger.getLogger(AF_loop_thread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }    
    

 
    private void do_AF_stuff() throws InterruptedException {
        if(parent_.get_do_single_shot()){
            loud_beep("TEST BEEP");//Helps alert user that AF succeeded in single shot
            parent_.set_do_single_shot(false);
        }
        if(parent_.get_do_continuous()){
            System.out.println("Continuous focus");
            int value = 0;
            if(value != 0){
                //AF fail
                loud_beep("Cont AF FAIL!");
                parent_.set_do_continuous(false);
            }
        }
    }

    
    private void measure() {
        //Do comms and read
        boolean focusing = true;
        int loop = 0;    
        double home_pos = 9550.00;
        while(focusing){
            String current_pos;
            String val_tmp = "0";
            synchronized(parent_.control_){
                parent_.control_.set_flagRead(true);
                parent_.control_.set_flagSent(true);
                boolean Control_bool = true;
                while(Control_bool){
                    if(parent_.control_.flagRead()){
                        val_tmp = parent_.control_.pyZ();
                        Control_bool = false;
                        parent_.control_.set_flagRead(false);
                    }
                }
            }
            boolean Ctrl_bool = true;
            while(Ctrl_bool){
                if(val_tmp!=parent_.control_.pyZ()){
                    AF_value = Double.parseDouble(parent_.control_.pyZ());
                    Ctrl_bool=false;
                }
            }
            AF_value = Double.parseDouble(parent_.control_.pyZ());
            focusing =false;
            double offset_at_measurement_time = 0.0;
            measurement_time = System.currentTimeMillis();
            //Store values
            measured_points[measurement_counter] = new AF_measurement(AF_value,meas_type,measurement_time,offset_at_measurement_time);
            measurement_counter++;
            if(measurement_counter>=num_measurements_to_store){
                measurement_counter=0;
            }
        }
    }

    private void loud_beep(String msg) {
        Toolkit.getDefaultToolkit().beep();
        String title = "OpenAF";
        JDialog dialog = nonblock_dialogue(msg,title);
        dialog.setVisible(true);        
    }
    
    private final JDialog nonblock_dialogue(String msg, String title){
        JOptionPane pane = new JOptionPane();
        pane.setOptions(new Object[]{}); // Removes all buttons
        JDialog dialog = pane.createDialog(null, title);       
        dialog.setModal(false); // IMPORTANT! Now the thread isn't blocked
        return dialog;
    }    
    
    public void initSocket(){
        System.out.println("Iniating socket");
        boolean SocketConnect = false;
        String message = "Do you want to try to connect to Socket?";
        int n = JOptionPane.showConfirmDialog(null, message, "Information:",JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            SocketConnect = true;                    
        }
        if(SocketConnect){
            while(SocketConnect){
                try {
                    socket = new Socket("localhost",9999);
                    SocketConnect = false;
                    JOptionPane.showMessageDialog(null, "Socket Connected!");
                    parent_.socketConnected = true;
                } catch (UnknownHostException e1) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        //no action useful
                    }
                } catch (IOException e1) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        //no action useful
                    }
                }
            }
        }
        if(socket == null){
            System.out.println("Socket is null.");
        }
    }
    
    public void setSocket(Socket s){
        socket = s;
    }
}