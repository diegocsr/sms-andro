package com.androstock.smsapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by SHAJIB on 7/13/2017.
 */

public class Chat extends AppCompatActivity {

    ListView listView;
    ChatAdapter adapter;
    LoadSms loadsmsTask;

    LoadSmsD loadsmsDTask;

    String name;
    String address;
    EditText keyRailFence;
    EditText keyDES;
    ImageButton dekripsi;
    int thread_id_main;
    private Handler handler = new Handler();
    Thread t;
    ArrayList<HashMap<String, String>> smsList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> customList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> tmpList = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        address = intent.getStringExtra("address");
        thread_id_main = Integer.parseInt(intent.getStringExtra("thread_id"));

        listView = (ListView) findViewById(R.id.listView);

        startLoadingSms();


        keyRailFence    = findViewById(R.id.keyRailFence);
        keyDES          = findViewById(R.id.keyDES);

        dekripsi  = findViewById(R.id.dekripsi);
//        hasilDekripsi   = findViewById(R.id.hasilDekripsi);

        dekripsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (keyRailFence.length()==0){
                    keyRailFence.setError("Kunci Rail Fence harus diisi!");
                    Toast.makeText(getApplicationContext(), "Kunci Rail Fence harus diisi!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (Integer.parseInt(keyRailFence.getText().toString()) < 2) {
                    keyRailFence.setError("Harus lebih besar dari 1!");
                    Toast.makeText(getApplicationContext(), "Kunci Rail Fence harus lebih besar dari 1!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (keyDES.length() != 8) {
                    keyDES.setError("Panjang harus 8 karakter!");
                    Toast.makeText(getApplicationContext(), "Kunci DES harus diisi 8 karakter!", Toast.LENGTH_LONG).show();
                    return;
                }

                readSMS();
            }
        });
    }

    public void readSMS(){

        loadsmsDTask = new LoadSmsD();
        String[] kunci = new String[2];
        kunci[0] = keyDES.getText().toString();
        kunci[1] = keyRailFence.getText().toString();
        loadsmsDTask.execute(kunci[0],kunci[1]);

//        DES des= new DES();
//        RailFence railFence = new RailFence();
//
//        char[][] kunciDES= des.generateKunci(Konversi.asciiToHexa(keyDES.getText().toString()));
//
//        Uri uriInbox = Uri.parse("content://sms/inbox");
//        Cursor cursor = getContentResolver().query(uriInbox, null, "thread_id=" + thread_id_main, null, null);
////        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
//        if (cursor.moveToFirst()) {
//            StringBuffer sb = new StringBuffer();
//            do {
//                String body = cursor.getString(cursor.getColumnIndex("body"));
//                if (body.startsWith("RFD")){
//
//                    String pesan = des.dekripsi(body.substring(3), kunciDES);
//                    pesan = Konversi.hexaToascii(pesan);
//                    pesan = railFence.dekripsi(pesan,Integer.parseInt(keyRailFence.getText().toString()));
//
//                    String address = cursor.getString(cursor.getColumnIndex("address"));
//                    sb.append("Pesan dari : "+ address);
//                    sb.append("\n");
//                    sb.append("Isi pesan : "+ body);
//                    sb.append("\n");
//
//                    Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show();
//                }
//            } while (cursor.moveToNext());
//
////            hasilDekripsi.setText(sb.toString());
//
//
//        }else {
//            Toast.makeText(this,"Tidak ada pesan yang menggunkan kunci tersebut", Toast.LENGTH_LONG).show();
//        }
    }


    class LoadSmsD extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tmpList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            String keyDes = args[0];
            String keyRail = args[1];

            DES des= new DES();
            RailFence railFence = new RailFence();

            char[][] kunciDES= des.generateKunci(Konversi.asciiToHexa(keyDes));

            //Log.d("ISI", "kunci1:"+isiA+" "+"kunci2:"+isiB);

            try {
                Uri uriInbox = Uri.parse("content://sms/inbox");
                Cursor inbox = getContentResolver().query(uriInbox, null, "thread_id=" + thread_id_main, null, null);
                Uri uriSent = Uri.parse("content://sms/sent");
                Cursor sent = getContentResolver().query(uriSent, null, "thread_id=" + thread_id_main, null, null);
                Cursor c = new MergeCursor(new Cursor[]{inbox,sent}); // Attaching inbox and sent sms

                if (c.moveToFirst()) {
                    for (int i = 0; i < c.getCount(); i++) {

                        String phone = "";
                        String _id = c.getString(c.getColumnIndexOrThrow("_id"));
                        String thread_id = c.getString(c.getColumnIndexOrThrow("thread_id"));

                        String msg = c.getString(c.getColumnIndexOrThrow("body"));
                        String pesan = des.dekripsi(msg.substring(3), kunciDES);
                        pesan = Konversi.hexaToascii(pesan);
                        msg = railFence.dekripsi(pesan,Integer.parseInt(keyRailFence.getText().toString()));

                        String type = c.getString(c.getColumnIndexOrThrow("type"));
                        String timestamp = c.getString(c.getColumnIndexOrThrow("date"));
                        phone = c.getString(c.getColumnIndexOrThrow("address"));

                        tmpList.add(Function.mappingInbox(_id, thread_id, name, phone, msg, type, timestamp, Function.converToTime(timestamp)));
                        c.moveToNext();
                    }
                }
                c.close();

            }catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Collections.sort(tmpList, new MapComparator(Function.KEY_TIMESTAMP, "asc"));

            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            if(!tmpList.equals(smsList))
            {
                smsList.clear();
                smsList.addAll(tmpList);
                adapter = new ChatAdapter(Chat.this, smsList);
                listView.setAdapter(adapter);

            }
        }
    }


    class LoadSms extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tmpList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            try {
                Uri uriInbox = Uri.parse("content://sms/inbox");
                Cursor inbox = getContentResolver().query(uriInbox, null, "thread_id=" + thread_id_main, null, null);
                Uri uriSent = Uri.parse("content://sms/sent");
                Cursor sent = getContentResolver().query(uriSent, null, "thread_id=" + thread_id_main, null, null);
                Cursor c = new MergeCursor(new Cursor[]{inbox,sent}); // Attaching inbox and sent sms



                if (c.moveToFirst()) {
                    for (int i = 0; i < c.getCount(); i++) {
                        String phone = "";
                        String _id = c.getString(c.getColumnIndexOrThrow("_id"));
                        String thread_id = c.getString(c.getColumnIndexOrThrow("thread_id"));
                        String msg = c.getString(c.getColumnIndexOrThrow("body"));
                        String type = c.getString(c.getColumnIndexOrThrow("type"));
                        String timestamp = c.getString(c.getColumnIndexOrThrow("date"));
                        phone = c.getString(c.getColumnIndexOrThrow("address"));
                        if(msg.startsWith("RFD")){
                            tmpList.add(Function.mappingInbox(_id, thread_id, name, phone, msg, type, timestamp, Function.converToTime(timestamp)));
                        }
                        c.moveToNext();
                    }
                }
                c.close();

            }catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Collections.sort(tmpList, new MapComparator(Function.KEY_TIMESTAMP, "asc"));

            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            if(!tmpList.equals(smsList))
            {
                smsList.clear();
                smsList.addAll(tmpList);
                adapter = new ChatAdapter(Chat.this, smsList);
                listView.setAdapter(adapter);

            }
         }
    }




    public void startLoadingSms()
    {
        loadsmsTask = new LoadSms();
        loadsmsTask.execute();
//        final Runnable r = new Runnable() {
//            public void run() {
//
//                loadsmsTask = new LoadSms();
//                loadsmsTask.execute();
//
//                handler.postDelayed(this, 10000);
//            }
//        };
//        handler.postDelayed(r, 0);
    }
}







class ChatAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap< String, String >> data;
    public ChatAdapter(Activity a, ArrayList < HashMap < String, String >> d) {
        activity = a;
        data = d;
    }
    public int getCount() {
        return data.size();
    }
    public Object getItem(int position) {
        return position;
    }
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ChatViewHolder holder = null;
        if (convertView == null) {
            holder = new ChatViewHolder();
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.chat_item, parent, false);


            holder.txtMsgYou = (TextView)convertView.findViewById(R.id.txtMsgYou);
            holder.lblMsgYou = (TextView)convertView.findViewById(R.id.lblMsgYou);
            holder.timeMsgYou = (TextView)convertView.findViewById(R.id.timeMsgYou);
            holder.lblMsgFrom = (TextView)convertView.findViewById(R.id.lblMsgFrom);
            holder.timeMsgFrom = (TextView)convertView.findViewById(R.id.timeMsgFrom);
            holder.txtMsgFrom = (TextView)convertView.findViewById(R.id.txtMsgFrom);
            holder.msgFrom = (LinearLayout)convertView.findViewById(R.id.msgFrom);
            holder.msgYou = (LinearLayout)convertView.findViewById(R.id.msgYou);

            convertView.setTag(holder);
        } else {
            holder = (ChatViewHolder) convertView.getTag();
        }
        holder.txtMsgYou.setId(position);
        holder.lblMsgYou.setId(position);
        holder.timeMsgYou.setId(position);
        holder.lblMsgFrom.setId(position);
        holder.timeMsgFrom.setId(position);
        holder.txtMsgFrom.setId(position);
        holder.msgFrom.setId(position);
        holder.msgYou.setId(position);

        HashMap < String, String > song = new HashMap < String, String > ();
        song = data.get(position);
        try {


            if(song.get(Function.KEY_TYPE).contentEquals("1"))
            {
                holder.lblMsgFrom.setText(song.get(Function.KEY_NAME));
                holder.txtMsgFrom.setText(song.get(Function.KEY_MSG));
                holder.timeMsgFrom.setText(song.get(Function.KEY_TIME));
                holder.msgFrom.setVisibility(View.VISIBLE);
                holder.msgYou.setVisibility(View.GONE);
            }else{
                holder.lblMsgYou.setText("You");
                holder.txtMsgYou.setText(song.get(Function.KEY_MSG));
                holder.timeMsgYou.setText(song.get(Function.KEY_TIME));
                holder.msgFrom.setVisibility(View.GONE);
                holder.msgYou.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {}
        return convertView;
    }
}


class ChatViewHolder {
    LinearLayout msgFrom, msgYou;
    TextView txtMsgYou, lblMsgYou, timeMsgYou, lblMsgFrom, txtMsgFrom, timeMsgFrom;
}

