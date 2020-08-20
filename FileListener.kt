import java.io.File

class FileFactory(val format: String) {
      val keys = listOf("labname", "patientnumber", "firstname", "lastname", "dob", "referencenumber")
      init {
      	  var data: List<String>  = format.split("_")
	  for (d in data) {
	      val index = keys.find {it.equals(d, true)}
	      if (index != null) {
	      	 println("Found")
	      }
	      else {
		 error("Unrecognised data field")
	      }
	  }
      }
      fun create_directory(directory: String) {
      	  val dir = File(directory)
	  if (!dir.exists()) {
	     dir.mkdirs()
	  }
      }
      fun create_files(n: Int) {
      	  println("Creating ${n} files")
      }
}

data class PatientFile(val labname: String, val patientnumber: Int, val firstname: String, val lastname: String, val dob: Int, val referencenumber: Int)

fun main(args: Array<String>) {
    if (args.size != 1) {
       println("Please provide a single string representing the filename format")
       return
    }
    val filefactory = FileFactory(args[0])
    filefactory.create_directory("input")
    filefactory.create_files(4)
}