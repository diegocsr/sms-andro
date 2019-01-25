package com.androstock.smsapp;

import java.util.Arrays;

public class DES {

    public DES(){

    }

    //untuk hexa 32 = 0A ==0
    private static String H0 = "0A00000000000000000000000000000000";
    //untuk 64 bit string
    private static String B0 = "0000000000000000000000000000000000000000000000000000000000000000";

    //tabel IP untuk enkripsi dan dekripsi 64 bit
    private static int[] IP = new int[]{
            58, 50, 42, 34, 26, 18, 10, 2,
            60, 52, 44, 36, 28, 20, 12, 4,
            62, 54, 46, 38, 30, 22, 14, 6,
            64, 56, 48, 40, 32, 24, 16, 8,
            57, 49, 41, 33, 25, 17, 9, 1,
            59, 51, 43, 35, 27, 19, 11, 3,
            61, 53, 45, 37, 29, 21, 13, 5,
            63, 55, 47, 39, 31, 23, 15, 7
    };

    //tabel PC1 untuk generate kunci 58 bit
    private static int[] PC1 = new int[]{
            57, 49, 41, 33, 25, 17, 9,
            1, 58, 50, 42, 34, 26, 18,
            10, 2, 59, 51, 43, 35, 27,
            19, 11, 3, 60, 52, 44, 36,
            63, 55, 47, 39, 31, 23, 15,
            7, 62, 54, 46, 38, 30, 22,
            14, 6, 61, 53, 45, 37, 29,
            21, 13, 5, 28, 20, 12, 4
    };

    //tabel pergeseran bit CiDi untuk generate kunci
    private static int[] pergeseranBit = new int[]{1,1,2,2,2,2,2,2,1,2,2,2,2,2,2,1};

    //tabel PC2 untuk generate kunci 48 bit
    private static int[] PC2 = new int[]{
            14, 17, 11, 24, 1, 5,
            3, 28, 15, 6, 21, 10,
            23, 19, 12, 4, 26, 8,
            16, 7, 27, 20, 13, 2,
            41, 52, 31, 37, 47, 55,
            30, 40, 51, 45, 33, 48,
            44, 49, 39, 56, 34, 53,
            46, 42, 50, 36, 29, 32
    };

    public char[][] generateKunci(String kunciHexa) {
        //konversi kunci dari ascii ke hexa
        String kunciBit = Konversi.hexaToBin(kunciHexa);
        // validasi kunci harus 64 bit
        if (kunciBit.length() != 64) {
            System.out.println("Panjang key harus 8 digit/karakter");
            return null;
        }
        // kunci 64 bit dipermutasi menggunakan PC1 menjadi 56 bit
        char[] arKunci64Bit = kunciBit.toCharArray();
        char[] arKunci56Bit = new char[56];
        for (int i=0; i<56; i++) {
            arKunci56Bit[i] = arKunci64Bit[PC1[i]-1];
        }
        //56 bit kunci dibagi menjadi 2, yaitu C0(bit ke 1-28) dan D0(bit 29-58)
        StringBuffer C0 = new StringBuffer(new String(arKunci56Bit, 0, 28));
        StringBuffer D0 = new StringBuffer(new String(arKunci56Bit, 28, 28));
        // genetate 16 kunci 48 bit
        char[][] arKunci = new char[16][48];
        for (int i=0; i<16;  i++) {
            // menggeser CiDi sesuai dengan tabel pergeseran bit
            for (int j=0; j<pergeseranBit[i]; j++) {
                C0.append(C0.charAt(0));
                C0.deleteCharAt(0);
                D0.append(D0.charAt(0));
                D0.deleteCharAt(0);
            }
            // permutasi setiap CiDi dari 56 bit ke 48 bit menggunakan tabel PC2
            char[] arCiDi = (C0.toString() + D0.toString()).toCharArray();
            for (int j=0; j<48; j++) {
                arKunci[i][j] = arCiDi[PC2[j]-1];
            }
        }
        return arKunci;
    }

    public String enkripsi(String pesanHexa, char[][]arKunci) {
        //cek panjang pesan(sudah dalam bentuk hexa)
        int panjangPesanHexa = pesanHexa.length();
        //cek panjang pesan apakah habis dibagi 16 atau tidak
        if (panjangPesanHexa % 16 != 0) {
            //jika tidak habis dibagi 16 maka ditambah dg bit H0 agar habis dibagi 16
            pesanHexa = pesanHexa+ H0.substring(0, 16 - (panjangPesanHexa % 16));
        }
        //konversi hexa ke binary
        String pesanBit = Konversi.hexaToBin(pesanHexa);
        //cek pangjang bit
        int panjangPesanBit = pesanBit.length();
        //untuk menyimpan hasil
        StringBuffer cipher = new StringBuffer();
        //enkripsi setiap 64 bit blok
        for (int i=0; i<panjangPesanBit; i+=64) {
            // diambil 64-bit pertama
            char[] sub64BitPesan = pesanBit.substring(i, i+64).toCharArray();
            // permutasi awal 64bit pesan menggunakan tabel IP sehingga menghasilkan IP(X)
            char[] ipX = new char[64];
            for (int j=0; j<64; j++) {
                ipX[j] = sub64BitPesan[IP[j]-1];
            }
            // IP(X) dibagi menjadi 2, yaitu L(bit ke 1-32) dan R(bit ke 33-64)
            char[][] L = new char[17][];
            char[][] R = new char[17][];
            L[0] = Arrays.copyOfRange(ipX, 0, 32);
            R[0] = Arrays.copyOfRange(ipX, 32, 64);
            // 16 iterasi untuk menghasilkan L16 dan R16
            for (int j=1; j<=16; j++) {
                // L(n) = R(n-1)
                L[j] = R[j-1];
                // R(n) = L(n-1) + f(R(n-1),K(n))
                // f(R(n-1),K(n)) = ER + K(n)
                // ER = menambah bit dari 32 menjadi 48
                char[] ER = ekspansiR(R[j-1]);
                // ER + K(n) = XOR(ER, K(n)), K(n) adalah key yang dihasikan dari proses sebelumnya
                for (int m=0; m<48; m++) {
                    if (ER[m] == arKunci[j-1][m]) ER[m] = '0';
                    else ER[m] = '1';
                }
                // proses S-Boxes (merubah 6-bit menjadi 4-bit)
                StringBuffer Ai = new StringBuffer();
                for (int m=0; m<48; m+=6) {
                    // diambil setiap 6-bit, dan setiap 6-bit diberi index
                    Ai.append(SBox(Arrays.copyOfRange(ER, m, m+6), m/6));
                }
                char[] Bi = Ai.toString().toCharArray();
                // permutasi akhir dari, 32-bit data
                char[] pBi = new char[32];
                for (int m=0; m<32; m++) {
                    pBi[m] = Bi[P[m]-1];
                }
                // R(n) = L(n-1) + f(R(n-1),K(n)) == XOR L(n-1) dengan hasil permutasi akhir
                R[j] = new char[32];
                for (int m=0; m<32; m++) {
                    if (L[j-1][m] == pBi[m]) R[j][m] = '0';
                    else R[j][m] = '1';
                }
            }
            // hasil akhir L dan R dibalik jadi R dan L serta digabung menjadi 64 bit
            char[] RL = new StringBuffer().append(R[16]).append(L[16]).toString().toCharArray();
            // permutasi akhir untuk gabungan bit R dan L (64-bit)
            char[] IP_1X = new char[64];
            for (int j=0; j<64; j++) {
                IP_1X[j] = RL[IP_1[j]-1];
            }
            // dikembalikan ke bentuk hexa
            cipher.append(Konversi.binToHexa(new String(IP_1X)));
        }
        return cipher.toString();
    }

    public String dekripsi(String cipherHexa, char[][]arKunci) {
        //cek panjang cipher(berupa hexa)
        int panjangCipherHexa = cipherHexa.length();
        //konversi cipher ke binary
        String cipherBit = Konversi.hexaToBin(cipherHexa);
        if (panjangCipherHexa% 16 != 0) {
            //jika tidak habis dibagi 16 maka ditambah dg bit H0 agar habis dibagi 16
            cipherHexa = cipherHexa + H0.substring(0, 16 - (panjangCipherHexa % 16));
        }
        //cek panjang cipher(berupa binary)
        int panjangCipherBit = cipherBit.length();
        //untuk menyimpan plaintext sementara
        StringBuffer plaintext = new StringBuffer();
        // proses sama dengan encrypt tapi prosesnya dibalik
        for (int i=0; i<panjangCipherBit; i+=64) {
            // decode setiap 64-bit blok data
            char[] CIP = cipherBit.substring(i, i+64).toCharArray();
            // permutasi akhir untuk gabungan bit R dan L (64-bit)
            char[] RL = new char[64];
            for (int j=0; j<64; j++) {
                RL[IP_1[j]-1] = CIP[j];
            }
            // 64-bit data dibagi 2 Right dan Left masing2 32-bit
            // 16 iterasi terbalik untuk menghasilkan L0 dan R0
            char[][] L = new char[17][];
            char[][] R = new char[17][];
            L[16] = Arrays.copyOfRange(RL, 32, 64);
            R[16] = Arrays.copyOfRange(RL, 0, 32);
            for (int j=16; j>=1; j--) {
                // R(n-1) = L(n)
                R[j-1] = L[j];
                // R(n) = L(n-1) + f(R(n-1),K(n))
                // f(R(n-1),K(n)) = ER + K(n)
                // ER = bit selection
                char[] ER = ekspansiR(R[j-1]);
                // ER + K(n) = XOR(ER, K(n))
                for (int m=0; m<48; m++) {
                    if (ER[m] == arKunci[j-1][m]) ER[m] = '0';
                    else ER[m] = '1';
                }

                // proses S-Boxes (merubah 6-bit menjadi 4-bit)
                StringBuffer Ai = new StringBuffer();
                for (int m=0; m<48; m+=6) {
                    Ai.append(SBox(Arrays.copyOfRange(ER, m, m+6), m/6));
                }
                char[] Bi = Ai.toString().toCharArray();
                // permutasi akhir dari, 32-bit data
                char[] pBi = new char[32];
                for (int m=0; m<32; m++) {
                    pBi[m] = Bi[P[m]-1];
                }
                // L(n-1) = R(n) + f(R(n-1),K(n)) == XOR R(n) dengan hasil permutasi akhir
                L[j-1] = new char[32];
                for (int m=0; m<32; m++) {
                    if (R[j][m] == pBi[m]) L[j-1][m] = '0';
                    else L[j-1][m] = '1';
                }
            }
            // inisial permutasi 64-bit data
            // sama dengan proses awal enkripsi
            char[] ipMsg = new StringBuffer().append(L[0]).append(R[0]).toString().toCharArray();
            char[] subPlaintext = new char[64];
            for (int j=0; j<64; j++) {
                subPlaintext[IP[j]-1] = ipMsg[j];
            }
            plaintext.append(subPlaintext);
        }
        // dikembalikan ke bentuk hexa
        return Konversi.binToHexa(new String(plaintext)).replaceAll("0A[0]+$", "");
    }

    private char[] ekspansiR(char[] R) {
        char[] ER = new char[48];
        for (int i=0; i<48; i++) {
            ER[i] = R[E[i]-1];
        }
        return ER;
    }
    private static int[] E = new int[]{
            32, 1, 2, 3, 4, 5,
            4, 5, 6, 7, 8, 9,
            8, 9, 10, 11, 12, 13,
            12, 13, 14, 15, 16, 17,
            16, 17, 18, 19, 20, 21,
            20, 21, 22, 23, 24, 25,
            24, 25, 26, 27, 28, 29,
            28, 29, 30, 31, 32, 1
    };

    private char[] SBox(char[] B, int index) {
        int baris = Integer.parseInt(new String(new char[]{B[0], B[5]}), 2);
        int kolom = Integer.parseInt(new String(B, 1, 4), 2);
        String hasil = "";
        switch (index) {
            case 0:
                hasil = Integer.toBinaryString(S1[baris][kolom]);
                break;
            case 1:
                hasil = Integer.toBinaryString(S2[baris][kolom]);
                break;
            case 2:
                hasil = Integer.toBinaryString(S3[baris][kolom]);
                break;
            case 3:
                hasil = Integer.toBinaryString(S4[baris][kolom]);
                break;
            case 4:
                hasil = Integer.toBinaryString(S5[baris][kolom]);
                break;
            case 5:
                hasil = Integer.toBinaryString(S6[baris][kolom]);
                break;
            case 6:
                hasil = Integer.toBinaryString(S7[baris][kolom]);
                break;
            case 7:
                hasil = Integer.toBinaryString(S8[baris][kolom]);
                break;
            default :
                return null;
        }

        hasil = B0.substring(0, 4-hasil.length()) + hasil;
        return hasil.toCharArray();
    }

    private static int[][] S1 = new int[][]{
            {14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7},
            {0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8},
            {4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0},
            {15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13}
    };

    private static int[][] S2 = new int[][]{
            {15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10},
            {3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5},
            {0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15},
            {13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9}
    };

    private static int[][] S3 = new int[][]{
            {10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8},
            {13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1},
            {13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7},
            {1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12}
    };

    private static int[][] S4 = new int[][]{
            {7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15},
            {13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9},
            {10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4},
            {3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14}
    };

    private static int[][] S5 = new int[][]{
            {2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9},
            {14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6},
            {4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14},
            {11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3}
    };

    private static int[][] S6 = new int[][]{
            {12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11},
            {10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8},
            {9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6},
            {4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13}
    };

    private static int[][] S7 = new int[][]{
            {4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1},
            {13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6},
            {1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2},
            {6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12}
    };

    private static int[][] S8 = new int[][]{
            {13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7},
            {1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2},
            {7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8},
            {2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11}
    };

    private static int[] P = new int[] {
            16, 7, 20, 21,
            29, 12, 28, 17,
            1, 15, 23, 26,
            5, 18, 31, 10,
            2, 8, 24, 14,
            32, 27, 3, 9,
            19, 13, 30, 6,
            22, 11, 4, 25
    };

    private static int[] IP_1 = new int[] {
            40, 8, 48, 16, 56, 24, 64, 32,
            39, 7, 47, 15, 55, 23, 63, 31,
            38, 6, 46, 14, 54, 22, 62, 30,
            37, 5, 45, 13, 53, 21, 61, 29,
            36, 4, 44, 12, 52, 20, 60, 28,
            35, 3, 43, 11, 51, 19, 59, 27,
            34, 2, 42, 10, 50, 18, 58, 26,
            33, 1, 41, 9, 49, 17, 57, 25
    };



}