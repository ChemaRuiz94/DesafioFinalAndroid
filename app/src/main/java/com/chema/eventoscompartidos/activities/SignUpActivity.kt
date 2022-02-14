package com.chema.eventoscompartidos.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.ProviderType
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var btn_confirmar_signUp: Button
    private lateinit var ed_txt_userName_signUp: EditText
    private lateinit var ed_txt_email_signUp: EditText
    private lateinit var ed_txt_pwd_signUp: EditText
    private lateinit var ed_txt_pwd2_signUp: EditText
    private lateinit var ed_txt_phone_signUp: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btn_confirmar_signUp = findViewById(R.id.btn_confirmar_signUp)
        ed_txt_userName_signUp = findViewById(R.id.ed_txt_userName_signUp)
        ed_txt_email_signUp = findViewById(R.id.ed_txt_email_signUp)
        ed_txt_pwd_signUp = findViewById(R.id.ed_txt_pwd_signUp)
        ed_txt_pwd2_signUp = findViewById(R.id.ed_txt_pwd2_signUp)
        ed_txt_phone_signUp = findViewById(R.id.ed_txt_phone_signUp)


        btn_confirmar_signUp.setOnClickListener{
            check_signUp_correcto()
        }
    }


    fun check_signUp_correcto(){
        //primero comprueba que los campos no esten vacios
        if(check_campos_vacios()){

            //Despues comprueba que las contraseñas sean iguales
            if(check_pwd_iguales()){

                //Despues se comprueba que el formato del telefono sea correcto
                if(check_movil(ed_txt_phone_signUp.text.trim())){

                    //Si lo anterior ha salido bien, se registra el usuario
                    check_firebase_auth()

                }else{
                    Toast.makeText(this, (getString(R.string.phoneNotCorrect)),Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, (getString(R.string.samePassword)),Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, (getString(R.string.emptyCamps)),Toast.LENGTH_SHORT).show()
        }
    }
    /*
    Funcion que comprueba que todos los campos esten rellenos
    Devuelve true en caso de que esten rellenos
     */
    fun check_campos_vacios():Boolean{
        var correcto = true

        if(ed_txt_userName_signUp.text.trim().isEmpty()){
            correcto = false
        }

        if(ed_txt_email_signUp.text.trim().isEmpty()){
            correcto = false
        }

        if(ed_txt_pwd_signUp.text.trim().isEmpty()){
            correcto = false
        }

        if(ed_txt_pwd2_signUp.text.trim().isEmpty()){
            correcto = false
        }

        if(ed_txt_phone_signUp.text.trim().isEmpty()){
            correcto = false
        }

        return correcto
    }

    /*
    Funcion que comprueba si las dos contraseñas son iguales
     */
    fun check_pwd_iguales():Boolean{
        if(ed_txt_pwd_signUp.text.trim().equals(ed_txt_pwd2_signUp.text.trim())){
            return true
        }
        return false
    }

    /*
    * Comprueba si el numero de telefono es correcto
     */
    private fun check_movil(target: CharSequence?): Boolean {
        return if (target == null) {
            false
        } else {
            if (target.length < 6 || target.length > 13) {
                false
            } else {
                Patterns.PHONE.matcher(target).matches()
            }
        }
    }

    /*
    Autenticacion Firebase
     */
    private fun check_firebase_auth(){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(ed_txt_email_signUp.text.toString(),ed_txt_pwd_signUp.text.toString()).addOnCompleteListener {
            if (it.isSuccessful){
                reg_user() //guardamos el usuario
                VariablesCompartidas.emailUsuarioActual = (it.result?.user?.email?:"")
                Toast.makeText(this, " IR A HOME ", Toast.LENGTH_SHORT).show()
                //irHome(it.result?.user?.email?:"") //vamos a home
            } else {
                showAlert()
            }
        }
    }

    /*
    Registrar un usuario en FireStore
     */
    private fun reg_user(){
        val rol = "user"
        val email = ed_txt_email_signUp.text.toString()


        //Se guardarán en modo HashMap (clave, valor).
        var user = hashMapOf(
            "provider" to ProviderType.BASIC,
            "userName" to ed_txt_userName_signUp.text.toString().trim(),
            "email" to email,
            "phone" to ed_txt_phone_signUp.text.toString().trim(),
            "rol" to rol
        )

        db.collection("${Constantes.collectionUser}")
            .document(email) //Será la clave del documento.
            .set(user).addOnSuccessListener {
                Toast.makeText(this, getString(R.string.SignUpSuscesfull), Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Toast.makeText(this, getString(R.string.ocurridoErrorAutenticacion), Toast.LENGTH_SHORT).show()
            }
    }
    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.ERROR))
        builder.setMessage(getString(R.string.ocurridoErrorAutenticacion))
        builder.setPositiveButton(getString(R.string.aceptar),null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


}