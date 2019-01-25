package com.androstock.smsapp;

public class RailFence {

    public RailFence(){

    }

    public String enkripsi(String pesan, int kunci) {
// hitung panjang pesan
        int panjangpsn = pesan.length();
// membuat “rel” dengan panjang kunci sebagai baris dan panjang pesan sebagai kolom
        char rail[][] = new char[kunci][panjangpsn];
// rel diisi kosong
        for (int i=0; i<kunci; i++) {
            for (int j=0; j<panjangpsn; j++) {
                rail[i][j] = '\n';
            }
        }
// mendeklarasikan nilai batas bawah rel bernilai false
        boolean turun = false;
        int baris = 0, kolom = 0;
// mengisi rel dengan Plaintext
        for (int i=0; i<panjangpsn; i++) {
            //salah satu true maka true
//jika baris bernilai sama dengan 0, maka turun bernilai false dan jika baris bernilai sama dengan kunci-1 maka turun bernilai true, || = operator OR
            if (baris==0 || baris==kunci-1) {
                turun = !turun;
                //f atau t
            }
//mengisi karakter pesan pada rel sesuai dengan baris dan kolom, kemudian menambah nilai kolom berikutnya
            rail[baris][kolom++] = pesan.charAt(i);
// mengecek apakah nilai turun bernilai false atau tidak
            if (turun) {
                baris++;
            } else {
                baris--;
            }
        }
// menyimpan cipher
        StringBuffer cipher = new StringBuffer();
// membaca dan menulis cipher pada rel dengan urutan baris dan tidak kosong
        for (int i=0; i<kunci; i++) {
            for (int j=0; j<panjangpsn; j++) {
                if (rail[i][j] != '\n') {
                    cipher.append(rail[i][j]);
                }
            }
        }
        return cipher.toString();
    }
    public String dekripsi(String cipher, int kunci){
        //hitung panjang cipher
        int panjangCipher = cipher.length();
        // membuat “rel” dengan panjang kunci sebagai baris dan panjang pesan sebagai kolom
        char rail[][] = new char[kunci][panjangCipher];
        // rel diisi kosong
        for (int i=0; i<kunci; i++){
            for (int j=0; j<panjangCipher; j++){
                rail[i][j]='\n';
            }
        }
        // mendeklarasikan nilai batas bawah rel bernilai false
        boolean turun = false;
        int baris = 0, kolom = 0;
        // mengisi rel # secara diagonal
        for (int i=0; i<panjangCipher; i++){
            if (baris==0 || baris==kunci-1){
                turun = !turun;
            }
            rail[baris][kolom++] = '#';
            if (turun){
                baris++;
            }else {
                baris--;
            }
        }
        //mengganti # dengan cipher
        int index =0;
        for (int i=0; i<kunci; i++){
            for (int j=0; j<panjangCipher; j++){
                if (rail[i][j] == '#' && index < panjangCipher){
                    rail[i][j] = cipher.charAt(index++);
                }
            }
        }
        //membaca cipher secara diagonal zig zag
        turun = false;
        baris =0; kolom = 0;
        StringBuffer plaintext = new StringBuffer();
        for (int i=0; i<panjangCipher; i++){
            if (baris==0 || baris==kunci-1){
                turun = !turun;
            }
            if (rail[baris][kolom] != '\n'){
                plaintext.append(rail[baris][kolom]);
            }
            kolom++;

            if (turun){
                baris++;
            }else {
                baris--;
            }
        }

        return plaintext.toString();
    }
}
