module dev.medzik.librepass.client {
    requires dev.medzik.librepass.shared;
    requires dev.medzik.libcrypto;
    requires okhttp3;
    requires com.google.gson;
    requires kotlin.stdlib;

    // export API Client
    exports dev.medzik.librepass.client;
    exports dev.medzik.librepass.client.api.v1;
    exports dev.medzik.librepass.client.errors;
    exports dev.medzik.librepass.client.utils;
}
