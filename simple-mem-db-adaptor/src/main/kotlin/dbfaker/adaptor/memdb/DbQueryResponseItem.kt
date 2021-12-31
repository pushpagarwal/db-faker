package dbfaker.adaptor.memdb

import com.fasterxml.jackson.databind.JsonNode
import dbfaker.QueryResponseItem

data class DbQueryResponseItem(
    override val item: JsonNode,
    override val nextCursor: String
) : QueryResponseItem