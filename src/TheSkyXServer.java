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
        String command = "ccdsoftCamera.Connect();";
        this.sendCommandNoReturn(command);
    }

    /**
     * Send "Disconnect from camera" command to server.  No response expected
     * @throws IOException          I/O error from network
     */
    public void disconnectFromCamera() throws IOException {
        String command = "ccdsoftCamera.Disconnect();";
        this.sendCommandNoReturn(command);
    }

    /**
     * Send to the server a command that is not expecting a returned string
     * @param commandToSend     Command to send to server
     * @throws IOException      I/O error from network
     */
    private void sendCommandNoReturn(String commandToSend) throws IOException {
        String commandPacket =  "/* Java Script */"
                + "/* Socket Start Packet */"
                + commandToSend
                + "var Out;"
                + "Out=\"0\\n\";"
                + "/* Socket End Packet */";
        try {
            this.serverLock.lock();
            this.sendCommandPacket(commandPacket);
        } finally {
            this.serverLock.unlock();
        }

    }

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
        String command = "ccdsoftCamera.Abort();";
        this.sendCommandNoReturn(command);
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
        this.sendCommandNoReturn(commandNoReturn);
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
                + "sky6RASCOMTele.IsTracking=" + boolToJS(oldTrackingState) + ";";
        this.sendCommandNoReturn(commandNoReturn);
    }

    public boolean scopeSlewComplete() throws IOException {
        String commandWithReturn = "sky6RASCOMTele.Connect();"
                + "Out=sky6RASCOMTele.IsSlewComplete;"
                + "Out+=\"\\n\";";
        String returnString = this.sendCommandWithReturn(commandWithReturn);
        return returnString.trim().equals("1");
    }

    public void abortSlew() throws IOException {
        String commandNoReturn = "Out=sky6RASCOMTele.Abort();"
                + "Out+=\"\\n\";";
        this.sendCommandNoReturn(commandNoReturn);
    }
}
