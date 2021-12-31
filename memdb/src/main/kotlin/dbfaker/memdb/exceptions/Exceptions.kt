package dbfaker.memdb.exceptions

class NotFound : Exception()

class PreConditionFailed(message: String?) : Exception(message)