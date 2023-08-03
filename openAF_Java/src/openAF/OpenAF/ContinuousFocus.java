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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.micromanager.internal.utils.MMException;

public class ContinuousFocus implements Runnable{
    private MainAF parent_;
    boolean StartupDone = false;
    Integer ctr = 0;
    
    public ContinuousFocus (MainAF parent_ref){
        parent_ = parent_ref;
    }
    
    @Override
    public void run() {
        while(true){
            if(StartupDone){
                try {
                    if(parent_.isContinuousFocusEnabled()){
                        if(!parent_.get_do_continuous()){
                            parent_.set_do_continuous(true);
                            Logger.getLogger(ContinuousFocus.class.getName()).log(Level.WARNING, null, "AF off");
                            System.out.println("On");

                            
                        }              
                    }  
                    else{
                        if(parent_.get_do_continuous()){
                            parent_.set_do_continuous(false); 
                            Logger.getLogger(ContinuousFocus.class.getName()).log(Level.WARNING, null, "AF off");
                            System.out.println("Off"); 
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ContinuousFocus.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ContinuousFocus.class.getName()).log(Level.SEVERE, null, ex);
                }
                List<String> AFnames = parent_.gui_.getAutofocusManager().getAllAutofocusMethods();
                
                if(AFnames.contains(parent_.getName())){
                    System.out.println("we shouldn't be here!"+ctr.toString());
                    parent_.gui_.getAutofocusManager().setAutofocusMethod(parent_);
                    try {
                        parent_.gui_.getAutofocusManager().getAutofocusMethod().setPropertyValue("Continuous Focus", "Off");
                        parent_.contFocus_ = parent_.gui_.getAutofocusManager().getAutofocusMethod().getPropertyValue("Continuous Focus");
                    } catch (Exception ex) {
                        Logger.getLogger(ContinuousFocus.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    StartupDone = true;
                    ctr = ctr+1;
                }
            }
        }
    }
}


