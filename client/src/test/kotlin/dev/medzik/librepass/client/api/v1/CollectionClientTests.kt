package dev.medzik.librepass.client.api.v1

import net.datafaker.Faker
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CollectionClientTests {
    private val authClient = AuthClient("http://localhost:8080")

    private val email = "_test_" + Faker().internet().emailAddress()
    private val password = Faker().internet().password()

    @Test
    fun `create collection`() {
        authClient.register(email, password)
        val credentials = authClient.login(email, password)

        val collectionClient = CollectionClient(credentials.accessToken)
        val collection = collectionClient.createCollection("test")

        assertNotNull(collection.id)
    }

    @Test
    fun `get collections`() {
        authClient.register(email, password)
        val credentials = authClient.login(email, password)

        val collectionClient = CollectionClient(credentials.accessToken)

        collectionClient.createCollection("test")
        val collections = collectionClient.getCollections()

        assertNotNull(collections)
        assertEquals(collections[0].id, collections[0].id)
        assertEquals("test", collections[0].name)
    }

    @Test
    fun `get collection by id`() {
        authClient.register(email, password)
        val credentials = authClient.login(email, password)

        val collectionClient = CollectionClient(credentials.accessToken)

        val collection = collectionClient.createCollection("test")
        val fetchedCollection = collectionClient.getCollection(collection.id)

        assertNotNull(fetchedCollection)
        assertEquals(collection.id, fetchedCollection.id)
        assertEquals("test", fetchedCollection.name)
    }

    @Test
    fun `update collection`() {
        authClient.register(email, password)
        val credentials = authClient.login(email, password)

        val collectionClient = CollectionClient(credentials.accessToken)

        val collection = collectionClient.createCollection("test")
        collectionClient.updateCollection(collection.id, "test2")

        val fetchedCollection = collectionClient.getCollection(collection.id)

        assertEquals("test2", fetchedCollection.name)
    }

    @Test
    fun `delete collection`() {
        authClient.register(email, password)
        val credentials = authClient.login(email, password)

        val collectionClient = CollectionClient(credentials.accessToken)

        val collection = collectionClient.createCollection("test")
        collectionClient.deleteCollection(collection.id)

        val collections = collectionClient.getCollections()

        assertEquals(0, collections.size)
    }
}
