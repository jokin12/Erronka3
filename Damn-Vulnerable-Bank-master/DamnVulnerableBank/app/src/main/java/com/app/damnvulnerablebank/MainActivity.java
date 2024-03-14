package com.app.damnvulnerablebank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private Toast toast;
    static final int KEY_SIZE = MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE;
    static final String MASTER_KEY_ALIAS = MasterKey.DEFAULT_MASTER_KEY_ALIAS;
    private MasterKey masterKey;

    private static final int INTERNET_PERMISSION_REQUEST_CODE = 100;
    private static final int BIOMETRIC_PERMISSION_REQUEST_CODE = 101;
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        MainActivity.super.onBackPressed();
                        System.exit(0);
                    }
                }).create().show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_banklogin);


        // Verificar si el permiso de Internet está concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            // Si no está concedido, solicitar el permiso
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    INTERNET_PERMISSION_REQUEST_CODE);
        }

        // Verificar si el permiso biométrico está concedido (para versiones de Android 28 y posteriores)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC)
                        != PackageManager.PERMISSION_GRANTED) {
            // Si no está concedido, solicitar el permiso
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.USE_BIOMETRIC},
                    BIOMETRIC_PERMISSION_REQUEST_CODE);
        }


        try {
            EncryptDecrypt.generateRSAKeys();
        } catch (Exception e) {
            e.printStackTrace();
        }
       boolean isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
       FridaCheckJNI fridaCheck = new FridaCheckJNI();


       if(android.os.Debug.isDebuggerConnected()){
            Toast.makeText(getApplicationContext(), "Debug from vm",Toast.LENGTH_LONG).show();
        }

        if(EmulatorDetectortest.isEmulator()){
            Toast.makeText(getApplicationContext(), "Emulator Detected",Toast.LENGTH_LONG).show();
        }

        if(isDebuggable){
            Toast.makeText(getApplicationContext(),"Debbuger is Running", Toast.LENGTH_SHORT).show();
        }

        if(RootUtil.isDeviceRooted()) {
            Toast.makeText(getApplicationContext(), "Phone is Rooted", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Check frida

        if(fridaCheck.fridaCheck() == 1) {
            Toast.makeText(getApplicationContext(), "Frida is running", Toast.LENGTH_SHORT).show();
            Log.d("FRIDA CHECK", "FRIDA Server DETECTED");

            finish();
        } else {
            Log.d("FRIDA CHECK", "FRIDA Server NOT RUNNING");
            Toast.makeText(getApplicationContext(), "Frida is NOT running", Toast.LENGTH_SHORT).show();
        }





        SharedPreferences sharedPreferences = getSharedPreferences("jwt", Context.MODE_PRIVATE);
        boolean isloggedin=sharedPreferences.getBoolean("isloggedin", false);
        if(isloggedin)
        {
            startActivity(new Intent(getApplicationContext(), Dashboard.class));
            finish();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == INTERNET_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso de Internet concedido
                Toast.makeText(this, "Permiso de Internet concedido", Toast.LENGTH_SHORT).show();
            } else {
                // Permiso de Internet denegado
                Toast.makeText(this, "Permiso de Internet denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == BIOMETRIC_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso biométrico concedido
                Toast.makeText(this, "Permiso biométrico concedido", Toast.LENGTH_SHORT).show();
            } else {
                // Permiso biométrico denegado
                Toast.makeText(this, "Permiso biométrico denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void loginPage(View view){
        Intent intent =new Intent(getApplicationContext(), BankLogin.class);
        startActivity(intent);
    }

    public void signupPage(View view){
        Intent intent =new Intent(getApplicationContext(), RegisterBank.class);
        startActivity(intent);
    }

    public void healthCheck(View v){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("apiurl", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        EditText ed=findViewById(R.id.apiurl);
        final String api =ed.getText().toString().trim();

        // Encriptar la URL de la API y guardarla en SharedPreferences
        try {
            String encryptedApi = EncryptDecrypt.encrypt(api);
            editor.putString("apiurl", encryptedApi);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
            return; // Si hay un error al encriptar, sal de la función
        }

        // Obtener la URL de la API encriptada de SharedPreferences
        final String encryptedUrl = pref.getString("apiurl", null);

        if (encryptedUrl == null) {
            // Si no se encuentra la URL de la API en SharedPreferences, sal de la función
            return;
        }

        // Desencriptar la URL de la API
        final String url;
        try {
            url = EncryptDecrypt.decrypt(encryptedUrl);
            EditText urlEditText = findViewById(R.id.ip); // Suponiendo que tienes un EditText en tu diseño para mostrar la URL
            urlEditText.setText(url);

        } catch (Exception e) {
            e.printStackTrace();
            return; // Si hay un error al desencriptar, sal de la función
        }

        // Crear la URL final
        String endpoint="/api/health/check";
        String finalurl = url + endpoint;

        // Hacer la solicitud a la URL final
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, finalurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Si la respuesta es exitosa, actualiza el botón
                        Button bButton = findViewById(R.id.healthc);
                        bButton.setText("Api is Up");
                        bButton.setTextColor(Color.GREEN);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Si hay un error en la respuesta, actualiza el botón
                Button bButton = findViewById(R.id.healthc);
                bButton.setText("Api is Down");
                bButton.setTextColor(Color.RED);
            }
        });
        queue.add(stringRequest);
        queue.getCache().clear();
    }
}