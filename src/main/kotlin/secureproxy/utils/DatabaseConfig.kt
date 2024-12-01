package secureproxy.utils

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
    val host: String,
    val db: String,
    val port: Int,
    val user: String,
    val password: String
)
