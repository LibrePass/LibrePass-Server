package dev.medzik.librepass.client.api.v1

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectionClientTests {
    private lateinit var collectionClient: CollectionClient

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            val authClient = AuthClient("http://localhost:8080")
            authClient.register("test_collection@example.com", "test")
            // wait for 1 second to prevent unauthorized error
            Thread.sleep(1000)
        }
    }

    @BeforeEach
    fun beforeEach() {
        val authClient = AuthClient("http://localhost:8080")
        val credentials = authClient.login("test_collection@example.com", "test")

        collectionClient = CollectionClient(credentials.apiKey, "http://localhost:8080")
    }

    @Test
    fun `create collection`() {
        val collection = collectionClient.createCollection("test")

        assertNotNull(collection.id)
    }

    @Test
    fun `get collections`() {
        val insertedCollection = collectionClient.createCollection("test")
        val collections = collectionClient.getCollections()

        val collection = collections.first { it.id == insertedCollection.id }

        assertNotNull(collection)
        assertEquals(insertedCollection.id, collection.id)
        assertEquals("test", collection.name)
    }

    @Test
    fun `get collection by id`() {
        val collection = collectionClient.createCollection("test")
        val fetchedCollection = collectionClient.getCollection(collection.id)

        assertNotNull(fetchedCollection)
        assertEquals(collection.id, fetchedCollection.id)
        assertEquals("test", fetchedCollection.name)
    }

    @Test
    fun `update collection`() {
        val collection = collectionClient.createCollection("test")
        collectionClient.updateCollection(collection.id, "test2")

        val fetchedCollection = collectionClient.getCollection(collection.id)

        assertEquals("test2", fetchedCollection.name)
    }

    @Test
    fun `delete collection`() {
        val collection = collectionClient.createCollection("test")
        collectionClient.deleteCollection(collection.id)
    }
}
