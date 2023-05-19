package dev.medzik.librepass.client.api.v1

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CollectionClientTests {
    private lateinit var collectionClient: CollectionClient

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            val authClient = AuthClient("http://localhost:8080")
            authClient.register("test_collection@example.com", "test")
        }
    }

    @BeforeEach
    fun beforeEach() {
        val authClient = AuthClient("http://localhost:8080")
        val credentials = authClient.login("test_collection@example.com", "test")

        collectionClient = CollectionClient(credentials.accessToken, "http://localhost:8080")
    }

    @Test
    fun `create collection`() {
        val collection = collectionClient.createCollection("test")

        assertNotNull(collection.id)
    }

    @Test
    fun `get collections`() {
        collectionClient.createCollection("test")
        val collections = collectionClient.getCollections()

        assertNotNull(collections)
        assertEquals(collections[0].id, collections[0].id)
        assertEquals("test", collections[0].name)
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
