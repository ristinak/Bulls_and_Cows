package BullsCows

import org.sqlite.SQLiteException

import java.sql.DriverManager

/**
 * DB class with its methods
 * @param dbPath - filepath where the database will be saved
 */
class  BullsAndCowsDatabase(val dbPath: String) {
  val url = s"jdbc:sqlite:$dbPath"

  val dbConnection = DriverManager.getConnection(url)
  println(s"Opened Database at ${dbConnection.getMetaData.getURL}")

  /** Drops all tables in database if they exist */
  def dropAllTables(): Unit = {
    val query = dbConnection.createStatement()
    val sql1 =
      """
        |DROP TABLE IF EXISTS players
        |""".stripMargin
    val sql2 =
      """
        |DROP TABLE IF EXISTS history
        |""".stripMargin
    val sql3 =
      """
        |DROP TABLE IF EXISTS results
        |""".stripMargin
    val sql4 =
      """
        |DROP TABLE IF EXISTS modes
        |""".stripMargin

    query.addBatch(sql1)
    query.addBatch(sql2)
    query.addBatch(sql3)
    query.addBatch(sql4)
    query.executeBatch()
  }

  /** Creates tables players, results, history, modes in database if they do not exist */
  def migrate(): Unit = {
    val query = dbConnection.createStatement()

    val sql1 =
      """
        |CREATE TABLE IF NOT EXISTS players (
        |id INTEGER PRIMARY KEY,
        |player TEXT NOT NULL UNIQUE,
        |created TEXT
        |);
        |""".stripMargin
    val sql2 =
      """
        |CREATE TABLE IF NOT EXISTS results (
        |id INTEGER PRIMARY KEY,
        |winner INTEGER NOT NULL,
        |loser INTEGER NOT NULL,
        |created TEXT,
        |FOREIGN KEY (winner) REFERENCES players (id),
        |FOREIGN KEY (loser) REFERENCES players (id)
        |);
        |""".stripMargin
    val sql3 =
      """
        |CREATE TABLE IF NOT EXISTS history (
        |id INTEGER PRIMARY KEY,
        |game_id INTEGER NOT NULL,
        |turn INTEGER NOT NULL,
        |guess TEXT NOT NULL,
        |outcome TEXT NOT NULL,
        |created TEXT,
        | FOREIGN KEY (game_id) REFERENCES results (id)
        |);
        |""".stripMargin

    val sql4 =
      """
        |CREATE TABLE IF NOT EXISTS modes (
        |id INTEGER PRIMARY KEY,
        |game_id INTEGER NOT NULL,
        |mode TEXT,
        |created TEXT,
        | FOREIGN KEY (game_id) REFERENCES results (id)
        |);
        |""".stripMargin

    query.addBatch(sql1)
    query.addBatch(sql2)
    query.addBatch(sql3)
    query.addBatch(sql4)

    query.executeBatch()
  }

  /**
   * Returns last id in a specific table
   * @param table
   * @return last id
   */
  private def getLastId(table: String): Int = {
    val query = dbConnection.createStatement()
    val sql =
      s"""
         |SELECT MAX(id) id FROM $table;
         |""".stripMargin
    val resultSet = query.executeQuery(sql)
    val id = resultSet.getInt("id")

    query.close()

    id
  }

  /**
   * Returns player id in the db using player's name
   * @param player
   * @return playerId
   * Usage: get player id in specific game to write it into the results table
   */
  private def getPlayerId(player: String): Int = {
    val sql =
      s"""
         |SELECT id FROM players
         |WHERE player == ?
         |""".stripMargin

    val query = dbConnection.prepareStatement(sql)

    query.setString(1, player)

    val resultSet = query.executeQuery
    val id = resultSet.getInt("id")

    resultSet.close()
    id
  }

  /**
   * Prints players' ratings from database
   */
  def getScoreboard: Unit = {
    val sql =
      """
        |SELECT p.player, COUNT(winner) wins FROM results r
        |JOIN players p
        |ON p.id == r.winner
        |GROUP BY winner
        |ORDER BY wins DESC;
        |""".stripMargin

    val query = dbConnection.createStatement()
    val result = query.executeQuery(sql)

    println("=" * 10 + " Scoreboard " + "=" * 10)
    while (result.next()) {
      println(result.getString("player") + ": " + result.getInt("wins"))
    }

    query.close()
  }

  /**
   * Inserts a new player into the database table players
   * @param player
   * @return false if player already exists, true if new player
   * if returns false, this player already exists in database
   */
  def insertNewPlayer(player: String): Boolean = {
    val lastId = getLastId("players")
    var result = true

    val sql =
      """
        |INSERT INTO players (id, player, created)
        |values (?,?,CURRENT_TIMESTAMP)
        |""".stripMargin

    val query = dbConnection.prepareStatement(sql)

    /** Because id's in db is unique and can't be duplicated
     * So with our function we get last used id in db and giving it +1
     * So now it will be the new id, that does not exist
     */
    query.setInt(1, lastId+1)
    query.setString(2, player)

    /** Catching an error, because our field name in db is unique
     * So we need to catch an error if we insert duplicate name
     */
    try {
      query.execute()
    }
    catch {
      case e:SQLiteException =>
        result = false
    }
    query.close()
    result
  }

  /**
   * Inserts game result into the database table results
   * @param winner
   * @param loser
   */
  def insertResult(winner: String, loser: String): Unit = {
    val winnerId = getPlayerId(winner)
    val loserId = getPlayerId(loser)
    val lastId = getLastId("results")

    val sql =
      """
        |INSERT INTO results (id, winner, loser, created)
        |values (?,?,?,CURRENT_TIMESTAMP)
        |""".stripMargin

    val query =  dbConnection.prepareStatement(sql)

    query.setInt(1, lastId+1)
    query.setInt(2, winnerId)
    query.setInt(3, loserId)

    query.execute()

    query.close()
  }

  /**
   * Inserts game mode into the database table modes
   * @param mode
   */
  def insertMode (mode: String): Unit ={
    val lastId = getLastId("modes")
    val lastResultId = getLastId("results")

    val sql =
      """
        |INSERT INTO modes (id, game_id, mode, created)
        |values (?,?,?,CURRENT_TIMESTAMP)
        |""".stripMargin

    val query =  dbConnection.prepareStatement(sql)

    query.setInt(1, lastId+1)
    query.setInt(2, lastResultId)
    query.setString(3, mode)

    query.execute()

    query.close()
  }

  /**
   * Inserts game's turns, guesses, outcomes into the database table history
   * @param gameTurns - array of guesses and their outcomes
   */
  def insertHistory(gameTurns: Array[(String, String)]): Unit = {
    val lastGameId = getLastId("results")
    val sql =
      """
        |INSERT INTO history (id, game_id, turn, guess, outcome, created)
        |values (?,?,?,?,?,CURRENT_TIMESTAMP)
        |""".stripMargin

    val query = dbConnection.prepareStatement(sql)

    for (((guess, result), index) <- gameTurns.zipWithIndex) {
      val lastHistoryId = getLastId("history")

      query.setInt(1, lastHistoryId+1)
      query.setInt(2, lastGameId)
      query.setInt(3, index+1)
      query.setString(4, guess)
      query.setString(5, result)

      query.execute()
    }

    query.close()
  }
}
