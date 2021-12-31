package dbfaker.memdb

import java.util.concurrent.ConcurrentSkipListMap

class ValueRangeIndex<K> : ConcurrentSkipListMap<JsonValue, Set<K>>() {

}


