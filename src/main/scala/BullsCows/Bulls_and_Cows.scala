package BullsCows

import scala.io.StdIn.readLine

/**
 * Object for launching the Bulls and Cows game
 * @author Marija Safronova, Kristina Kudriašova
 * @version 1.0.0
 * {{{Game starts with creating new BullsAndCows class.
 * Game is played.
 * All guesses are printed.
 * If new game is needed, the process repeats.
 * If not needed, app closes.}}}
 * @see [[https://en.wikipedia.org/wiki/Bulls_and_Cows]] on how to play
 */
object Bulls_and_Cows extends App {
  var isNewGameNeeded = true

  while (isNewGameNeeded) {
    val BullsAndCowsGame = new BullsAndCows
    BullsAndCowsGame.play
    BullsAndCowsGame.printGuesses
    val nextGameInput = readLine("Do you want to play another game? (Y/N) ")
    if (!nextGameInput.toLowerCase.startsWith("y")) {
      isNewGameNeeded = false
    }
  }
  System.exit(0)

//    BullsAndCowsGame.db.dropAllTables()
}
