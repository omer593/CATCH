package com.example.acatch;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn, registerBtn;
    TextView forgotPassword;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔥 אם המשתמש כבר מחובר - נכנסים ישר לאפליקציה
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            startActivity(new Intent(
                    LoginActivity.this,
                    MainActivity.class
            ));

            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);
        forgotPassword = findViewById(R.id.forgotPassword);

        mAuth = FirebaseAuth.getInstance();

        // 🔵 LOGIN
        loginBtn.setOnClickListener(v -> {

            String userEmail = email.getText().toString().trim();
            String userPass = password.getText().toString().trim();

            if (userEmail.isEmpty() || userPass.isEmpty()) {
                Toast.makeText(this,
                        "Fill all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Toast.makeText(this,
                                    "Login Success!",
                                    Toast.LENGTH_SHORT).show();

                            startActivity(
                                    new Intent(
                                            LoginActivity.this,
                                            MainActivity.class
                                    )
                            );

                            finish();

                        } else {

                            Toast.makeText(
                                    this,
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });

        // 🟢 REGISTER
        registerBtn.setOnClickListener(v -> {

            String userEmail = email.getText().toString().trim();
            String userPass = password.getText().toString().trim();

            if (userEmail.isEmpty() || userPass.isEmpty()) {

                Toast.makeText(
                        this,
                        "Fill all fields",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (userPass.length() < 6) {

                Toast.makeText(
                        this,
                        "Password must be at least 6 characters",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            mAuth.createUserWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Toast.makeText(
                                    this,
                                    "User Created!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            startActivity(
                                    new Intent(
                                            LoginActivity.this,
                                            CreateProfileActivity.class
                                    )
                            );

                            finish();

                        } else {

                            Toast.makeText(
                                    this,
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });
        forgotPassword.setOnClickListener(v -> {

            String userEmail = email.getText().toString().trim();

            if (userEmail.isEmpty()) {

                Toast.makeText(
                        LoginActivity.this,
                        "Enter your email first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            mAuth.sendPasswordResetEmail(userEmail)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Password reset email sent",
                                    Toast.LENGTH_LONG
                            ).show();

                        } else {

                            Toast.makeText(
                                    LoginActivity.this,
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });
    }
}