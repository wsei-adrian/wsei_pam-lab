package pl.wsei.pam.lab03

import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import pl.wsei.pam.R
import java.util.Stack

class MemoryBoardView(
    private val gridLayout: GridLayout,
    private val cols: Int,
    private val rows: Int
) {
    private val tiles: MutableMap<String, Tile> = linkedMapOf()
    private val icons: List<Int> = listOf(
        R.drawable.baseline_rocket_launch_24,
        R.drawable.baseline_home_24,
        R.drawable.baseline_star_24,
        R.drawable.baseline_favorite_24,
        R.drawable.baseline_camera_alt_24,
        R.drawable.baseline_directions_car_24,
        R.drawable.baseline_cake_24,
        R.drawable.baseline_lightbulb_24,
        R.drawable.baseline_music_note_24,
        R.drawable.baseline_work_24,
        R.drawable.baseline_school_24,
        R.drawable.baseline_phone_24,
        R.drawable.baseline_email_24,
        R.drawable.baseline_map_24,
        R.drawable.baseline_alarm_24,
        R.drawable.baseline_shopping_cart_24,
        R.drawable.baseline_sports_soccer_24,
        R.drawable.baseline_build_24
    )
    private val deckResource: Int = R.drawable.deck
    private var onGameChangeStateListener: (MemoryGameEvent) -> Unit = {}
    private val matchedPair: Stack<Tile> = Stack()
    private var logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)
    private var inputEnabled: Boolean = true

    init {
        createBoard()
    }

    fun getState(): IntArray {
        return tiles.values
            .map { if (it.matched) it.tileResource else HIDDEN_TILE }
            .toIntArray()
    }

    fun setState(state: IntArray) {
        if (state.size != cols * rows) {
            return
        }
        createBoard(state)
        val restoredMatches = state.count { it != HIDDEN_TILE } / 2
        logic = MemoryGameLogic(cols * rows / 2, restoredMatches)
    }

    fun setOnGameChangeListener(listener: (event: MemoryGameEvent) -> Unit) {
        onGameChangeStateListener = listener
    }

    fun setInputEnabled(enabled: Boolean) {
        inputEnabled = enabled
    }

    private fun createBoard(state: IntArray? = null) {
        val selectedIcons = icons.take(cols * rows / 2)
        val shuffledIcons = buildIconList(selectedIcons, state).toMutableList()

        gridLayout.removeAllViews()
        gridLayout.columnCount = cols
        gridLayout.rowCount = rows
        tiles.clear()
        matchedPair.clear()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val index = row * cols + col
                val resourceImage = shuffledIcons.removeAt(0)
                val btn = ImageButton(gridLayout.context).also {
                    it.tag = "${row}x${col}"
                    it.contentDescription = "Memory tile ${row + 1}, ${col + 1}"
                    it.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    it.adjustViewBounds = true
                    it.setPadding(18, 18, 18, 18)
                    val layoutParams = GridLayout.LayoutParams()
                    layoutParams.width = 0
                    layoutParams.height = 0
                    layoutParams.setGravity(Gravity.FILL)
                    layoutParams.columnSpec = GridLayout.spec(col, 1, 1f)
                    layoutParams.rowSpec = GridLayout.spec(row, 1, 1f)
                    layoutParams.setMargins(8, 8, 8, 8)
                    it.layoutParams = layoutParams
                    gridLayout.addView(it)
                }
                val tile = addTile(btn, resourceImage)
                if (state != null && state.getOrNull(index) != HIDDEN_TILE) {
                    tile.matched = true
                    tile.revealed = true
                    tile.removeOnClickListener()
                }
            }
        }
    }

    private fun buildIconList(selectedIcons: List<Int>, state: IntArray?): List<Int> {
        if (state == null || state.size != cols * rows) {
            return mutableListOf<Int>().also {
                it.addAll(selectedIcons)
                it.addAll(selectedIcons)
                it.shuffle()
            }
        }

        val restoredCounts = state
            .filter { it != HIDDEN_TILE }
            .groupingBy { it }
            .eachCount()
        val hiddenIcons = mutableListOf<Int>()
        selectedIcons.forEach { icon ->
            repeat(2 - (restoredCounts[icon] ?: 0)) {
                hiddenIcons.add(icon)
            }
        }
        hiddenIcons.shuffle()

        return state.map {
            if (it == HIDDEN_TILE) {
                hiddenIcons.removeAt(0)
            } else {
                it
            }
        }
    }

    private fun onClickTile(v: View) {
        if (!inputEnabled) {
            return
        }

        val tile = tiles[v.tag.toString()] ?: return
        if (tile.revealed || tile.matched) {
            return
        }

        matchedPair.push(tile)
        val matchResult = logic.process {
            tile.tileResource
        }
        onGameChangeStateListener(MemoryGameEvent(matchedPair.toList(), matchResult))
        if (matchResult != GameStates.Matching) {
            matchedPair.clear()
        }
    }

    private fun addTile(button: ImageButton, resourceImage: Int): Tile {
        button.setOnClickListener(::onClickTile)
        val tile = Tile(button, resourceImage, deckResource)
        tiles[button.tag.toString()] = tile
        return tile
    }

    companion object {
        private const val HIDDEN_TILE = -1
    }
}
