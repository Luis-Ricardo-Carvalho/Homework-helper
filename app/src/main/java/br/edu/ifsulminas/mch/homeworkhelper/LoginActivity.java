package br.edu.ifsulminas.mch.homeworkhelper;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister, buttonForgotPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApplicationId("1:41395378460:android:9ad50fefa01066247702d9")
                        .setProjectId("homeworkhelper-984a5")
                        .setApiKey("AIzaSyA0xfTdhNTIBcb74L2zcXodvGVc2WH7cUk")
                        .setStorageBucket("homeworkhelper-984a5.firebasestorage.app")
                        .build();
                FirebaseApp.initializeApp(this, options);
            }
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Log.e("FirebaseInit", "Erro detalhado ao inicializar: ", e);
            Toast.makeText(this, "Erro ao inicializar Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        editTextEmail    = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin      = findViewById(R.id.buttonLogin);
        buttonRegister   = findViewById(R.id.buttonRegister);
        buttonForgotPassword = findViewById(R.id.buttonForgotPassword);

        if (editTextEmail != null)    editTextEmail.setText("teste@teste.com");
        if (editTextPassword != null) editTextPassword.setText("123456");

        if (buttonLogin != null)
            buttonLogin.setOnClickListener(v -> loginUser());

        if (buttonRegister != null)
            buttonRegister.setOnClickListener(v ->
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
            );

        if (buttonForgotPassword != null)
            buttonForgotPassword.setOnClickListener(v -> resetPassword());
    }

    private void loginUser() {
        String email    = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth == null) {
            Toast.makeText(this, "O serviço do Firebase não está disponível.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login realizado!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Erro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetPassword() {
        String email = editTextEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Informe o email para recuperar a senha", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth == null) return;

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Email de recuperação enviado!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}