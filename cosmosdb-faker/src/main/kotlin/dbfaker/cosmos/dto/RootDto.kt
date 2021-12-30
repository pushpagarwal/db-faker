package dbfaker.cosmos.dto

import dbfaker.cosmos.common.Constants


data class DbLocation(val name: String, val databaseAccountEndpoint: String)

data class UserReplicationPolicy(
    val asyncReplication: Boolean,
    val minReplicaSetSize: Int,
    val maxReplicasetSize: Int,
)

data class UserConsistencyPolicy(
    val defaultConsistencyLevel: String
)

data class SystemReplicationPolicy(
    val minReplicaSetSize: Int,
    val maxReplicasetSize: Int,
)

data class ReadPolicy(
    val primaryReadCoefficient: Int,
    val secondaryReadCoefficient: Int,
)

data class RootDto(val id: String = "fake") {
    val _self = ""
    val _rid = "localhost"
    val media = "//media/"
    val addresses = "//addresses/"
    val _dbs = "//dbs/"
    val userReplicationPolicy = UserReplicationPolicy(false, 3, 4)
    val userConsistencyPolicy = UserConsistencyPolicy("session")
    val systemReplicationPolicy = SystemReplicationPolicy(3, 4)
    val writableLocations = listOf(DbLocation("East US 2", Constants.HOST_URL))
    val readableLocations = listOf(DbLocation("East US 2", Constants.HOST_URL))
    val enableMultipleWriteLocations = false
    val readPolicy = ReadPolicy(1, 1)
    val queryEngineConfiguration =
        "{\"maxSqlQueryInputLength\":262144,\"maxJoinsPerSqlQuery\":5,\"maxLogicalAndPerSqlQuery\":500,\"maxLogicalOrPerSqlQuery\":500,\"maxUdfRefPerSqlQuery\":10,\"maxInExpressionItemsCount\":16000,\"queryMaxInMemorySortDocumentCount\":500,\"maxQueryRequestTimeoutFraction\":0.9,\"sqlAllowNonFiniteNumbers\":false,\"sqlAllowAggregateFunctions\":true,\"sqlAllowSubQuery\":true,\"sqlAllowScalarSubQuery\":true,\"allowNewKeywords\":true,\"sqlAllowLike\":true,\"sqlAllowGroupByClause\":true,\"maxSpatialQueryCells\":12,\"spatialMaxGeometryPointCount\":256,\"sqlDisableOptimizationFlags\":0,\"sqlAllowTop\":true,\"enableSpatialIndexing\":true}"
}
