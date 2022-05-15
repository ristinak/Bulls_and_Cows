//import java.io.FileNotFoundException
//import java.nio.file.{Files, Paths}
//import java.text.SimpleDateFormat
//import java.time.format.DateTimeFormatter
//import java.time.{ZoneOffset, ZonedDateTime}
//import java.util.Calendar
//import scala.collection.mutable.ArrayBuffer
import scala.io.StdIn.readLine
//import java.sql.DriverManager

object BullsAndCowsTheGame extends App {

  //val saveDst = "src/resources/bulls_and_cows/scores.csv"
  val db = new BullsAndCowsDB("src/resources/BullsAndCows/BullsAndCows.db")
  val gameEndCondition = "4B0C"
//  val minGuess = 123 //all digits must be different
//  val maxGuess = 9876
  val playerA = readLine("Player A, what is your name? ")
  var playerB = readLine("Player B, what is your name? (press ENTER for computer) ")
  println(s"Player A -  $playerA and Player B - $playerB let us play Bulls and Cows!")
  if (playerB == "") playerB = "COMPUTER"
  val isPlayerAStarting = true
  val BullsAndCowsGame = new BullsAndCows(playerA, playerB, gameEndCondition, isPlayerAStarting)

  def getGuess:String = {
    var needs4DigitNumber = true //we use this as a flag for our code
    var myGuess = "0123"
    //we keep going until we get an input which is 4 unique digits
    while (needs4DigitNumber) {
      val guessInput = readLine(s"What's your 4-digit guess, ${BullsAndCowsGame.currentPlayer}? ").trim
      if (guessInput.length != 4) {
        println("The number must have 4 digits. Try again.")
      } else if (!guessInput.forall(Character.isDigit)) {
        println("Please enter a number. Try again.")
      } else if (guessInput.distinct != guessInput) {
        println("All digits need to be unique. Try again.")
      } else {
        myGuess = guessInput
        needs4DigitNumber = false
      }
    }
    myGuess
  }

  while (BullsAndCowsGame.isGameActive) {
    val guess = getGuess
    BullsAndCowsGame.takeAGuess(guess)
    BullsAndCowsGame.nextPlayer()
    BullsAndCowsGame.showStatus()
  }

  BullsAndCowsGame.printGuesses()

  db.migrate()
  db.insertResult(BullsAndCowsGame.getWinner, BullsAndCowsGame.getLoser)
  db.insertFullScore(BullsAndCowsGame.getGuesses)
  db.conn.close()

}
