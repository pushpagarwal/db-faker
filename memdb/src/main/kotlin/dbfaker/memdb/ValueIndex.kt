package dbfaker.memdb

import dbfaker.DocumentValue
import java.util.concurrent.ConcurrentSkipListMap

class ValueRangeIndex<K> : ConcurrentSkipListMap<DocumentValue, Set<K>>() {

}


