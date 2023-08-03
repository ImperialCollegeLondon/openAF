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

public class AF_measurement {
    double AF_value;
    String meas_type;
    static long measured_at;
    double offset_at_measurement_time;
    public final String LR_measurement = "LR_MEAS";
    public final String SR_measurement = "SR_MEAS";
    
    
    public AF_measurement(double value_in, String type_in, long meas_time_in, double offset_val){
        AF_value = value_in;
        meas_type = type_in;
        measured_at = System.currentTimeMillis();
        offset_at_measurement_time = offset_val;
    }
    
    double AF_value(){
        return AF_value;
    }
    
    String meas_type(){
        return meas_type;
    }    
    
    long measured_at(){
        return measured_at;
    }
    
    double offset_at_measurement_time(){
        return offset_at_measurement_time;
    }    
}