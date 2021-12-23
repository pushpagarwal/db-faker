package testClient

import com.azure.cosmos.CosmosAsyncClient
import com.azure.cosmos.CosmosClientBuilder
import com.azure.cosmos.models.PartitionKey
import com.fasterxml.jackson.databind.JsonNode
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class DocumentTest {

    val url =
        //"https://cjm-policy-test-cost.documents.azure.com:443/"
        "http://localhost:443/"

    lateinit var cosmosAsyncClient: CosmosAsyncClient

    @Before
    fun setup() {
        cosmosAsyncClient = CosmosClientBuilder()
            .gatewayMode()
            .endpoint(url)
            .key("Tbne3c6mXVCdfZD8YUfGpXRzhRMbTpAQh3qULLJzZdRDVajoWXqExbqqfKS0Mx9okv7gZiAGCFP1FLMQXZOQew==")
            .buildAsyncClient()
    }

    @Test
    fun testGetDocument(){
        val node = cosmosAsyncClient.getDatabase("test-cjm-cs").getContainer("contents")
            .readItem("db993447-c9cd-48ff-a333-2e8f366dcfc8",
                PartitionKey("745F37C35E4B776E0A49421B@AdobeOrg_70f58060-5d47-11ea-bdff-a5384333ff34"),
                JsonNode::class.java)
            .block()
        Assert.assertNotNull(node?.item)
    }

}