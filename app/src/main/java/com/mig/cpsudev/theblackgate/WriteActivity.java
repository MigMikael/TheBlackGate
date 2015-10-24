package com.mig.cpsudev.theblackgate;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;

public class WriteActivity extends AppCompatActivity {

    private static final String TAG = "NFCWriteTag";
    private NfcAdapter mNfcAdapter;
    private IntentFilter[] mWriteTagFilters;
    private PendingIntent mNfcPendingIntent;
    private Context context;
    WriteResponse wr = new WriteResponse();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        final EditText input = (EditText) findViewById(R.id.editText);
        input.setText("google.com");
        Button okBtn = (Button) findViewById(R.id.button);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wr.setUniqueId(input.getText().toString());
                Toast.makeText(WriteActivity.this, "Set Text Complete", Toast.LENGTH_SHORT).show();
            }
        });

        context = getApplicationContext();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mNfcPendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP),
                0
        );
        IntentFilter discovery = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        mWriteTagFilters = new IntentFilter[]{discovery};
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNfcAdapter != null) {
            if (!mNfcAdapter.isEnabled()) {
                new AlertDialog.Builder(this)
                        .setMessage("Please Turn On your NFC")
                        .setPositiveButton("Update Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent setnfc = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(setnfc);
                            }
                        })
                        .setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        })
                        .show();
            }
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
        } else {
            Toast.makeText(context, "Sorry, No NFC Adapter found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (wr.supportedTechs(detectedTag.getTechList())) {
                if (wr.writableTag(detectedTag, context)) {
                    wr.writeTag(wr.getTagAsNdef(), detectedTag);
                    String message = (wr.getStatus() == 1 ? "Success:" : "Failed:") + wr.getMessage();
                    Toast.makeText(WriteActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "This tag is not writable", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "This tag type is not supported", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


