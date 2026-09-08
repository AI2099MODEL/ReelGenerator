import re

with open("app/src/main/java/com/example/util/OrganiserStorageManager.kt", "r") as f:
    content = f.read()

new_func = """    fun getOrganiserRootDir(context: Context): File {
        val ext = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val dir = File(ext, ROOT_FOLDER_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }"""

content = re.sub(r'    fun getOrganiserRootDir\(context: Context\): File \{.*?(?=    fun getSubDir)', new_func + "\n\n", content, flags=re.DOTALL)

with open("app/src/main/java/com/example/util/OrganiserStorageManager.kt", "w") as f:
    f.write(content)
