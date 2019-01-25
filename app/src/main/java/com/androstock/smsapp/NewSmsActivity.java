package com.androstock.smsapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

public class NewSmsActivity extends AppCompatActivity {


    private TextInputEditText pesan;
    private TextInputEditText kunciRailFence;
    private TextInputEditText kunciDES;
    private Button buttonEnkripsi;
    private Button buttonKirim;
    private TextInputEditText hasilEnkripsi;
    private AutoCompleteTextView noTelpon;
    private ImageButton addcontacts;
    ConstraintLayout mLayout;


    private static final int PICK_CONTACT = 1;

    //uri for contact data
    Uri contactDataUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_new);

        noTelpon = findViewById(R.id.toNumber);
        pesan = findViewById(R.id.pesan);
        kunciRailFence = findViewById(R.id.kunciRailFence);
        kunciDES = findViewById(R.id.kunciDES);
        buttonEnkripsi = findViewById(R.id.buttonEnkripsi);
        buttonKirim = findViewById(R.id.buttonKirim);
        hasilEnkripsi = findViewById(R.id.hasilEnkripsi);

        addcontacts = findViewById(R.id.contacts);

        noTelpon.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedContact = parent.getItemAtPosition(position).toString();
                String selectedContactNumber = selectedContact.substring(selectedContact.lastIndexOf("\n") + 1);
                String selectedContactName = selectedContact.substring(0, selectedContact.indexOf("\n"));

                noTelpon.setText(selectedContactNumber);
                showToast(selectedContactName);
            }
        });

        addcontacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startContactPicker();
            }
        });
        buttonKirim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noTelpon.length()==0){
                    noTelpon.setError("No Telp harus diisi!");
                    Toast.makeText(getApplicationContext(), "No Telp harus diisi!", Toast.LENGTH_LONG).show();
                }
                if (hasilEnkripsi.length()==0){
                    hasilEnkripsi.setError("Enkripsi pesan terlebih dahulu!");
                    Toast.makeText(getApplicationContext(), "Enkripsi pesan terlebih dahulu!", Toast.LENGTH_LONG).show();
                }else {
                    try {

                        SmsManager smsManager = SmsManager.getDefault();
                        String message = "RFD" + hasilEnkripsi.getText().toString();
                        ArrayList<String> parts = smsManager.divideMessage(message);
                        smsManager.sendMultipartTextMessage(noTelpon.getText().toString(), null, parts, null, null);
                        Toast.makeText(getApplicationContext(), "SMS Berhasil Dikirim", Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        Toast.makeText(getApplicationContext(), "SMS Gagal Dikirim", Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                    }
                }
            }
        });
        buttonEnkripsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pesan.length()==0){
                    pesan.setError("Pesan harus diisi!");
                    Toast.makeText(getApplicationContext(), "Pesan harus diisi!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (kunciRailFence.length()==0){
                    kunciRailFence.setError("Kunci Rail Fence harus diisi!");
                    Toast.makeText(getApplicationContext(), "Kunci Rail Fence harus diisi!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (Integer.parseInt(kunciRailFence.getText().toString()) < 2) {
                    kunciRailFence.setError("Harus lebih besar dari 1!");
                    Toast.makeText(getApplicationContext(), "Kunci Rail Fence harus lebih besar dari 1!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (Integer.parseInt(kunciRailFence.getText().toString()) > pesan.length()) {
                    kunciRailFence.setError("Nilai maksimal sesuai panjang pesan!");
                    return;
                }

                if (kunciDES.length() != 8) {
                    kunciDES.setError("Panjang harus 8 karakter!");
                    Toast.makeText(getApplicationContext(), "Kunci DES harus diisi 8 karakter!", Toast.LENGTH_LONG).show();
                    return;
                }
                RailFence railFence = new RailFence();
                String cipher = railFence.enkripsi(pesan.getText().toString(),Integer.parseInt(kunciRailFence.getText().toString()));

                DES des = new DES();
                char[][] key = des.generateKunci(Konversi.asciiToHexa(kunciDES.getText().toString()));
                String sHex = des.enkripsi(Konversi.asciiToHexa(cipher), key);

                hasilEnkripsi.setText(sHex);
            }
        });
    }


    private void startContactPicker() {
        Log.d("IN ", "startContactPicker() called");
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("In result ", String.valueOf(resultCode));
        if (requestCode == PICK_CONTACT &&
                resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            contactDataUri = data.getData();
            //showToast(String.valueOf(contactDataUri)); //working
            Log.d("Got URI", String.valueOf(contactDataUri));
            getContact(contactDataUri); //also sets the contact number to our autocomplete textview

        } else {
            noTelpon.setText("");
            Snackbar.make(mLayout, "No contact selected.", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void getContact(Uri contactDataUri) {
        Cursor c = managedQuery(contactDataUri, null, null, null, null);
        if (c.moveToFirst()) {
            String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if (hasPhone.equalsIgnoreCase("1") || hasPhone.equalsIgnoreCase("2")) {
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                phones.moveToFirst();

                String pickedContactNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String pickedContactName = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)); //name is here
                //TODO: email aauchha ki nai check
                Log.d("Got name", " from Contact " + pickedContactName);
                //Log.d("Got email", " from Contact "+pickedContactEmail);
                showToast(pickedContactName);

                //setting picked contact's number to our edittext
                noTelpon.setText(pickedContactNumber);

            }
        }
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

