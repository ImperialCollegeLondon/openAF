package Utilities;

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
 * @author Sunil Kumar
 */

import java.awt.Component;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class utils2 {
    //Utility names and functions for various openScopes plugins
    //OHCA2 defaults
    boolean test;
    public static final String TIME_SEQ = "Time step";
    public static final String XY_SEQ = "XY position";
    public static final String Z_SEQ = "Z stack";
    public static final String CONF_SEQ = "Config change";
    public static final String HL1_ON = "<font color = 'ff0000'>";
    public static final String HL1_OFF = "</font>";
    public static final String MAIN_FOLDER = "OpenHCA2_files";
    public static final String RUNNABLE_SUBFOLDER = "OpenHCA2_runnables";
    public static final String PLATEPROPS_SUBFOLDER = "OpenHCA2_plate_properties";
    public static final String STAGE_MAPPING = "OpenHCA2_stage_mapping.txt";
    public static final String CONFIG_FILE = "OpenHCA2_settings.json";
    public static final String JSON_DELIMITER = "$#!+*";
    //STAGE POSITION PROPERTY NAME REFS
    public static final String POS_SAVE_GENERATED_BY = "POS_SAVE_GEN_BY";
    public static final String POS_SAVE_TYPE_STRIP_TILING = "Strip tiling";
    public static final String POS_SAVE_TYPE_SPIRAL = "Spiral";
    public static final String POS_SAVE_TYPE_RANDOM = "Random";
    public static final String POS_SAVE_TYPE_AREA_GRID = "Area grid";
    public static final String OHCA2_WELL = "OpenHCA2_Well";
    public static final String OHCA2_PLATE_ROW = "OpenHCA2_Plate_Row";
    public static final String OHCA2_PLATE_COL = "OpenHCA2_Plate_Column";
    public static final String OHCA2_INTRA_PATTERN_INDEX = "OpenHCA2_Intra_pattern_index";
    public static final String OHCA2_INTRA_WELL_GROUPING = "OpenHCA2_Intra_well_grouping";
    public static final String OHCA2_SORT_STYLE = "OpenHCA2_Sorting_style";
    //Info panel warnings
    public static final String IP_NO_PIX_CALIB = "* WARNING: pixel calibration not done for this configuration - default FOV size of 300x200 um being shown!\r\n";
    public static final String IP_NO_OFFSET_FOR_PIX_CALIB = "* No offset values were found for this pixel calibration!\r\n";
    public static final String IP_NO_STAGE = "* This scope configuration does not have an XY stage!\r\n";
    
    public utils2(){
    }
    
    public String[] needed_folders(){
        String[] folder_list = new String[]{
            PLATEPROPS_SUBFOLDER,
            RUNNABLE_SUBFOLDER
        };
        return folder_list;
    }
    
    public void generate_missing_folders(Path IJ_path){
        if(IJ_path.resolve(MAIN_FOLDER).toFile().exists()){

        } else {
            IJ_path.resolve(MAIN_FOLDER).toFile().mkdir();
        }
        //Once we're sure that the main is there, check if subdirs are too...
        for (String subdir : needed_folders()){
            if(IJ_path.resolve(MAIN_FOLDER).resolve(subdir).toFile().exists()){
            } else {
                IJ_path.resolve(MAIN_FOLDER).resolve(subdir).toFile().mkdir();
                //ADD POPUP NOTIFICATION?
            }            
        }
    }

    public double abs_min_diff_dbl(ArrayList<Double> inputlist){
        double min_diff = Double.MAX_VALUE;//Default position is to give the worst-case scenario
        if(inputlist.size()<2){
            min_diff = 0;
        } else {
            Collections.sort(inputlist);
            int i=0;
            while (i<(inputlist.size()-1)){
                double delta = inputlist.get(i+1)-inputlist.get(i);
                if(delta<Math.abs(min_diff)){
                    min_diff = delta;
                }
                i++;
            }
        }
        return min_diff;   
    }
    
    public int abs_min_diff_int(ArrayList<Integer> inputlist){
        ArrayList<Double> trans_list = new ArrayList<Double>();
        for(int val : inputlist){
            trans_list.add((double)val);
        }
        double ans = abs_min_diff_dbl(trans_list);
        return (int)ans;
    }
    
    public boolean does_file_exist(File file_to_check){
        return false;
    }
    
    public int s_to_ms(double s){
        return Integer.parseInt(read_num_sensible(String.valueOf(s*1000.0),true));
    }
    
    public int min_to_ms(double s){
        return Integer.parseInt(read_num_sensible(String.valueOf(s*1000*60),true));
    }    
    
    public int hr_to_ms(double s){
        return Integer.parseInt(read_num_sensible(String.valueOf(s*1000*60*60),true));
    }        
    
    public double ms_to_s(int ms){
        return Double.parseDouble(read_num_sensible(String.valueOf(ms/1000),false,true,3));//3d.p.s
    }
    
    public double ms_to_min(int ms){
        return Double.parseDouble(read_num_sensible(String.valueOf(ms/(60*1000)),false,true,3));//3d.p.s
    }    
    
    public double ms_to_hr(int ms){
        return Double.parseDouble(read_num_sensible(String.valueOf(ms/(60*60*1000)),false,true,3));//3d.p.s
    }        
    
    public String give_html(String inputstr, String format){
        boolean skip = false;
        format = format.toUpperCase();
        if (format == "I" || format == "B" || format =="U"){
        } else {
            skip = true;
        }
        if (skip == true){
            return(inputstr);
        } else {
            return("<"+format+">"+inputstr+"</"+format+">");
        }
    }
    
    public int option_popup(Object parent, String title, Object message, Object[] options){
        return JOptionPane.showOptionDialog((Component) parent, message, title, JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[0]);
    }
    
    // Following function from https://deano.me/java-resize-arrays-multi-dimensional-arrays/
    /** * Reallocates an array with a new size, and copies the contents* of the old array to the new array.* By Dean Williams - http://dean.resplace.net* @param oldArray the array to resize.* @param newSize the new array size.* @return The resized array.*/
    /*
    public static Object resizeArray(Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(elementType,newSize);
        int preserveLength = Math.min(oldSize,newSize);
        if (preserveLength > 0) {
            System.arraycopy(oldArray,0,newArray,0,preserveLength);
        }
        return newArray;
    }
    */

    public String strip_non_numeric(String stringtostrip){
        int negcheck = stringtostrip.indexOf("-");
        String strippedstring = stringtostrip.replaceAll("[^\\d.]", "");
        String nodots = strippedstring.replaceAll("[^\\d]","");
        int firstdot = strippedstring.indexOf("."); //First decimal point will be taken
        String floatval;
        if(firstdot>=0){
            floatval = nodots.substring(0,firstdot)+"."+nodots.substring(firstdot);
        } else {
            floatval = nodots;
        }
        if(negcheck == 0 && nodots.length()>0){//First character is a - sign, and there is actually a number there
            floatval = "-"+floatval;
        }
        return floatval;
    }
    
    public String constrain_val(String val, double min, double max){
        return Double.toString(constrain_val(Double.parseDouble(val),min,max));
    }
    
    public int set_decent_step(ArrayList<Double> scale_options, double range, int num_steps_min){
        Collections.sort(scale_options);
        int use_step = 1;
        for (double scale_opt : scale_options){
            if(range>(scale_opt*num_steps_min)){
                use_step = (int)scale_opt;
            }
        }
        return use_step;
    }
    
    public int round_to_nearest(double  value, int stepsize){
        return (int)(stepsize*(Math.round(value/stepsize)));
    }
    
    public int round_up_to_nearest(double  value, int stepsize){
        return (int)(stepsize*(Math.ceil(value/stepsize)));
    }    
    
    public int round_down_to_nearest(double value, int stepsize){
        return (int)(stepsize*(Math.floor(value/stepsize)));
    }        

    public double constrain_val(double val, double min, double max){    
        if (val<min){
            val = min;
        }
        if (val>max){
            val = max;
        }
        return val;
    }
    
    public String zero_pad(String input_str, int num_digits){
        //Will just return input if output is longer than num_digits
        String output_str = "";
        int delta = num_digits-input_str.length();
        if(delta>0){
            for (int i=0;i<delta;i++){
                output_str+="0";
            }
        }
        output_str+=input_str;
        return output_str;
    }
    
    public float sum_arr(byte[] arr){
        float total = 0;
        for (int i=0;i<arr.length;i++){
            total+=arr[i];
        }
        return total;
    }
    
    public float sum_arr(short[] arr){
        float total = 0;
        for (int i=0;i<arr.length;i++){
            total+=arr[i];
        }
        return total;        
    }    
    
    public String read_num_sensible(String input_value, boolean force_int, boolean pos_only, int num_dp){
        //Basic way to get right # of decimal points, but it should at least be safe
        //https://stackoverflow.com/questions/153724/how-to-round-a-number-to-n-decimal-places-in-java
        String retval = "0.0";
        if(strip_non_numeric(input_value).replaceAll("-", "").length()<1){//Also, just "-" would be bad...
            //Leave everything as zero...
        } else {
            double value = Double.parseDouble(strip_non_numeric(input_value));
            if(pos_only){
                value = Math.abs(value);
            }
            if(force_int){
                value = Math.round(value);
                return Integer.toString((int)value);
            }

            retval = Double.toString(value);
            if (num_dp>0){
                if(retval.indexOf(".")>0){
                    if(retval.length()>retval.indexOf(".")+num_dp){
                        retval = retval.substring(0, retval.indexOf(".")+num_dp+1);
                    } else {
                        retval = retval.substring(0, retval.length());
                    }
                }
            }
        }
        return retval;
    }    
    
    public String read_num_sensible(String input_value, boolean force_int, boolean pos_only){
        return read_num_sensible(input_value, force_int, pos_only, -1);
    }
    
    //Overloading for ease of use
    public String read_num_sensible(String input_value, boolean force_int){
        return read_num_sensible(input_value, force_int, false);
    }
    public String read_num_sensible(String input_value){
        return read_num_sensible(input_value, false, false);
    }    
    
    public String force_sf(Double inputnum, Integer n_dp, Integer zero_pad_length){
        //Decimal places
        String string_val = String.format("%."+n_dp.toString()+"f", inputnum);
        //Zeropad (including the dot)
        while(string_val.length()<zero_pad_length){
            string_val = "0"+string_val;
            System.out.println(string_val);
        }
        return inputnum.toString();
    }
    
    public File get_a_path(File startpath, String label, int mode, JFrame source_frame){
        JFileChooser j_FC = new JFileChooser();
        j_FC.setDialogTitle(label);
        File selectedFile;
        j_FC.setFileSelectionMode(mode);
        if (startpath != null) {
            j_FC.setCurrentDirectory(startpath);
        }
        int result = j_FC.showOpenDialog(source_frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            return j_FC.getSelectedFile();
        } else {
            return null;
        }        
    }
}