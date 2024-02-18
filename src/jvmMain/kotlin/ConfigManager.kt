import androidx.compose.ui.res.useResource
import data.model.Config
import kotlinx.serialization.json.Json

object ConfigManager {
    private const val CONFIG_FILE_PATH = "config.json" // or "config.yml" for YAML
    fun loadConfig(): Config {
        useResource(CONFIG_FILE_PATH) { stream ->
            val textJson = stream.bufferedReader().use { it.readText() }
            return Json.decodeFromString(textJson)
        }
    }
}