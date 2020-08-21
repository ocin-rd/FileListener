import java.io.File

class Classifier(val format: String) {
      val data_fields = setOf<String>("labname", "patientnumber", "firstname", "lastname", "dob", "referencenumber")
      var content = mutableSetOf<Int>()
      init {
      	  var data: List<String>  = format.split("_")
	  for (d in data) {
	      val data_field = data_fields.find {it.equals(d, true)}
	      if (data_field != null) {
	      	 val index = data_fields.indexOf(data_field)
	      	 if (index >= 0) {
	      	    content.add(index)
	      	 }
	      }
	      else {
		 error("${d} is an unrecognised data field in the filename format")
	      }
	  }
      }
}

class FileFactory(val classifier: Classifier, val directory: String) {
      val random_data: Map<String, Set<String>> =
      	  mapOf(classifier.data_fields.elementAt(0) to setOf("LabA", "LabB", "LabC", "LabD"),
	  classifier.data_fields.elementAt(1) to setOf("15235", "836", "597", "6889"),
	  classifier.data_fields.elementAt(2) to setOf("Joe", "Karen", "Jack", "Fred"),
	  classifier.data_fields.elementAt(3) to setOf("Bloggs", "Cool", "Johnson"),
	  classifier.data_fields.elementAt(4) to setOf("19701201", "19520816", "20010509", "19900227"),
	  classifier.data_fields.elementAt(5) to setOf("5487", "14", "797", "687"))
      init {
      	  val dir = File(directory)
	  if (!dir.exists()) {
	     dir.mkdirs()
	  }
      }
      fun create_files(n: Int) {
      	  println("Creating ${n} files")
	  for (i in 1..n) {
	      var filename: String = ""
	      for (data in classifier.content) {
	      	  filename += random_data.get(classifier.data_fields.elementAt(data)).random()
	      	  if (data != classifier.content.last()) {
	      	     filename += "_"
	      	  }
	      }
	      File(directory + "/" + filename + ".txt").writeText("Very important patient data.")
	   }
      }
}

fun main(args: Array<String>) {
    if (args.size != 2) {
       println("Please provide an input directory and a single string representing the filename format")
       return
    }
    val classifier = Classifier(args[1])
    val filefactory = FileFactory(classifier, args[0].replace("/", ""))
    filefactory.create_files(4)
}