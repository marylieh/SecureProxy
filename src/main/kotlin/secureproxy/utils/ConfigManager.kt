package secureproxy.utils

import com.moandjiezana.toml.Toml
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

object ConfigManager {

    private lateinit var config: Path

    lateinit var dbHost: String
    lateinit var dbName: String
    lateinit var dbPort: Integer
    lateinit var dbUser: String
    lateinit var dbPassword: String

    fun initConfig() {
        config = Path.of("plugins/secure-proxy/config.toml")

        if (!Files.exists(config)) {

            try {
                val configTemplate = javaClass.getResourceAsStream("/config.toml")
                assert(configTemplate != null)
                Files.write(config, configTemplate.readAllBytes(), StandardOpenOption.CREATE_NEW)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun getConfig(): Toml {
        return Toml().read(File("./plugins/secure-proxy/config.toml"))
    }
}