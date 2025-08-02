package com.example.finances;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.VIBRATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.ScanMode;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    //widget
    ViewFlipper viewFlipper;
    RequestQueue requestQueue;
    CodeScannerView scanView;
    CodeScanner scanner;
    Button valider;
    TextView validerTV;
    EditText mdp, confirmation;

    // variable et constante
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        valider = findViewById(R.id.validerBTN);
        viewFlipper = findViewById(R.id.financeVF);
        requestQueue = Volley.newRequestQueue(this);
        validerTV = findViewById(R.id.validerTV);
        mdp = findViewById(R.id.mdp);
        confirmation = findViewById(R.id.confirmation);

        if(!checkPermission()) {
            Toast.makeText(this, "Vous devez accepter les permissions requise pour utiliser l'application", Toast.LENGTH_LONG).show();
            requestPermisssion();
        }
        else{
            scanView = findViewById(R.id.camVIEW);
            scanner = new CodeScanner(this, scanView);
            scanner.setCamera(CodeScanner.CAMERA_BACK);
            scanner.setFormats(CodeScanner.ALL_FORMATS);
            scanner.setAutoFocusMode(AutoFocusMode.SAFE);
            scanner.setScanMode(ScanMode.CONTINUOUS);
            scanner.setAutoFocusEnabled(true);
            scanner.setFlashEnabled(false);

            scanner.setDecodeCallback(result -> runOnUiThread(() -> {
                scanner.stopPreview();
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(1000);
                id = result.getText();
                String url = "http://10.11.123.17:5000/API/citizens/exists?id=" + id;
                @SuppressLint("SetTextI18n") JsonObjectRequest requesti = new JsonObjectRequest(Request.Method.GET, url, null,
                        reponse -> {
                            String ter = "";
                            try {
                                ter = reponse.getString("reponse");
                            } catch (JSONException e) {
                                Log.d("erreur", e.toString());
                            }
                            if (ter.equals("cet utilisateur n'existe plus")){
                                Toast.makeText(this, ter, Toast.LENGTH_LONG).show();
                            }
                            else{
                                viewFlipper.showNext();
                                String urlm = "http://10.11.123.59:5000/API/finances/login?id=" + id;
                                @SuppressLint("SetTextI18n") JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlm, null,
                                        response -> {
                                            boolean bool = false;
                                            try {
                                                bool = response.getInt("id") == 0;
                                            } catch (JSONException e) {
                                                Log.d("erreur", e.toString());
                                            }
                                            if (bool){
                                                validerTV.setText("Vous n'êtes pas encore de compte pour en créer ajouter un mot de passe puis resaisissez le pour le confirmer");
                                                confirmation.setVisibility(View.VISIBLE);
                                                valider.setOnClickListener(view -> {
                                                    if (mdp.getText().toString().isEmpty())
                                                        Toast.makeText(this, "ERREUR : mot de passe vide", Toast.LENGTH_LONG).show();
                                                    else {
                                                        if (mdp.getText().toString().equals(confirmation.getText().toString())){
                                                            String urli = "http://10.11.123.59:5000/API/finances/enrolment?id=" + id + "&mdp=" + mdp.getText().toString().hashCode();
                                                            StringRequest requestI = new StringRequest(Request.Method.GET, urli,
                                                                    response1 -> {
                                                                        Toast.makeText(this, response1, Toast.LENGTH_LONG).show();
                                                                        Intent secondeActivite = new Intent(MainActivity.this,
                                                                                MainActivity2.class);
                                                                        secondeActivite.putExtra("id", id);
                                                                        secondeActivite.putExtra("mdp", mdp.getText().toString().hashCode());
                                                                        startActivity(secondeActivite);
                                                                        finish();
                                                                    },
                                                                    error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show());
                                                            requestQueue.add(requestI);
                                                        }
                                                        else
                                                            Toast.makeText(this, "ERREUR : mot de passe non confirmé", Toast.LENGTH_LONG).show();
                                                    }

                                                });
                                            }
                                            else {
                                                valider.setOnClickListener(view -> {
                                                    if (mdp.getText().toString().isEmpty())
                                                        Toast.makeText(this, "ERREUR : mot de passe vide", Toast.LENGTH_LONG).show();
                                                    else {
                                                        try {
                                                            if (mdp.getText().toString().hashCode() == response.getInt("mdp")){
                                                                Toast.makeText(this, "identifiant confirmé", Toast.LENGTH_LONG).show();
                                                                Intent secondeActivite = new Intent(MainActivity.this,
                                                                        MainActivity2.class);
                                                                secondeActivite.putExtra("id", id);
                                                                secondeActivite.putExtra("mdp", mdp.getText().toString().hashCode());
                                                                secondeActivite.putExtra("solde", response.getDouble("solde"));
                                                                startActivity(secondeActivite);
                                                                finish();
                                                            }
                                                        } catch (JSONException e) {
                                                            Log.d("erreur", e.toString());
                                                        }
                                                    }
                                                });
                                            }
                                        },
                                        error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show());
                                requestQueue.add(request);
                            }
                        },
                        error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show());
                requestQueue.add(requesti);
            }));
            scanner.setErrorCallback(error -> runOnUiThread(() ->
                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show()));
            scanner.startPreview();
        }

    }

    private boolean checkPermission(){
        int read = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
        int cam_permission = ContextCompat.checkSelfPermission(this,CAMERA);
        int storage_permission = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        int vibration_permission = ContextCompat.checkSelfPermission(this, VIBRATE);
        return cam_permission == PackageManager.PERMISSION_GRANTED && storage_permission == PackageManager.PERMISSION_GRANTED &&
                vibration_permission == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermisssion(){
        final int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{
                CAMERA, VIBRATE, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
    }
}