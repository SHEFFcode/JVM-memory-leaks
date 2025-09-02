import java.util.*
import kotlin.collections.ArrayList

fun main() {
    println("Memory Leak Examples in Kotlin")
    println("==============================")

    while (true) {
        println("\nChoose an example to run:")
        println("1. Static Collection Leak")
        println("2. Listener/Callback Leak")
        println("3. Thread Leak")
        println("4. Closure Capture Leak")
        println("5. Cache without Eviction")
        println("6. Proper Resource Management")
        println("0. Exit")

        when (readLine()) {
            "1" -> staticCollectionLeak()
            "2" -> listenerLeak()
            "3" -> threadLeak()
            "4" -> closureCaptureLeak()
            "5" -> cacheWithoutEvictionLeak()
            "6" -> properResourceManagementExample()
            "0" -> break
            else -> println("Invalid option, please try again")
        }

        println("\nPress Enter to continue...")
        readLine()
    }
}

// 1. Static Collection Leak
object StaticMemoryLeaker {
    private val staticList = mutableListOf<ExpensiveObject>()

    fun addData(data: ExpensiveObject) {
        staticList.add(data) // Objects never get removed!
    }

    fun getSize() = staticList.size
}

data class ExpensiveObject(
    val id: Int,
    val data: ByteArray = ByteArray(1024 * 1024) // 1MB object
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ExpensiveObject
        return id == other.id
    }

    override fun hashCode(): Int = id
}

fun staticCollectionLeak() {
    println("\n1. Static Collection Leak Example")
    println("Adding objects to static collection...")

    repeat(10) { i ->
        StaticMemoryLeaker.addData(ExpensiveObject(i))
        println("Added object $i, collection size: ${StaticMemoryLeaker.getSize()}")
    }

    println("Objects remain in memory even after function ends!")
    // Fix: Clear the collection when done or use WeakReferences
}

// 2. Listener/Callback Leak
class EventPublisher {
    private val listeners = mutableListOf<EventListener>()

    fun addListener(listener: EventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: EventListener) {
        listeners.remove(listener)
    }

    fun publishEvent(event: String) {
        listeners.forEach { it.onEvent(event) }
    }
}

interface EventListener {
    fun onEvent(event: String)
}

class LeakyComponent : EventListener {
    private val heavyResource = ByteArray(1024 * 1024) // 1MB

    override fun onEvent(event: String) {
        println("LeakyComponent received: $event")
    }
}

fun listenerLeak() {
    println("\n2. Listener/Callback Leak Example")
    val publisher = EventPublisher()

    // Create components that register as listeners
    val components = mutableListOf<LeakyComponent>()

    repeat(5) { i ->
        val component = LeakyComponent()
        publisher.addListener(component)
        components.add(component)
        println("Added listener $i")
    }

    // Simulate clearing components but forgetting to unregister listeners
    components.clear()
    println("Components cleared, but listeners still registered in publisher!")

    // Publisher still holds references to the components!
    publisher.publishEvent("Test event")

    // Fix: Always call publisher.removeListener(component) before clearing
}

// 3. Thread/Timer Leak
class ThreadLeakExample {
    private var isRunning = true

    fun startBackgroundWork() {
        Thread {
            while (isRunning) {
                try {
                    Thread.sleep(1000)
                    println("Background work running...")
                    // Simulate some work
                } catch (e: InterruptedException) {
                    break
                }
            }
        }.start()
    }

    fun stop() {
        isRunning = false
    }
}

fun threadLeak() {
    println("\n3. Thread Leak Example")
    val workers = mutableListOf<ThreadLeakExample>()

    repeat(3) { i ->
        val worker = ThreadLeakExample()
        worker.startBackgroundWork()
        workers.add(worker)
        println("Started worker thread $i")
    }

    // Simulate forgetting to stop threads
    workers.clear()
    println("Workers cleared, but threads still running!")

    Thread.sleep(3000) // Let threads run for a bit

    // Fix: Always call worker.stop() before clearing
}

// 4. Closure Capture Leak
class ClosureLeakExample {
    private val callbacks = mutableListOf<() -> Unit>()

    fun addCallback(callback: () -> Unit) {
        callbacks.add(callback)
    }

    fun executeCallbacks() {
        callbacks.forEach { it() }
    }
}

fun closureCaptureLeak() {
    println("\n4. Closure Capture Leak Example")
    val callbackManager = ClosureLeakExample()

    fun createHeavyObjects() {
        val heavyData = ByteArray(1024 * 1024) // 1MB
        val moreHeavyData = ByteArray(1024 * 1024) // Another 1MB

        // This closure captures the entire scope, including heavyData!
        callbackManager.addCallback {
            println("Callback executed - captured ${heavyData.size} bytes")
            // moreHeavyData is also captured even though not used!
        }
    }

    repeat(5) {
        createHeavyObjects()
        println("Created heavy objects and callback $it")
    }

    println("Heavy objects should be GC'd but are kept alive by closures!")

    // Fix: Only capture what you need:
    // val size = heavyData.size
    // callbackManager.addCallback { println("Size: $size") }
}

// 5. Cache without Eviction Policy
class NaiveCacheExample {
    private val cache = mutableMapOf<String, ExpensiveObject>()

    fun get(key: String): ExpensiveObject {
        return cache.getOrPut(key) {
            println("Creating expensive object for key: $key")
            ExpensiveObject(key.hashCode())
        }
    }

    fun getCacheSize() = cache.size
}

fun cacheWithoutEvictionLeak() {
    println("\n5. Cache without Eviction Leak Example")
    val cache = NaiveCacheExample()

    // Simulate accessing many different keys
    repeat(100) { i ->
        cache.get("key_$i")
        if (i % 20 == 0) {
            println("Cache size: ${cache.getCacheSize()}")
        }
    }

    println("Final cache size: ${cache.getCacheSize()}")
    println("All objects remain in cache forever!")

    // Fix: Implement LRU cache, use WeakHashMap, or add manual cleanup
}

// Bonus: Memory Leak Detection Helper
class MemoryLeakDetector {
    private val objectCounts = mutableMapOf<String, Int>()

    fun trackObject(className: String) {
        objectCounts[className] = objectCounts.getOrDefault(className, 0) + 1
    }

    fun untrackObject(className: String) {
        val count = objectCounts.getOrDefault(className, 0)
        if (count > 0) {
            objectCounts[className] = count - 1
        }
    }

    fun printStats() {
        println("\nObject counts:")
        objectCounts.forEach { (className, count) ->
            println("$className: $count instances")
        }
    }
}

// Example of proper resource management
class ProperResourceManagement : AutoCloseable {
    private val resources = mutableListOf<ExpensiveObject>()

    fun addResource(resource: ExpensiveObject) {
        resources.add(resource)
    }

    override fun close() {
        resources.clear()
        println("Resources properly cleaned up")
    }
}

fun properResourceManagementExample() {
    println("\n6. Proper Resource Management Example")

    ProperResourceManagement().use { manager ->
        repeat(5) { i ->
            manager.addResource(ExpensiveObject(i))
        }
        println("Resources added to manager")
    } // Automatically calls close() here

    println("Resources cleaned up automatically!")
}