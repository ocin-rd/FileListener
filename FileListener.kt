import java.io.File
import java.time.LocalDate
import java.time.Instant

/* This class takes a format definition and finds the matching data field.
   The result is a list of integers which are indices into the data field set */
class Classifier(val format: String) {
	val data_fields = setOf<String>("labname", "patientnumber", "firstname", "lastname", "dob", "referencenumber")
	var content = mutableSetOf<Int>()
	init {
		var data: List<String>  = format.split("_")
		for (d in data) {
			/* The data fields are case insensitive */
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

/* This class produces a number of random text files in a given directory.
   The random data is defined based on the data fields in the Classifier. */
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
		println("Creating ${n} random files in directory ${directory}/\n")
		for (i in 1..n) {
			var filename: String = ""
			for (data in classifier.content) {
				val entries = random_data.get(classifier.data_fields.elementAt(data))
				if (entries != null) {
					filename += entries.random()
					if (data != classifier.content.last()) {
						filename += "_"
					}
				}
				else {
					error("${classifier.data_fields.elementAt(data)} is an invalid data field in the random file generation")
				}
			}
			File(directory + "/" + filename + ".txt").writeText("Very important patient data.")
		}
	}
}

/* This class is used to scan all files in the source directory, to check whether the filenames
   match the given format and if they do extract the metadata and archive the file. */
class FileListener(val classifier: Classifier, val source_dir: String) {
	val source = File(source_dir)
	/* Archive needs to be initialised, but gets overwritten once files actually
	   get archived */
	var archive = File("tmp")
	fun process() {
		val walker = source.walk()
		val file_iterator = walker.iterator()
		file_iterator.forEach {
			/* Check that it is a file, and that it is not currently written to. */
			if (it.isFile()) {
				/* Only files that haven't been modified within the last five seconds will be archived */
				if (Instant.now().toEpochMilli() - it.lastModified() > 5000) {
					val is_valid = extract_metadata(it)
					if (is_valid) {
						archive_file(it)
						println()
					}
					else {
						println("Skipping file ${it} because it does not match the given format.")
					}
				}
				else {
					println("Skipping file ${it} because it is currently written to.")
				}
			}
		}
	}
	fun extract_metadata(file: File): Boolean {
		var is_valid = false
		var filename = file.toString()
		/* Store metadata in a map */
		var metadata = mutableMapOf<String, String>()
		/* Remove directory and file extension */
		filename = filename.replaceBeforeLast("/", "")
		filename = filename.drop(1)
		filename = filename.replaceAfterLast(".", "")
		filename = filename.dropLast(1)

		val data: List<String> = filename.split("_")
		if (data.size == classifier.content.size) {
	 		for (i in 0..data.size-1) {
	 			metadata.put(classifier.data_fields.elementAt(classifier.content.elementAt(i)), data.elementAt(i))
			}
			println(metadata)
			is_valid = true
		}
		return is_valid
	}
	fun archive_file(file: File) {
		make_archive()

		var filename = file.toString()
		filename = filename.replaceBeforeLast("/", "")
		filename = filename.drop(1)

		var target = file.toString()
		target = target.replaceBeforeLast("/", archive.toString())
		if (!File(target).exists()) {
			file.copyTo(File(target))
			file.delete()
			println("Archived file ${filename} in directory ${archive.toString()}/")
		}
		else {
			println("File ${filename} already exists in directory ${archive.toString()}/")
		}
	}
	fun make_archive() {
		val date = LocalDate.now()
		archive = File(date.toString())
		if (!archive.exists()) {
			archive.mkdirs()
		}
	}
}

fun main(args: Array<String>) {
	if (args.size != 2) {
		println("Please provide a source directory and a single string representing the filename format")
		return
	}
	var source_directory = args[0]
	val filename_format = args[1]

	val classifier = Classifier(filename_format)

	/* Remove any trailing slash (backslash on Windows) from the directory path */
	if (source_directory.endsWith("/") || source_directory.endsWith("\\")) {
		source_directory = source_directory.dropLast(1)
	}
	val dir = File(source_directory)
	if (!dir.exists()) {
		val filefactory = FileFactory(classifier, source_directory)
		filefactory.create_files(4)
	}

	val filelistener = FileListener(classifier, source_directory)
	filelistener.process()
}