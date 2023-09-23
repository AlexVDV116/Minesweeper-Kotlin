package minesweeper

import java.util.*
import kotlin.random.Random
import kotlin.system.exitProcess


@Suppress("SameParameterValue")
class Minesweeper(private val columns: Int, private val rows: Int) {
    // Create two boards, one for the player and one for the game
    // A board is a two-dimensional mutableList that holds all the rows as an own mutableList
    private val gameBoard = Array(rows) { mutableListOf<String>() }
    private val playerBoard = Array(rows) { mutableListOf<String>() }
    private var victoryBoard = Array(rows) { mutableListOf<String>() }
    private var firstTurn = true
    private var falseMines = 0
    private var numberOfMines = 0
    private var freeCells = 0

    fun play() {
        println("How many mines do you want on the field?")
        numberOfMines = readln().toInt()
        createMinefield()

        printMinefield(playerBoard)
        printMinefield(gameBoard)
        printMinefield(victoryBoard)

        userTurn()
    }

    private fun endGame(condition: String) {
        printMinefield(victoryBoard)
        when (condition) {
            "win" -> println("Congratulations! You found all the mines!")
            "mine" -> println("You stepped on a mine and failed!")
            "cells" -> println("You successfully explored all safe cells!")
        }
        exitProcess(0)
    }

    private fun isGameWon(): Boolean {
        var hasWon = false
        // If the user has correctly marked all the mines locations
        if (falseMines == 0 && numberOfMines == 0) hasWon = true

        // Calculate the remaining free cells
        var count = 0
        playerBoard.forEach { list ->
            count += list.count { it == "." }
        }
        freeCells = count - numberOfMines

        // If the player explored all safe cells and only unexplored mines are left
        if (freeCells == 0) hasWon = true

        println("freeCells: $freeCells")
        println("count: $count")
        println("numberOfMines $numberOfMines")

        return hasWon
    }

    private fun userTurn() {
        println("Set/unset mine marks or claim a cell as free:")
        val scanner = Scanner(System.`in`)
        val col = scanner.nextInt() - 1 // Column
        val row = scanner.nextInt() - 1 // Row
        val cmd = scanner.next().lowercase() // Command

        if (firstTurn) {
            addMines(col, row, numberOfMines)
            addHints()
        }

        when (cmd) {
            "free" -> explore(col, row)
            "mine" -> markMine(col, row)
        }
        firstTurn = false

        // After each turn check if any of the two win conditions are met, if true end the game
        if (isGameWon()) {
            endGame("win")
        } else {
            printMinefield(playerBoard)
            printMinefield(gameBoard)
            printMinefield(victoryBoard)
            userTurn()
        }
    }

    private fun explore(col: Int, row: Int) {
        // If the explored cell is a mine
        if (gameBoard[row][col] == "X") {
            endGame("mine")
        }

        // If the explored cell is empty but surrounding cells contain mines reveal the number of mines around it
        if (gameBoard[row][col] != "." || gameBoard[row][col] != "X") {
            playerBoard[row][col] = gameBoard[row][col]
        }

        // If the explored cell is empty and has no mines around
        if (gameBoard[row][col] == ".") {
            floodFill(gameBoard, col, row, "/")
        }

    }

    private fun markMine(col: Int, row: Int) {
        // If the cell contains a mine
        if (gameBoard[row][col] == "X") {
            // If the cell is already marked by the player reset the cell and add the mine to numberOfMines counter
            if (playerBoard[row][col] == "*") {
                playerBoard[row][col] = "."
                numberOfMines += 1
            } else {
                playerBoard[row][col] = "*"
                numberOfMines -= 1
            }
            // If the cell does not contain a mine
        } else {
            // If the cell is already marked by the player reset the cell and add the mine to falseMines counter
            if (playerBoard[row][col] == "*") {
                playerBoard[row][col] = "."
                falseMines -= 1
            } else {
                playerBoard[row][col] = "*"
                falseMines +=1
            }
        }
    }
    
    private fun createMinefield() {
        // For each row add an element '.' depending on the width of the minefield
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                gameBoard[i].add(".")
                playerBoard[i].add(".")
                victoryBoard[i].add(".")
            }
        }

        // Replace all 'X' with a '*' on the victory board, so we can use it as a comparison to the player board
        for (row in victoryBoard) {
            Collections.replaceAll(row, "X", "*")
        }
    }

    private fun addMines(col: Int, row: Int, numberOfMines: Int) {
        //var mineCounter = 0

        // Add a number of mines to a random row of the game board
//        while (mineCounter != numberOfMines) {
//            // Generate a random sequence of number excluding the players first chosen cell
//            val rowList = (0 until rows).toMutableList()
//            rowList.remove(row)
//            val shuffledRowList = rowList.shuffled()
//            val randomRow = shuffledRowList.first()
//
//            val colList = (0 until columns).toMutableList()
//            colList.remove(col)
//            val shuffledColList = colList.shuffled()
//            val randomColumn = shuffledColList.first()
//
//            if (gameBoard[randomRow][randomColumn] != "X") {
//                    gameBoard[randomRow][randomColumn] = "X"
//                    victoryBoard[randomRow][randomColumn] = "X"
//                    mineCounter += 1
//                }

        var placedMines = 0
        while (placedMines < numberOfMines) {
            val randomRow = Random.nextInt(rows)
            val randomCol = Random.nextInt(columns)
            if (gameBoard[randomRow][randomCol] != "X") {
                gameBoard[randomRow][randomCol] = "X"
                victoryBoard[randomRow][randomCol] = "X"
                placedMines++
            }
        }
    }

    // Function that checks whether the given cell is valid
    private fun isValid(row: Int, column: Int): Boolean {
        return row in 0 until rows && column in 0 until columns
    }

    // Function that checks whether the given cell is a mine
    private fun isMine(row: Int, column: Int): Boolean {
        return gameBoard[row][column] == "X"
    }

    // Function that returns the amount of adjacent cells with a mine
    private fun countAdjacentMines(row: Int, column: Int): Int {
        var count = 0

        if (isValid(row - 1, column)) {
            if (isMine(row - 1, column)) {
                count++
            }
        }

        if (isValid(row + 1, column)) {
            if (isMine(row + 1, column)) {
                count++
            }
        }

        if (isValid(row, column + 1)) {
            if (isMine(row, column + 1)) {
                count++
            }
        }

        if (isValid(row, column - 1)) {
            if (isMine(row, column - 1)) {
                count++
            }
        }

        if (isValid(row - 1, column + 1)) {
            if (isMine(row - 1, column + 1)) {
                count++
            }
        }

        if (isValid(row - 1, column - 1)) {
            if (isMine(row - 1, column - 1)) {
                count++
            }
        }

        if (isValid(row + 1, column + 1)) {
            if (isMine(row + 1, column + 1)) {
                count++
            }
        }

        if (isValid(row + 1, column - 1)) {
            if (isMine(row + 1, column - 1)) {
                count++
            }
        }

        return count
    }

    // Function that loops trough empty cells and adds the number of adjacent mines to that cell
    private fun addHints() {
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                if (!isMine(row, column)) {
                    if (countAdjacentMines(row, column) != 0) {
                        gameBoard[row][column] = countAdjacentMines(row, column).toString()
                    }
                }
            }
        }
    }

    // Print the player board that contains the hints on the empty cells
    private fun printMinefield(board: Array<MutableList<String>>) {
        print(" │")
        for (i in 1..columns) print(i)
        println("│")
        print("—│")
        for (i in 1..columns) print("—")
        println("│")
        for (i in 0 until rows) {
            print("${i + 1}│")
            when (board) {
                playerBoard -> print(playerBoard[i].joinToString(""))
                gameBoard -> print(gameBoard[i].joinToString(""))
                victoryBoard -> print(victoryBoard[i].joinToString(""))
            }
            println("│")
        }
        print("—│")
        for (i in 1..columns) print("—")
        println("│")
    }

    // A recursive function to replace previous value 'prevValue' at '(x, y)'
    // and all surrounding cells of (x, y) with new value 'newValue'
    private fun floodFillUtil(
        board: Array<MutableList<String>>, col: Int, row: Int,
        prevValue: String, newValue: String
    ) {
        // If x or y is outside the screen, then return.
        // If value of board[x][y] is not same as prevValue (X or number), then return
        if (row < 0 || row >= rows || col < 0 || col >= columns) return

        // If the cell contains a number, display this number on the playerBoard
        val numbers = setOf("1", "2", "3", "4", "5", "6", "7", "8")
        if (gameBoard[row][col] in numbers) playerBoard[row][col] = gameBoard[row][col]

        // If the cell contains a player mark and is not a mine replace mine marker with newValue
        if (playerBoard[row][col] == "*" && gameBoard[row][col] != "X") playerBoard[row][col] = "."

        if (gameBoard[row][col] != prevValue) return
        if (playerBoard[row][col] != prevValue) return

        // Else replace the value at (x, y) with the new value
        playerBoard[row][col] = newValue
        gameBoard[row][col] = newValue

        // Recur for north, east, south and west
        floodFillUtil(board, col + 1, row, prevValue, newValue)
        floodFillUtil(board, col - 1, row, prevValue, newValue)
        floodFillUtil(board, col, row + 1, prevValue, newValue)
        floodFillUtil(board, col, row - 1, prevValue, newValue)
        floodFillUtil(board, col + 1, row + 1, prevValue, newValue)
        floodFillUtil(board, col + 1, row - 1, prevValue, newValue)
        floodFillUtil(board, col - 1, row - 1, prevValue, newValue)
        floodFillUtil(board, col - 1, row + 1, prevValue, newValue)


    }

    // It mainly finds the previous color on (x, y) and calls floodFillUtil()
    private fun floodFill(board: Array<MutableList<String>>, col: Int, row: Int, newValue: String) {
        val prevValue = board[row][col]
        if (prevValue == newValue) return
        floodFillUtil(board, col, row, prevValue, newValue)
    }

}

fun main() {
    val minesweeper = Minesweeper(9, 9)
    minesweeper.play()
}
