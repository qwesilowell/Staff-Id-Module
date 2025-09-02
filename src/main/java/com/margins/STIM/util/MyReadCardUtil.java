///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.margins.STIM.util;
//
///**
// *
// * @author PhilipManteAsare
// */
///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//import com.c10n.cvcert.CVCertificate;
//import com.c10n.scalibur.PacePinType;
//import com.c10n.scalibur.SCalibur;
//import com.c10n.scalibur.SCaliburException;
//import com.c10n.scalibur.card.pin.PinCallback;
//import com.c10n.scalibur.card.request.AbstractEIdRequest;
//import com.c10n.scalibur.ghananid.card.EIdRequest;
//import com.c10n.scalibur.ghananid.card.GhanaNIDCard;
//import com.c10n.scalibur.smartcards.SmartCardTerminal;
//import com.c10n.scalibur.smartcards.SmartCardTerminals;
//import com.c10n.scalibur.tki.SimpleTerminalKey;
//import com.c10n.scalibur.tki.TerminalKeyInterface;
//import com.c10n.scalibur.tr3110.PaConfig;
//import com.c10n.scalibur.tr3110.PaConfigBuilder;
//import com.margins.nia.util.ReadCardUtil;
//import java.io.BufferedWriter;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.Serializable;
//import java.security.PrivateKey;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.json.JSONObject;
//
//public class MyReadCardUtil implements Serializable {
//
//    public static String csca = "C:\\Program Files (x86)\\DERMALOG\\CardReaderService\\Certificates\\eid\\csca\\";
//    public static String ds = "C:\\Program Files (x86)\\DERMALOG\\CardReaderService\\Certificates\\ds";
//    public static String taCertFile = "C:\\Program Files (x86)\\DERMALOG\\CardReaderService\\Certificates\\eid\\ta_AT.cvcert";
//    public static String taPrivKeyFile = "C:\\Program Files (x86)\\DERMALOG\\CardReaderService\\Certificates\\eid\\ta_AT.pkcs8";
//    public static String dvCertFile = "C:\\Program Files (x86)\\DERMALOG\\CardReaderService\\Certificates\\eid\\dv_and_cvca_link\\";
//
//    static SmartCardTerminals smartCards = new SmartCardTerminals();
//
//    static GhanaNIDCard idCard = null;
//    
//    public SmartCardTerminal loadSmartReader() {
//        System.out.println("loadSmartREader >>>>>>>>>>>>>>>>> 111 ");
//        ReadCardUtil rcu = new ReadCardUtil();
//        SmartCardTerminal sct = rcu.loadSmartReader();
//        System.out.println("loadSmartREader >>>>>>>>>>>>>>>>> 222 " + sct);
//        System.out.println("loadSmartREader >>>>>>>>>>>>>>>>> 333 " + sct.getName());
//        return sct;
//    }
//
//    public SmartCardTerminal loadSmartReader2() {
//        try {
//            SCalibur.initializeCryptoProvider();
//            List<SmartCardTerminal> goodSmartCards = new ArrayList<>();
//            System.out.println("SMART CARDS>>>>>>>>>>>>>>>>> " + smartCards.getSmartCardTerminals().size());
//            for (SmartCardTerminal smartCard : smartCards.getSmartCardTerminals()) {
//                System.out.println("SM " + smartCard.getName());
//                if (smartCard.isPresent()) {
//                    if (GhanaNIDCard.isCompatibleCardContainedInReader(smartCard)) {
//                        goodSmartCards.add(smartCard);
//                    }
//                }
//            }
//            if (goodSmartCards.isEmpty() == false) {
//                return goodSmartCards.get(0);
//            } else {
//                return null;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//
//    }
//
//    public String debugReaders() {
//        StringBuilder sb = new StringBuilder();
//        try {
//            System.out.println("Java arch: " + System.getProperty("os.arch"));
//            System.out.println("Library path: " + System.getProperty("java.library.path"));
//            SCalibur.initializeCryptoProvider();
//            List<SmartCardTerminal> terminals = smartCards.getSmartCardTerminals();
//            sb.append("SMART CARD READERS FOUND: ").append(terminals.size()).append("\n");
//            for (SmartCardTerminal terminal : terminals) {
//                sb.append(" >> ").append(terminal.getName())
//                        .append(" (present=").append(terminal.isPresent()).append(")\n");
//            }
//        } catch (Exception e) {
//            sb.append("Error: ").append(e.getMessage());
//            e.printStackTrace();
//        }
//        return sb.toString();
//    }
//
//    public GhanaNIDCard readCardDetail(SmartCardTerminal smartCard, String can, String fileName) {
//
//        try {
//            idCard = new GhanaNIDCard(smartCard);
//            TerminalKeyInterface tki = setCertificatesAndCredentials();
//
//            // Create a PinCallback, which asks the User for its PACE user PIN:
//            PinCallback pinCb = new PinCallback() {
//                @Override
//                public PinCallback.PinValue getPin() throws SCaliburException {
//
//                    return new PinCallback.PinValue(can.toCharArray());
//                }
//            };
//            // Note, you can learn more on PinCallbacks in the SCalibur
//            // Javadoc or the PinHandling_CardLayer-example.
//
//            // Create an instance of EIdRequest, and specify
//            // that we only want to read the GivenName and the FamilyName.
//            // Note that your choices here might be limited 
//            // by the Terminal Certificate or DV certificate,
//            // which where stored using the setCertificatesAndCredentials()
//            // method!
//            EIdRequest req = idCard.getNewEIdRequest();
//            req.getHolderDetails().setRead(true);
//            req.getFacialImage().setRead(false);
//            req.getAddress().setRead(false);
//            req.getDateOfBirth().setRead(true);
//            req.getGNin().setRead(true);
//            req.getLocation().setRead(false);
//            req.getNextOfKin().setRead(false);
//            req.getValidityDate().setRead(false);
//            req.getTrackingNumber().setRead(true);
//            req.getFingerprintTemplates().setRead(false);
//
//            // The default method for authentication is using the PIN.
//            // In our case, we use the CAN for authentication:
//            req.setCredentialType(PacePinType.CAN);
//
//            // Perform the request with the card:
//            // This method call does the main work, including collecting the card 
//            // holder's eID Pin, establishing secure messaging via PACE first and 
//            // afterwards completing the EACv2 protocol. Afterwards, we are able to 
//            // read data-groups from the card, so the name is read and stored in 
//            // the request:
//            req.perform(pinCb);
//
//            // Print the names, which were read from the card:
//            AbstractEIdRequest.ReadableDataGroup<?>[] array = req.getReadableDgs();
//
//            JSONObject d = new JSONObject();
//            for (int i = 0; i < array.length; ++i) {
//
//                if (null != array[i].getContent()) {
//                    //if (i == 6) {
////                    ;
////                    writeToFile(array[i].getContent().toString() + ">>>>>>>>>>>>>>>>>>" + i, fileName + ".txt");
////                    System.out.println(array[i].getContent().toString());
////                    //}
//                    if (i == 0) {
//                        d = convertMain(array[i].getContent().toString());
//                    }
//                    if (i == 4) {
//                        d.put("dateOfBirth", array[i].getContent().toString());
//                    }
//                    if (i == 5) {
//                        d.put("ghanaCardPin", array[i].getContent().toString());
//                    }
//
//                }
//                if (null != array[i].getReadError()) {
//                    System.out.println("DG" + (i + 1) + " Error: " + array[i].getReadError());
//                }
//            }
//            writeToFile(d.toString(), fileName + ".txt");
//            return idCard;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    //To return Json
//    public JSONObject readCardDetailAsJSON(SmartCardTerminal smartCard, String can) {
//        try {
//            idCard = new GhanaNIDCard(smartCard);
//            TerminalKeyInterface tki = setCertificatesAndCredentials();
//
//            // Create a PinCallback, which asks the User for its PACE user PIN:
//            PinCallback pinCb = new PinCallback() {
//                @Override
//                public PinCallback.PinValue getPin() throws SCaliburException {
//                    return new PinCallback.PinValue(can.toCharArray());
//                }
//            };
//
//            // Create an instance of EIdRequest
//            EIdRequest req = idCard.getNewEIdRequest();
//            req.getHolderDetails().setRead(true);
//            req.getFacialImage().setRead(false);
//            req.getAddress().setRead(false);
//            req.getDateOfBirth().setRead(true);
//            req.getGNin().setRead(true);
//            req.getLocation().setRead(false);
//            req.getNextOfKin().setRead(false);
//            req.getValidityDate().setRead(false);
//            req.getTrackingNumber().setRead(true);
//            req.getFingerprintTemplates().setRead(false);
//
//            // Use CAN for authentication
//            req.setCredentialType(PacePinType.CAN);
//
//            // Perform the request
//            req.perform(pinCb);
//
//            // Process the data
//            AbstractEIdRequest.ReadableDataGroup<?>[] array = req.getReadableDgs();
//
//            JSONObject d = new JSONObject();
//            for (int i = 0; i < array.length; ++i) {
//                if (null != array[i].getContent()) {
//                    if (i == 0) {
//                        d = convertMain(array[i].getContent().toString());
//                    }
//                    if (i == 4) {
//                        d.put("dateOfBirth", array[i].getContent().toString());
//                    }
//                    if (i == 5) {
//                        d.put("ghanaCardPin", array[i].getContent().toString());
//                    }
//                }
//                if (null != array[i].getReadError()) {
//                    System.out.println("DG" + (i + 1) + " Error: " + array[i].getReadError());
//                }
//            }
//
//            // Add status to indicate real card data
//            d.put("status", "real_card_data");
//
//            return d; // Return the JSON data instead of idCard
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    TerminalKeyInterface setCertificatesAndCredentials() throws Exception {
//
//        // First, we use the class PAConfigBuilder to
//        // create an instance of PAConfig, which
//        // stores all trust anchors, Document Signer certificates and
//        // CRLs, which are necessary to verify the signature of the Document
//        // Signer to verify that the Ghana NID Card is a valid one:
//        PaConfig pACredentials = new PaConfigBuilder()
//                // first, we add a trust anchor. In general, we have to add more 
//                // than one, namely all of the CSCA certificates:
//
//                .addTrustAnchors(new File(csca))
//                //		.addTrustAnchors(new File("certificates/eid/csca/"))
//                // next, we add all Document Signer certificates, which are all 
//                // stored in the folder certificates/ds
//                .addIntermediateCertificates(new File(ds))
//                //		.addIntermediateCertificates(new File("certificates/eid/ds/"))
//                // because we do not have a CRL, we have to deactivate CRL checking:
//                .deactivateCRL() // FIXME:
//                // deactivating CRL Checking is NOT suitable in real implementations of 
//                // a terminal or inspection system. You have to consider the CRL (Certificate
//                // Revocation Lists) in that context, otherwise invalid and revoked documents
//                // might be validated without warnings! Use
//                //
//                //  .activateCRL()
//                //  .addCRL(..)
//                //
//                // to do so!
//                // Finally, build the instance of PAConfig:
//                .build();
//
//        // This loads the Terminal Certificate, which is signed by the 
//        // Document Verifier. It is needed for the Terminal Authentication (TA)
//        // step of EACv2. Note that we also need the private key to the public 
//        // key in this certificate:
//        File taCerFile = new File(taCertFile);
////		File taCertFile = new File("certificates/eid/ta_AT.cvcert");
////		File taCertFile = new File("certificates/eid/ta_AT.cvcert");
//        if (!taCerFile.exists()) {
//            throw new FileNotFoundException("taCertFile does not exist!");
//        }
//        CVCertificate taCert = SCalibur.loadCertificate(taCerFile);
//
//        File taPrivFile = new File(taPrivKeyFile);
////		File taPrivKeyFile = new File("certificates/eid/ta_AT.pkcs8");
//        TerminalKeyInterface tki = null;
//        if (!taPrivFile.exists()) {
//            throw new FileNotFoundException("taPrivKeyFile does not exist!");
//        } else {
//            PrivateKey taPrivKey = SCalibur.loadEcPkcs8PrivateKey(taPrivFile);
//            tki = new SimpleTerminalKey(taPrivKey, taCert); // FIXME:
//            // this is not a safe method to handle a real terminal key!
//            // It is the most simple approach to get started and fine for test
//            // cards and keys, but no feasible way in a production system!
//        }
//        // The private key is submitted to the instance of Ghana NID Card as 
//        // implementation of TerminalKeyInterface. Note that other 
//        // implementations if TerminalKeyInterface allow usage of HSMs or SAM cards.
//        // Please study the TKI chapter of the manual for further information.
//
//        // This loads the Document Verifier Certificate, which is signed by the 
//        // CVCA. It is needed for the Terminal Authentication (TA) Step of EACv2:
////        File dvCertFile = new File("C:\\credenceid\\GhanaNID\\eid\\dv_and_cvca_link\\GHNIAeID200003.cvcert");
//        ////		File dvCertFile = new File("certificates/eid/dv_and_cvca_link/dv_AT.cvcert");
////        if (!dvCertFile.exists()) {
////            tki.close();
////            throw new FileNotFoundException("dvCertFile does not exist!");
////        }
//        //CVCertificate dvCert = SCalibur.loadCertificate(dvCertFile);
//        Collection<CVCertificate> dvCertList = SCalibur.loadCertificates(new File(dvCertFile));
//
//        // Finally, we store all credentials and certificates in the idCard object:
//        idCard.setEIdEacConfig(pACredentials, tki, taCert, dvCertList);
////		idCard.setEIdEacConfig(pACredentials, tki, taCert, Collections.singletonList(dvCert));
//        // Note that generally it may not suffice to submit one DV Certificate.
//        // While we always know, which DV Certificate we have to submit, it 
//        // might be the case that this DV certificate was signed by a new CVCA 
//        // certificate, which the Ghana NID Card is not aware of. In this case,
//        // we have to provide a list of CVCA link certificates to  the Ghana NID 
//        // Card, such that it is able to verify all of them. To do so, load 
//        // them with SCalibur.loadCertifcate() or SCalibur.loadCertificates()
//        // and provide the loaded certificates as a collection to the 
//        // setEacConfig()-method. Please read the corresponding 
//        // JavaDoc-documentation for further details!
//
//        return tki;
//    }
//
//    public void writeToFile(String data, String file) {
//        FileWriter fileWriter;
//        try {
//            File f = new File(file);
//            if (f.exists() == false) {
//                f.createNewFile();
//            }
//            fileWriter = new FileWriter(file, true);
//            try (BufferedWriter bufferFileWriter = new BufferedWriter(fileWriter)) {
//                bufferFileWriter.append(data);
//                bufferFileWriter.newLine();
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            Logger.getLogger(MyReadCardUtil.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    public JSONObject convertMain(String data) {
//        data = data.replaceAll("HolderDetails ", "");
//        data = data.substring(1, data.length() - 1);  // Remove the brackets
//
//        // Step 2: Split the string by commas to get key-value pairs
//        String[] keyValuePairs = data.split(", ");
//
//        // Step 3: Extract individual values by splitting each key-value pair
//        String giveName = "", midleName = "", surName = "", gender = "";
//
//        for (String pair : keyValuePairs) {
//            String[] keyValue = pair.split("=");
//            String key = keyValue[0];
//            String value = "";
//            if (keyValue.length > 1) {
//                value = keyValue[1];
//            }
//
//            // Assign the value to the corresponding field based on the key
//            if (key.equals("giveName")) {
//                giveName = value;
//            } else if (key.equals("midleName")) {
//                midleName = value;
//            } else if (key.equals("surName")) {
//                surName = value;
//            } else if (key.equals("gender")) {
//                gender = value;
//            }
//        }
//
//        JSONObject resp = new JSONObject();
//
//        resp.put("firstName", giveName);
//        resp.put("surname", surName);
//        resp.put("gender", gender);
//        resp.put("middleName", midleName);
//        return resp;
//
//    }
//}
