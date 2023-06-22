module dev.medzik.librepass.types {
    requires dev.medzik.libcrypto;
    requires com.google.gson;
    requires kotlin.stdlib;

    exports dev.medzik.librepass.types.adapters;

    exports dev.medzik.librepass.types.api;
    opens dev.medzik.librepass.types.api to com.google.gson;
    exports dev.medzik.librepass.types.api.auth;
    opens dev.medzik.librepass.types.api.auth to com.google.gson;
    exports dev.medzik.librepass.types.api.cipher;
    opens dev.medzik.librepass.types.api.cipher to com.google.gson;
    exports dev.medzik.librepass.types.api.collection;
    opens dev.medzik.librepass.types.api.collection to com.google.gson;
    exports dev.medzik.librepass.types.api.user;
    opens dev.medzik.librepass.types.api.user to com.google.gson;

    exports dev.medzik.librepass.types.cipher;
    opens dev.medzik.librepass.types.cipher to com.google.gson;
    exports dev.medzik.librepass.types.cipher.data;
    opens dev.medzik.librepass.types.cipher.data to com.google.gson;
}
