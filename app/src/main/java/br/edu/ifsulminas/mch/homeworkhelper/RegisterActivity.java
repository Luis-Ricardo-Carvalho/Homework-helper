package br.edu.ifsulminas.mch.homeworkhelper;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editName, editEmail, editPhone, editCourse, editPassword, editPasswordConfirm;
    private Button buttonRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.title_activity_register));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        editName            = findViewById(R.id.edit_name);
        editEmail           = findViewById(R.id.edit_email);
        editPhone           = findViewById(R.id.edit_phone);
        editCourse          = findViewById(R.id.edit_course);
        editPassword        = findViewById(R.id.edit_password);
        editPasswordConfirm = findViewById(R.id.edit_password_confirm);
        buttonRegister      = findViewById(R.id.button_register);

        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name            = editName.getText().toString().trim();
        String email           = editEmail.getText().toString().trim();
        String phone           = editPhone.getText().toString().trim();
        String course          = editCourse.getText().toString().trim();
        String password        = editPassword.getText().toString().trim();
        String passwordConfirm = editPasswordConfirm.getText().toString().trim();

        if (name.isBlank() || email.isBlank() || phone.isBlank() ||
                course.isBlank() || password.isBlank() || passwordConfirm.isBlank()) {
            Toast.makeText(this, getString(R.string.register_error_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(passwordConfirm)) {
            Toast.makeText(this, getString(R.string.register_error_password_match), Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, getString(R.string.register_error_password_length), Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        saveUserToFirestore(uid, name, email, phone, course);
                    } else {
                        Toast.makeText(this, "Erro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String name, String email, String phone, String course) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
        user.put("course", course);

        db.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar dados: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}