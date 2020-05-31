package mx.edu.ittepic.ladm_u4_practica3_serviciosms_martinbaez

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.telephony.SmsManager
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

class Receiver : BroadcastReceiver() {
    var baseRemota = FirebaseFirestore.getInstance()

    override fun onReceive(context: Context?, intent: Intent) {
        var cadMSJ = ""
        val extras = intent.extras
        if(extras != null) {
            var sms = extras.get("pdus") as Array<Any>
            for (indice in sms.indices) {
                var formato = extras.getString("format")

                var smsMensaje = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    SmsMessage.createFromPdu(sms[indice] as ByteArray, formato)
                } else {
                    SmsMessage.createFromPdu(sms[indice] as ByteArray)
                }

                var contacto = smsMensaje.originatingAddress
                var contenidoSMS = smsMensaje.messageBody.toString()
                var cadena = contenidoSMS.split(" ")
                var envio = ""
                Toast.makeText(context, "Recibiste mensaje de: " + contacto, Toast.LENGTH_LONG).show()

                if (cadena.size != 2) {

                } else {
                    if (cadena[1] != "disponible") {
                        SmsManager.getDefault().sendTextMessage(
                            contacto, null,
                            "ERROR: La sintaxis : (disponible) (Platillo a consultar)", null, null
                        )
                    } else {
                        if (validarPlatillo(cadena[2])) {
                            SmsManager.getDefault().sendTextMessage(
                                contacto, null,
                                "ERROR: El platillo indicado no existe", null, null
                            )
                        } else {
                            try {
                                baseRemota.collection("nozomi").whereEqualTo("platillo",cadena[2]).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                    for(document in querySnapshot!!){
                                        cadMSJ = document.getString("platillo")+("\n")+document.getString("precio")+
                                                ("\n")+document.getBoolean("nodisponible").toString()
                                    }
                                    SmsManager.getDefault().sendTextMessage(contacto,null, ""+cadMSJ,null,null)
                                }
                            }catch (e: FirebaseFirestoreException){
                                SmsManager.getDefault().sendTextMessage(
                                    contacto,null,
                                    e.message,null,null)

                            }

                        }
                    }
                }
            }
        }
    }

    private fun validarPlatillo(s: String): Boolean {
        var platilloValido = true
            baseRemota.collection("nozomi").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                for(document in querySnapshot!!){
                    if(document.getString("platillo").toString() != s){
                        platilloValido = false
                    }
                }
            }
        return platilloValido
    }
}