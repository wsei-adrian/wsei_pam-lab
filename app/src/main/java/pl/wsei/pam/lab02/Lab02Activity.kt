package pl.wsei.pam.lab02

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pl.wsei.pam.R
import pl.wsei.pam.lab03.Lab03Activity

class Lab02Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lab02)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.favorites_grid)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun printGridSize(v: View) {
        val tag: String? = v.tag as String?
        val tokens: List<String>? = tag?.split(" ")
        val rows = tokens?.get(0)?.toInt()
        val columns = tokens?.get(1)?.toInt()

        if (rows == null || columns == null) {
            Toast.makeText(this, "Niepoprawny rozmiar planszy", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, Lab03Activity::class.java)
        intent.putExtra(Lab03Activity.EXTRA_ROWS, rows)
        intent.putExtra(Lab03Activity.EXTRA_COLUMNS, columns)
        startActivity(intent)
    }
}
