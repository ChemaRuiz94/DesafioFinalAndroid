package com.chema.eventoscompartidos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.ProviderType
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class LoginActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private var RC_SIGN_IN = 1

    private var usuarios = ArrayList<User>()

    private lateinit var btn_login : Button
    private lateinit var btn_login_google : Button
    private lateinit var btn_signUp : Button
    private lateinit var txt_email : EditText
    private lateinit var txt_pwd : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login = findViewById(R.id.bt_Login)
        btn_login_google = findViewById(R.id.bt_Google)
        btn_signUp = findViewById(R.id.bt_Registrar)
        txt_email = findViewById(R.id.ed_txt_Email_login)
        txt_pwd = findViewById(R.id.ed_txt_Pass_login)

        runBlocking {
            Log.e("preuba2","Prueba2")
            val job2 : Job = launch(context = Dispatchers.Default) {
                val datos2 : QuerySnapshot = getDataFromFireStore2() as QuerySnapshot //Obtenermos la colección
                Log.e("preuba1",datos2.toString())
                obtenerDatos2(datos2 as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
            }
            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
            job2.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
        }


        btn_signUp.setOnClickListener{
            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        btn_login_google.setOnClickListener{
            check_login_google()
        }

        btn_login.setOnClickListener{
            check_login()
        }
    }

    private fun check_login(){
        if(chekc_campos_vacios()){

            FirebaseAuth.getInstance().signInWithEmailAndPassword(txt_email.text.toString(),txt_pwd.text.toString()).addOnCompleteListener {
                if (it.isSuccessful){

                    VariablesCompartidas.emailUsuarioActual = (it.result?.user?.email?:"")

                    check_user_rol()
                    //irHome(it.result?.user?.email?:"")  //Esto de los interrogantes es por si está vacío el email.
                    //Toast.makeText(this, " IR A HOME ", Toast.LENGTH_SHORT).show()



                } else {
                    showAlert()
                }
            }

        }else{
            Toast.makeText(this,getString(R.string.emptyCamps), Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getDataFromFireStore2()  : QuerySnapshot? {
        return try{
            val data = db.collection("${Constantes.collectionUser}")
                .get()
                .await()
            data
        }catch (e : Exception){
            null
        }
    }

    private fun obtenerDatos2(datos: QuerySnapshot?) {
        usuarios.clear()

        for(dc: DocumentChange in datos?.documentChanges!!){

            if (dc.type == DocumentChange.Type.ADDED){

                //var prov = dc.document.get("provider").toString()
                var al = User(
                    //prov as ProviderType,
                    ProviderType.BASIC,
                    dc.document.get("userName").toString(),
                    dc.document.get("email").toString(),
                    dc.document.get("phone").toString().toInt(),
                    dc.document.get("rol").toString()
                )
                Log.e("CHE","${al.rol}")
                usuarios.add(al)
            }
        }
    }

    fun check_user_rol(){
        for (u in usuarios){
            if(u.email.equals(VariablesCompartidas.emailUsuarioActual)){
                VariablesCompartidas.rolUsuarioActual = u.rol
            }
        }

        if(VariablesCompartidas.rolUsuarioActual.equals("user")){
            Toast.makeText(this, "USUARIO MINDUNDI", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "USUARIO ADMIN", Toast.LENGTH_SHORT).show()
        }
    }

    private fun chekc_campos_vacios():Boolean{
        var correcto = true

        if(txt_email.text.toString().trim().isEmpty()){
            correcto = false
        }

        if(txt_pwd.text.toString().trim().isEmpty()){
            correcto = false
        }

        return correcto
    }

    private fun check_login_google(){
        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.request_id_token)) //Esto se encuentra en el archivo google-services.json: client->oauth_client -> client_id
            .requestEmail()
            .build()

        val googleClient = GoogleSignIn.getClient(this,googleConf) //Este será el cliente de autenticación de Google.
        googleClient.signOut() //Con esto salimos de la posible cuenta  de Google que se encuentre logueada.
        val signInIntent = googleClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Si la respuesta de esta activity se corresponde con la inicializada es que viene de la autenticación de Google.
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!

                //Ya tenemos la id de la cuenta. Ahora nos autenticamos con FireBase.
                if (account != null) {
                    val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful){
                            VariablesCompartidas.emailUsuarioActual = account.email?:""
                            //irHome(account.email?:"")  //Esto de los interrogantes es por si está vacío el email.
                            Toast.makeText(this, " IR A HOME ", Toast.LENGTH_SHORT).show()
                        } else {
                            showAlert()
                        }
                    }
                }
                //firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately

                showAlert()
            }
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

    /*
    private fun check_rol(){
        try{
            if(VariablesCompartidas.rolUsuarioActual.equals("user")){
                Toast.makeText(this, "USUARIO MINDUNDI", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "USUARIO ADMIN", Toast.LENGTH_SHORT).show()
            }
            /*
            db.collection("${Constantes.collectionUser}").document("${VariablesCompartidas.emailUsuarioActual}").get().addOnSuccessListener {

                var user = it as User
                var rol = (it.get("rol") as String?)

                if(rol.equals("user")){
                    Toast.makeText(this, "USUARIO MINDUNDI", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "USUARIO ADMIN", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener{
                Toast.makeText(this, "Algo ha ido mal al recuperar", Toast.LENGTH_SHORT).show()

            }

             */
        }catch(e: Exception){
            Log.e("CHE","ERROR  ${e.toString()}")
        }
    }

     */
}