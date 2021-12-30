package testClient

import com.azure.cosmos.CosmosAsyncClient
import com.azure.cosmos.CosmosAsyncContainer
import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.models.PartitionKey
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.*


class DocumentTest {

    private val url =
        //"https://cjm-policy-test-cost.documents.azure.com:443/"
        "http://localhost:443/"

    private lateinit var cosmosAsyncClient: CosmosAsyncClient
    private lateinit var cosmosAsyncContainer: CosmosAsyncContainer
    private val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Before
    fun setup() {
        cosmosAsyncClient = CosmosClientBuilder()
            .gatewayMode()
            .endpoint(url)
            .key("Tbne3c6mXVCdfZD8YUfGpXRzhRMbTpAQh3qULLJzZdRDVajoWXqExbqqfKS0Mx9okv7gZiAGCFP1FLMQXZOQew==")
            .buildAsyncClient()
        cosmosAsyncContainer = cosmosAsyncClient.getDatabase("test-cjm-cs").getContainer("contents")
    }

    @Test
    fun testGetDocument() {
        val node = cosmosAsyncContainer
            .readItem(
                "db993447-c9cd-48ff-a333-2e8f366dcfc8",
                PartitionKey("745F37C35E4B776E0A49421B@AdobeOrg_70f58060-5d47-11ea-bdff-a5384333ff34"),
                ObjectNode::class.java
            )
            .block()
        Assert.assertNotNull(node?.item)
        val sample = mapper.treeToValue<Sample>(node?.item!!)
        Assert.assertNotNull(sample)
    }

    @Test
    fun testCreateThenReadDocument() {
        val sample = Sample(
            UUID.randomUUID().toString(), "partition1",
            "test101", ""
        )
        val node = mapper.valueToTree<JsonNode>(sample)
        cosmosAsyncContainer
            .createItem(node)
            .map { response ->
                Assert.assertNotNull(response?.item)
                val newSample = mapper.treeToValue<Sample>(response?.item!!)
                Assert.assertEquals(sample.name, newSample.name)
            }
            .flatMap {
                cosmosAsyncContainer
                    .readItem(
                        sample.id,
                        PartitionKey(sample.partitionKey),
                        ObjectNode::class.java
                    )
            }
            .map { response ->
                Assert.assertNotNull(response?.item)
                val newSample = mapper.treeToValue<Sample>(response?.item!!)
                Assert.assertEquals(sample.name, newSample.name)
            }
            .block()

    }

}