import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

class BullsAndCowsDB(val dbPath:String) {

  val url =  s"jdbc:sqlite:$dbPath"
  val conn: Connection = DriverManager.getConnection(url)
  println(s"Opened Database at ${conn.getMetaData.getURL}")

  def dropAllTables():Unit = {
    val statement = conn.createStatement()
    val sql1 =
      """
        |DROP TABLE IF EXISTS results;
        |""".stripMargin
    val sql2 =
      """
        |DROP TABLE IF EXISTS scores;
        |""".stripMargin
    val sql3 =
      """
        |DROP TABLE IF EXISTS users;
        |""".stripMargin
    statement.addBatch(sql1)
    statement.addBatch(sql2)
    statement.addBatch(sql3)
    statement.executeBatch()  //more efficient than 3 single queries
  }

  /**
   * Perform table migration in a new installation, does nothing otherwise
   */
  def migrate():Unit = {
    //migrate for db refers to table creation other setup needed to start work in a new enviroment
    //https://www.sqlitetutorial.net/sqlite-create-table/

    //TODO created multiple tables with users in one table
    //so when we do insert we will reference the users table in our results table

    val statement = conn.createStatement() //we create a statement object that will handl sending SQL statements to the DB

    //this query should do nothing if table already exists
    //this query should do nothing if table already exists
    val sql0 =
    """
      |CREATE TABLE IF NOT EXISTS users (
      |id INTEGER PRIMARY KEY,
      |name TEXT NOT NULL,
      |email TEXT,
      |created TEXT
      |);
      |""".stripMargin
    statement.addBatch(sql0)

    val sql1 =
      """
        |CREATE TABLE IF NOT EXISTS results (
        |id INTEGER PRIMARY KEY,
        |winner INTEGER NOT NULL,
        |loser INTEGER NOT NULL,
        |created TEXT,
        |    FOREIGN KEY (winner)
        |       REFERENCES users (id),
        |   FOREIGN KEY (loser)
        |       REFERENCES users (id)
        |);
        |""".stripMargin
    statement.addBatch(sql1)

    val sql2 =
    """
      |CREATE TABLE IF NOT EXISTS scores (
      |id INTEGER PRIMARY KEY,
      |game_id INTEGER NOT NULL,
      |turn INTEGER NOT NULL,
      |guess TEXT NOT NULL,
      |outcome TEXT NOT NULL,
      |created TEXT,
      |    FOREIGN KEY (game_id)
      |       REFERENCES results (id)
      |);
      |""".stripMargin
    statement.addBatch(sql2)

    statement.executeBatch()
  }

  def insertResult(winner:String, loser:String):Unit = {

    val insertSql = """
                      |INSERT INTO results (winner,loser,created)
                      |values (?,?,CURRENT_TIMESTAMP)
""".stripMargin

    val preparedStmt: PreparedStatement = conn.prepareStatement(insertSql)

    preparedStmt.setString (1, winner)
    preparedStmt.setString (2, loser)
    preparedStmt.execute

    preparedStmt.close()
  }

  def insertScore(game_id:Int, turn:Int, guess:String, outcome:String): Unit = {
    val insertSql = """
                      |INSERT INTO scores (game_id,turn,guess,outcome,created)
                      |values (?,?,?,?,CURRENT_TIMESTAMP)
  """.stripMargin

    val preparedStmt: PreparedStatement = conn.prepareStatement(insertSql)

    preparedStmt.setInt(1, game_id)
    preparedStmt.setInt(2, turn)
    preparedStmt.setString(3, guess)
    preparedStmt.setString(4, outcome)

    preparedStmt.execute

    preparedStmt.close()
  }

  def getIdOfLastGame:Int = {
    val statement = conn.createStatement()
    val sql =
    """
      |SELECT MAX(id) id FROM results;
      |""".stripMargin
    val resultSet = statement.executeQuery(sql)
    val lastGameId = resultSet.getInt("id")
    lastGameId
  }

  def insertFullScore(guesses:Array[(String, String)]):Unit = {
    val id = getIdOfLastGame
    for (((guess, outcome), turn) <- guesses.zipWithIndex) {
      insertScore(id, turn+1, guess, outcome)
    }
  }

}
