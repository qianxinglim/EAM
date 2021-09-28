package com.example.eam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.eam.databinding.ActivityNoCompanyBinding;

public class NoCompanyActivity extends AppCompatActivity {
    private ActivityNoCompanyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_no_company);

        binding.btnCreateCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NoCompanyActivity.this, CreateCompanyActivity.class).putExtra("pos", 1));
            }
        });
    }
}