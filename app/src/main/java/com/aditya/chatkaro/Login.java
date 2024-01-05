package com.aditya.chatkaro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aditya.chatkaro.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Login extends AppCompatActivity {
    ActivityLoginBinding binding;
    Button bt;
    EditText et1,et2;
    FirebaseDatabase db;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://chat-karo-5a12d-default-rtdb.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListiner();

        bt = (Button) findViewById(R.id.login);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"Button Clicked",Toast.LENGTH_LONG);
                et1 = findViewById(R.id.username);
                et2 = findViewById(R.id.password);
                String username= et1.getText().toString();
                String password = et2.getText().toString();
                if(username.isEmpty() && password.isEmpty())
                    Toast.makeText(getApplicationContext(),"Enter username and password",Toast.LENGTH_LONG).show();
                else if(username.isEmpty())
                    Toast.makeText(getApplicationContext(),"Enter username",Toast.LENGTH_LONG).show();
                else if(password.isEmpty())
                    Toast.makeText(getApplicationContext(),"Enter password",Toast.LENGTH_LONG).show();
                else{
                    checkUsernameAndPassword(username,password);
                }
            }
        });
    }
    public void setListiner(){
        binding.signUp.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignUp.class)));
    }
    private void checkUsernameAndPassword(final String username, final String password) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .document(username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Username exists, check password
                                String storedPassword = document.getString("password");
                                if (storedPassword != null && storedPassword.equals(password)) {
                                    // Password is correct
                                    Toast.makeText(getApplicationContext(), "Login successful.", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(getApplicationContext(),DashBoard.class);
                                    startActivity(i);
                                    finish();
                                } else {
                                    // Incorrect password
                                    Toast.makeText(getApplicationContext(), "Incorrect password.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Username does not exist
                                Toast.makeText(getApplicationContext(), "Username does not exist.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle the error
                            Log.e("LoginActivity", "Error checking username existence", task.getException());
                            Toast.makeText(getApplicationContext(), "Error checking username existence", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void retriveData() {
        String username= et1.getText().toString();
        String password = et2.getText().toString();
        boolean val = true;
        for(int i=0;i<username.length();i++){
            if(username.charAt(i)=='.' || username.charAt(i)=='#' || username.charAt(i) =='[' || username.charAt(i) ==']'){
                val = false;
                break;
            }
        }
        if(val == true){
            databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //check for matching username in database;
                    if(snapshot.hasChild(username)){
                        //username is in database
                        //now check for password and match it
                        String pass = snapshot.child(username).child("password").getValue(String.class);
                        if(password.equals(pass)){
                            Toast.makeText(getApplicationContext(),"Login Succesfull",Toast.LENGTH_LONG).show();
                            Intent i = new Intent(Login.this,DashBoard.class);
                            startActivity(i);
                            finish();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Incorrect Password",Toast.LENGTH_LONG).show();
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Username and password is incorrect!",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(),error.getMessage().toString(),Toast.LENGTH_LONG).show();
                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(),"username contains invalid literals",Toast.LENGTH_LONG).show();
        }


    }


}