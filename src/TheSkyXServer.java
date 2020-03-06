// Class for sending commands, encoded in Javascript, to TheSkyX server


import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.InvalidPropertiesFormatException;
import java.util.concurrent.locks.ReentrantLock;

public class TheSkyXServer {
    private static final int SOCKET_TIMEOUT = 5 * 1000;

    //  We use a lock for server commands since more than one task may be asking the server to act
    private ReentrantLock serverLock = null;

    private InetSocketAddress inetSocketAddress;
    private double rememberedExposureForSimulation = 10.0;
    private int rememberedBinningForSimulation = 1;
    private Integer rememberedFilterSlotForSimulation = 1;

    /**
     * Constructor, taking address and port number, create socket for trial connection
     * @param serverAddress         String giving server name or IP address
     * @param portNumber            Port number where server is listening
     * @throws IOException          I/O error from network
     */
    public TheSkyXServer(String serverAddress, Integer portNumber) throws IOException {
        super();
        this.serverLock = new ReentrantLock();
        //  Trial connection
        Socket socket = new Socket();
        this.inetSocketAddress = new InetSocketAddress(serverAddress, portNumber);
        socket.connect(this.inetSocketAddress, SOCKET_TIMEOUT);
        socket.close();
    }

    /**
     * Ask the server for the path set via the camera AutoSave button
     * @return (String)         Absolute path to file save area on TheSkyX machine
     * @throws IOException      I/O error from network
     */
    public String getCameraAutosavePath() throws IOException {
        String commandWithReturn = "var path=ccdsoftCamera.AutoSavePath;"
                + "var Out;Out=path+\"\\n\";";
        return this.sendCommandWithReturn(commandWithReturn);
    }

    /**
     * Send to the server a command packet that gets a return value, and return it
     * @param commandToSend         Command to be sent to server
     * @return (String)             String returned from server
     * @throws IOException          I/O error from network
     */
    private String sendCommandWithReturn(String commandToSend) throws IOException {
        String commandPacket =  "/* Java Script */"
                + "/* Socket Start Packet */"
                + commandToSend
                + "/* Socket End Packet */";
        return sendCommandPacket(commandPacket);
    }

    /**
     * Low-level send given command packet to server, retrieve server response
     * @param commandPacket         Command to be sent to server
     * @return (String)             String returned from server
     * @throws IOException          I/O error from network
     */

    String sendCommandPacket(String commandPacket) throws IOException {

        //  Create socket and connect
        Socket socket = new Socket();
        socket.connect(this.inetSocketAddress, SOCKET_TIMEOUT);

        //  Send the command to the server
        PrintStream toServerStream = new PrintStream(socket.getOutputStream());
        toServerStream.println(commandPacket);

        //  Read the response
        InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
        BufferedReader response = new BufferedReader(inputStreamReader);
        String serverAnswer = response.readLine();

        response.close();
        inputStreamReader.close();
        toServerStream.close();
        socket.close();

        return serverAnswer;
    }

    /**
     * Send "Connect to camera" command to server.  No response expected
     * @throws IOException          I/O error from network
     */
    public void connectToCamera() throws IOException {
        String command = "var Out=ccdsoftCamera.Connect();"
                + "Out+=\"\\n\";";
        String result = this.sendCommandWithReturn(command);
        int errorCode = this.errorCheckResult(result);
        if (errorCode != 0) {
            throw new IOException("I/O error code " + errorCode);
        };
    }

    /**
     * Send "Disconnect from camera" command to server.  No response expected
     * @throws IOException          I/O error from network
     */
    public void disconnectFromCamera() throws IOException {
        String command = "var Out=ccdsoftCamera.Disconnect();"
                + "Out+=\"\\n\";";
        String result = this.sendCommandWithReturn(command);
        int errorCode = this.errorCheckResult(result);
        if (errorCode != 0) {
            throw new IOException("I/O error code " + errorCode);
        };
    }

    /**
     * Send to the server a command that is not expecting a returned string
     * @param commandToSend     Command to send to server
     * @throws IOException      I/O error from network
     */
//    private void sendCommandNoReturn(String commandToSend) throws IOException {
//        String commandPacket =  "/* Java Script */"
//                + "/* Socket Start Packet */"
//                + commandToSend
//                + "var Out;"
//                + "Out=\"0\\n\";"
//                + "/* Socket End Packet */";
//        try {
//            this.serverLock.lock();
//            this.sendCommandPacket(commandPacket);
//        } finally {
//            this.serverLock.unlock();
//        }
//
//    }

    /**
     * Convert a Java boolean to the text used by JavaScript
     * @param theBool           Boolean value to convert
     * @return (String)         JavaScript representation of the boolean value
     */

    public static String boolToJS(boolean theBool) {
        return theBool ? "true" : "false";
    }

    /**
     * One of the peculiarities of the TheSkyX tcp interface.  Sometimes you get "success" back
     * from the socket, but the returned string contains an error encoded in the text message.
     * The "success" meant that the server was successful in sending this error text to you, not
     * that all is well.  Awkward.  We check for that, and return a better "success" indicator
     * and a message of any failure we found.
     *
     *      0:  No error
     *      1:  Camera was aborted
     *      2:  CFITSIO error (bad file save name or location)
     *      3:  Some other TYPE ERROR
     * @param returnedText      Server response text to be checked
     * @return (int)            Error code as described above
     */
    private int errorCheckResult(String returnedText) {
        String returnedTextUpper = returnedText.toUpperCase();
        int result = 0;
        if (returnedTextUpper.contains("TYPEERROR: PROCESS ABORTED"))
            result = 1;
        else if (returnedTextUpper.contains("TYPEERROR: CFITSIO ERROR"))
            result = 2;
        else if (returnedTextUpper.contains("TYPEERROR:"))
            result = 3;
        return result;
    }

    /**
     *
     *   Send a camera abort command to the server, stopping any image acquisition in progress
    */

    public void abortImageInProgress() throws IOException {
        String command = "var Out=\"0\";"
                + "if (!ccdsoftCamera.IsExposureComplete) {"
                +       "Out=ccdsoftCamera.Abort();"
                + "}"
                + "Out+=\"\\n\";";
        String result = this.sendCommandWithReturn(command);
        int errorCode = this.errorCheckResult(result);
        if (errorCode != 0) {
            throw new IOException("I/O error code " + errorCode);
        };
    }

    /**
     * Ask server if the recently-started asynchronous exposure is complete.
     * @return Indicator of completion
     */
    public boolean exposureIsComplete() throws IOException {
        String commandWithReturn = "var path=ccdsoftCamera.IsExposureComplete;"
                + "var Out;Out=path+\"\\n\";";
        String returnString = this.sendCommandWithReturn(commandWithReturn);
        return returnString.trim().equals("1");
    }

    /**
     * Get the current pointing location of the scope in Alt/Az coordinates
     * @return (pair)                   Doubles for Altitude and Azimuth, in that order
     * @throws IOException              Error communicating with server
     * @throws NumberFormatException    Badly-formatted response from server
     */
    public ImmutablePair<Double, Double> getScopeAltAz() throws IOException,NumberFormatException {
        String commandWithReturn = "sky6RASCOMTele.GetAzAlt();"
        + "var Out=sky6RASCOMTele.dAlt + '/' + sky6RASCOMTele.dAz;"
        + "Out += \"\\n\";";
        String returnString = this.sendCommandWithReturn(commandWithReturn);

        // Parse two floats separated by /
        String[] parts = returnString.split("/");
        if (parts.length != 2) {
            throw new InvalidPropertiesFormatException("Server response invalid");
        }
        Double altitude = Double.valueOf(parts[0]);
        Double azimuth = Double.valueOf(parts[1]);

        return ImmutablePair.of(altitude, azimuth);
    }

    /**
     * Start scope slewing to given alt-az coordinates.  Alt-ax, not RA-Dec, because
     * the original use of this method was to slew to a flat frame light panel, which is
     * at a fixed location in the observatory and doesn't move with the sky
     * Slewing is asynchronous. This just starts the slew - must poll for completion
     * Doing a slew turns tracking on.  We'll restore it to previous state in case it was off
     * @param targetAltitude        Altitude to slew to
     * @param targetAzimuth         Azimuth to slew to
     * @param asynchronous          Attempt to do it asynchronously?  (May not have any effect)
     */
    public void slewToAltAz(double targetAltitude, double targetAzimuth, boolean asynchronous) throws IOException {
        String commandNoReturn = "sky6RASCOMTele.Connect();"
                + "sky6RASCOMTele.Asynchronous=" + boolToJS(asynchronous) + ";"
                + "Out=sky6RASCOMTele.SlewToAzAlt("
                    + String.valueOf(targetAzimuth) + ","
                    + String.valueOf(targetAltitude) + ",'');"
                + "Out+=\"\\n\";";
        String result = this.sendCommandWithReturn(commandNoReturn);
        int errorCode = this.errorCheckResult(result);
        if (errorCode != 0) {
            throw new IOException("I/O error code " + errorCode);
        };
    }

    /**
     * Get and return whether the scope's tracking is on
     * @return
     */
    public boolean getScopeTracking() throws IOException {
        String commandWithReturn = "sky6RASCOMTele.Connect();"
                                    + "var Out=sky6RASCOMTele.IsTracking;"
                                    + "Out+=\"\\n\";";
        String returnString = this.sendCommandWithReturn(commandWithReturn);
        return returnString.trim().equals("1");
    }

    /**
     * Set scope tracking to given on/off state
     * @param oldTrackingState      true or false for tracking
     */
    public void setScopeTracking(boolean oldTrackingState) throws IOException {
        String commandNoReturn = "sky6RASCOMTele.Connect();"
                + "sky6RASCOMTele.IsTracking=" + boolToJS(oldTrackingState) + ";"
                + "var Out=sky6RASCOMTele.IsTracking;"
                + "Out+=\"\\n\";";
        String result = this.sendCommandWithReturn(commandNoReturn);
        int errorCode = this.errorCheckResult(result);
        if (errorCode != 0) {
            throw new IOException("I/O error code " + errorCode);
        };
    }

    /**
     * Ask TheSkyX whether the recently-started slew operation is complete
     * @return (boolean)
     * @throws IOException
     */
    public boolean scopeSlewComplete() throws IOException {
        String commandWithReturn = "sky6RASCOMTele.Connect();"
                + "Out=sky6RASCOMTele.IsSlewComplete;"
                + "Out+=\"\\n\";";
        String returnString = this.sendCommandWithReturn(commandWithReturn);
        return returnString.trim().equals("1");
    }

    /**
     * Ask TheSkyX to abort the recently-starte mount operation (a slew in our case)
     * @throws IOException
     */
    public void abortSlew() throws IOException {
        String commandNoReturn = "var Out=\"0\";"
                + "if (!sky6RASCOMTele.IsSlewComplete) {"
                +       "Out=sky6RASCOMTele.Abort();"
                + "}"
                + "Out+=\"\\n\";";
        String result = this.sendCommandWithReturn(commandNoReturn);
        int errorCode = this.errorCheckResult(result);
        if (errorCode != 0) {
            throw new IOException("I/O error code " + errorCode);
        };
    }

    /**
     * Send command to TheSkyX to connect to and home the mount. No response.
     */
    public void homeMount() throws IOException {
        String commandNoReturn = "sky6RASCOMTele.Connect();"
        + "sky6RASCOMTele.Asynchronous=false;"
        + "Out=sky6RASCOMTele.FindHome();"
        + "Out += \"\\n\";";
        String result = this.sendCommandWithReturn(commandNoReturn);
        int errorCode = this.errorCheckResult(result);
        if (errorCode != 0) {
            throw new IOException("I/O error code " + errorCode);
        };
    }

    /**
     * Expose a bias frame at the given binning size
     * @param binning           Binning level for bias frame
     * @param asynchronous      Start image asynchronously?  (false=wait)
     * @param autosave          Auto-save image to defined directory?
     */
    public void exposeBiasFrame(Integer binning, boolean asynchronous, boolean autosave) throws IOException {
        String command = "ccdsoftCamera.Autoguider=false;"        //  Use main camera
                + "ccdsoftCamera.Asynchronous=" + boolToJS(asynchronous) + ";"   //  Wait for camera?
                + "ccdsoftCamera.Frame=2;"
                + "ccdsoftCamera.ImageReduction=0;"       // No autodark or calibration
                + "ccdsoftCamera.ToNewWindow=false;"      // Reuse window, not new one
                + "ccdsoftCamera.ccdsoftAutoSaveAs=0;"    //  0 = FITS format
                + "ccdsoftCamera.AutoSaveOn=" + boolToJS(autosave) + ";"
                + "ccdsoftCamera.BinX=" + binning + ";"
                + "ccdsoftCamera.BinY=" + binning + ";"
                + "ccdsoftCamera.ExposureTime=0;"
                + "var cameraResult = ccdsoftCamera.TakeImage();"
                + "var Out;Out=cameraResult+\"\\n\";";

        String result = this.sendCommandWithReturn(command);
        int errorCode = this.errorCheckResult(result);
        if (errorCode != 0) {
            System.out.println("Error returned from camera: " + result);
            throw new IOException("Error from camera");
        }
    }

    /**
     * Select the filter that will be used with the next acquired image(s).
     * Note that with most setups this does not move the filter wheel.  It will move when the
     * first image acquisition is started.
     * @param slotNumber     1-based slot number for the filter to use
     */
    public void selectFilter(Integer slotNumber) throws IOException {
        String commandNoReturn = "ccdsoftCamera.filterWheelConnect();"
                + "ccdsoftCamera.FilterIndexZeroBased=" + (String.valueOf(slotNumber - 1)) + ";"
                + "var Out;Out=cameraResult+\"\\n\";";
        String result = this.sendCommandWithReturn(commandNoReturn);
        int errorCode = this.errorCheckResult(result);
        if (errorCode != 0) {
            throw new IOException("I/O error code " + errorCode);
        };
        this.rememberedFilterSlotForSimulation = slotNumber;
    }

    /**
     * Expose a flat frame with the given exposure and binning.  We assume the filter has already been set.
     * Arguments determine if the exposure is synchronous or asynchronous, and if we autosave the result
     * @param exposureSeconds       Exposure time in seconds (with fractions)
     * @param binning               Binning value (1-4)
     * @param asynchronous          true if exposure should be started asynchronously
     * @param autosave              true if camera should write exposure to autosave folder
     */
    public void exposeFlatFrame(double exposureSeconds, int binning, boolean asynchronous, boolean autosave) throws IOException {
        String command = "ccdsoftCamera.Autoguider=false;"        //  Use main camera
                + "ccdsoftCamera.Asynchronous=" + boolToJS(asynchronous) + ";"   //  Wait for camera?
                + "ccdsoftCamera.Frame=4;"      // Magic code for flat frame
                + "ccdsoftCamera.ImageReduction=0;"       // No autodark or calibration
                + "ccdsoftCamera.ToNewWindow=false;"      // Reuse window, not new one
                + "ccdsoftCamera.ccdsoftAutoSaveAs=0;"    //  0 = FITS format
                + "ccdsoftCamera.AutoSaveOn=" + boolToJS(autosave) + ";"
                + "ccdsoftCamera.BinX=" + binning + ";"
                + "ccdsoftCamera.BinY=" + binning + ";"
                + "ccdsoftCamera.ExposureTime=" + String.valueOf(exposureSeconds) + ";"
                + "var cameraResult = ccdsoftCamera.TakeImage();"
                + "var Out;Out=cameraResult+\"\\n\";";

        String result = this.sendCommandWithReturn(command);
        int errorCode = this.errorCheckResult(result);
        if (errorCode != 0) {
            System.out.println("Error returned from camera: " + result);
            throw new IOException("Error from camera");
        }

        //  Remember the exposure information we just used, in case we need it for the simulation
        //  of ADU calculation
        this.rememberedExposureForSimulation = exposureSeconds;
        this.rememberedBinningForSimulation = binning;
    }

    /**
     * Get the average ADU value of the last-acquired image
     * Note that testing this function without a live camera and a real flat target is difficult, as
     * the ADU value returned will not be typical.  (From TheSkyX's camera simulator, it is a constant value).
     * So, we have an optional testing simulator that can return ADUs empirically calculated from testing.
     * @return (int)    Average ADU value of the image
     */
    public int getLastImageADUs() throws IOException {
        if (Common.SIMULATE_ADU_MEASUREMENT) {
            return this.getSimulatedADUMeasurement();
        } else {
            // Not a simulation - get the value from the server
            String command = "ccdsoftCameraImage.AttachToActive();"
                + "var averageAdu = ccdsoftCameraImage.averagePixelValue();"
                + "var Out;"
                + "Out=averageAdu+\"\\n\";";
            String returnString = this.sendCommandWithReturn(command);
            try {
                Double averageADU = Double.valueOf(returnString);
                return (int) Math.round(averageADU);
            } catch (NumberFormatException e) {
                System.out.println("Invalid response from querying camera average pixel value: " + returnString);
                throw new IOException("Invalid response querying ADUs");
            }
        }
    }

    /**
     * The Simulator flag is on, so instead of asking the server for the last frame's ADU measurement,
     * we're going to calculate a realistic one.  I empirically measured a bunch of cases to get some
     * regression parameters, which we'll use here.
     * @return (int)    Simulated ADU value
     */
    private int getSimulatedADUMeasurement() {
        // Get the regression values.  Only measured them for certain data.
        double exposure = this.rememberedExposureForSimulation;
        int binning = this.rememberedBinningForSimulation;
        int filter = this.rememberedFilterSlotForSimulation;
        Double slope;
        Double intercept;
        if ((binning == 1) && (filter == 4)) {
            // Luminance, binned 1x1
            slope = 721.8;
            intercept = 19817.0;
        } else if ((binning == 2) && (filter == 1)) {
            // Red filter, binned 2x2
            slope = 7336.7;
            intercept = -100.48;
        } else if ((binning == 2) && (filter == 2)) {
            // Green filter, binned 2x2
            slope = 11678.0;
            intercept = -293.09;
        } else if ((binning == 2) && (filter == 3)) {
            // Blue filter, binned 2x2
            slope = 6820.4;
            intercept = 1858.3;
        } else if ((binning == 1) && (filter == 5)) {
            // H-alpha filter, binned 1x1
            slope = 67.247;
            intercept = 2632.7;
        } else {
            slope = 721.8;
            intercept = 19817.0;
        }
        double calculatedResult = slope * exposure + intercept;

        // Now we'll put a small percentage noise into the value so it has some variability for realism
        double randFactorZeroCentered = Common.SIMULATION_NOISE_FRACTION * (Math.random() - 0.5);
        double noisyResult = calculatedResult + randFactorZeroCentered * calculatedResult;

        int clippedAt16Bits = Math.min((int) Math.round(noisyResult), 65535);
        return clippedAt16Bits;
    }

    /**
     * Tell the server to save the last acquired image to its autosave path
     */
    public void saveImageToAutoSave(String fileName) throws IOException {
        String command = "cam = ccdsoftCamera;"
                + "img = ccdsoftCameraImage;"
                + "img.AttachToActiveImager();"
                + "asp = cam.AutoSavePath;"
                + String.format("img.Path = asp + '/%s';", fileName)
                + "var Out=img.Save();"
                + "Out += \"\\n\";";

        String result = this.sendCommandWithReturn(command);
        int errorCode = this.errorCheckResult(result);
        if (errorCode != 0) {
            System.out.println("Error returned from camera: " + result);
            throw new IOException("Error from camera");
        }
    }

    /**
     * Tell the server to save the last acquired image to the given path name
     * @param localPath   Absolute path name of the file to be saved, including file name
     */
    public void saveImageToLocalPath(String localPath) throws IOException {
        String command = "cam = ccdsoftCamera;"
                + "img = ccdsoftCameraImage;"
                + "img.AttachToActiveImager();"
                + String.format("img.Path = '%s';", localPath)
                + "var Out=img.Save();"
                + "Out += \"\\n\";";

        String result = this.sendCommandWithReturn(command);
        int errorCode = this.errorCheckResult(result);
        if (errorCode != 0) {
            System.out.println("Error returned from camera: " + result);
            throw new IOException("Error from camera");
        }
    }
}
