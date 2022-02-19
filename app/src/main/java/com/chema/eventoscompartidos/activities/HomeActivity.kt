package com.chema.eventoscompartidos.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.fragment.UserActivatedFragment
import com.chema.eventoscompartidos.utils.VariablesCompartidas

class HomeActivity : AppCompatActivity() {
    private lateinit var txt_user_email_not_activated : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        val fragment = UserActivatedFragment()
        replaceFragment(fragment)

        //txt_user_email_not_activated = findViewById(R.id.txt_user_email_not_activated)
        //txt_user_email_not_activated.setText(VariablesCompartidas.emailUsuarioActual.toString())

    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentTransiction = supportFragmentManager.beginTransaction()
        fragmentTransiction.replace(R.id.fragment_user_not_activated, fragment)

//        txt_user_email_not_activated = findViewById(R.id.txt_user_email_not_activated)
//        txt_user_email_not_activated.setText(VariablesCompartidas.emailUsuarioActual.toString())

        fragmentTransiction.commit()
    }
}