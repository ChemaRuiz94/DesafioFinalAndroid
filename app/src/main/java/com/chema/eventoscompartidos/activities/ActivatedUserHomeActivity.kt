package com.chema.eventoscompartidos.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.AttributeSet
import android.util.Base64
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.databinding.ActivityActivatedUserHomeBinding
import com.chema.eventoscompartidos.fragment.ProfileFragment
import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.Opinion
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


//import com.chema.eventoscompartidos.activities.databinding.ActivityActivatedUserHomeBinding

class ActivatedUserHomeActivity : AppCompatActivity() {

    private val db = Firebase.firestore

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityActivatedUserHomeBinding

    private lateinit var txt_userName : TextView
    private lateinit var txt_userEmail : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityActivatedUserHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarActivatedUserHome.toolbar)

        /*
        binding.appBarActivatedUserHome.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

         */


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_activated_user_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_profile, R.id.nav_events, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        val headerView = navigationView.getHeaderView(0)
        val navUserEmail = headerView.findViewById<View>(R.id.txt_userEmail_header) as TextView
        val navUserName = headerView.findViewById<View>(R.id.txt_userName_header) as TextView

        navUserEmail.text = VariablesCompartidas.emailUsuarioActual.toString()

        val u = VariablesCompartidas.userActual as User
        navUserName.text = u.userName.toString()

        setHeaderImgUser()

    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activated_user_home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_activated_user_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    fun setHeaderImgUser(){
        var u : User? = VariablesCompartidas.userActual
        if(u?.img != null){
            var imgST : String? = u?.img.toString()
            var photo: Bitmap? = StringToBitMap(imgST)
            val navigationView: NavigationView =
                (this as AppCompatActivity).findViewById(R.id.nav_view)
            val header: View = navigationView.getHeaderView(0)
            val imgHe = header.findViewById<ImageView>(R.id.img_user_header)
            imgHe.setImageBitmap(photo)
        }

    }

    fun StringToBitMap(encodedString: String?): Bitmap? {
        return try {
            val encodeByte: ByteArray = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message
            null
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

    //********************
    suspend fun getDataFromFireStore()  : QuerySnapshot? {
        return try{
            val data = db.collection("${Constantes.collectionEvents}")
                .get()
                .await()
            data
        }catch (e : Exception){
            null
        }
    }

    private fun obtenerDatos(datos: QuerySnapshot?) {
        VariablesCompartidas.eventosUserActual.clear()
        for(dc: DocumentChange in datos?.documentChanges!!){
            if (dc.type == DocumentChange.Type.ADDED){

                var al = Evento(
                    dc.document.get("idEvento").toString(),
                    dc.document.get("nombreEvento").toString(),
                    dc.document.get("horaEvento").toString().toInt(),
                    dc.document.get("minEvento").toString().toInt(),
                    dc.document.get("diaEvento").toString().toInt(),
                    dc.document.get("mesEvento").toString().toInt(),
                    dc.document.get("yearEvento").toString().toInt(),
                    dc.document.get("latUbi").toString(),
                    dc.document.get("lonUbi").toString(),
                    dc.document.get("asistentes") as ArrayList<User>,
                    dc.document.get("emailAsistentes") as ArrayList<String>,
                    dc.document.get("idAsistentesHora") as HashMap<UUID, Date>,
                    dc.document.get("listaOpiniones") as ArrayList<Opinion>
                )
                //Log.e(TAG, al.toString())
                VariablesCompartidas.eventosUserActual.add(al)
            }
        }
    }
}