package com.aditya.chatkaro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aditya.chatkaro.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {
    ActivitySignUpBinding binding;
    EditText et1, et2, et3, et4;
    Button bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        clicklistener();
        et1 = findViewById(R.id.name);
        et2 = findViewById(R.id.editTextText2);
        et3 = findViewById(R.id.email);
        et4 = findViewById(R.id.confirmpass);
        bt = (Button) findViewById(R.id.sign_up);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et1.getText().toString();
                String username = et2.getText().toString();
                String email = et3.getText().toString();
                String password = et4.getText().toString();

                if (!email.isEmpty() && !name.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                    boolean val = true;
                    for(int i=0;i<username.length();i++){
                        if(username.charAt(i) == '.' || username.charAt(i) == '#'){
                            val = false;
                            break;
                        }
                    }
                    if(val)
                        addDatatoFirestore(name, username, email, password);
                    else
                        Toast.makeText(getApplicationContext(),"Invalid Username",Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Enter all Details", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    public void clicklistener() {
        binding.login.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Login.class)));
    }
    private void addDatatoFirestore(final String name, final String username, final String email, final String password) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Check if username exists
        firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                // Username already exists
                                Toast.makeText(getApplicationContext(), "Username already exists. Please choose another username.", Toast.LENGTH_LONG).show();
                            }
                            else {
                                // Check if email exists
                                firestore.collection("users")
                                        .whereEqualTo("email", email)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    if (!task.getResult().isEmpty()) {
                                                        // Email already exists
                                                        Toast.makeText(getApplicationContext(), "Email already exists. Please use another email.", Toast.LENGTH_LONG).show();
                                                        et1.setText("");
                                                        et2.setText("");
                                                        et3.setText("");
                                                        et4.setText("");
                                                    } else {
                                                        // Both username and email are unique, proceed with adding the new user
                                                        Map<String, Object> userData = new HashMap<>();
                                                        userData.put("name", name);
                                                        userData.put("username", username);
                                                        userData.put("email", email);
                                                        userData.put("password", password);

                                                        // Add data to Firestore with username as document ID
                                                        firestore.collection("users")
                                                                .document(username)
                                                                .set(userData)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            // Data added successfully
                                                                            Toast.makeText(getApplicationContext(), "Data Added Succesfully!", Toast.LENGTH_LONG).show();
                                                                            Intent i = new Intent(getApplicationContext(),Login.class);
                                                                            startActivity(i);
                                                                            finish();

                                                                        } else {
                                                                            // Failed to add data
                                                                            Toast.makeText(getApplicationContext(), "Failed to store data", Toast.LENGTH_LONG).show();
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                } else {
                                                    // Handle the error
                                                    Toast.makeText(getApplicationContext(), "Error checking email existence", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            // Handle the error
                            Toast.makeText(getApplicationContext(), "Error checking username existence", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void UploadData() {
        final String name = et1.getText().toString();
        final String username = et2.getText().toString();
        final String email = et3.getText().toString();
        final String password = et4.getText().toString();

        // Check if both username and email already exist
        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference().child("users");
        usersReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot usernameSnapshot) {
                if (usernameSnapshot.exists()) {
                    // Username already exists, show an error message
                    Toast.makeText(getApplicationContext(), "Username already exists. Please choose another username.", Toast.LENGTH_LONG).show();
                } else {
                    // Check if the email already exists
                    usersReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot emailSnapshot) {
                            if (emailSnapshot.exists()) {
                                // Email already exists, show an error message
                                Toast.makeText(getApplicationContext(), "Email already exists. Please use another email.", Toast.LENGTH_LONG).show();
                            } else {
                                // Username and email are unique, proceed with adding the new user
                                DataClass dataClass = new DataClass(name, username, email, password);
                                FirebaseDatabase.getInstance().getReference().child("users").child(username)
                                        .setValue(dataClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), "Data stored!", Toast.LENGTH_LONG).show();
                                                    Intent i = new Intent(getApplicationContext(), Login.class);
                                                    startActivity(i);
                                                    finish();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle the error if any
                            Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if any
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
