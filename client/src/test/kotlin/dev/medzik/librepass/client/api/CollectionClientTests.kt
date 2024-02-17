package dev.medzik.librepass.client.api

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectionClientTests {
    private lateinit var collectionClient: CollectionClient

    companion object {
        private const val EMAIL = "test_collection@example.com"
        private const val PASSWORD = "test"

        @BeforeAll
        @JvmStatic
        fun setup() {
            val authClient = AuthClient(API_URL)
            authClient.register(EMAIL, PASSWORD)
            // wait for 1 second to prevent unauthorized error
            Thread.sleep(1000)
        }

        @AfterAll
        @JvmStatic
        fun delete() {
            val authClient = AuthClient(API_URL)
            val credentials = authClient.login(EMAIL, PASSWORD)
            UserClient(EMAIL, credentials.apiKey, API_URL).deleteAccount(PASSWORD)
        }
    }

    @BeforeEach
    fun beforeEach() {
        val authClient = AuthClient(API_URL)
        val credentials = authClient.login(EMAIL, PASSWORD)

        collectionClient = CollectionClient(credentials.apiKey, API_URL)
    }

    @Test
    fun `create collection`() {
        val collection = collectionClient.save(API_URL)

        assertNotNull(collection.id)
    }

    @Test
    fun `get collections`() {
        val insertedCollection = collectionClient.save("test")
        val collections = collectionClient.get()

        val collection = collections.first { it.id == insertedCollection.id }

        assertNotNull(collection)
        assertEquals(insertedCollection.id, collection.id)
        assertEquals("test", collection.name)
    }

    @Test
    fun `get collection by id`() {
        val collection = collectionClient.save("test")
        val fetchedCollection = collectionClient.get(collection.id)

        assertNotNull(fetchedCollection)
        assertEquals(collection.id, fetchedCollection.id)
        assertEquals("test", fetchedCollection.name)
    }

    @Test
    fun `update collection`() {
        val collection = collectionClient.save("test")
        collectionClient.save(collection.id, "test2")

        val fetchedCollection = collectionClient.get(collection.id)

        assertEquals("test2", fetchedCollection.name)
    }

    @Test
    fun `delete collection`() {
        val collection = collectionClient.save("test")
        collectionClient.delete(collection.id)
    }
}
