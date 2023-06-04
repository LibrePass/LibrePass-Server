module dev.medzik.librepass.types {
    // Crypto
    requires dev.medzik.libcrypto;

    // Kotlin serialization
    requires kotlinx.serialization.json;

    // export API types
    exports dev.medzik.librepass.types.api;
    exports dev.medzik.librepass.types.api.auth;
    exports dev.medzik.librepass.types.api.cipher;
    exports dev.medzik.librepass.types.api.collection;
    exports dev.medzik.librepass.types.api.serializers;
    exports dev.medzik.librepass.types.api.user;

    // export Cipher types
    exports dev.medzik.librepass.types.cipher;
    exports dev.medzik.librepass.types.cipher.data;

    // export Utils
    exports dev.medzik.librepass.types.utils;
}
