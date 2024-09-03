package com.example.lanjutanpertemuan1

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.SimpleAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lanjutanpertemuan1.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class MainActivity : AppCompatActivity(), View.OnClickListener {
    val COLLECTION = "students"
    val F_ID = "Mahasiswa"
    val F_NAME = "Name"
    val F_ADDRESS = "Address"
    val F_PHONE = "Phone"
    var docId = ""
    lateinit var db : FirebaseFirestore
    lateinit var alStudent : ArrayList<HashMap<String,Any>>
    lateinit var adapter: SimpleAdapter
    lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        alStudent = ArrayList()
        b.btnInsert.setOnClickListener(this)
        b.btnUpdate.setOnClickListener(this)
        b.btnDelete.setOnClickListener(this)
        b.lsData.setOnItemClickListener(itemClick)
    }

    override fun onStart() {
        super.onStart()
        db = FirebaseFirestore.getInstance()
        db.collection(COLLECTION).addSnapshotListener{ querySnapshot, e->
            if(e != null)Log.d("fireStore", e.localizedMessage)
            showData()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnInsert -> {
                val hm = hashMapOf(
                    F_ID to b.edId.text.toString(),
                    F_NAME to b.edName.text.toString(),
                    F_ADDRESS to b.edAddress.text.toString(),
                    F_PHONE to b.edPhone.text.toString()
                )

                db.collection(COLLECTION).document(b.edId.text.toString()).set(hm)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Data successfully added", Toast.LENGTH_SHORT).show()
                        b.edId.setText("")
                        b.edName.setText("")
                        b.edPhone.setText("")
                        b.edAddress.setText("")
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Data unsuccessfully added: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

            }
            R.id.btnUpdate -> {
                val hm = HashMap<String, Any>()
                hm.set(F_ID, docId)
                hm.set(F_NAME, b.edName.text.toString())
                hm.set(F_ADDRESS, b.edAddress.text.toString())
                hm.set(F_PHONE, b.edPhone.text.toString())

                db.collection(COLLECTION).document(docId).update(hm)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Data successfully updated", Toast.LENGTH_SHORT).show()
                        b.edId.setText("")
                        b.edName.setText("")
                        b.edPhone.setText("")
                        b.edAddress.setText("")
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Data unsuccessfully updated: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            R.id.btnDelete -> {
                db.collection(COLLECTION).whereEqualTo(F_ID, docId).get()
                    .addOnSuccessListener { results ->
                        for (doc in results) {
                            db.collection(COLLECTION).document(doc.id).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Data successfully deleted", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Data unsuccessfully deleted: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Can't get data references: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    val itemClick = AdapterView.OnItemClickListener{ parent, view, position, id ->
        val hm = alStudent.get(position)
        docId = hm.get(F_ID).toString()
        b.edId.setText(docId)
        b.edName.setText(hm.get(F_NAME).toString())
        b.edAddress.setText(hm.get(F_ADDRESS).toString())
        b.edPhone.setText(hm.get(F_PHONE).toString())
    }

    fun showData() {
        db.collection(COLLECTION).get().addOnSuccessListener { result ->
            alStudent.clear()
            for (doc in result) {
                val hm = HashMap<String, Any>()
                hm.set(F_ID, doc.get(F_ID).toString())
                hm.set(F_NAME, doc.get(F_NAME).toString())
                hm.set(F_ADDRESS, doc.get(F_ADDRESS).toString())
                hm.set(F_PHONE, doc.get(F_PHONE).toString())
                alStudent.add(hm)
            }
            adapter = SimpleAdapter(
                this, alStudent, R.layout.row_data,
                arrayOf(F_ID, F_NAME, F_ADDRESS, F_PHONE),
                intArrayOf(R.id.txId, R.id.txName, R.id.txAddress, R.id.txPhone)
            )
            b.lsData.adapter = adapter}
    }
}