package amit.trg

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

import com.mongodb.spark._

import java.io.File

object Solution {
  def getAllFiles(inputDir: File): Array[File] = {
    val currentFiles = inputDir.listFiles
    currentFiles ++ currentFiles.filter(_.isDirectory).flatMap(getAllFiles)
  }

  def main(args: Array[String]): Unit = {
    val dataDir = if (args.length == 0) "data" else args(0)
    val csvFileNames = getAllFiles(new File(dataDir))
      .filter(_.getName.endsWith(".csv"))
      .map(_.getAbsolutePath)
    val crimeDataFiles = csvFileNames.filter(!_.endsWith("-outcomes.csv"))
    val outcomesDataFiles = csvFileNames.filter(_.endsWith("-outcomes.csv"))

    val sparkConf = new SparkConf()
    sparkConf.set("spark.mongodb.input.uri", "mongodb://root:example@127.0.0.1:27017/trg.crimeanalysis?authSource=admin")
    sparkConf.set("spark.mongodb.output.uri", "mongodb://root:example@127.0.0.1:27017/trg.crimeanalysis?authSource=admin")

    val spark = SparkSession
      .builder
      .appName(this.getClass.getSimpleName)
      .master("local[*]")
      .config(sparkConf)
      .getOrCreate()

    import spark.implicits._
    val crimesDf = spark.read.format("CSV")
      .option("header","true")
      .load(crimeDataFiles: _*)
      .select($"Crime ID".alias("crimeId"),
        $"Latitude".alias("latitude"),
        $"Longitude".alias("longitude"),
        $"Crime type".alias("crimeType"),
        $"Last outcome category".alias("lastOutcome"))
      .withColumn("fullFileName", input_file_name())

    val outcomesDf = spark.read.format("CSV")
      .option("header","true")
      .load(outcomesDataFiles: _*)
      .select($"Crime ID".alias("crimeId"),
        $"Outcome type".alias("lastOutcome"),
        $"Month".alias("month"))
      .withColumn("row_number", row_number()
        .over(Window.partitionBy($"crimeId")
          .orderBy(desc("month"))))
      .filter("row_number=1")

    val resultsDf = joinCrimesWithOutcomes(crimesDf, outcomesDf)(spark)
    MongoSpark.save(resultsDf.write.option("collection", "trg.crimeanalysis").mode("overwrite"))
  }

  def joinCrimesWithOutcomes(crimesDf: DataFrame, outcomesDf: DataFrame)(implicit spark:SparkSession) = {
    import spark.implicits._

    import org.apache.spark.sql.functions.udf
    val getDistrictName = udf((fullFileName: String) => fullFileName.split("/").last.split("-",3).last.replace("-street.csv",""))

    val resultsDf = crimesDf
      .joinWith(outcomesDf, crimesDf("crimeId") === outcomesDf("crimeId"), "leftouter").
      select($"_1.crimeId",
        $"_1.latitude",
        $"_1.longitude",
        $"_1.crimeType",
        getDistrictName($"_1.fullFileName").as("districtName"),
        when($"_2.lastOutcome".isNull, $"_1.lastOutcome").otherwise($"_2.lastOutcome").as("lastOutcome"))

    resultsDf
  }
}
