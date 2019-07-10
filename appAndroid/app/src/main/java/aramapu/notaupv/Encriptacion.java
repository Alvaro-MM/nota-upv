package aramapu.notaupv;


import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Clase encargada de encriptar y desencriptar un texto a partir de la informacion almacenada por
 * claves generadas usando android.
 *
 * @author Mario Aragones Lozano
 */
public class Encriptacion {

    private final String NOMBRE_CLAVE = "NotaUPV";
    private KeyStore almacenClaves;

    /**
     * Constructor. Es el encaragdo de obtener el almacen de claves de Android.
     *
     */
    Encriptacion(){

        try {
            almacenClaves = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            Log.w("Encriptacion-generateKey",e);
        }

    }

    /**
     * Metodo encargado de crear la clave NOMBRE_CLAVE y almacenarla en el almacen.
     *
     */
    @TargetApi(Build.VERSION_CODES.N)
    protected void generateKey() {

        KeyGenerator genClaves;

        try {
            //Sera una clave con algoritmo AES
            genClaves = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Error al obtener la instancia del generador de claves", e);
        }

        try {
            almacenClaves.load(null);

            //La clave podra ser usada para encriptar y desencriptar. Modo NIST CBC
            genClaves
                    .init(new KeyGenParameterSpec.Builder(NOMBRE_CLAVE,KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            genClaves.generateKey();

        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Metodo encargado de encriptar la informacion
     *
     * @param textToEncrypt String a encriptar
     * @return Cadena de bytes con Vector de inicializacion + datos a desencriptar
     */
    byte[] encryptText(final String textToEncrypt){

        Cipher cipher;

        try {
            //Se inicializa el cipher (Algoritmo de cifrado bloque) para AES en modo CBC
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Error al obtener el cipher", e);
        }

        try{

            //Se obtiene la clave del almacen
            almacenClaves.load(null);
            SecretKey claveSecreta = (SecretKey) almacenClaves.getKey(NOMBRE_CLAVE,null);

            cipher.init(Cipher.ENCRYPT_MODE, claveSecreta );

            byte[] iv = cipher.getIV();
            byte[] textoCodificado = cipher.doFinal(textToEncrypt.getBytes());

            //Se envia al usuario el IV concatenado con el texto cifrado
            byte[] textoEnviar = new byte[iv.length+textoCodificado.length];

            System.arraycopy(iv,0,textoEnviar,0,iv.length);
            System.arraycopy(textoCodificado,0,textoEnviar,iv.length,textoCodificado.length);

            return textoEnviar;

        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error al inicializar el cipher", e);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("Error al encriptar", e);
        }

    }

    /**
     *  Metodo encargado de desencriptar la informacion
     *
     * @param encryptedData Vector de inicializacion + datos a desencriptar
     * @return Datos desencriptados
     */
    String decryptData(final byte[] encryptedData){

        Cipher cipher;

        try {
            //Se inicializa el cipher (Algoritmo de cifrado bloque) para AES en modo CBC
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Error al obtener el cipher", e);
        }

        try{

            //Se obtiene la clave del almacen
            almacenClaves.load(null);
            SecretKey claveSecreta = (SecretKey) almacenClaves.getKey(NOMBRE_CLAVE,null);

            //El vector de inicializacion siempre son 16 bytes -> 128 bits, en esta codificacion
            byte[] iv = Arrays.copyOfRange(encryptedData,0,16);
            byte[] dataEncriptada = Arrays.copyOfRange(encryptedData,16,encryptedData.length);

            cipher.init(Cipher.DECRYPT_MODE, claveSecreta, new IvParameterSpec(iv));

            return new String(cipher.doFinal(dataEncriptada),"UTF-8");

        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Error al inicializar el cipher", e);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("Error al desencriptar", e);
        }

    }

}
