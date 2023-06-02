module dev.medzik.librepass.types {
    requires password4j;
    requires dev.medzik.libcrypto;
    requires kotlinx.serialization.core;
    requires kotlinx.serialization.json;

    exports dev.medzik.librepass.types.api;
    exports dev.medzik.librepass.types.api.auth;
    exports dev.medzik.librepass.types.api.cipher;
    exports dev.medzik.librepass.types.api.collection;
    exports dev.medzik.librepass.types.api.serializers;
    exports dev.medzik.librepass.types.api.user;

    exports dev.medzik.librepass.types.cipher;
    exports dev.medzik.librepass.types.cipher.data;
}