import java.net.*;
import java.io.*;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Random;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
//import oracle.security.crypto.core.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.NoSuchProviderException;
import java.security.*;
import javax.crypto.*;
import java.util.GregorianCalendar;
import javax.xml.bind.DatatypeConverter;


public class Client1 {
    private int _calendar[][][] = new int[13][32][24];
    //private DiffieHellman _dh;
    private BigInteger _sharedKeyBI;
    private byte[] _sharedKey;
    private TreeMap<String, Long> noncesMap;
    private AES aes;
    private Utils utils;
    private GregorianCalendar cal;
    private static boolean dbug = true;
    private int freeSlots = 0;

    public Client1() {
        // _calendar = new int[13][32][24];
        //_dh = new DiffieHellman();
        noncesMap = new TreeMap<String, Long>();
        aes = new AES();
        utils = new Utils();

        for (int month = 1; month < 13 ; month++) {
            // System.out.println("MONTHS!!!");
            for (int day = 1; day < 32; day++) {
                // System.out.println("DAYS!!!");
                for (int hour = 8; hour < 21; hour++) {
                    // System.out.println("DAYS!!!");
                    int rand = utils.randInt(0, 10);
                    // System.out.println("randInt: " + rand);
                    if (rand == 0) {
                        // System.out.println("Random0");
                        _calendar[month][day][hour] = 0;
                    } else {
                        // System.out.println("RandomElse");
                        _calendar[month][day][hour] = 1;
                        // freeSlots++;
                    }
                }
            }
        }

        // for (int i = 1; i < 13; i++) {

        //     Random generator = new Random();

        //     for (int j = 1; j < 32; j++) {

        //         for (int k = 0; k < 24; k++) {

        //             if ((k / 10) < 1) {

        //                 _calendar[i][j][k] = 1;
        //             } else {
        //                 boolean res = generator.nextBoolean();
        //                 if (res) {
        //                     _calendar[i][j][k] = 0;
        //                 } else {
        //                     _calendar[i][j][k] = 1;
        //                 }
        //             }
        //         }
        //     }
        // }
    }

    public String[] parseMessage(String msg, Socket socketClient, DataOutputStream out) throws IOException, Exception {

        AES aes = new AES();
        Utils utils = new Utils();
        String[] maux1 = msg.split(":");
        String[] maux2 = null;
        String[] parsedMsg = null;
        if (maux1.length == 3) {

            if (maux1[0].equals("Server")) {

                // String decryptedMsg = aes.decrypt(utils.stringToByteArray(maux1[1]), aes.readKeyFromFile("AliceKeyStore"), maux1[2]);
                // maux2 = decryptedMsg.split(",");
                maux2 = decryptAndSplitMsg(maux1[1], maux1[2], "Alice");
            } else {
                // String decryptedMsg = aes.decrypt(DatatypeConverter.parseBase64Binary(maux1[1]), DatatypeConverter.parseBase64Binary(_sharedKeyBI.toString()), maux1[2]);
                String decryptedMsg = aes.decrypt(DatatypeConverter.parseBase64Binary(maux1[1]), _sharedKey, maux1[2]);
                maux2 = decryptedMsg.split(",");
            }
        } else {
            System.out.println("ParseC1:1");
            wrongFormatMessage(socketClient, out);
            return parsedMsg;
        }

        if (maux1[0].equals(maux2[0]) && (maux2.length > 1)) {

            if (maux2[1].equals("CHECK")) {

                if (maux2.length == 5) {

                    if (validNonce(maux2[3], utils.getTimeStamp()) && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(maux2[4]))) {

                        parsedMsg = new String[7];
                        parsedMsg[0] = maux1[0];
                        parsedMsg[1] = maux2[0];
                        parsedMsg[2] = maux2[1];
                        parsedMsg[3] = maux2[2];
                        parsedMsg[4] = maux2[3];
                        parsedMsg[5] = maux2[4];
                        parsedMsg[6] = maux1[2];
                        return parsedMsg;
                    } else {
                        expiredMessage(socketClient, out);
                    }
                } else {
                    System.out.println("ParseC1:2");
                    wrongFormatMessage(socketClient, out);
                }
            } else if (maux2[1].equals("DH")) {

                if (maux2[0].equals("Server")) {

                    if (maux2.length == 7) {

                        if (validNonce(maux2[5], utils.getTimeStamp()) && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(maux2[6]))) {

                            parsedMsg = new String[9];
                            parsedMsg[0] = maux1[0];
                            parsedMsg[1] = maux2[0];
                            parsedMsg[2] = maux2[1];
                            parsedMsg[3] = maux2[2];
                            parsedMsg[4] = maux2[3];
                            parsedMsg[5] = maux2[4];
                            parsedMsg[6] = maux2[5];
                            parsedMsg[7] = maux2[6];
                            parsedMsg[8] = maux1[2];
                            return parsedMsg;
                        } else {
                            expiredMessage(socketClient, out);
                        }
                    } else if (maux2.length == 6) {

                        if (validNonce(maux2[4], utils.getTimeStamp()) && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(maux2[5]))) {

                            parsedMsg = new String[8];
                            parsedMsg[0] = maux1[0];
                            parsedMsg[1] = maux2[0];
                            parsedMsg[2] = maux2[1];
                            parsedMsg[3] = maux2[2];
                            parsedMsg[4] = maux2[3];
                            parsedMsg[5] = maux2[4];
                            parsedMsg[6] = maux2[5];
                            parsedMsg[7] = maux1[2];
                            return parsedMsg;
                        } else {
                            expiredMessage(socketClient, out);
                        }
                    } else {
                        System.out.println("ParseC1:3");
                        wrongFormatMessage(socketClient, out);
                    }
                } else {

                    if (maux2.length == 6) {

                        if (validNonce(maux2[4], utils.getTimeStamp()) && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(maux2[5]))) {

                            parsedMsg = new String[8];
                            parsedMsg[0] = maux1[0];
                            parsedMsg[1] = maux2[0];
                            parsedMsg[2] = maux2[1];
                            parsedMsg[3] = maux2[2];
                            parsedMsg[4] = maux2[3];
                            parsedMsg[5] = maux2[4];
                            parsedMsg[6] = maux2[5];
                            parsedMsg[7] = maux1[2];
                            return parsedMsg;
                        }
                    } else {
                        System.out.println("ParseC1:4");
                        wrongFormatMessage(socketClient, out);
                    }
                }
            } else {
                System.out.println("Received unknown type message.");
            }
        } else {
            wrongCredentialsProvided(socketClient, out);
        }
        return parsedMsg;
    }

    public int[] parseDateInput(String interval) {

        int[] dateInterval = new int[4];

        String[] split1 = interval.split("-");
        String[] split2 = split1[0].split("/");
        String[] split3 = split1[1].split("/");
        // System.out.println(split2[0]);
        // System.out.println(split2[1]);

        dateInterval[0] = Integer.parseInt(split2[0]);
        dateInterval[1] = Integer.parseInt(split2[1]);
        dateInterval[2] = Integer.parseInt(split3[0]);
        dateInterval[3] = Integer.parseInt(split3[1]);

        return dateInterval;
    }

    public void findCommonDate(Socket socketClient, DataOutputStream out, DataInputStream in) {

        AES aes = new AES();
        Utils utils = new Utils();
        int[] dateInterval = new int[4];
        int lastCheckedDay, lastCheckedMonth;

        try {
            String message = "";
            String[] parsedMsg = null;

            System.out.println("Please insert date interval [DD/MM-DD/MM]");
            dateInterval = parseDateInput(getInput());
            // dateInterval = parseDateInput("12/12-18/12");
            lastCheckedDay = dateInterval[0];
            lastCheckedMonth = dateInterval[1];

            loop11: while (lastCheckedMonth != dateInterval[3]) {
                loop12: while (lastCheckedDay < 32) {
                    int i = 0;
                    loop13: while (i < 24) {

                        if (_calendar[lastCheckedMonth][lastCheckedDay][i] != 0) {
                            i++;
                        } else {
                            String msg = "Alice,CHECK," + Integer.toString(lastCheckedDay) + "/" + Integer.toString(lastCheckedMonth) + "/14-" + Integer.toString(i) + "," + utils.generateRandomNonce() + "," + String.valueOf(System.currentTimeMillis());
                            String iv = utils.generateRandomIV();
                            // out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(msg, DatatypeConverter.parseBase64Binary(_sharedKeyBI.toString()), iv)) + ":" + iv);
                            out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(msg, _sharedKey, iv)) + ":" + iv);
                            System.out.println("Checking date: " + lastCheckedDay + "/" + lastCheckedMonth + "/14 - " + i + "h");
                        }
                        message = in.readUTF();
                        parsedMsg = parseMessage(message, socketClient, out);
                        if (!(parsedMsg == null)) {

                            if (parsedMsg[3].equals("Yes")) {
                                System.out.println("Found common date! Meeting with Bob scheduled to " + lastCheckedDay + "/" + lastCheckedMonth + "/14 at " + i + " hours.");
                                _calendar[lastCheckedMonth][lastCheckedDay][i] = 1;
                                --freeSlots;
                                System.out.println("Free slots: " + freeSlots);
                                return;
                            }
                        } else {
                            socketClient.close();
                        }
                    }
                    if (parsedMsg[3].equals("Yes")) {
                        System.out.println("Found common date! Meeting with Bob scheduled to " + lastCheckedDay + "/" + lastCheckedMonth + "/14 at " + i + " hours.");
                        _calendar[lastCheckedMonth][lastCheckedDay][i] = 1;
                        --freeSlots;
                        System.out.println("Free slots: " + freeSlots);
                        return;
                    }
                    lastCheckedDay++;
                }
                if (parsedMsg[3].equals("Yes")) {
                    // System.out.println("Found common date! Meeting with Bob scheduled to " + lastCheckedDay + "/" + lastCheckedMonth + "/14 at " + i + " hours.");
                    return;
                }
                lastCheckedDay = 1;
                lastCheckedMonth++;
            }
            if (lastCheckedMonth == dateInterval[3] && !message.equals("Yes")) {
                loop21: while (lastCheckedDay <= dateInterval[2]) {
                    int i = 8;
                    loop22: while (i < 20) {
                        // System.out.println("LCD: " + lastCheckedDay + " " + lastCheckedMonth);
                        if (_calendar[lastCheckedMonth][lastCheckedDay][i] != 0) {
                            i++;
                            break;
                        } else {
                            String msg = "Alice,CHECK," + Integer.toString(lastCheckedDay) + "/" + Integer.toString(lastCheckedMonth) + "/14-" + Integer.toString(i) + "," + utils.generateRandomNonce() + "," + String.valueOf(System.currentTimeMillis());
                            String iv = utils.generateRandomIV();
                            // out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(msg, DatatypeConverter.parseBase64Binary(_sharedKeyBI.toString()), iv)) + ":" + iv);
                            out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(msg, _sharedKey, iv)) + ":" + iv);
                            System.out.println("Checking date: " + lastCheckedDay + "/" + lastCheckedMonth + "/14 - " + i + "h");
                        }
                        message = in.readUTF();
                        parsedMsg = parseMessage(message, socketClient, out);
                        if (!(parsedMsg == null)) {

                            if (parsedMsg[3].equals("Yes")) {
                                System.out.println("Found common date! Meeting with Bob scheduled to " + lastCheckedDay + "/" + lastCheckedMonth + "/14 at " + i + " hours.");
                                _calendar[lastCheckedMonth][lastCheckedDay][i] = 1;
                                --freeSlots;
                                System.out.println("Free slots: " + freeSlots);
                                return;
                            } else {
                                System.out.println("BUSY");
                                i++;
                            }
                        } else {
                            socketClient.close();
                        }
                    }
                    if (parsedMsg[3].equals("Yes")) {
                        System.out.println("Found common date! Meeting with Bob scheduled to " + lastCheckedDay + "/" + lastCheckedMonth + "/14 at " + i + " hours.");
                        _calendar[lastCheckedMonth][lastCheckedDay][i] = 1;
                        --freeSlots;
                        System.out.println("Free slots: " + freeSlots);
                        return;
                    }
                    lastCheckedDay++;
                }
            }
            if (!parsedMsg[3].equals("Yes")) {

                System.out.println("Unable to find common date.");
                String msg = "Alice,NODATE,No common date found," + utils.generateRandomNonce() + "," + String.valueOf(System.currentTimeMillis());
                String iv = utils.generateRandomIV();
                // out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(msg, DatatypeConverter.parseBase64Binary(_sharedKeyBI.toString()), iv)) + ":" + iv);
                out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(msg, _sharedKey, iv)) + ":" + iv);
            }
            //socketClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void findRandomCommonDate(Socket socketClient, DataOutputStream out, DataInputStream in) {

        AES aes = new AES();
        Utils utils = new Utils();
        int[] dateInterval = new int[4];
        int lastCheckedDay, lastCheckedMonth;
        int day, month, hour;

        try {
            String message = "";
            String[] parsedMsg = null;

            System.out.println("Please insert date interval [DD/MM-DD/MM]");
            if (!dbug)
                dateInterval = parseDateInput(getInput());
            else
                dateInterval = parseDateInput("15/12-19/12");
            lastCheckedDay = dateInterval[0];
            lastCheckedMonth = dateInterval[1];

            // Compute free slots for the given interval
            freeSlots = 0;
            for (int tmonth = dateInterval[1]; tmonth <= dateInterval[3]; tmonth++) {
                for (int tday = dateInterval[0]; tday <= dateInterval[2]; tday++) {
                    for (int thour = 8; thour < 21; thour++) {
                        // System.out.println(tmonth + "/" + tday + "-" + thour + "h --- Free? " + _calendar[tmonth][tday][thour] + "\nFree slots: " + freeSlots);
                        if (_calendar[tmonth][tday][thour] == 0) {
                            freeSlots++;
                        }
                    }
                }
            }
            System.out.println("Initial free slots: " + freeSlots);


            int limit =  10 * (dateInterval[2] - dateInterval[0] + 1) * (dateInterval[3] - dateInterval[1] + 1);
            System.out.println("Limit: " + limit);
            int tmpCal[][][] = new int[13][32][24];
            for (int i = 1; i < 13; i++) {
                for (int j = 1; j < 32; j++) {
                    for (int k = 0; k < 24; k++) {
                        tmpCal[i][j][k] = 0;
                    }
                }
            }
            int cnt = 0;


            while (cnt < limit) {
                month = utils.randInt(dateInterval[1], dateInterval[3]);
                day = utils.randInt(dateInterval[0], dateInterval[2]);
                hour = utils.randInt(8, 20);
                System.out.println("Checking date: " + day + "/" + month + "/14 - " + hour + "h");
                if (tmpCal[month][day][hour] != 0) {
                    System.out.println("Already checked this date. Checking another date...");
                    continue;
                }
                if (_calendar[month][day][hour] != 0) {
                    System.out.println("I'm busy on thi date. Checking another date...");
                    cnt++;
                    continue;
                } else {
                    tmpCal[month][day][hour] = 1;
                    cnt++;
                    // System.out.println("Checking date: " + day + "/" + month + "/14 - " + hour + "h");
                    String msg = "Alice,CHECK," + Integer.toString(day) + "/" + Integer.toString(month) + "/14-" + Integer.toString(hour) + "," + utils.generateRandomNonce() + "," + String.valueOf(System.currentTimeMillis());
                    String iv = utils.generateRandomIV();
                    // out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(msg, DatatypeConverter.parseBase64Binary(_sharedKeyBI.toString()), iv)) + ":" + iv);
                    out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(msg, _sharedKey, iv)) + ":" + iv);
                    message = in.readUTF();
                    parsedMsg = parseMessage(message, socketClient, out);
                    if (!(parsedMsg == null)) {
                        if (parsedMsg[3].equals("Yes")) {
                            System.out.println("Found common date! Meeting with Bob scheduled to " + lastCheckedDay + "/" + lastCheckedMonth + "/14 at " + hour + " hours.");
                            _calendar[lastCheckedMonth][lastCheckedDay][hour] = 1;
                            --freeSlots;
                            System.out.println("Free slots: " + freeSlots);
                            return;
                        } else {
                            System.out.println("Bob\'s busy.");
                            cnt++;
                            continue;
                        }
                    } else {
                        socketClient.close();
                        System.out.println("ERROR: parsedMsg == null");
                        return;
                    }
                }
            }
            if (!parsedMsg[3].equals("Yes")) {
                System.out.println("Unable to find common date.");
                String msg = "Alice,CHECK,NODATE," + utils.generateRandomNonce() + "," + String.valueOf(System.currentTimeMillis());
                String iv = utils.generateRandomIV();
                // out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(msg, DatatypeConverter.parseBase64Binary(_sharedKeyBI.toString()), iv)) + ":" + iv);
                out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(msg, _sharedKey, iv)) + ":" + iv);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validNonce(String nonce, long currentTimeStamp) throws IOException {

        try {
            if (noncesMap.containsKey(nonce)) {
                // long tempTimeStamp = (long)noncesMap.get(nonce);
                if (!(withinTimeFrame(currentTimeStamp, noncesMap.get(nonce))))
                    noncesMap.remove(nonce);
                return false;
            } else
                return true;
        } catch (NullPointerException e) {
            return true;
        }
    }

    private boolean withinTimeFrame(long currentTimeStamp, long oldTimeStamp) {

        if ((currentTimeStamp - oldTimeStamp) < 30000)
            return true;
        else
            return false;
    }

    private void wrongFormatMessage(Socket socketClient, DataOutputStream out) throws IOException {

        String errorMessage = "ERROR: Message with wrong format received. Aborting current connection...";
        System.out.println(errorMessage);
        out.writeUTF("Alice:Alice,ERROR," + errorMessage);
        socketClient.close();
    }

    private void expiredMessage(Socket socketClient, DataOutputStream out) throws IOException {

        String errorMessage = "ERROR: Message with expired timstamp pr invalid nonce received. Aborting current connection...";
        System.out.println(errorMessage);
        out.writeUTF("Alice:Alice,ERROR," + errorMessage);
        socketClient.close();
    }

    private void wrongCredentialsProvided(Socket socketClient, DataOutputStream out) throws IOException {

        String errorMessage = "ERROR: Wrong credentials provided. Aborting current connection...";
        System.out.println(errorMessage);
        out.writeUTF("Alice:Alice,ERROR," + errorMessage);
        socketClient.close();
    }

    public void createDHPublicValues(Socket socketCl, DataOutputStream outsrv, DataInputStream insrv, int bobPort) {

        try {
            Socket socketClient = socketCl;
            AES aes = new AES();
            Utils utils = new Utils();
            String iv;
            String message = insrv.readUTF();
            String[] parsedMsg = null;
            BigInteger x = null, A, p = null, g, B;
            int bitLength = 1024; // 1024 bits
            SecureRandom rnd = new SecureRandom();
            String Na = null;


            parsedMsg = parseMessage(message, socketClient, outsrv);

            if (!(parsedMsg == null)) {
                x = BigInteger.probablePrime(bitLength, rnd);
                p = new BigInteger(parsedMsg[4]);
                g = new BigInteger(parsedMsg[5]);
                A = g.modPow(x, p);

                message = "Alice,DH,Bob," + A.toString() + "," + utils.generateRandomNonce() + "," + String.valueOf(System.currentTimeMillis());
                iv = utils.generateRandomIV();
                outsrv.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(message, aes.readKeyFromFile("AliceKeyStore"), iv)) + ":" + iv);
                // System.out.println("DHC1:1");
            } else {
                socketClient.close();
            }

            message = insrv.readUTF();
            socketClient.close();
            socketClient = new Socket("localhost", bobPort);
            OutputStream outToServer = socketClient.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            InputStream inFromServer = socketClient.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            // System.out.println("DHC1:2");
            parsedMsg = parseMessage(message, socketClient, out);

            if (!(parsedMsg == null)) {
                B = new BigInteger(parsedMsg[4]);
                _sharedKeyBI = B.modPow(x, p);
                // System.out.println(_sharedKeyBI);
                _sharedKey = utils.getSHA256(_sharedKeyBI);
                x = null;

                Na = utils.generateRandomNonce();
                message = "Alice,DH,Bob," + Na + "," + String.valueOf(System.currentTimeMillis());
                iv = utils.generateRandomIV();
                // out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(message, DatatypeConverter.parseBase64Binary(_sharedKeyBI.toString()), iv)) + ":" + iv);
                out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(message, _sharedKey, iv)) + ":" + iv);
                // System.out.println(aes.decrypt(aes.encrypt(message,_sharedKey,iv),_sharedKey,iv));
                System.out.println("Sent challenge to Bob with generated shared key.");
            } else {
                socketClient.close();
            }

            System.out.println("Waiting for Bob\'s response to challenge...");
            message = in.readUTF();
            // System.out.println("DHC1:3");
            parsedMsg = parseMessage(message, socketClient, out);

            if (!(parsedMsg == null)) {
                // System.out.println(Na + "\n\n\n" + parsedMsg[0] + "\n\n\n" + parsedMsg[1] + "\n\n\n" + parsedMsg[2] + "\n\n\n" + parsedMsg[3]);
                if (Na.equals(parsedMsg[4])) {
                    System.out.println("Bob responded successfuly to challenge. Sending challenge acknowledgement.");
                    message = "Alice,DH,Bob," + parsedMsg[5] + "," + String.valueOf(System.currentTimeMillis());
                    iv = utils.generateRandomIV();
                    // out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(message, DatatypeConverter.parseBase64Binary(_sharedKeyBI.toString()), iv)) + ":" + iv);
                    out.writeUTF("Alice:" + DatatypeConverter.printBase64Binary(aes.encrypt(message, _sharedKey, iv)) + ":" + iv);
                    // System.out.println("AQUI VOU EU!!!");
                    findRandomCommonDate(socketClient, out, in);
                    socketClient.close();
                }

                else if (!(Na.equals(parsedMsg[3]))) {
                    System.out.println("Challenge unsuccessful.");
                    socketClient.close();
                }


            } else {
                socketClient.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void authenticateUser(String correctHash) {
        System.out.println("Welcome back, Alice. Please enter your password:");

        PasswordHash ph = new PasswordHash();
        char[] passwd;
        Console cons = System.console();
        if (cons != null) {
            try {
                while (!(PasswordHash.validatePassword(passwd = cons.readPassword("[%s]", "Password:"), correctHash)))
                    System.out.println("Wrong password, please try again");
                java.util.Arrays.fill(passwd, ' ');
                // System.out.println("Password is clear: " + passwd);
            } catch (NoSuchAlgorithmException e) {
                System.out.println("ERROR: " + e);
            } catch (InvalidKeySpecException e) {
                System.out.println("ERROR: " + e);
            }
        } else {
            System.err.println("No console.");
            System.exit(1);
        }
    }

    private static String getInput() {
        String s = "";
        try {
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            s = bufferRead.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    public String encryptAndComposeMsg(String plaintext, String username) throws IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, NoSuchProviderException, FileNotFoundException, UnsupportedEncodingException, NoSuchAlgorithmException, IOException {
        AES aes = new AES();
        Utils utils = new Utils();
        String iv = utils.generateRandomIV();
        // System.out.println("ENTREI!!");
        byte[] cipheredMsg = aes.encrypt(plaintext, aes.readKeyFromFile(username + "KeyStore"), iv);
        return "Alice:" + DatatypeConverter.printBase64Binary(cipheredMsg) + ":" + iv;
    }

    public String[] decryptAndSplitMsg(String cipheredMsg, String iv, String username) throws IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IOException, FileNotFoundException, UnsupportedEncodingException {
        AES aes = new AES();
        Utils utils = new Utils();
        String decipheredText = aes.decrypt(DatatypeConverter.parseBase64Binary(cipheredMsg), aes.readKeyFromFile(username + "KeyStore"), iv);
        String[] decMsg = decipheredText.split(",");
        return decMsg;
    }

    private void registerWithServer(String serverName, int serverPort, int localPort) {
        try {
            Utils utils = new Utils();
            // while(out == null)
            //  out = new DataOutputStream(outToServer);
            String response = "";
            loop: while (!response.equals("ACKREG")) {
                Socket socketClient = new Socket(serverName, serverPort);
                OutputStream outToServer = socketClient.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                String testMsg = encryptAndComposeMsg("Alice,REG," + localPort + "," + utils.generateRandomNonce() + "," + String.valueOf(System.currentTimeMillis()), "Alice");
                // System.out.println(testMsg);
                out.writeUTF(testMsg);
                System.out.println("Sent registration to server. Awaiting response...");
                InputStream inFromServer = socketClient.getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                String inMsg = in.readUTF();
                String[] outerMsg = inMsg.split(":");
                String[] decMsg = null;
                if (outerMsg.length == 3) {
                    decMsg = decryptAndSplitMsg(outerMsg[1], outerMsg[2], "Alice");
                } else {
                    String errorMessage = "ERROR: Message with wrong format received. Aborting current connection...";
                    System.out.println(errorMessage);
                    socketClient.close();
                    continue;
                }
                if (outerMsg[0].equals(decMsg[0]) && (decMsg.length > 1)) { // Checks if it is the actual user

                    if (decMsg[1].equals("ACKREG")) {
                        System.out.println("Registered with server with success!");
                        socketClient.close();
                        break loop;
                    } else {
                        socketClient.close();
                        continue;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
            // } catch (IllegalBlockSizeException e){e.printStackTrace();
            // } catch (IllegalBlockSizeException e){e.printStackTrace();
            // } catch (IllegalBlockSizeException e){e.printStackTrace();


        }
    }

    private boolean requestMeeting(String serverName, int serverPort) {
        try {
            Utils utils = new Utils();

            Socket socketClient = new Socket(serverName, serverPort);
            OutputStream outToServer = socketClient.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            String testMsg = encryptAndComposeMsg("Alice,REQ,Bob," + utils.generateRandomNonce() + "," + String.valueOf(System.currentTimeMillis()), "Alice");
            // System.out.println(testMsg);
            out.writeUTF(testMsg);
            System.out.println("Sent meeting scheduling request to server. Awaiting response...");
            InputStream inFromServer = socketClient.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            String inMsg = in.readUTF();
            String[] outerMsg = inMsg.split(":");
            String[] decMsg = null;
            if (outerMsg.length == 3) {
                decMsg = decryptAndSplitMsg(outerMsg[1], outerMsg[2], "Alice");
            } else {
                String errorMessage = "ERROR: Message with wrong format received. Aborting current connection...";
                System.out.println(errorMessage);
                socketClient.close();
                return false;
            }
            if (outerMsg[0].equals(decMsg[0]) && (decMsg.length > 1)) { // Checks if it is the actual user
                if (decMsg[1].equals("ACCEPT")) {
                    if ((validNonce(decMsg[decMsg.length - 2], utils.getTimeStamp()))
                            && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(decMsg[decMsg.length - 1]))) {
                        int bobPort = Integer.parseInt(decMsg[4]);
                        // System.out.println(bobPort);
                        System.out.println("Meeting scheduling accepted by Bob");
                        createDHPublicValues(socketClient, out, in, bobPort);
                        return true;
                    }

                } else if (decMsg[1].equals("REJECT")) {
                    if ((validNonce(decMsg[decMsg.length - 2], utils.getTimeStamp()))
                            && withinTimeFrame(utils.getTimeStamp(), Long.parseLong(decMsg[decMsg.length - 1]))) {
                        System.out.println("Meeting scheduling rejected by Bob");
                        socketClient.close();
                        return false;
                    }
                } else {
                    System.out.printf("Wrong message format");
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
        //          }

        // } catch (FileNotFoundException e){e.printStackTrace();
        // } catch (UnknownHostException e){e.printStackTrace();
        // } catch (IOException e){e.printStackTrace();
        // } catch (NoSuchAlgorithmException e){e.printStackTrace();
        // } catch (NoSuchProviderException e){e.printStackTrace();
        // } catch (IllegalBlockSizeException e){e.printStackTrace();
        // } catch (InvalidKeyException e){e.printStackTrace();
        // } catch (NoSuchPaddingException e){e.printStackTrace();
        // } catch (InvalidAlgorithmParameterException e){e.printStackTrace();
        // } catch (BadPaddingException e){e.printStackTrace();
        // // } catch (IllegalBlockSizeException e){e.printStackTrace();
        // // } catch (IllegalBlockSizeException e){e.printStackTrace();
        // // } catch (IllegalBlockSizeException e){e.printStackTrace();


        // }

    }

    public String deparseMessage(String[] message) {

        int i;
        String res = null;

        for (i = 1; i < message.length - 1; i++) {

            if (i == message.length - 2) {

                res += message[i];
            } else {
                res += message[i] + ":";
            }
        }
        return res;
    }

    public static void main(String [] args) {
        Client1 alice = new Client1();
        String serverName = "";
        int port = 0;
        int localPort = 8081;
        String correctHash = "1000:4ef06e4a486a7f279ee1fb67b9f66ff1ad0c1fc1da22e8f7:1cb66a2517552374390797c200ff32205a457184e4ba4cf5";

        if (args.length != 2) {
            // System.err.println("Too few arguments. Run using Client1 [serverHostname] [serverPort]");
            // System.exit(-1);
            serverName = "localhost";
            port = 8080;
        } else {
            serverName = args[0];
            port = Integer.parseInt(args[1]);
        }

        if (!dbug)
            authenticateUser(correctHash);

        // String input = "";
        // while(!input.equals("y")){
        //  System.out.println("Register with server? [y/n]");
        //  input = getInput();
        // }
        alice.registerWithServer(serverName, port, localPort);

        boolean repeat = true;
        input1: while (repeat) {
            System.out.println("What do you want to do?");
            System.out.println("[1] Schedule a meeting");
            // System.out.println("");
            System.out.println("[0] Exit");
            try {
                int opt = 1000;
                if (!dbug)
                    opt = Integer.parseInt(getInput());
                else
                    opt = 1;
                if (opt == 0)
                    System.exit(0);
                if (opt == 1) {
                    boolean repeat1 = true;
                    input2: while (repeat1) {
                        System.out.println("Who do you want to schedule a meeting with?");
                        System.out.println("[1] Bob\n[0] Go back.");
                        opt = Integer.parseInt(getInput());
                        // opt = 1;
                        if (opt == 1) {
                            if (alice.requestMeeting(serverName, port)) {
                                // System.out.println("HURRA!");

                            }
                        } else if (opt == 0) {
                            continue input1;
                        } else {
                            System.out.println("Bob rejected your request to schedule a meeting.");
                            continue input1;
                        }
                    }
                } else break;


            } catch (NumberFormatException e) {
                System.out.println("Input must be a number");
                continue input1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
