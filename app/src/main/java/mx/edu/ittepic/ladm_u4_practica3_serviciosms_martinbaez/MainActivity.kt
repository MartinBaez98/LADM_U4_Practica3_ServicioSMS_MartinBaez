package mx.edu.ittepic.ladm_u4_practica3_serviciosms_martinbaez

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var baseRemota = FirebaseFirestore.getInstance()
    var dataLista = ArrayList<String>()
    var listaID = ArrayList<String>()

    val siPermiso = 1
    val siPermisoReceiver = 2
    val siPermisoLectura = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setTitle("Servicio Menú -Nozomi- SMS")

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_SMS),siPermisoLectura)
        }

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.RECEIVE_SMS),siPermisoReceiver)
        }

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.SEND_SMS) !=
            PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.SEND_SMS), siPermiso)
        }



        baseRemota.collection("nozomi").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if(firebaseFirestoreException != null){
                //Si es diferente de null, hay un error
                mensaje("ERROR: No se puede acceder a consulta")
                return@addSnapshotListener
            }
            dataLista.clear()
            listaID.clear()
            for(document in querySnapshot!!){
                var cadena = document.getString("platillo")+("\n")+document.getString("precio")+ ("\n")

                if(document.getBoolean("nodisponible").toString() == "true"){
                    cadena += "No está disponible"
                }else {cadena += "Disponible"}

                dataLista.add(cadena)
                listaID.add(document.id)
            }
            if(dataLista.size == 0){
                dataLista.add("No hay datos")
            }
            var adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataLista)
            lista.adapter = adapter
        }

        lista.setOnItemClickListener { parent, view, position, id ->
            if(listaID.size == 0){
                return@setOnItemClickListener
            }
            AlertaEliminar(position)
        }

        button.setOnClickListener {
            aggDatos()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == siPermiso){

        }
        if(requestCode == siPermisoReceiver){

        }
        if(requestCode == siPermisoLectura){

        }
    }

    private fun AlertaEliminar(position: Int) {
        AlertDialog.Builder(this).setTitle("ATENCIÓN")
            .setMessage("¿Qué desea hacer con este platillo?")
            .setPositiveButton("Eliminar"){d,w ->
                eliminar(listaID[position])
            }
            .setNegativeButton("Actualizar"){d,w ->
                llamarVentanaActualizar(listaID[position])
            }
            .setNeutralButton("Cancelar"){dialog, wich ->

            }.show()
    }

    private fun llamarVentanaActualizar(idActualizar: String) {
        baseRemota.collection("nozomi").document(idActualizar).get()
            .addOnSuccessListener {
                var v = Intent(this, Actualizar::class.java)
                v.putExtra("id", idActualizar)
                v.putExtra("platillo", it.getString("platillo"))
                v.putExtra("precio", it.getString("precio"))
                v.putExtra("nodisponible", it.getBoolean("nodisponible"))

                startActivity(v)
            }.addOnFailureListener {
                mensaje("ERROR: No hay conexión de red")
            }
    }

    private fun eliminar(idEliminar: String) {
        baseRemota.collection("nozomi").document(idEliminar).delete()
            .addOnSuccessListener {
                mensaje("Platillo eliminado del menú")
            }
            .addOnFailureListener {
                mensaje("No se pudo eliminar")
            }
    }

    private fun aggDatos() {

        var datos = hashMapOf(
            "platillo" to platillo.text.toString(),
            "precio"   to precio.text.toString(),
            "nodoisponible" to NoDisponible.isChecked
        )

        baseRemota.collection("nozomi").add(datos)
            .addOnSuccessListener {
                mensaje("Platillo agregado con éxito a la base de datos")
                limpiarCampos()
            }
            .addOnFailureListener {
                mensaje("No se pudo hacer la actualización de los datos")
            }
    }

    private fun limpiarCampos() {
        platillo.setText("")
        precio.setText("")
        NoDisponible.isChecked = false
    }

    private fun mensaje(msj: String){
        Toast.makeText(this, msj, Toast.LENGTH_LONG).show()
    }
}
