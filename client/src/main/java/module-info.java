module dev.medzik.librepass.client {
    // LibrePass Types
    requires dev.medzik.librepass.types;

    // Crypto
    requires dev.medzik.libcrypto;

    // HTTP Client
    requires okhttp3;

    // Kotlin std library
    requires kotlin.stdlib;
    // Kotlin serialization
    requires kotlinx.serialization.json;

    // export API Client
    exports dev.medzik.librepass.client;
    exports dev.medzik.librepass.client.api.v1;
    exports dev.medzik.librepass.client.errors;
    exports dev.medzik.librepass.client.utils;
}
