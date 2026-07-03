package br.edu.ifsulminas.mch.homeworkhelper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btnMaterias = findViewById(R.id.btn_materias);
        btnMaterias.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, IndexActivity.class);
            startActivity(intent);
        });

        Button btnClassrom = findViewById(R.id.btn_classrom);
        btnClassrom.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ClassromActivity.class);
            startActivity(intent);
        });
    }
}