package BullsCows

import scala.collection.mutable.ArrayBuffer
import scala.io.StdIn.readLine
import scala.util.Random

/**
 * A game of Bulls and Cows
 * has methods for 3 different game modes
 * main method - play
 * */
class BullsAndCows {
  /** It is playerA's turn */
  private var isPlayerATurn = true

  /** Game state */
  private var gameWon = false

  /** Storing players' secret numbers for the game */
  private var numberA = List[Int]()
  private var numberB = List[Int]()

  /** Storing players' names for the game */
  var playerA = ""
  var playerB = ""

  val thankYou = "\n▀█▀ █░█ ▄▀█ █▄░█ █▄▀   █▄█ █▀█ █░█\n░█░ █▀█ █▀█ █░▀█ █░█   ░█░ █▄█ █▄█"
  val startGame = "\n█▀ ▀█▀ ▄▀█ █▀█ ▀█▀   █▀▀ ▄▀█ █▀▄▀█ █▀▀\n▄█ ░█░ █▀█ █▀▄ ░█░   █▄█ █▀█ █░▀░█ ██▄"

  /** Creating the database */
  val db = new BullsAndCowsDatabase("src/resources/db/game.db")
  db.migrate()

  private var gameOption = 0

  /** Array buffer for storing all guesses and their results */
  private val guessesArray: ArrayBuffer[(String, String)] = ArrayBuffer()

  /** True if playerB is COMPUTER. */
  private def isSecondPlayerAComputer: Boolean = playerB == "COMPUTER"

  /** Toggles next player. */
  private def nextPlayer: Unit = isPlayerATurn = !isPlayerATurn

  /** Returns an array of guesses and their outcomes. */
  private def getGuesses: Array[(String, String)] = guessesArray.toArray

  /**
   * Returns an integer of distinct digits of required length
   * @param msg - message to be displayed when asking for player input
   * @param numOfDigits - the number of digits needed to be entered by a player
   */
  private def getInteger(msg: String, numOfDigits: Int): Int = {
    var needsInt = true
    var playerInt = 0
    while (needsInt) {
      val input = readLine(msg).trim
      if (input.length != numOfDigits) {
        println(s"The number needs $numOfDigits digits. ")
      } else if (!input.forall(Character.isDigit)) {
        println("Please enter a number. ")
      } else if (input.distinct != input) {
        println("All digits need to be unique. ")
      } else {
        playerInt = input.toInt
        needsInt = false
      }
    }
    playerInt
  }

  /** Takes an integer and returns a list of its digits. */
  private def getIntegerList(input: Int): List[Int] = input.toString.map(_.asDigit).toList

  /** Returns the current player. */
  private def getCurrentPlayer: String = if (isPlayerATurn) playerA else playerB

  /** Returns the other player, not the current one. */
  private def getOppositePlayer: String = if (isPlayerATurn) playerB else playerA

  /** Returns a player's entry of distinct 4-digit guess in List[Int] form. */
  private def getGuess(msg: String): List[Int] = {
    val guessInt = getInteger(msg, 4)
    var guess = getIntegerList(guessInt)
    guess
  }

  /** Stores a player's or computer's distinct 4-digit secret number in List[Int] form. */
  private def getPlayerSecretNumber: Unit = {
    var inputList = List[Int]()

    if (!isPlayerATurn && isSecondPlayerAComputer) {
      inputList = randomizeNumber
      println(s"Computer generated a number: ${inputList.mkString}")

      numberB = inputList
    }
    else {
      val input = getInteger(s"Player $getCurrentPlayer, enter your secret 4-digit number: ", 4)
      inputList = getIntegerList(input)

      if (isPlayerATurn)
        numberA = inputList
      else
        numberB = inputList
    }
  }

  /**
   * Gets playerA and playerB's names (or COMPUTER in mode 2)
   * Inserts players' names in DB if they are new players
   * Prints a greeting
   * @param mode 1: player vs player, 2: player vs computer, 3: 2 players vs computer
   */
  private def Greetings(mode: Int): Unit = {
    mode match {
      case 2 | 3 =>
        playerA = readLine("Player A what is your name? ")
        playerB = readLine("Player B what is your name? ")
        while (playerA==playerB)
        {
          playerB = readLine(s"Player B $playerB, your name is already taken, please, choose another nickname for your game: ")
        }
      case 1 =>
        playerA = readLine("Player A what is your name? ")
        playerB = "COMPUTER"
    }
    db.insertNewPlayer(playerA)
    db.insertNewPlayer(playerB)

    println("\nGame begins!")
    println(s"$playerA VS $playerB\n")
  }

  /** Displays the menu with mode choices. */
  private def menu(): Unit = {
    println("*" * 10 + " Bulls & Cows " + "*" * 10)
    println("-" * 10 + "  Game Menu  " + "-" * 10)
    println("1. Single Player Mode")
    println("2. Two Player Mode")
    println("3. Player vs Player")
    println("4. Scoreboard")
    println("5. Rules")
    println("6. Exit")
  }

  /** Returns a list of 4 distinct digits not starting with 0. */
  private def randomizeNumber: List[Int] = {
    var number = List[Int]()
    while (number.size < 4) { // player vs computer
      val d = Random.nextInt(10)
      if (!number.contains(d))
        if (number.isEmpty && d != 0)
          number = number :+ d
        else if (number.nonEmpty)
          number = number :+ d
    }
    number
  }

  /**
   * Returns the outcome of a guess in the form of XBXC string.
   * Adds guesses to the guessesArray buffer.
   * If there is a winner, adds game result to the database.
   */
  private def BullsCowsCounter(correctAnswer: List[Int], guess: List[Int]): String = {
    var cows = 0
    var bulls = 0
    var guessResult = "0B0C"

    if (!gameWon) {
      for (i <- 0 to 3) {
        if (correctAnswer(i) == guess(i)) {
          bulls += 1
        }
        else {
          if (correctAnswer.contains(guess(i)))
            cows += 1
        }
      }

      if (bulls == 4) {
        gameWon = true
        println(s"$getCurrentPlayer you won, congratulations!")
        guessResult = "4B0C"
        db.insertResult(getCurrentPlayer, getOppositePlayer)
        db.insertMode(getModeName(gameOption))
        guessesArray.append((guess.mkString, guessResult))
        db.insertHistory(getGuesses)
      }
      else {
        println( s"Bulls: $bulls Cows: $cows")
        guessResult = s"$bulls" + "B" + s"$cows" + "C"
        guessesArray.append((guess.mkString, guessResult))
      }
    }
    guessResult
  }

  /** Returns and prints an array of all guesses and their outcomes. */
  def printGuesses(): Array[(String, String)] = {
    println("*" * 10 + " List of your guesses: " + "*" * 10)
    for (((guess, result), index) <- getGuesses.zipWithIndex) {
      val playerName = {
        if (playerB == "COMPUTER") playerA
        else if (index % 2 == 0) playerA else playerB
      }
      println(s"Guess ${index + 1}. $playerName guessed $guess resulting in $result.")
    }
    getGuesses
  }

  /**
   * Returns game mode of that game.
   * Game mode is saved in database later.
   */
  def getModeName(modeId: Int): String = {
    modeId match {
      case 1 => "Single Player"
      case 2 => "Two Player"
      case 3 => "Player VS Player"
    }
  }

  /**
   * Mode 3: Player Vs Player
   * Both players enter their secret numbers
   * They take turns guessing the opponent's number
   */
  private def playerVsPlayer: Unit = {
    Greetings(3)
    getPlayerSecretNumber
    println("\n" * 10 + thankYou + "\n" * 5)
    nextPlayer
    getPlayerSecretNumber
    println("\n" * 10 + startGame + "\n" * 5)
    nextPlayer

    while (!gameWon) {
      if (isPlayerATurn) {
        val guess = getGuess(s"Player $getCurrentPlayer, your guess: ")
        BullsCowsCounter(numberB, guess)
        nextPlayer
      }
      else {
        val guess = getGuess(s"Player $getCurrentPlayer, your guess: ")
        BullsCowsCounter(numberA, guess)
        nextPlayer
      }
    }
  }

  /**
   * Mode 1: Single Player Mode
   * Player guesses the secret number generated by the program.
   */
  private def singlePlayerVsComputer: Unit = {
    Greetings(1)
    nextPlayer
    getPlayerSecretNumber
    println("\n" * 10 + startGame + "\n" * 5)
    nextPlayer

    while (!gameWon) {
      val guess = getGuess(s"Player $getCurrentPlayer, your guess: ")
      BullsCowsCounter(numberB, guess)
    }
  }

  /**
   * Mode 2: Two Player Mode
   * Players take turns guessing the secret number generated by the program
   */
  private def twoPlayersVsComputer: Unit = {
    Greetings(2)
    val computerSecretNumberList = randomizeNumber
    println(s"The secret number is ${computerSecretNumberList.mkString}")
    println("\n" * 10 + startGame + "\n" * 5)
    while (!gameWon) {
      val guess = getGuess(s"Player $getCurrentPlayer, your guess: ")
      BullsCowsCounter(computerSecretNumberList, guess)
      nextPlayer
    }
  }

  /** Prints the scoreboard of top players and number of wins. */
  private def scoreboard: Unit = {
    db.getScoreboard
    println("\n"*2)
    play
  }

  /** Prints the rules of the game. */
  private def rules: Unit = {
    println("\n"*2)
    println(
      "All 4 digits in the secret number are different.\n" +
        "The secret number cannot start with zero.\n" +
        "The digits of the number guessed must also be different and cannot start with zero.\n" +
        "If your guess has matching digits in their right positions, they are \"bulls\",\n" +
        "if they are in different positions, they are \"cows.\"\n\n" +
        "In Mode 1 (Single Player Mode), the player guesses a computer-generated secret number.\n" +
        "In Mode 2 (Two Player Mode), players take turns to guess a computer-generated secret number.\n" +
        "In Mode 3 (Player vs Player Mode), both players enter their secret numbers\n" +
        "and then take turns to guess the opponent's number\n" +
        "Good luck!\n"
    )
    play
  }

  /** Displays game menu, asks to choose game mode, launches that mode */
  def play: Unit = {
    menu
    gameOption = getInteger("Choose game mode: ", 1)

    gameOption match {
      case 1 => singlePlayerVsComputer
      case 2 => twoPlayersVsComputer
      case 3 => playerVsPlayer
      case 4 => scoreboard
      case 5 => rules
      case 6 => System.exit(0)
      case _ => play
    }
  }
}
