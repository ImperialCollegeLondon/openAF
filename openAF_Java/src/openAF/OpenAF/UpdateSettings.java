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

import openAF.OpenAF.MainAF;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.micromanager.AutofocusPlugin;
import org.micromanager.internal.utils.MMException;
import org.micromanager.internal.utils.NumberUtils;

public class UpdateSettings implements Runnable{
    private MainAF parent_;
    
    public UpdateSettings (MainAF parent_ref){
        parent_ = parent_ref;
    }
    
    @Override
    public void run() {
        try {
            while(parent_.core_.getAvailableConfigGroups().size() < 1){
                System.out.println(parent_.core_.getAvailableConfigGroups().size());
            } 
            System.out.println(parent_.core_.isConfigDefined("System", "Startup"));
            try {
            Thread.sleep(10000);
            } catch (InterruptedException ex) {
               Logger.getLogger(UpdateSettings.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (Exception ex) {
            Logger.getLogger(MainAF.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        while(parent_.terminate  == false){
            try {
                String PluginName = parent_.gui_.getAutofocusManager().getAutofocusMethod().getName();
                if(PluginName == parent_.getName()){   
                    String cont = parent_.gui_.getAutofocusManager().getAutofocusMethod().getPropertyValue("Continuous Focus");
                    if(!(parent_.contFocus_ == cont)){    
                        parent_.contFocus_ = cont;
                        parent_.setPropertyValue(parent_.Key_Continuous_enabled, cont);
                    }
                    String off = parent_.gui_.getAutofocusManager().getAutofocusMethod().getPropertyValue("Offset");
                    if(!(parent_.offsetValue == NumberUtils.displayStringToDouble(off))){
                        parent_.offsetValue = NumberUtils.displayStringToDouble(off);
                        parent_.setPropertyValue(parent_.Key_Offset, off);
                    }
                    String last = parent_.gui_.getAutofocusManager().getAutofocusMethod().getPropertyValue("Last focus value");
                    if(!(parent_.lastFocusValue == NumberUtils.displayStringToDouble(last))){
                        parent_.lastFocusValue = NumberUtils.displayStringToDouble(last);
                        parent_.setPropertyValue(parent_.Key_Last_focus_value, last);
                    }
                    String val = parent_.gui_.getAutofocusManager().getAutofocusMethod().getPropertyValue("Upper limit");
                    if(!(parent_.upperLimit_ == NumberUtils.displayStringToDouble(val))){                      
                        parent_.upperLimit_ = NumberUtils.displayStringToDouble(val);
                        parent_.setPropertyValue(parent_.Key_Upper_limit, val);
                    }   
                    String lower = parent_.gui_.getAutofocusManager().getAutofocusMethod().getPropertyValue("Lower limit");
                    if(!(parent_.lowerLimit_ == NumberUtils.displayStringToDouble(lower))){
                        parent_.lowerLimit_ = NumberUtils.displayStringToDouble(lower);
                        parent_.setPropertyValue(parent_.Key_Lower_limit, lower);
                    } 
                }
            } catch (MMException ex) {
            Logger.getLogger(UpdateSettings.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(UpdateSettings.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(UpdateSettings.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Update settings terminated");
    }    
}
