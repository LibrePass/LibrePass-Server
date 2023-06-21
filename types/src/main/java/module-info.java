module dev.medzik.librepass.types {
    requires dev.medzik.libcrypto;
    requires com.google.gson;
    requires kotlin.stdlib;

    exports dev.medzik.librepass.types.adapters;

    exports dev.medzik.librepass.types.api;
    exports dev.medzik.librepass.types.api.auth;
    exports dev.medzik.librepass.types.api.cipher;
    opens dev.medzik.librepass.types.cipher to com.google.gson;
    opens dev.medzik.librepass.types.cipher.data to com.google.gson;
    exports dev.medzik.librepass.types.api.collection;
    exports dev.medzik.librepass.types.api.user;

    exports dev.medzik.librepass.types.cipher;
    exports dev.medzik.librepass.types.cipher.data;
}
