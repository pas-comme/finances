package com.example.finances;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.ScanMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class MainActivity2 extends AppCompatActivity {
    //widget
    ViewFlipper viewFlipper;
    RequestQueue requestQueue;
    Button depot, retrait, soldeBTN, trasaction, retourSolde, retourTransaction;
    TextView soldeTV;
    ListView list;

    //variable et constante
    String id, idCP;
    int mdp, mdpCP;
    Double solde;
    ArrayList<JSONObject> array = new ArrayList<>();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        viewFlipper = findViewById(R.id.acceuilVF);
        list = findViewById(R.id.liste);
        depot = findViewById(R.id.depotBTN);
        retrait = findViewById(R.id.retraitBTN);
        soldeBTN = findViewById(R.id.consultationBTN);
        trasaction = findViewById(R.id.transactionBTN);
        soldeTV = findViewById(R.id.soldeTV);
        retourSolde = findViewById(R.id.retourSolde);
        retourTransaction = findViewById(R.id.retourTransaction);
        requestQueue = Volley.newRequestQueue(this);


        Intent i = getIntent();
        id = i.getStringExtra("id");
        solde = i.getDoubleExtra("solde", 0);
        mdp = i.getIntExtra("mdp", 0);

        String urlp = "http://10.11.123.17:5000/API/citizens/onePRS?id=" + id;
        JsonObjectRequest requestP = new JsonObjectRequest(Request.Method.GET, urlp, null,
                response -> {

                    try {

                        String [] temp = response.getString("daty").split("-");
                        Date actu = new Date();
                        @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                        String [] vao = format.format(actu).split("-");
                        int age = Integer.parseInt(vao[2]) - Integer.parseInt(temp[2]);
                        this.setTitle(response.getString("anarana") + " " +
                                response.getString("fanampiny") + " " + age + " ans "+ response.getString("sexe"));

                    } catch (JSONException e) {
                        Log.d("erreur", e.toString());
                    }

                },
                error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show());
        requestQueue.add(requestP);


        retourSolde.setOnClickListener(view -> viewFlipper.showPrevious());
        retourTransaction.setOnClickListener(view -> {
            viewFlipper.showPrevious();viewFlipper.showPrevious();
            array.clear();
        });
        retrait.setOnClickListener(view -> dialogue("retrait"));
        depot.setOnClickListener(view -> dialogue("dépôt"));

        soldeBTN.setOnClickListener(view -> {
            viewFlipper.showNext();
            soldeTV.setText("Votre solde actuel est de  : " + solde);
        });

        trasaction.setOnClickListener(view -> {
            String url = "http://10.11.123.59:5000/API/finances/deals?id=" + id;
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            for (int j = 0; j < response.length(); j++) {
                                array.add((JSONObject) response.get(j));
                            }
                        } catch (JSONException e) {
                            Log.d("erreur", e.toString());
                        }

                        list.setAdapter(new JSONadapter(this, R.layout.help, array));
                        viewFlipper.showNext();viewFlipper.showNext();
                    },
                    error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show());
            requestQueue.add(request);
        });
    }
    @SuppressLint("SetTextI18n")
    public void dialogue(String action){
        Dialog dlg = new Dialog(this);
        @SuppressLint("InflateParams")
        View alertView = LayoutInflater.from(this).inflate(R.layout.item, null);
        dlg.setContentView(alertView);
        TextView titre = alertView.findViewById(R.id.titreDLG);
        if (action.equals("dépôt"))
            titre.setText("Saisissez le montant à déposer suivi de votre mot de passe");
        else if(action.equals("retrait"))
            titre.setText("Saisissez le montant à retirer suivi de votre mot de passe");

        EditText montant = alertView.findViewById(R.id.montant),
                pwd = alertView.findViewById(R.id.mdp), pwdCP = alertView.findViewById(R.id.mdpCP);
        Button valider = alertView.findViewById(R.id.valider), suivant = alertView.findViewById(R.id.suivant);
        suivant.setOnClickListener(view -> {
            ViewFlipper vf = alertView.findViewById(R.id.vfDLG);
            vf.showNext();
            CodeScannerView scanView = alertView.findViewById(R.id.camVIEW);
            CodeScanner scanner = new CodeScanner(this, scanView);
            scanner.setCamera(CodeScanner.CAMERA_BACK);
            scanner.setFormats(CodeScanner.ALL_FORMATS);
            scanner.setAutoFocusMode(AutoFocusMode.SAFE);
            scanner.setScanMode(ScanMode.CONTINUOUS);
            scanner.setAutoFocusEnabled(true);
            scanner.setFlashEnabled(false);
            scanner.setDecodeCallback(result -> runOnUiThread(() -> {
                scanner.stopPreview();
                String url = "http://10.11.123.17:5000/API/citizens/exists?id=" + id;
                @SuppressLint("SetTextI18n") JsonObjectRequest requesti = new JsonObjectRequest(Request.Method.GET, url, null,
                        reponse -> {
                            vf.showPrevious();
                            valider.setVisibility(View.VISIBLE);
                            idCP = result.getText();
                            pwdCP.setVisibility(View.VISIBLE);
                            suivant.setVisibility(View.GONE);

                            String urlm = "http://10.11.123.59:5000/API/finances/login?id=" + result.getText();
                            @SuppressLint("SetTextI18n") JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlm, null,
                                    response -> {
                                        try {
                                            mdpCP = response.getInt("mdp");
                                        } catch (JSONException e) {
                                            Log.d("erreur", e.toString());
                                        }
                                    },
                                    error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show());
                            requestQueue.add(request);
                        },
                        error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show());
                requestQueue.add(requesti);

            }));
            scanner.setErrorCallback(error -> runOnUiThread(() ->
                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show()));
            scanner.startPreview();
        });
        valider.setOnClickListener(view -> {
            if (montant.getText().toString().isEmpty() || pwd.getText().toString().isEmpty())
                Toast.makeText(this, "ERREUR : vous devez remplir tous les champs", Toast.LENGTH_LONG).show();
            else if (volaIScorrect(montant.getText().toString()))
                Toast.makeText(this, "ERREUR : montant invalide", Toast.LENGTH_LONG).show();
            else if (action.equals("retrait") && Double.parseDouble(montant.getText().toString()) > solde)
                Toast.makeText(this, "ERREUR : montant de retrait supérieur au solde", Toast.LENGTH_LONG).show();
            else if (pwd.getText().toString().hashCode() != mdp)
                Toast.makeText(this, "ERREUR : mot de passe client incorrecte", Toast.LENGTH_LONG).show();
            else if (pwdCP.getText().toString().hashCode() != mdpCP)
                Toast.makeText(this, "ERREUR : mot de passe cash point incorrecte", Toast.LENGTH_LONG).show();
            else{
                if (action.equals("retrait"))
                    solde -= Double.parseDouble(montant.getText().toString());
                else if (action.equals("dépôt"))
                    solde += Double.parseDouble(montant.getText().toString());
                @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String temp = format.format(new Date());

                String urli = "http://10.11.123.59:5000/API/finances/changeBalance?solde=" + solde + "&id=" + id
                        + "&type=" + action + "&montant=" + montant.getText() + "&idCP=" + idCP + "&daty=" + temp;
                StringRequest requestI = new StringRequest(Request.Method.GET, urli,
                        response1 -> {
                            Toast.makeText(this, response1, Toast.LENGTH_LONG).show();
                            dlg.dismiss();
                        },
                        error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show());
                requestQueue.add(requestI);
            }
        });
        dlg.show();
    }
    public static class JSONadapter extends ArrayAdapter<JSONObject> {
        private final Context context;
        private final int mResource;
        public JSONadapter(@NonNull Context context, int resource, ArrayList<JSONObject> objects) {
            super(context, resource, objects);
            this.context = context;
            this.mResource = resource;
        }
        @NonNull
        @SuppressLint({"ViewHolder", "SetTextI18n"})
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(mResource, parent, false);

            TextView daty = convertView.findViewById(R.id.date);
            TextView description = convertView.findViewById(R.id.description);

            try {

                daty.setText(Objects.requireNonNull(getItem(position)).optString("daty"));
                description.setText(
                        Objects.requireNonNull(getItem(position)).getString("type") + " de " + Objects.requireNonNull(getItem(position)).getString("montant"));

            } catch (JSONException e) {
                Log.d("erreur", e.toString());
            }
            return convertView;
        }

    }
    public boolean volaIScorrect(String vola){
        return !vola.matches("[1-9][0-9.]*");
    }
}