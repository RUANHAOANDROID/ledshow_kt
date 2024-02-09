import androidx.compose.ui.res.useResource
import data.model.Config
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object ConfigManager {
    private const val CONFIG_FILE_PATH = "config.json" // or "config.yml" for YAML
    private const val CONFIG_FILE_USER_PATH = "config.json"
    fun loadConfig(): Config {
        val customConfig = File(CONFIG_FILE_USER_PATH)
        if (customConfig.exists()) {
            val textJson = customConfig.bufferedReader().use {
                it.readText()
            }
            return Json.decodeFromString(textJson)
        }
        useResource(CONFIG_FILE_PATH) { stream ->
            val textJson = stream.bufferedReader().use { it.readText() }
            return Json.decodeFromString(textJson)
        }
    }

    fun saveConfig(config: Config) {
        val currentDirectory = System.getProperty("user.dir")
        val file = File(CONFIG_FILE_USER_PATH)
        if (!file.exists()) {
            file.createNewFile()
            useResource(CONFIG_FILE_PATH) {
                it.copyTo(file.outputStream())
            }
        } else {
            file.bufferedWriter().use {
                it.write(Json.encodeToString(config))
            }
        }
    }
}