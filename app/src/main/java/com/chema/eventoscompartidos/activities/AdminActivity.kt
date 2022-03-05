package com.chema.eventoscompartidos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.chema.eventoscompartidos.AdminTabbActivity
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.utils.VariablesCompartidas

class AdminActivity : AppCompatActivity() {

    private lateinit var btn_admin: Button
    private lateinit var btn_user: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        btn_admin = findViewById(R.id.btn_admin)
        btn_user = findViewById(R.id.btn_user)

        btn_admin.setOnClickListener{
            var myIntent = Intent(this, AdminTabbActivity::class.java)
            VariablesCompartidas.adminMode = true
            startActivity(myIntent)
        }

        btn_user.setOnClickListener{
            var myIntent = Intent(this, ActivatedUserHomeActivity::class.java)
            startActivity(myIntent)
        }
    }

    override fun onBackPressed(){
        AlertDialog.Builder(this)
            .setTitle("Cerrar sersion")
            .setMessage("Desea cerrar sesion")
            .setPositiveButton("OK") { view, _ ->
                super.onBackPressed()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                VariablesCompartidas.adminMode = false
                finish()
                view.dismiss()
            }
            .setNegativeButton("NO") { view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .create()
            .show()
    }
}