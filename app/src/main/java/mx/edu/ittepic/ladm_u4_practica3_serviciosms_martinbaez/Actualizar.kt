package mx.edu.ittepic.ladm_u4_practica3_serviciosms_martinbaez

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_actualizar.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.NoDisponible
import kotlinx.android.synthetic.main.activity_main.platillo

class Actualizar : AppCompatActivity() {

    var id = ""
    var baseDatos = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actualizar)

        var extras = intent.extras

        id = extras!!.getString("id").toString()

        platillo.setText(extras.getString("platillo").toString())
        precio2.setText(extras.getString("precio")!!.toInt())
        if(extras.getBoolean("nodisponible") == true){
            NoDisponible.isChecked = true
        }

        actualizar.setOnClickListener {
            baseDatos.collection("contactos").document(id)
                .update("platillo", platillo.text.toString(),
                    "precio", precio2.text.toString(),
                    "nodisponible", NoDisponible.isChecked() )
        }

        regresar.setOnClickListener {
            finish()
        }
    }
}
