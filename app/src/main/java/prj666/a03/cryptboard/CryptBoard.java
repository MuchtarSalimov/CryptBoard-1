package prj666.a03.cryptboard;


import android.annotation.TargetApi;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import prj666.a03.cryptboard.RSAStrings.RSAStrings;

public class CryptBoard extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener{

    private static final int KEYCODE_ENCRYPT = -100; // ENCRYPT KEYCODE_CAMERA
    private static final int KEYCODE_PHOTO = -101; // SEND
    private static final int KEYCODE_DECRYPT = -102; // TOGGLE
    private static final int KEYCODE_CONTACTS = -104;
    private static final int KEYCODE_CLEAR = -105;
    private static final int KEYCODE_MATH_MODE = -200;

    private KeyboardView keyboardView;
    private Keyboard keyboard;
    private Keyboard keyboardNum;
    private Keyboard keyboardNormal;
    private Keyboard keyboardNumNormal;

    private InputConnection ic;
    private boolean caps = false;
    private boolean capsLock = false;
    private boolean numMode = false;
    private boolean stegMode = true;

    private String text = "";
    private String decryptedText = "";
    private String encryptedText = "";
    private String Stringpubkey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAurl9C573/toKc8XCi+jwQpxzwemUKbxdpxX0+l1MUm7LdPNzDYQeUU+SSdOC5xIQ646nyzRDKZjrCPALIaeXxAbHaiDqX22ab3BA+pSDZuD39KkWhiZuhmXZPY9uQJYlOjPy63pH7LeM03ZQQNiMZz8oXT94u+yeKxU77uHHecBFxGQEzswnXRGUeUYgz5FALrX1LZJG3IY0W7Rr5gvxWQxjA6E9kFG05cY34DFazElc/Z3cv90cle6lNb0szeHIIMAvIYj+gMUcwzVzsOMDVPZVHuglWvxD5jrOc0aq2tiJgqzlvAVTFS5qlwovGtwN34W71FLwscp2GEeryeCH2QIDAQAB";
    private String Stringprivkey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC6uX0Lnvf+2gpzxcKL6PBCnHPB6ZQpvF2nFfT6XUxSbst083MNhB5RT5JJ04LnEhDrjqfLNEMpmOsI8Ashp5fEBsdqIOpfbZpvcED6lINm4Pf0qRaGJm6GZdk9j25AliU6M/Lrekfst4zTdlBA2IxnPyhdP3i77J4rFTvu4cd5wEXEZATOzCddEZR5RiDPkUAutfUtkkbchjRbtGvmC/FZDGMDoT2QUbTlxjfgMVrMSVz9ndy/3RyV7qU1vSzN4cggwC8hiP6AxRzDNXOw4wNU9lUe6CVa/EPmOs5zRqra2ImCrOW8BVMVLmqXCi8a3A3fhbvUUvCxynYYR6vJ4IfZAgMBAAECggEAO4b4y2ShkRi37lKkg+/98G5qJO6vMmL/xE2mrM5jj4AM0rrt+egdtjRU4b5RZBMJW989tPVzV+aNP2svUUpZgr/agQX/Ue7iJha2UGxaO8kKo2/oY1oLMEN17z8zmdmEArse/V0dYuTdO2jjiti+YgfreVbLybVUc02wrqZB7pkGvsSMx8jPT10OLjKDUJm9/Ixf5lXwCWJ4HUcTowGo9AJ4rbnatffCjOTZvcOE67ofnY/PxbiQWcjwaA578Gab0/6myJtOICmjDZzrJIbWtH9RgJAfFkpDcKHo1DMx8XQDxN6BBkKvzi00NLkUniOnmIWtmE7/JTJYJUa2KzqdZQKBgQDoBPUcpYqfoikGAhAUaIbEMEtfwsGbulIKL3r10ZLN5Wq+PCPpuzD8etqxNy7h7CY7abYECiuVWrYsiv6vibZ3SDCSKA3ZdX5d0wK+OH1z8SAfoBcU++u6OdRscN41kEYQ3DFsR0UfI9ZiVIGz7G1aXpZG09Rd9+ySqbagSfiO/wKBgQDOBhFP/PqfGCLl76LvCwuhAZwkJIWtFxJqA3Tp1QlIPSyeti6SmzogZmsVUDKILSiRN8VTc+2RDcumo9KSZ2oC2UpQPmDHnCIVH0YrYwci6S9TCeEoA7C8UOQUbDMIowbCy0ll6mHazfUV2Wlz4D5H68PfPBwdj7XztVp3C0xBJwKBgQCrDHSToOsYkpkBx+WI1iJ8Yko/F7paDztKLQTOUqmSx04xXu7u8kTD7eJqAY+7mLf61w0L31+QJSbmobXvPWxadcrxBTxok7kMfHKqP8UlA5+2EPTTUIHRca7MH02CWZF9/oclF0m7ElWLeleAiI15sP/CyYnnmM48tYdglgf7iwKBgQCdeG3LIaW97IjgDyYOZ/bffYeG6JN0FWpxtWqrP7X0jS2Jsd4vGI55LU8z3zSAeWPEe0hL3RP8Bvtdx2GvnXOd8c+nPcZjS6eRVXIgv3Q47trJMYfzOb7gcUOjiIAJXfJQ+WiEiX157Goj5SWA+Ckid8Yi3qLuxWVhfYBD9VK3iQKBgHa+th8etmPmivviplBtHJ23Y3a6VDoGYr427oB8k3PsalO1LDi0V4JUf7cgtRXiZaGr8R4pQuj+tu8wd1UCpv7wdXEnOOpwNM8gjt9veLSfxy7M8vI2B3V9j7G3fztpBB2GeFsxqp+a9mcx8VPdrGk+azaseRazgGG7FEJiVKUs";
    private RSAPublicKey  publicKey =  null;

    private PrivateKey privateKey = null;


    KeyFactory rsaKeyFac;
    {
        try {
            rsaKeyFac = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(android.util.Base64.decode(Stringpubkey,0));
            publicKey = (RSAPublicKey)rsaKeyFac.generatePublic(keySpec);

            KeyFactory rsaKeyFac = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(android.util.Base64.decode(Stringprivkey,0));
            privateKey = (RSAPrivateKey)rsaKeyFac.generatePrivate(encodedKeySpec);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }







    long currentTime = 0;

    @Override

    public View onCreateInputView() {
        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        keyboardNum = new Keyboard(this, R.xml.alt_qwerty);
        keyboardNormal = new Keyboard(this, R.xml.qwerty_normal);
        keyboardNumNormal = new Keyboard(this, R.xml.alt_qwerty_normal);

        keyboardView.setPreviewEnabled(false);
        keyboardView.setKeyboard(keyboardNormal);
        keyboardView.setOnKeyboardActionListener(this);

        return keyboardView;
    }

    private void playClick(int keyCode) {
/*        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch(keyCode) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }*/
    }
    @Override
    @TargetApi(25)
    public void onPress(int primaryCode) {
        ic = getCurrentInputConnection();
        playClick(primaryCode);
        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1,0);
                break;
            case Keyboard.KEYCODE_SHIFT:
                if (capsLock) {
                    capsLock = false;
                    caps = false;
                    keyboardView.getKeyboard().setShifted(caps);
                    keyboardView.invalidateAllKeys();
                }
                else {
                    if (caps) {
                        capsLock = true;
                    }
                    else {
                        caps = true;
                        keyboardView.getKeyboard().setShifted(caps);
                        keyboardView.invalidateAllKeys();
                    }
                }
                break;
            case Keyboard.KEYCODE_MODE_CHANGE:
                caps = false;
                capsLock = false;
                keyboardView.getKeyboard().setShifted(caps);

                numMode = !numMode;
                if (stegMode) {
                    if (numMode)
                        keyboardView.setKeyboard(keyboardNum);
                    else
                        keyboardView.setKeyboard(keyboard);
                }
                else{
                    if (numMode)
                        keyboardView.setKeyboard(keyboardNumNormal);
                    else
                        keyboardView.setKeyboard(keyboardNormal);
                }
                keyboardView.invalidateAllKeys();
                break;

            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                currentTime = Calendar.getInstance().getTimeInMillis();
                break;
            case  KEYCODE_ENCRYPT:
                EncryptMessage();
                break;
            case KEYCODE_PHOTO:
                getMessage();
                break;
            case KEYCODE_DECRYPT:
                DecryptMessage();
                break;
            case KEYCODE_CONTACTS:
                Intent contacts = new Intent(this, Contact_List_Main.class);
                startActivity(contacts);
                break;
            case KEYCODE_CLEAR:
                clearMessage();
                break;
            case KEYCODE_MATH_MODE:
                break;
            default:
                char code = (char) primaryCode;
                if(Character.isLetter(code) && caps) {
                    code = Character.toUpperCase(code);
                }
                ic.commitText(String.valueOf(code),1);
                if (!capsLock && caps){
                    caps = false;
                    keyboardView.getKeyboard().setShifted(caps);
                    keyboardView.invalidateAllKeys();
                }
        }

    }

    @Override
    public void onRelease(int primaryCode) {
        // this isnt working
/*        if (primaryCode == Keyboard.KEYCODE_DONE){
            if (stegMode) {
                if (numMode) {
                    keyboardView.setKeyboard(keyboardNumNormal);
                }
                else {
                    keyboardView.setKeyboard(keyboardNormal);
                }
                keyboardView.invalidateAllKeys();
                stegMode = false;
            }
            else {
                if (numMode) {
                    keyboardView.setKeyboard(keyboardNum);
                } else {
                    keyboardView.setKeyboard(keyboard);
                }
                keyboardView.invalidateAllKeys();
                stegMode = true;
            }
        }*/
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeDown() {
        if (stegMode) {
            if (numMode){
                keyboardView.setKeyboard(keyboardNumNormal);
            }
            else{
                keyboardView.setKeyboard(keyboardNormal);
            }
            keyboardView.invalidateAllKeys();
            stegMode = false;
        }
    }

    @Override
    public void swipeUp() {
        if (numMode){
            keyboardView.setKeyboard(keyboardNum);
        }
        else{
            keyboardView.setKeyboard(keyboard);
        }
        keyboardView.invalidateAllKeys();
        stegMode = true;
    }

/*    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", "test");
        return Uri.parse(path);
    }

    public void commitImage(Uri contentUri, String imageDescription) {
        InputContentInfoCompat inputContentInfo = new InputContentInfoCompat(
                contentUri,
                new ClipDescription(imageDescription, new String[]{"image/gif"}),
                null
        );
        InputConnection inputConnection = ic;
        EditorInfo editorInfo = getCurrentInputEditorInfo();
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            flags |= InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
        }
        InputConnectionCompat.commitContent(
                inputConnection, editorInfo, inputContentInfo, flags, null);
    }

    public void saveAndGetImage(Context inContext, Bitmap inImage){

        try (FileOutputStream out = new FileOutputStream("test.jpeg")) {
            inImage.compress(Bitmap.CompressFormat.PNG, 100, out);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, "Title", "test");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/

    private void getMessage(){
        text = ic.getExtractedText(new ExtractedTextRequest(), 0).text.toString();
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void clearMessage(){
        CharSequence currentText = ic.getExtractedText(new ExtractedTextRequest(), 0).text;
        CharSequence beforeCursorText = ic.getTextBeforeCursor(currentText.length(), 0);
        CharSequence afterCursorText = ic.getTextAfterCursor(currentText.length(), 0);
        ic.deleteSurroundingText(beforeCursorText.length(), afterCursorText.length());
    }

    private void EncryptMessage(){
        try {
            getMessage();
            encryptedText =  android.util.Base64.encodeToString(RSAStrings.encryptString(publicKey, text.trim()),0);
            setMessage(encryptedText);
        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void DecryptMessage(){
        try {
            getMessage();
            byte [] decrypted = RSAStrings.decryptString(privateKey,android.util.Base64.decode(text.trim().getBytes(),0));
            setMessage(new String(decrypted));
            Toast.makeText(this, new String(decrypted), Toast.LENGTH_LONG).show();
        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setMessage(String message){
        clearMessage();
        ic.commitText(message,1);
    }


/*    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }*/
}
