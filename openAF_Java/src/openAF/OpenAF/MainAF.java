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

import openAF.OpenAF.DefineFocus;
import openAF.OpenAF.ContinuousFocus;
import bsh.ParseException;
import ij.process.ImageProcessor;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import mmcorej.CMMCore;
import org.micromanager.AutofocusPlugin;
import org.micromanager.Studio;
import org.micromanager.UserProfile;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.AutofocusBase;
import org.micromanager.internal.utils.MMException;
import org.micromanager.internal.utils.NumberUtils;
import org.micromanager.internal.utils.PropertyItem;
import org.micromanager.internal.utils.PropertyTableData;
import org.micromanager.internal.utils.PropertyValueCellEditor;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

@Plugin(type = AutofocusPlugin.class)
public class MainAF implements AutofocusPlugin, SciJavaPlugin{
    Studio gui_;
    public CMMCore core_;
    private ArrayList<PropertyItem> properties_ = null;
    public Control control_ = new Control(this);
    public AFclass af_ = null;
    
    static final String Key_Offset = "Offset";
    static final String Key_Last_focus_value = "Last focus value";
    static final String Key_Upper_limit = "Upper limit";
    static final String Key_Lower_limit = "Lower limit";
    static final String Key_Continuous_enabled = "Continuous Focus";
    static final String Key_Define_Focus = "Define Focus";
    static final String Key_Calibration = "Calibration";
    static final String Key_Range = "Range";
    static final String Key_Step_Size = "Step Size";
    static final String Key_Set_noise_background = "Noise Background";
    static final String Key_Threshold = "Threshold";
    static final String Key_Intensity_threshold = "Intensity Threshold";
    
    //These variables store current settings for the plugin
    String hardwareFocusDevice_;
    String zDrive_;
    public double offsetValue = 0.0;
    public double lastFocusValue = 3200.0;
    public double upperLimit_ = 3400.0;
    public double lowerLimit_ = 3000.0;
    public String contFocus_ = "Off";
    public String defineFocus_ = "Off";
    public String calib_ = "Off";
    public String noiseBg_ = "Off";
    public double range_ = 50; //um
    public double stepSize_ = 0.2; //um
    public double zpos = lastFocusValue;
    public double FWHM_threshold = 1.30;
    public double Intensity_threshold = 10.0;
    
    boolean socketConnected = false;
    
    boolean terminate = false;
    boolean do_continuous = false;
    boolean do_single_shot = false;
    boolean do_calibration = false;
    
    boolean initialised = false;
    boolean settings_applied = false;
    
    String zDev = null;
    private static final String AF_DEVICE_NAME = "OpenAF";
    Socket socket = null;
    
    Utilities.utils2 Utils_ = null;
    
    public void loadSettings() {
        UserProfile profile = MMStudio.getInstance().profile();
        for (int i=0; i<properties_.size(); i++) {
           properties_.get(i).value = profile.getSettings(this.getClass()).getString(properties_.get(i).name, properties_.get(i).value);
        }      
    }
    
    @Override
    public void applySettings() {
        try {
            String upper_ = Utils_.read_num_sensible(getPropertyValue(Key_Upper_limit));
            upperLimit_ = Double.parseDouble(upper_);
            String low_ = Utils_.read_num_sensible(getPropertyValue(Key_Lower_limit));
            lowerLimit_ = Double.parseDouble(low_);            
            contFocus_ = getPropertyValue(Key_Continuous_enabled);
            defineFocus_ = getPropertyValue(Key_Define_Focus);
            calib_ = getPropertyValue(Key_Calibration);
            String rang_ = Utils_.read_num_sensible(getPropertyValue(Key_Range));
            range_ = Double.parseDouble(rang_);
            String stepsiz_ = Utils_.read_num_sensible(getPropertyValue(Key_Step_Size));
            stepSize_ = Double.parseDouble(stepsiz_);            
            noiseBg_ = getPropertyValue(Key_Set_noise_background);
            String thresh_ = Utils_.read_num_sensible(getPropertyValue(Key_Threshold));
            FWHM_threshold = Double.parseDouble(thresh_);
            String IntThresh_ = Utils_.read_num_sensible(getPropertyValue(Key_Intensity_threshold));
            Intensity_threshold = Double.parseDouble(IntThresh_);
            settings_applied = true;        
        } catch (MMException ex) {
              Logger.getLogger(MainAF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void saveSettings() {
        UserProfile profile = MMStudio.getInstance().profile();
        for (int i=0; i<properties_.size(); i++) {
            profile.getSettings(this.getClass()).putString(properties_.get(i).name, properties_.get(i).value);
        }      
    }  

    @Override
    public double fullFocus() throws Exception {
        do_single_shot = true;
        return(1000.1234);//Unique value (not used outside testing)
    }

    @Override
    public double incrementalFocus() throws Exception {
        return(1000.4321);
    }

    @Override
    public int getNumberOfImages() {
        return 1;// Always 1
    }

    @Override
    public String getVerboseStatus() {
        return("Verbose");
    }

    @Override
    public PropertyItem[] getProperties() {
        return properties_.toArray(new PropertyItem[properties_.size()]);
    }

    @Override
    public String[] getPropertyNames() {
        String[] propName = new String[properties_.size()];
        for (int i=0; i<properties_.size(); i++) {
            propName[i] = properties_.get(i).name;
        }
        return propName;
    }

    @Override
    public PropertyItem getProperty(String name) throws Exception {
        for (int i=0; i<properties_.size(); i++) {
            if (name.equals(properties_.get(i).name)) {
               return properties_.get(i);
            }
        }
        throw new MMException("Unknown property: " + name);
    }

    @Override
    public void setProperty(PropertyItem pi) throws MMException {
        for (int i=0; i<properties_.size(); i++) {
            if (pi.name.equals(properties_.get(i).name)) {
                properties_.set(i, pi);
                return;
            } 
        }
        properties_.add(pi);
   }

    @Override
    public String getPropertyValue(String name) throws MMException {
        for (int i=0; i<properties_.size(); i++) {
            if (name.equals(properties_.get(i).name)) {
               return properties_.get(i).value;
            }
        }
        throw new MMException("Unknown property: " + name);
    } 

    @Override
    public void setPropertyValue(String name, String value) throws MMException {
        for (int i=0; i<properties_.size(); i++) {
            if (name.equals(properties_.get(i).name)) {
                properties_.get(i).value = value;
                return;
            }
        }
        throw new MMException("Unknown property: " + name);
   }

    @Override
    public double getCurrentFocusScore() {
        return 999;
    }

    @Override
    public void enableContinuousFocus(boolean bln) throws Exception {
        if(bln){
            setPropertyValue("Continuous Focus", "On");
            contFocus_ = gui_.getAutofocusManager().getAutofocusMethod().getPropertyValue("Continuous Focus");
        }
        else if(!bln){
            setPropertyValue("Continuous Focus", "Off");
            contFocus_ = gui_.getAutofocusManager().getAutofocusMethod().getPropertyValue("Continuous Focus");
        }
    }
    
    @Override
    public boolean isContinuousFocusEnabled() throws Exception {
        String value = gui_.getAutofocusManager().getAutofocusMethod().getPropertyValue("Continuous Focus");
        contFocus_ = value;
        if(contFocus_.equalsIgnoreCase("On")){
            return true;
        }
        else{
            return false;
        }
    }
    
    public boolean isDefineFocusEnabled() throws Exception {
        if(defineFocus_.equalsIgnoreCase("On")){
            return true;
        }
        else{
            return false;
        }
    }
    
    public void defineFocus(){
        
    }
    
    public void setSocket(Socket s){
        socket = s;
    }
    
    public Socket getSocket(){
        return socket;
    }

    @Override
    public boolean isContinuousFocusLocked(){
        return false;
    }

    @Override
    public double computeScore(ImageProcessor ip) {
        return 666; //Unique value - not used
    }

    public void getZdev(){
        zDev = core_.getFocusDevice();
    }
    
    @Override
    public void setContext(Studio studio) {
        if(!initialised){
            gui_ = studio;
            initialize();
            initialised = true;
        } else {   
        }
    }

    protected void createProperty(String name) {
        PropertyItem p = new PropertyItem();
        p.name = name;
        p.device = getName();
        properties_.add(p);
   }
    
   protected void createProperty(String name, String value) {
        PropertyItem p = new PropertyItem();
        p.name = name;
        p.value = value;
        p.device = getName();
        properties_.add(p);
   }

   protected void createProperty(String name, String value, String[] allowed) {
        PropertyItem p = new PropertyItem();
        p.allowed = allowed;
        p.name = name;
        p.value = value;
        p.device = getName();
        properties_.add(p);
   }
    
    @Override
    public String getName() {
        return(AF_DEVICE_NAME);
    }

    @Override
    public String getHelpText() {
        return("Please check https://www.github.com/ImperialCollegeLondon/openAF for help");
    }

    @Override
    public String getVersion() {
        return("1.0.0");
    }

    @Override
    public String getCopyright() {
        return("(c) Imperial College London [2023]");
    }

    public boolean get_AF_not_dead() {
        return !terminate;
    }

    public boolean get_do_calibration() {
        return do_calibration;  
    }    
    
     public boolean set_do_calibration() {
        return do_calibration;  
     }
    
    public boolean get_do_single_shot() {
        return do_single_shot;
    }

    public boolean get_do_continuous() {
        return do_continuous;
    }
    
    public void set_do_single_shot(boolean bool_in) {
        do_single_shot = bool_in;
    }

    public void set_do_continuous(boolean bool_in) {
        do_continuous = bool_in;
    }    
    
    public Control control_(){
        return control_;
    }
    
    public double get_z(){
        try {
            zpos = core_.getPosition(zDev);
        } catch (Exception ex) {
            Logger.getLogger(MainAF.class.getName()).log(Level.SEVERE, null, ex);
        }
        return zpos;
    }
    
    public double get_offset(){
        return offsetValue;
    }

    public void initSocket(){
        System.out.println("suofsuofgo");
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
                    socketConnected = true;
                } catch (UnknownHostException e1) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                } catch (IOException e1) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
        if(socket == null){
            System.out.println("Socket is null.");
        }
    }
    
    @Override
    public void initialize() {
        core_ = gui_.getCMMCore();
        zDev = core_.getFocusDevice();
        properties_ = new ArrayList<PropertyItem>();
        Utils_ = new Utilities.utils2();
        createProperty(Key_Offset, Utils_.read_num_sensible(Double.toString(offsetValue)));
        createProperty(Key_Upper_limit, Utils_.read_num_sensible(Double.toString(upperLimit_)));
        createProperty(Key_Lower_limit, Utils_.read_num_sensible(Double.toString(lowerLimit_)));
        String[] blah = new String[]{"Off","On"};
        createProperty(Key_Continuous_enabled, contFocus_, blah);
        createProperty(Key_Define_Focus, defineFocus_, blah);
        createProperty(Key_Calibration, calib_, blah);
        createProperty(Key_Range,Utils_.read_num_sensible(Double.toString(range_)));
        createProperty(Key_Step_Size,Utils_.read_num_sensible(Double.toString(stepSize_)));
        String[] noises = new String[]{"Off","Set noise BG", "Set BG"};
        createProperty(Key_Set_noise_background, noiseBg_, noises);
        createProperty(Key_Threshold, Utils_.read_num_sensible(Double.toString(FWHM_threshold)));
        createProperty(Key_Intensity_threshold, Utils_.read_num_sensible(Double.toString(Intensity_threshold)));
        loadSettings();
        try {
            setPropertyValue(Key_Offset, Utils_.read_num_sensible(Double.toString(offsetValue)));
            setPropertyValue(Key_Upper_limit, Utils_.read_num_sensible(Double.toString(upperLimit_)));
            setPropertyValue(Key_Lower_limit, Utils_.read_num_sensible(Double.toString(lowerLimit_)));
            setPropertyValue(Key_Continuous_enabled, "Off");
            setPropertyValue(Key_Define_Focus, "Off");
            setPropertyValue(Key_Calibration, "Off");
            setPropertyValue(Key_Range,Utils_.read_num_sensible(Double.toString(range_)));
            setPropertyValue(Key_Step_Size, Utils_.read_num_sensible(Double.toString(stepSize_)));
            setPropertyValue(Key_Set_noise_background, "Off");
            setPropertyValue(Key_Threshold, Utils_.read_num_sensible(Double.toString(FWHM_threshold)));
            setPropertyValue(Key_Intensity_threshold, Utils_.read_num_sensible(Double.toString(Intensity_threshold)));
        } catch (MMException ex) {
            Logger.getLogger(MainAF.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MainAF.class.getName()).log(Level.SEVERE, null, ex);
        }
        applySettings();
        Thread AF_BG_thread = new Thread(new AF_loop_thread(this));
        AF_BG_thread.start();
        initialised = true; 
    }
    
    public String setZDev(){
        zDev = core_.getFocusDevice();
        return zDev;
    }
}
