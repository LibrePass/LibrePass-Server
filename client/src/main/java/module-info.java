module dev.medzik.librepass.client {
    requires password4j;
    requires dev.medzik.libcrypto;
    requires dev.medzik.librepass.types;
    requires okhttp3;
    requires kotlin.stdlib;
    requires org.apache.commons.codec;
    requires transitive kotlinx.serialization.json;

    exports dev.medzik.librepass.client;
    exports dev.medzik.librepass.client.api.v1;
    exports dev.medzik.librepass.client.errors;
    exports dev.medzik.librepass.client.utils;
}
