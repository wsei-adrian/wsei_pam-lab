package pl.wsei.pam.lab03

import android.os.Bundle
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pl.wsei.pam.R
import java.util.Timer
import kotlin.concurrent.schedule

class Lab03Activity : AppCompatActivity() {
    private lateinit var mBoard: GridLayout
    private lateinit var mBoardModel: MemoryBoardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lab03)

        mBoard = findViewById(R.id.memory_grid)
        ViewCompat.setOnApplyWindowInsetsListener(mBoard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val rows = intent.getIntExtra(EXTRA_ROWS, 3)
        val columns = intent.getIntExtra(EXTRA_COLUMNS, 2)
        mBoardModel = MemoryBoardView(mBoard, columns, rows)

        savedInstanceState?.getIntArray(STATE_BOARD)?.let {
            mBoardModel.setState(it)
        }

        mBoardModel.setOnGameChangeListener { event ->
            when (event.state) {
                GameStates.Matching -> {
                    event.tiles.forEach { it.revealed = true }
                }
                GameStates.Match -> {
                    event.tiles.forEach {
                        it.matched = true
                        it.revealed = true
                        it.removeOnClickListener()
                    }
                }
                GameStates.NoMatch -> {
                    mBoardModel.setInputEnabled(false)
                    event.tiles.forEach { it.revealed = true }
                    Timer().schedule(2000) {
                        runOnUiThread {
                            event.tiles.forEach { it.revealed = false }
                            mBoardModel.setInputEnabled(true)
                        }
                    }
                }
                GameStates.Finished -> {
                    event.tiles.forEach {
                        it.matched = true
                        it.revealed = true
                        it.removeOnClickListener()
                    }
                    Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::mBoardModel.isInitialized) {
            outState.putIntArray(STATE_BOARD, mBoardModel.getState())
        }
    }

    companion object {
        const val EXTRA_ROWS = "rows"
        const val EXTRA_COLUMNS = "columns"
        private const val STATE_BOARD = "state_board"
    }
}
