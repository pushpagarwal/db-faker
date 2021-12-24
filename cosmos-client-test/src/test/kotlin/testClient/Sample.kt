package testClient

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class Sample(
    val id: String,
    val partitionKey: String,
    val name: String,
    val description: String,
    @JsonProperty("_etag") val eTag: String? = null,
    @JsonProperty("_ts") val timestamp: Long = Instant.now().epochSecond
)
