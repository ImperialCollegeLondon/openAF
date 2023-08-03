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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.analysis.interpolation.LinearInterpolator;

public class AFlogic {
    private MainAF parent_;
    ArrayList<Double> z_pos_list  = new ArrayList<Double>();
    ArrayList<Double> fwhm_list  = new ArrayList<Double>();
    LinearInterpolator interp= new LinearInterpolator();
    Double fwhm_max_value;
    int fwhm_max_idx;
    ArrayList<Double> lower_half_fwhm_list = new ArrayList<Double>();
    ArrayList<Double> upper_half_fwhm_list = new ArrayList<Double>();
    ArrayList<Double> lower_half_fine_z_list = new ArrayList<Double>();
    ArrayList<Double> upper_half_fine_z_list = new ArrayList<Double>();
    ArrayList<Double> lower_half_coarse_z_list = new ArrayList<Double>();
    ArrayList<Double> upper_half_coarse_z_list = new ArrayList<Double>();
    Double fine_proj_max_value;
    int fine_proj_max_idx;
    ArrayList<Double> lower_half_fine_proj_list = new ArrayList<Double>();
    ArrayList<Double> upper_half_fine_proj_list = new ArrayList<Double>();
    Double coarse_proj_max_value;
    int coarse_proj_max_idx;
    ArrayList<Double> lower_half_coarse_proj_list = new ArrayList<Double>();
    ArrayList<Double> upper_half_coarse_proj_list = new ArrayList<Double>();
    Double int_max_value;
    int int_max_idx;
    ArrayList<Double> lower_half_int_list = new ArrayList<Double>();
    ArrayList<Double> upper_half_int_list = new ArrayList<Double>();
    ArrayList<Double> lower_half_int_z_list = new ArrayList<Double>();
    ArrayList<Double> upper_half_int_z_list = new ArrayList<Double>();
    
    
    

    
    public double check_z_spacing(){
        String first_z = z_pos_list.get(0).toString();
        String second_z = z_pos_list.get(1).toString();
        double step_size = Double.parseDouble(second_z)-Double.parseDouble(first_z);
        return step_size;
    }
    
    public void read_file(String path) throws FileNotFoundException {
        z_pos_list.clear();
        fwhm_list.clear();
        Scanner sc=new Scanner(new FileReader(path));
        while (sc.hasNextLine()){
            String line = sc.nextLine();
            String[] values = line.split("\t");
            z_pos_list.add(Double.parseDouble(values[1]));
            fwhm_list.add(Double.parseDouble(values[0]));       
        }
    }

    public void interpolate(ArrayList x, ArrayList y) throws ArgumentOutsideDomainException{        
        ArrayList intermediate_x  = new ArrayList();
        ArrayList intermediate_y  = new ArrayList();
        if(Math.round(100*Math.abs(Double.parseDouble(x.get(0).toString()) - Double.parseDouble(x.get(1).toString())))!=1){
            double increments = 100*Math.abs(Double.parseDouble(x.get(0).toString()) - Double.parseDouble(x.get(1).toString()));
            System.out.println("increment = " + increments);
            for(int i=0; i<=x.size()-2; i++){
                double first_x = Double.parseDouble(x.get(i).toString());
                double next_x = Double.parseDouble(x.get(i+1).toString());
                double first_y = Double.parseDouble(y.get(i).toString());
                double next_y = Double.parseDouble(y.get(i+1).toString());
                if(Math.signum(first_y)*Math.signum(next_y) >0){
                    double[] interp_x_points  = {first_x, next_x};
                    double[] interp_y_points = {first_y, next_y};
                    double gradient = (next_y - first_y)/(next_x - first_x);
                    double intercept = first_y - gradient * first_x;
                    for(int j=0; j<=increments-1; j++){
                        double x_new  = Math.round((first_x+(next_x - first_x)*(1/increments)*j)* 100.0) / 100.0;
                        intermediate_x.add(x_new);          
                        double y_new = gradient*x_new + intercept;
                        intermediate_y.add(y_new);

                    }
                intermediate_x.add(next_x);
                intermediate_y.add(next_y);
                }
                else{
                    double first_xx = Double.parseDouble(x.get(i-1).toString());
                    double next_xx = Double.parseDouble(x.get(i).toString());
                    double first_yy = Double.parseDouble(y.get(i-1).toString());
                    double next_yy = Double.parseDouble(y.get(i).toString());
                    double[] interp_xx_points  = {first_xx, next_xx};
                    double[] interp_yy_points = {first_yy, next_yy};
                    double gradient = (next_yy - first_yy)/(next_xx - first_xx);
                    double intercept = first_yy - gradient * first_xx;
                    double fake_next_x = first_x+(increments/100);
                    for(int j=1; j<=increments; j++){
                        double x_new  = Math.round((first_x+(fake_next_x - first_x)*(1/increments)*j)* 100.0) / 100.0;
                        intermediate_x.add(x_new);          
                        double y_new = gradient*x_new + intercept;
                        intermediate_y.add(y_new);
                    }

                    double first_xxx = Double.parseDouble(x.get(i+1).toString());
                    double next_xxx = Double.parseDouble(x.get(i+2).toString());
                    double first_yyy = Double.parseDouble(y.get(i+1).toString());
                    double next_yyy = Double.parseDouble(y.get(i+2).toString());
                    double[] interp_xxx_points  = {first_xxx, next_xxx};
                    double[] interp_yyy_points = {first_yyy, next_yyy};
                    double gradient2 = (next_yyy - first_yyy)/(next_xxx - first_xxx);
                    double intercept2 = first_yyy - gradient2 * first_xxx;
                    double fake_first_x = next_x-(increments/100);
                    for(int k=0; k<=increments-1; k++){
                        double x_new2  = Math.round((fake_first_x+(next_x - fake_first_x)*(1/increments)*k)* 100.0) / 100.0;
                        intermediate_x.add(x_new2);          
                        double y_new2 = gradient2*x_new2 + intercept2;
                        intermediate_y.add(y_new2);                
                    }
                }
            }

            z_pos_list = intermediate_x;
            fwhm_list = intermediate_y;
        }
        else{
            z_pos_list = x;
            fwhm_list = y;
            
        }
    }

      
    
    public void check_lists(){
        System.out.println("Z list = " + z_pos_list);
        System.out.println("FWHM list = " + fwhm_list);  
    }
    
    public String look_up_defocus(double target_value, ArrayList<Double> list_fwhm, ArrayList<Double> z_list){
        double closest = Double.parseDouble(list_fwhm.get(0).toString());
        int closest_index = 0;

        for(int i = 1; i<=list_fwhm.size()-1; i++){
            if (Math.abs(closest - target_value) > Math.abs(Double.parseDouble(list_fwhm.get(i).toString()) - target_value)){
                closest_index = i;
                closest = Double.parseDouble(list_fwhm.get(i).toString());
            }
        }
        String current_position = z_list.get(closest_index).toString();
        return current_position;
    }
        
    public int look_up_closest_index_from_Z(double current_Pos, ArrayList<Double> z_list){
        double closest = Double.parseDouble(z_list.get(0).toString());
        int closest_index = 0;
        for(int i = 1; i<=z_list.size()-1; i++){
            if (Math.abs(closest - current_Pos) > Math.abs(Double.parseDouble(z_list.get(i).toString()) - current_Pos)){
                closest_index = i;
                closest = Double.parseDouble(z_list.get(i).toString());
            }
        }
        return closest_index;
    }

    public int look_up_closest_index(double target_value, ArrayList<Double> list_fwhm, ArrayList<Double> z_list){
        double closest = Double.parseDouble(list_fwhm.get(0).toString());
        int closest_index = 0;
        
        for(int i = 1; i<=list_fwhm.size()-1; i++){
            if (Math.abs(closest - target_value) > Math.abs(Double.parseDouble(list_fwhm.get(i).toString()) - target_value)){
                closest_index = i;
                closest = Double.parseDouble(list_fwhm.get(i).toString());
            } 
        }
        return closest_index;
    }

    void split_file() {
        fwhm_max_value = Collections.max(fwhm_list);
        fwhm_max_idx = fwhm_list.indexOf(fwhm_max_value);
        lower_half_fwhm_list = new ArrayList<Double>(fwhm_list.subList(0, fwhm_max_idx));
        upper_half_fwhm_list = new ArrayList<Double>(fwhm_list.subList(fwhm_max_idx, fwhm_list.size()));
        lower_half_fine_z_list = new ArrayList<Double>(z_pos_list.subList(0, fwhm_max_idx));
        upper_half_fine_z_list = new ArrayList<Double>(z_pos_list.subList(fwhm_max_idx, fwhm_list.size()));
        if(Collections.min(lower_half_fwhm_list)>Collections.min(upper_half_fwhm_list)){
            parent_.FWHM_threshold = Collections.min(lower_half_fwhm_list)+0.1*(Collections.max(lower_half_fwhm_list)-Collections.min(lower_half_fwhm_list));
        }
        else{
            parent_.FWHM_threshold = Collections.min(upper_half_fwhm_list)+0.1*(Collections.max(upper_half_fwhm_list)-Collections.min(upper_half_fwhm_list));
        }
    }
    
    void split_file_Proj(ArrayList<Double> proj_list, ArrayList<Double> proj_list2, ArrayList<Double> z_list,  ArrayList<Double> Intensity_list, double intThresh) {
        int_max_value = Collections.max(Intensity_list);
        int_max_idx = Intensity_list.indexOf(int_max_value);
        lower_half_int_list = new ArrayList<Double>(Intensity_list.subList(0, int_max_idx));
        upper_half_int_list = new ArrayList<Double>(Intensity_list.subList(int_max_idx, Intensity_list.size()));
        lower_half_int_z_list = new ArrayList<Double>(z_list.subList(0, int_max_idx));
        upper_half_int_z_list = new ArrayList<Double>(z_list.subList(int_max_idx, Intensity_list.size())); 
        
        String current_low_int_pos = look_up_defocus(intThresh, lower_half_int_list, lower_half_int_z_list);
        String current_high_int_pos = look_up_defocus(intThresh, upper_half_int_list, upper_half_int_z_list);       
   
        fine_proj_max_value = Collections.max(proj_list);
        fine_proj_max_idx = proj_list.indexOf(fine_proj_max_value);
        lower_half_fine_proj_list = new ArrayList<Double>(proj_list.subList(0, fine_proj_max_idx));
        upper_half_fine_proj_list = new ArrayList<Double>(proj_list.subList(fine_proj_max_idx, proj_list.size()));
        lower_half_fine_z_list = new ArrayList<Double>(z_list.subList(0, fine_proj_max_idx));
        upper_half_fine_z_list = new ArrayList<Double>(z_list.subList(fine_proj_max_idx, proj_list.size()));
        coarse_proj_max_value = Collections.max(proj_list2);
        coarse_proj_max_idx = proj_list2.indexOf(coarse_proj_max_value);
        lower_half_coarse_proj_list = new ArrayList<Double>(proj_list2.subList(0, coarse_proj_max_idx));
        upper_half_coarse_proj_list = new ArrayList<Double>(proj_list2.subList(coarse_proj_max_idx, proj_list2.size()));
        lower_half_coarse_z_list = new ArrayList<Double>(z_list.subList(0, coarse_proj_max_idx));
        upper_half_coarse_z_list = new ArrayList<Double>(z_list.subList(coarse_proj_max_idx, proj_list.size()));
        
        int fine_thresh_lower_idx = look_up_closest_index_from_Z(Double.parseDouble(current_low_int_pos), lower_half_fine_z_list);
        int fine_thresh_higher_idx = look_up_closest_index_from_Z(Double.parseDouble(current_high_int_pos), upper_half_fine_z_list);
        int coarse_thresh_lower_idx = look_up_closest_index_from_Z(Double.parseDouble(current_low_int_pos), lower_half_coarse_z_list);
        int coarse_thresh_higher_idx = look_up_closest_index_from_Z(Double.parseDouble(current_high_int_pos), upper_half_coarse_z_list);
        
        lower_half_fine_proj_list = new ArrayList<Double>(proj_list.subList(fine_thresh_lower_idx, fine_proj_max_idx));
        upper_half_fine_proj_list = new ArrayList<Double>(proj_list.subList(fine_proj_max_idx, fine_proj_max_idx+fine_thresh_higher_idx));
        lower_half_fine_z_list = new ArrayList<Double>(z_list.subList(fine_thresh_lower_idx, fine_proj_max_idx));
        upper_half_fine_z_list = new ArrayList<Double>(z_list.subList(fine_proj_max_idx, fine_proj_max_idx+fine_thresh_higher_idx));
        lower_half_coarse_proj_list = new ArrayList<Double>(proj_list2.subList(coarse_thresh_lower_idx, coarse_proj_max_idx));
        upper_half_coarse_proj_list = new ArrayList<Double>(proj_list2.subList(coarse_proj_max_idx, coarse_proj_max_idx+coarse_thresh_higher_idx));
        lower_half_coarse_z_list = new ArrayList<Double>(z_list.subList(coarse_thresh_lower_idx, coarse_proj_max_idx));
        upper_half_coarse_z_list = new ArrayList<Double>(z_list.subList(coarse_proj_max_idx, coarse_proj_max_idx+coarse_thresh_higher_idx));
    }
}

