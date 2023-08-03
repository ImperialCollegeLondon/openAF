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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Math.round;
import java.net.Socket;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcorej.CMMCore;
import org.jfree.ui.RefineryUtilities;

public class AFclass {
    AFlogic aflog_ = new AFlogic();
    MainAF parent_ = null;
    private CMMCore core_;
    Scanner sc; 
    double afFoc = 1;
    double afFoc2 = 1;
    double afFoc3 = 1;
    double realFoc = 0;
    String zDev = null;
    long mody = 0;
    ArrayList<Double> afList = new ArrayList<Double>();
    ArrayList<Double> afList2 = new ArrayList<Double>();
    ArrayList<Double> afList3 = new ArrayList<Double>();
    ArrayList<Double> reList = new ArrayList<Double>();
    ArrayList<Double> xList = new ArrayList<Double>();
    double rangeZ = 20; // um
    double incZ = 0.2; // um
    double lowerLim = 1278;
    double upperLim = 1298;
    double linear_range = 20;
    double currZ = 0;
    double current_z = 9000;
    double next_z = 90001;
    double new_z_pos = 9000;
    double current_fwhm = 0;
    double current_fwhm_2 = 0;
    double current_int = 0;
    double current_fwhm_2b = 0;
    double current_int2 = 0;
    double start_pos = 9000;
    double end_pos = 9001;
    ArrayList<Double> stepSizeList = new ArrayList<Double>();
    double repRange = 5;
    double repSteps = 0.25;
    double repMax = 1;
    double dither = 0.1;
    double serStart = 50;
    double serEnd = 250;
    double serStep = 50;
    double fineRange = 6;
    double fineSteps = 0.2;
    int rep = 5;
    //int ccc =0;
    boolean dp = false;
    double calibMax = 1350;
    double calibMin = 1220;
    ArrayList<Double> LUT1 = new ArrayList<Double>();
    ArrayList<Double> LUT2 = new ArrayList<Double>();
    ArrayList<Double> LUT3 = new ArrayList<Double>();
    ArrayList<Double> calibList = new ArrayList<Double>();
    int countH;
        
    static Thread sent;
    static Thread receive;
    static Socket socket;
    static Thread buttonListner;
    static Thread backgroundSent;
    static Thread backgroundReceive;
       
    String zpos_STORM_path = "";   
    String z_log_path = "";   
    String defined_list = "upper";
    public static final String Z_FILE = "zPosSTORM.txt";
    public static final String Z_LOG = "z_log.txt";
    
    ArrayList<Double> lookuplist_fwhm = new ArrayList<Double>();
    ArrayList<Double> lookuplist_z = new ArrayList<Double>();
    
    AFclass(MainAF parent_in){
        parent_ = parent_in;
        socket = parent_.getSocket();
        zDev = parent_.core_.getFocusDevice();     
        set_file();
    }
    
    public void set_file(){
        Path IJpath = new File(ij.IJ.getDirectory("imagej")).toPath();
        if(IJpath.resolve(Z_FILE).toFile().exists()){
            zpos_STORM_path = IJpath.resolve(Z_FILE).toString();
        } else {
            File z_log = IJpath.resolve(Z_FILE).toFile();
            try {
                z_log.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(IJpath.resolve(Z_LOG).toFile().exists()){
            z_log_path = IJpath.resolve(Z_LOG).toString();
        } else {
            File z_log = IJpath.resolve(Z_LOG).toFile();
            try {
                z_log.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void defineAFFocus_Proj() {
        Double target_fine_value = null;
        Double target_coarse_value = null;
        try {
            current_z = parent_.core_.getPosition(zDev);
        } catch (Exception ex) {
            Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(dp != true){
            aflog_.split_file_Proj(afList, afList2, reList, afList3, parent_.Intensity_threshold);
        }
        boolean focusing = true;
        while(focusing){
            String val_tmp = "0";
            synchronized(parent_.control_){
                parent_.control_.flagRead=true;
                parent_.control_.flagSent=true;
                boolean Control_bool = true;
                while(Control_bool){
                    if(parent_.control_.flagRead){
                        val_tmp = parent_.control_.pyZ;
                        Control_bool = false;
                        parent_.control_.flagRead = false;
                    }
                }
            }
            boolean Ctrl_bool = true;
            while(Ctrl_bool){
                if(val_tmp!=parent_.control_.pyZ){
                    current_fwhm = Double.parseDouble(parent_.control_.pyZ);
                    Ctrl_bool=false;
                }
            }
            focusing = false;
        }
        current_fwhm = Double.parseDouble(parent_.control_.pyZ);
        current_fwhm_2 = Double.parseDouble(parent_.control_.pyZ2);
        current_int = Double.parseDouble(parent_.control_.avgInt);
        afFoc  = Double.parseDouble(parent_.control_.pyZ);
        afFoc2  = Double.parseDouble(parent_.control_.pyZ2);  
        afFoc3 = Double.parseDouble(parent_.control_.avgInt);
        String current_low_fine_pos = aflog_.look_up_defocus(current_fwhm, aflog_.lower_half_fine_proj_list, aflog_.lower_half_fine_z_list);
        String current_high_fine_pos = aflog_.look_up_defocus(current_fwhm, aflog_.upper_half_fine_proj_list, aflog_.upper_half_fine_z_list);
        int coarse_lower_idx = 0;
        int coarse_upper_idx = 0;
        ArrayList<Double> coarse_lower_list = null;
        ArrayList<Double> coarse_upper_list = null;
        if(aflog_.lower_half_coarse_z_list.contains(Double.parseDouble(current_low_fine_pos))){
            coarse_lower_idx = aflog_.look_up_closest_index_from_Z(Double.parseDouble(current_low_fine_pos), aflog_.lower_half_coarse_z_list);
            coarse_lower_list = aflog_.lower_half_coarse_proj_list;
        }
        else if(aflog_.upper_half_coarse_z_list.contains(Double.parseDouble(current_low_fine_pos))){
            coarse_lower_idx = aflog_.look_up_closest_index_from_Z(Double.parseDouble(current_low_fine_pos), aflog_.upper_half_coarse_z_list);
            coarse_lower_list = aflog_.upper_half_coarse_proj_list;
        }
        if(aflog_.lower_half_coarse_z_list.contains(Double.parseDouble(current_high_fine_pos))){
            coarse_upper_idx = aflog_.look_up_closest_index_from_Z(Double.parseDouble(current_high_fine_pos), aflog_.lower_half_coarse_z_list);
            coarse_upper_list = aflog_.lower_half_coarse_proj_list;
        }
        else if(aflog_.upper_half_coarse_z_list.contains(Double.parseDouble(current_high_fine_pos))){
            coarse_upper_idx = aflog_.look_up_closest_index_from_Z(Double.parseDouble(current_high_fine_pos), aflog_.upper_half_coarse_z_list);
            coarse_upper_list = aflog_.upper_half_coarse_proj_list;
        }
        Double target_low_coarse_value = coarse_lower_list.get(coarse_lower_idx);
        Double target_high_coarse_value = coarse_upper_list.get(coarse_upper_idx);
        if(current_fwhm >= parent_.FWHM_threshold){
            if(Math.abs(current_fwhm_2-target_low_coarse_value) < Math.abs(current_fwhm_2-target_high_coarse_value)){         
                defined_list = "lower";                
            }
            else{
                defined_list = "upper";
            }           
        }   
    }
    
    public double defineAFFocus() throws InterruptedException {
        if(dp != true){
            try {
                aflog_.read_file(zpos_STORM_path);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String val_tmp = "0";
        synchronized(parent_.control_){
            parent_.control_.flagRead=true;
            parent_.control_.flagSent=true;
            boolean Control_bool = true;
            
            while(Control_bool){
                if(parent_.control_.flagRead){
                    val_tmp = parent_.control_.pyZ;
                    Control_bool = false;
                    parent_.control_.flagRead = false;
                }
            }        
        }
        boolean Ctrl_bool = true;
            while(Ctrl_bool){
                if(val_tmp!=parent_.control_.pyZ){
                    afFoc = Double.parseDouble(parent_.control_.pyZ);
                    Ctrl_bool=false;
                }
            }
        afFoc = Double.parseDouble(parent_.control_.pyZ);
        System.out.println(afFoc);
        return afFoc; 
    }
    
    public String setZDev(){
        zDev = parent_.core_.getFocusDevice();
        return zDev;
    }
    
    public void calib(boolean dia){
        reList.clear();
        afList.clear();
        afList2.clear();
        afList3.clear();
        long stepsZ = round(2*parent_.range_/parent_.stepSize_);
        double af1 = 0;
        double af2 = 0;
        double af3 = 0;
        double re1 = 0; 
        double currZpreCalib = 3000;
        // set current position to start of scan range
        try{
            currZ = parent_.core_.getPosition(zDev);
            currZpreCalib = currZ;
            re1 = currZ-parent_.range_-parent_.stepSize_;
            parent_.core_.setPosition(zDev, re1);
            parent_.core_.waitForDevice(zDev);
            Thread.sleep(1000);
        }catch(Exception ex){
                System.out.println("Skipped z device outside loop");
            }
        //Go through z planes and read txt file
        synchronized(parent_.control_){parent_.control_.flagRead=true;}
        for (int i = 0; i <= stepsZ; i++) {
            re1 = re1+parent_.stepSize_;
            reList.add(re1);
            try{
                parent_.core_.setPosition(zDev, re1);
                parent_.core_.waitForDevice(zDev);
                currZ = re1;
            }catch(Exception ex){
                System.out.println("Skipped z device in loop");
            }
            boolean Control_bool = true;
            String val_tmp = "0";
            synchronized(parent_.control_){
                while(Control_bool){
                    if(parent_.control_.flagRead){
                        parent_.control_.flagSent = true;
                        parent_.control_.flagRead = false;
                        Control_bool = false;
                        val_tmp = parent_.control_.pyZ;
                    }else{
                        System.out.println("wait for message");
                    }    
                }
            }
            boolean Ctrl_bool = true;
            while(Ctrl_bool){
                if(val_tmp!=parent_.control_.pyZ){
                    af1 = Double.parseDouble(parent_.control_.pyZ);
                    af2 = Double.parseDouble(parent_.control_.pyZ2);
                    af3 = Double.parseDouble(parent_.control_.avgInt);
                    Ctrl_bool=false;
                }
            }
            afList.add(af1);
            afList2.add(af2);
            afList3.add(af3);
        }
        System.out.println(afList);
        System.out.println(reList);

        // prepare data for diagram
        int l = afList.size();
        double[][] a = new double[l][4];
        for (int ii = 0; ii < l-1; ii++) {  
            a[ii][0] = reList.get(ii);
            a[ii][1] = afList.get(ii);
            a[ii][2] = afList2.get(ii);
            a[ii][3] = afList3.get(ii);
        }
        
        // show diagram
        if(dia){
            final diagram demo = new diagram(a, parent_);
            demo.pack();
            RefineryUtilities.centerFrameOnScreen(demo);
            demo.setVisible(true);
        }
        // move to starting position
        try {
            parent_.core_.setPosition(zDev, currZpreCalib);
            parent_.core_.waitForDevice(zDev);
            Thread.sleep(1000);
        } catch (Exception ex) {
            Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void noise_background(boolean dia){
        double af1 = 0;
        synchronized(parent_.control_){
            parent_.control_.flagRead=true;
        }
        boolean Control_bool = true;
        String val_tmp = "0";
        synchronized(parent_.control_){
            while(Control_bool){
                if(parent_.control_.flagRead){
                    parent_.control_.flagNoiseBackgroundSent =true;   
                    parent_.control_.flagRead = false;
                    Control_bool = false;
                    val_tmp = parent_.control_.back;
                }else{
                    System.out.println("wait for message");
                }    
            }
        }
        boolean Ctrl_bool = true;
        while(Ctrl_bool){
            if(val_tmp!=parent_.control_.back){
                af1 = Double.parseDouble(parent_.control_.back);
                Ctrl_bool=false;
            }
        }
    }
    
    public void background(boolean dia){
        long stepsZ = round(2*parent_.range_/parent_.stepSize_);
        double af1 = 0;
        double re1 = 0; 
        double currZpreCalib = 3000;
        // set current position to start of scan range
        try{
            currZ = parent_.core_.getPosition(zDev);
            currZpreCalib = currZ;
            re1 = currZ-parent_.range_-parent_.stepSize_;
            parent_.core_.setPosition(zDev, re1);
            parent_.core_.waitForDevice(zDev);
            Thread.sleep(1000);
        } catch(Exception ex){
            System.out.println("Skipped z device outside loop");
        }
        // Go through z planes and read txt file
        synchronized(parent_.control_){
            parent_.control_.flagRead=true;
        }
        for (int i = 0; i <= stepsZ; i++) {
            re1 = re1+parent_.stepSize_;
            try{
                parent_.core_.setPosition(zDev, re1);
                parent_.core_.waitForDevice(zDev);
                currZ = re1;
            }catch(Exception ex){
                System.out.println("Skipped z device in loop");
            }
            boolean Control_bool = true;
            String val_tmp = "0";
            synchronized(parent_.control_){
                while(Control_bool){
                    if(parent_.control_.flagRead){
                        if(i==0){
                            parent_.control_.flagBackgroundFirstSent =true;   
                        }
                        else{
                            parent_.control_.flagBackgroundSent = true;
                        }
                        parent_.control_.flagRead = false;
                        Control_bool = false;
                        val_tmp = parent_.control_.back;
                    } else {
                        System.out.println("wait for message");
                    }    
                }
            }
            boolean Ctrl_bool = true;
            while(Ctrl_bool){
                if(val_tmp!=parent_.control_.back){
                    af1 = Double.parseDouble(parent_.control_.back);
                    Ctrl_bool=false;
                }
            }
        }
        // move to starting position
        try {
            parent_.core_.setPosition(zDev, currZpreCalib);
            parent_.core_.waitForDevice(zDev);
            Thread.sleep(1000);
        } catch (Exception ex) {
            Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void goToFocus_Projections() {
        double pre_defocus_z = current_z;
        if(dp != true){
            aflog_.split_file_Proj(afList, afList2, reList, afList3, parent_.Intensity_threshold);
        }
        boolean focusing = true;
        int repeats_ = 0;
        while(focusing){
            String val_tmp = "0";
            synchronized(parent_.control_){
                parent_.control_.flagRead=true;
                parent_.control_.flagSent=true;
                boolean Control_bool = true;
                while(Control_bool){
                    if(parent_.control_.flagRead){
                        val_tmp = parent_.control_.pyZ;
                        Control_bool = false;
                        parent_.control_.flagRead = false;
                    }
                }
            }
            boolean Ctrl_bool = true;
            while(Ctrl_bool){
                if(val_tmp!=parent_.control_.pyZ){
                    current_fwhm = Double.parseDouble(parent_.control_.pyZ);
                    Ctrl_bool=false;
                }
            }
            current_fwhm = Double.parseDouble(parent_.control_.pyZ);
            current_fwhm_2 = Double.parseDouble(parent_.control_.pyZ2);
            current_int = Double.parseDouble(parent_.control_.avgInt);
            if(dp == true){
                try {
                    current_z = parent_.core_.getPosition(zDev);
                    double defocus = (Math.round(100*((afFoc - Double.parseDouble(parent_.control_.pyZ)))))/100.0;
                    System.out.println(defocus);
                    new_z_pos = current_z + defocus;
                    current_z = Math.round(new_z_pos*100.0)/100.0;
                    parent_.core_.setPosition(zDev, current_z);
                    parent_.core_.waitForDevice(zDev);
                    focusing =false;
                } catch (Exception ex) {
                    Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if(current_int > parent_.Intensity_threshold){
                    String current_pos = null;
                    String current_low_coarse_pos = null;
                    String current_high_coarse_pos = null;
                    Double target_low_coarse_value = null;
                    Double target_high_coarse_value = null;                    
                    int fine_lower_idx = aflog_.look_up_closest_index(current_fwhm, aflog_.lower_half_fine_proj_list, aflog_.lower_half_fine_z_list);
                    int fine_upper_idx = aflog_.look_up_closest_index(current_fwhm, aflog_.upper_half_fine_proj_list, aflog_.upper_half_fine_z_list);
                    String current_low_fine_pos = aflog_.look_up_defocus(current_fwhm, aflog_.lower_half_fine_proj_list, aflog_.lower_half_fine_z_list);
                    String current_high_fine_pos = aflog_.look_up_defocus(current_fwhm, aflog_.upper_half_fine_proj_list, aflog_.upper_half_fine_z_list);
                    int coarse_lower_idx = 0;
                    int coarse_upper_idx = 0;
                    ArrayList<Double> coarse_lower_list = null;
                    ArrayList<Double> coarse_upper_list = null;
                    if(aflog_.lower_half_coarse_z_list.contains(Double.parseDouble(current_low_fine_pos))){
                        coarse_lower_idx = aflog_.look_up_closest_index_from_Z(Double.parseDouble(current_low_fine_pos), aflog_.lower_half_coarse_z_list);
                        coarse_lower_list = aflog_.lower_half_coarse_proj_list;
                    }
                    else if(aflog_.upper_half_coarse_z_list.contains(Double.parseDouble(current_low_fine_pos))){
                        coarse_lower_idx = aflog_.look_up_closest_index_from_Z(Double.parseDouble(current_low_fine_pos), aflog_.upper_half_coarse_z_list);
                        coarse_lower_list = aflog_.upper_half_coarse_proj_list;
                    }
                    if(aflog_.lower_half_coarse_z_list.contains(Double.parseDouble(current_high_fine_pos))){
                        coarse_upper_idx = aflog_.look_up_closest_index_from_Z(Double.parseDouble(current_high_fine_pos), aflog_.lower_half_coarse_z_list);
                        coarse_upper_list = aflog_.lower_half_coarse_proj_list;
                    }
                    else if(aflog_.upper_half_coarse_z_list.contains(Double.parseDouble(current_high_fine_pos))){
                        coarse_upper_idx = aflog_.look_up_closest_index_from_Z(Double.parseDouble(current_high_fine_pos), aflog_.upper_half_coarse_z_list);
                        coarse_upper_list = aflog_.upper_half_coarse_proj_list;;
                    }
                    target_low_coarse_value = coarse_lower_list.get(coarse_lower_idx);
                    target_high_coarse_value = coarse_upper_list.get(coarse_upper_idx);
                    if(current_fwhm >= parent_.FWHM_threshold){
                        if(Math.abs(current_fwhm_2-target_low_coarse_value) < Math.abs(current_fwhm_2-target_high_coarse_value)){         
                            current_pos = current_low_fine_pos;
                        }
                        else{
                            current_pos = current_high_fine_pos;
                        }
                        if(defined_list == "upper"){
                            lookuplist_fwhm = aflog_.upper_half_fine_proj_list;
                            lookuplist_z = aflog_.upper_half_fine_z_list;
                        }
                        else{
                            lookuplist_fwhm = aflog_.lower_half_fine_proj_list;
                            lookuplist_z = aflog_.lower_half_fine_z_list;
                            }
                        String Focus_pos = aflog_.look_up_defocus(afFoc, lookuplist_fwhm,lookuplist_z);
                        double defocus = (Math.round(100*((Double.parseDouble(Focus_pos) - (Double.parseDouble(current_pos))))))/100.0;
                        try {
                            current_z = parent_.core_.getPosition(zDev);
                            pre_defocus_z = current_z;
                        } catch (Exception ex) {
                            Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        new_z_pos = current_z + defocus;
                        current_z = Math.round(new_z_pos*100.0)/100.0;
                        parent_.lastFocusValue = current_z;
                        repeats_ = 0;
                        System.out.println(current_z);
                        try {
                            parent_.core_.setPosition(zDev, current_z);
                            parent_.core_.waitForDevice(zDev);
                        } catch (Exception ex) {
                            Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        File Z_pos = new File(zpos_STORM_path);
                        boolean exists = Z_pos.exists();
                        if(exists == false){
                            try {
                                if (Z_pos.createNewFile()) {
                                    System.out.println("File created: " + Z_pos.getName());
                                } else {
                                    System.out.println("File already exists.");
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
                            }  
                        }
                        FileWriter myWriter;
                        try {
                            myWriter = new FileWriter(z_log_path,true);
                            ZonedDateTime zdt = java.time.ZonedDateTime.now();
                            String timestamp = DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm:ss").format(zdt);
                            String details = "AF CONT ON "+timestamp+", Z position: "+Double.toString(pre_defocus_z)+" Defocus: "+Double.toString(defocus)+"\r\n";
                            myWriter.write(details);
                            myWriter.close();
                        } catch (IOException ex) {
                            Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        focusing =false;
                    } else {
                        try {
                            next_z = parent_.core_.getPosition(zDev);
                            new_z_pos  = next_z - 20.0;
                            next_z = Math.round(new_z_pos*100.0)/100.0;
                            parent_.core_.setPosition(zDev, next_z);
                            parent_.core_.waitForDevice(zDev);
                        } catch (Exception ex) {
                            Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
                        }                        
                        String val_tmp_2 = "0";
                        synchronized(parent_.control_){
                            parent_.control_.flagRead=true;
                            parent_.control_.flagSent=true;
                            boolean Control_bool_2 = true;
                            while(Control_bool_2){
                                if(parent_.control_.flagRead){
                                    val_tmp_2 = parent_.control_.pyZ;
                                    Control_bool_2 = false;
                                    parent_.control_.flagRead = false;
                                }
                            }
                        }
                        boolean Ctrl_bool_2 = true;
                        while(Ctrl_bool_2){
                            if(val_tmp_2!=parent_.control_.pyZ){
                                current_fwhm = Double.parseDouble(parent_.control_.pyZ);
                                Ctrl_bool_2=false;
                            }
                        }
                        current_fwhm_2b = Double.parseDouble(parent_.control_.pyZ2);
                        coarse_lower_idx = aflog_.look_up_closest_index(current_fwhm_2, aflog_.lower_half_coarse_proj_list, aflog_.lower_half_coarse_z_list);
                        coarse_upper_idx = aflog_.look_up_closest_index(current_fwhm_2, aflog_.upper_half_coarse_proj_list, aflog_.upper_half_coarse_z_list);
                        current_low_coarse_pos = aflog_.look_up_defocus(current_fwhm_2b, aflog_.lower_half_coarse_proj_list, aflog_.lower_half_coarse_z_list);
                        current_high_coarse_pos = aflog_.look_up_defocus(current_fwhm_2b, aflog_.upper_half_coarse_proj_list, aflog_.upper_half_coarse_z_list);  
                        int steps_per_micron = (int) Math.round(1.0/parent_.stepSize_);
                        if(coarse_lower_idx  - 20*steps_per_micron < 0){
                            target_low_coarse_value = aflog_.lower_half_coarse_proj_list.get(0);
                        }
                        else{
                            int new_idx = (int) Math.round(coarse_lower_idx  - 20*steps_per_micron);
                            target_low_coarse_value = aflog_.lower_half_coarse_proj_list.get(new_idx);
                        }
                        if(coarse_upper_idx  - 20*steps_per_micron < 0){
                            int new_idx = (int) Math.round(coarse_upper_idx  - 20*steps_per_micron + aflog_.lower_half_coarse_proj_list.size());
                            target_high_coarse_value = aflog_.lower_half_coarse_proj_list.get(new_idx);
                        }
                        else{
                            target_high_coarse_value = aflog_.upper_half_coarse_proj_list.get(coarse_upper_idx  - 20*steps_per_micron);
                        } 
                        
                        if(Math.abs(current_fwhm_2b-target_low_coarse_value) < Math.abs(current_fwhm_2b-target_high_coarse_value)){
                            current_pos = current_low_coarse_pos;
                        }
                        else{
                            current_pos = current_high_coarse_pos;
                        }
                        if(defined_list == "upper"){
                            lookuplist_fwhm = aflog_.upper_half_fine_proj_list;
                            lookuplist_z = aflog_.upper_half_fine_z_list;
                        }
                        else{
                            lookuplist_fwhm = aflog_.lower_half_fine_proj_list;
                            lookuplist_z = aflog_.lower_half_fine_z_list;
                            }
                        String Focus_pos = aflog_.look_up_defocus(afFoc, lookuplist_fwhm,lookuplist_z);
                        double defocus = (Math.round(100*((Double.parseDouble(Focus_pos) - (Double.parseDouble(current_pos))))))/100.0;
                        System.out.println("defocus = " + (defocus - 20.0));
                        try {
                            current_z = parent_.core_.getPosition(zDev);
                        } catch (Exception ex) {
                            Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
                        }
                            new_z_pos = current_z + defocus;
                            current_z = Math.round(new_z_pos*100.0)/100.0;                        
                        try {
                            parent_.core_.setPosition(zDev, current_z);
                            parent_.core_.waitForDevice(zDev);
                        } catch (Exception ex) {
                            Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        repeats_ += 1;
                        if(repeats_ > 1){
                        try {
                            parent_.core_.setPosition(zDev, parent_.lastFocusValue);
                            parent_.core_.waitForDevice(zDev);
                        } catch (Exception ex) {
                            Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        try {
                            focusing = false;
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        repeats_ = 0;
                    }
                }
            } else {
                String current_pos = null;
                int int_lower_idx = 0;
                int int_upper_idx = 0;
                ArrayList<Double> int_lower_list = null;
                ArrayList<Double> int_upper_list = null;
                String current_low_int_pos = null;
                String current_high_int_pos = null;
                Double target_low_int_value = null;
                Double target_high_int_value = null;
                try {
                    next_z = parent_.core_.getPosition(zDev);

                    new_z_pos  = next_z - 20.0;
                    next_z = Math.round(new_z_pos*100.0)/100.0;

                    parent_.core_.setPosition(zDev, next_z);
                    parent_.core_.waitForDevice(zDev);
                } catch (Exception ex) {
                    Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
                }                    
                String val_tmp_2 = "0";
                synchronized(parent_.control_){
                    parent_.control_.flagRead=true;
                    parent_.control_.flagSent=true;
                    boolean Control_bool_2 = true;
                    while(Control_bool_2){
                        if(parent_.control_.flagRead){
                            val_tmp_2 = parent_.control_.pyZ;
                            Control_bool_2 = false;
                            parent_.control_.flagRead = false;
                        }
                    }
                }
                boolean Ctrl_bool_2 = true;
                while(Ctrl_bool_2){
                    if(val_tmp_2!=parent_.control_.pyZ){
                        current_fwhm = Double.parseDouble(parent_.control_.pyZ);
                        Ctrl_bool_2=false;
                    }
                }
                current_int2 = Double.parseDouble(parent_.control_.avgInt);
                int_lower_idx = aflog_.look_up_closest_index(current_int, aflog_.lower_half_int_list, aflog_.lower_half_int_z_list);
                int_upper_idx = aflog_.look_up_closest_index(current_int, aflog_.upper_half_int_list, aflog_.upper_half_int_z_list);
                current_low_int_pos = aflog_.look_up_defocus(current_int2, aflog_.lower_half_int_list, aflog_.lower_half_int_z_list);
                current_high_int_pos = aflog_.look_up_defocus(current_int2, aflog_.upper_half_int_list, aflog_.upper_half_int_z_list);  
                int steps_per_micron = (int) Math.round(1.0/parent_.stepSize_);
                if(int_lower_idx  - 50*steps_per_micron < 0){
                    target_low_int_value = aflog_.lower_half_int_list.get(0);
                }
                else{
                    int new_idx = (int) Math.round(int_lower_idx  - 20*steps_per_micron);
                    target_low_int_value = aflog_.lower_half_int_list.get(new_idx);
                }
                if(int_upper_idx  - 20*steps_per_micron < 0){
                    int new_idx = (int) Math.round(int_upper_idx  - 20*steps_per_micron + aflog_.lower_half_int_list.size());
                    target_high_int_value = aflog_.lower_half_int_list.get(new_idx);
                }
                else{
                    target_high_int_value = aflog_.upper_half_int_list.get(int_upper_idx  - 20*steps_per_micron);
                } 
                if(Math.abs(current_int2-target_low_int_value) < Math.abs(current_int2-target_high_int_value)){
                    current_pos = current_low_int_pos;
                }
                else{
                    current_pos = current_high_int_pos;
                }
                if(defined_list == "upper"){
                    lookuplist_fwhm = aflog_.upper_half_fine_proj_list;
                    lookuplist_z = aflog_.upper_half_fine_z_list;
                }
                else{
                    lookuplist_fwhm = aflog_.lower_half_fine_proj_list;
                    lookuplist_z = aflog_.lower_half_fine_z_list;
                    }
                String Focus_pos = aflog_.look_up_defocus(afFoc, lookuplist_fwhm,lookuplist_z);
                double defocus = (Math.round(100*((Double.parseDouble(Focus_pos) - (Double.parseDouble(current_pos))))))/100.0;
                System.out.println("defocus = " + (defocus - 20.0));//Because of the 20um step
                try {
                    current_z = parent_.core_.getPosition(zDev);
                } catch (Exception ex) {
                    Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
                }
                    new_z_pos = current_z + defocus;
                    current_z = Math.round(new_z_pos*100.0)/100.0;                        

                try {
                    parent_.core_.setPosition(zDev, current_z);
                    parent_.core_.waitForDevice(zDev);
                } catch (Exception ex) {
                    Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
                }
                repeats_ += 1;
                if(repeats_ > 1){
                try {
                    parent_.core_.setPosition(zDev, parent_.lastFocusValue);
                    parent_.core_.waitForDevice(zDev);
                } catch (Exception ex) {
                    Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(AFclass.class.getName()).log(Level.SEVERE, null, ex);
                }
                repeats_ = 0;
                }
            }
        }
    }       
}
