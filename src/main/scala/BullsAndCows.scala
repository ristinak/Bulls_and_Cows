import java.nio.file.{Files, Paths}
import java.text.SimpleDateFormat
import java.time.{ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.util.Calendar
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class BullsAndCows(
                  val playerA:String,
                  val playerB:String,
                  val gameEndCondition:String = "4B0C",
//                  val minGuess:String = "0123",
//                  val maxGuess:String = "9876",
                  var isPlayerATurn:Boolean = true) {
  println("Created a new game object of CowsAndBulls")
  var currentState = "0B0C" //0 bulls and 0 cows
  var currentPlayer:String = if (isPlayerATurn) playerA else playerB
  var guessesArray:ArrayBuffer[(String, String)] = ArrayBuffer()

  val requiredDigits = 4
  val digits:List[Char] = List('0','1','2','3','4','5','6','7','8','9')
//  Random.setSeed(42)
  val theAnswer:String = Random.shuffle(digits).slice(0, requiredDigits).mkString("")
  println(s"The answer is $theAnswer") //TODO delete this line later

  def isGameActive: Boolean = currentState != gameEndCondition

  def isCurrentPlayerComputer: Boolean = currentPlayer == "COMPUTER"

  def getGuesses: Array[(String, String)] = guessesArray.toArray

  def nextPlayer(): String = {
    isPlayerATurn = !isPlayerATurn
    currentPlayer = if (isPlayerATurn) playerA else playerB
    currentPlayer
  }

  def getLoser: String = {
    if (isGameActive) "N/A" // could be empty string
    else currentPlayer //since currentPlayer with no moves to make is the winner
  }

  def getWinner: String = {
    if (isGameActive) "N/A" // could be empty string
    else { //game is finished
      if (isPlayerATurn) playerB else playerA
    }
  }

  def showStatus(): Unit = {
    if (currentState != gameEndCondition) {
      println(s"The last guess resulted in $currentState.")
      println(s"It is $currentPlayer's turn.")
    }
    else {
      println(s"The game has ended. $currentPlayer has won. ")
      println(s"Better luck next time $getLoser.")
    }
  }

  def takeAGuess(playerGuess:String): String = {
    var bulls = 0
    var cows = 0
    for (i <- 0 until playerGuess.length) {
      if (playerGuess(i) == theAnswer(i)) bulls += 1
    }
    for (i <- 0 until playerGuess.length) {
      for (j <- 0 until theAnswer.length) {
        if ((playerGuess(i) == theAnswer(j)) && (i != j)) cows += 1
      }
    }
    currentState = s"$bulls" + "B" + s"$cows" + "C"
    guessesArray += ((playerGuess, currentState))
    currentState
  }

  def printGuesses(): Array[(String, String)] = {
    for (((guess, result), index) <- guessesArray.zipWithIndex) {
      val playerName = if (index % 2 == 0) playerA else playerB
      println(s"Guess ${index + 1}. $playerName guessed $guess resulting in $result.")
    }
    guessesArray.toArray
  }

}
