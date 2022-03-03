package com.chema.eventoscompartidos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.chema.eventoscompartidos.R

class AdminActivity : AppCompatActivity() {

    private lateinit var btn_admin: Button
    private lateinit var btn_user: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        btn_admin = findViewById(R.id.btn_admin)
        btn_user = findViewById(R.id.btn_user)

        btn_admin.setOnClickListener{
            var myIntent = Intent(this, ActivatedUserHomeActivity::class.java)
            startActivity(myIntent)
        }

        btn_user.setOnClickListener{
            var myIntent = Intent(this, ActivatedUserHomeActivity::class.java)
            startActivity(myIntent)
        }
    }
}