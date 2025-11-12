package com.example.puli
 
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

   override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)

    findViewById<LinearLayout>(R.id.tileCredit).setOnClickListener {
        startActivity(Intent(this, BenfListActivity::class.java))
    }

    findViewById<LinearLayout>(R.id.tileDebit).setOnClickListener {
        startActivity(Intent(this, DebitListActivity::class.java))
    }

    findViewById<LinearLayout>(R.id.tileCalculate).setOnClickListener {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
}
