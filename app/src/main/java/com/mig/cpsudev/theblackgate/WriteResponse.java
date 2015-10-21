package com.mig.cpsudev.theblackgate;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by Mig on 21-Oct-15.
 */
public class WriteResponse {
    private int status;
    private String message;
    private String uniqueId = " ";
    private boolean writeProtect = false;

    public WriteResponse() {

    }

    WriteResponse(int Status, String Message) {
        this.status = Status;
        this.message = Message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        String mess;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    this.status = 0;
                    this.message = "Tag is read-only";
                }
                if (ndef.getMaxSize() < size) {
                    mess = "Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                            + " bytes.";
                    this.status = 0;
                    this.message = mess;
                }
                ndef.writeNdefMessage(message);
                if (writeProtect) ndef.makeReadOnly();
                mess = "Wrote message to pre-formatted tag.";
                this.status = 1;
                this.message = mess;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        mess = "Formatted tag and wrote message";
                        this.status = 1;
                        this.message = mess;
                    } catch (IOException e) {
                        mess = "Failed to format tag.";
                        this.status = 0;
                        this.message = mess;
                    }
                } else {
                    mess = "Tag doesn't support NDEF.";
                    this.status = 0;
                    this.message = mess;
                }
            }
        } catch (Exception e) {
            mess = "Failed to write tag";
            this.status = 0;
            this.message = mess;
        }
    }

    public static boolean supportedTechs(String[] techs) {
        boolean ultralight = false;
        boolean nfcA = false;
        boolean ndef = false;
        for (String tech : techs) {
            if (tech.equals("android.nfc.tech.MifareUltralight")) {
                ultralight = true;
            } else if (tech.equals("android.nfc.tech.NfcA")) {
                nfcA = true;
            } else if (tech.equals("android.nfc.tech.Ndef") || tech.equals("android.nfc.tech.NdefFormatable")) {
                ndef = true;
            }
        }
        if (ultralight || nfcA || ndef) {
            return true;
        } else {
            return false;
        }
    }

    public boolean writableTag(Tag tag, Context context) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Toast.makeText(context, "Tag is read-only.", Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return false;
                }
                ndef.close();
                return true;
            }
        } catch (Exception e) {
            Toast.makeText(context, "Failed to read tag", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public NdefMessage getTagAsNdef() {
        boolean addAAR = false;
        byte[] uriField = uniqueId.getBytes(Charset.forName("US-ASCII"));
        byte[] payload = new byte[uriField.length + 1];       //add 1 for the URI Prefix
        payload[0] = 0x01;                        //prefixes http://www. to the URI
        System.arraycopy(uriField, 0, payload, 1, uriField.length); //appends URI to payload
        NdefRecord rtdUriRecord = new NdefRecord(
                NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, new byte[0], payload);
        if (addAAR) {
            // note: returns AAR for different app (nfcreadtag)
            return new NdefMessage(new NdefRecord[]{
                    rtdUriRecord, NdefRecord.createApplicationRecord("com.mig.cpsudev.theblackgate")
            });
        } else {
            return new NdefMessage(new NdefRecord[]{
                    rtdUriRecord});
        }
    }
}