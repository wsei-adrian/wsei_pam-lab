package pl.wsei.pam.lab03

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pl.wsei.pam.R
import java.util.Random

class Lab03Activity : AppCompatActivity() {
    private lateinit var mBoard: GridLayout
    private lateinit var mBoardModel: MemoryBoardView
    private lateinit var completionPlayer: MediaPlayer
    private lateinit var negativePLayer: MediaPlayer
    private var isSound: Boolean = true

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
                    mBoardModel.setInputEnabled(false)
                    if (isSound) {
                        completionPlayer.start()
                    }
                    event.tiles.forEach {
                        it.matched = true
                        it.revealed = true
                    }
                    animatePairedTiles(event.tiles) {
                        event.tiles.forEach {
                            it.removeOnClickListener()
                        }
                        mBoardModel.setInputEnabled(true)
                    }
                }
                GameStates.NoMatch -> {
                    mBoardModel.setInputEnabled(false)
                    if (isSound) {
                        negativePLayer.start()
                    }
                    event.tiles.forEach { it.revealed = true }
                    animateNotPairedTiles(event.tiles) {
                        event.tiles.forEach {
                            it.revealed = false
                        }
                        mBoardModel.setInputEnabled(true)
                    }
                }
                GameStates.Finished -> {
                    mBoardModel.setInputEnabled(false)
                    if (isSound) {
                        completionPlayer.start()
                    }
                    event.tiles.forEach {
                        it.matched = true
                        it.revealed = true
                    }
                    animatePairedTiles(event.tiles) {
                        event.tiles.forEach {
                            it.removeOnClickListener()
                        }
                        mBoardModel.setInputEnabled(true)
                        Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        completionPlayer = MediaPlayer.create(applicationContext, R.raw.completion)
        negativePLayer = MediaPlayer.create(applicationContext, R.raw.negative_guitar)
    }

    override fun onPause() {
        super.onPause()
        completionPlayer.release()
        negativePLayer.release()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::mBoardModel.isInitialized) {
            outState.putIntArray(STATE_BOARD, mBoardModel.getState())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.board_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.board_activity_sound -> {
                if (item.icon?.constantState == resources.getDrawable(
                        R.drawable.baseline_volume_up_24,
                        theme
                    ).constantState
                ) {
                    Toast.makeText(this, "Sound turn off", Toast.LENGTH_SHORT).show()
                    item.setIcon(R.drawable.baseline_volume_off_24)
                    isSound = false
                } else {
                    Toast.makeText(this, "Sound turn on", Toast.LENGTH_SHORT).show()
                    item.setIcon(R.drawable.baseline_volume_up_24)
                    isSound = true
                }
            }
        }
        return false
    }

    private fun animatePairedTiles(tiles: List<Tile>, action: Runnable) {
        var finishedAnimations = 0
        tiles.forEach { tile ->
            animatePairedButton(tile.button) {
                finishedAnimations++
                if (finishedAnimations == tiles.size) {
                    action.run()
                }
            }
        }
    }

    private fun animateNotPairedTiles(tiles: List<Tile>, action: Runnable) {
        var finishedAnimations = 0
        tiles.forEach { tile ->
            animateNotPairedButton(tile.button) {
                finishedAnimations++
                if (finishedAnimations == tiles.size) {
                    action.run()
                }
            }
        }
    }

    private fun animatePairedButton(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()
        val random = Random()
        button.pivotX = random.nextFloat() * 200f
        button.pivotY = random.nextFloat() * 200f
        val rotation = ObjectAnimator.ofFloat(button, "rotation", 1080f)
        val scallingX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 4f)
        val scallingY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 4f)
        val fade = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f)
        set.startDelay = 500
        set.duration = 2000
        set.interpolator = DecelerateInterpolator()
        set.playTogether(rotation, scallingX, scallingY, fade)
        set.addListener(object : AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
            }

            override fun onAnimationEnd(animator: Animator) {
                button.scaleX = 1f
                button.scaleY = 1f
                button.alpha = 0.0f
                action.run()
            }

            override fun onAnimationCancel(animator: Animator) {
            }

            override fun onAnimationRepeat(animator: Animator) {
            }
        })
        set.start()
    }

    private fun animateNotPairedButton(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()
        val firstRotation = ObjectAnimator.ofFloat(button, "rotation", 0f, -12f)
        val secondRotation = ObjectAnimator.ofFloat(button, "rotation", -12f, 12f)
        val thirdRotation = ObjectAnimator.ofFloat(button, "rotation", 12f, -8f)
        val lastRotation = ObjectAnimator.ofFloat(button, "rotation", -8f, 0f)
        set.duration = 1000
        set.interpolator = DecelerateInterpolator()
        set.playSequentially(firstRotation, secondRotation, thirdRotation, lastRotation)
        set.addListener(object : AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
            }

            override fun onAnimationEnd(animator: Animator) {
                button.rotation = 0f
                action.run()
            }

            override fun onAnimationCancel(animator: Animator) {
            }

            override fun onAnimationRepeat(animator: Animator) {
            }
        })
        set.start()
    }

    companion object {
        const val EXTRA_ROWS = "rows"
        const val EXTRA_COLUMNS = "columns"
        private const val STATE_BOARD = "state_board"
    }
}
