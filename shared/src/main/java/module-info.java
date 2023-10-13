module dev.medzik.librepass.shared {
    requires dev.medzik.libcrypto;
    requires com.google.gson;
    requires kotlin.stdlib;
    requires org.apache.commons.codec;
    requires otp.java;

    exports dev.medzik.librepass.responses;

    exports dev.medzik.librepass.types.adapters;
    exports dev.medzik.librepass.types.api;
    exports dev.medzik.librepass.types.api.auth;
    exports dev.medzik.librepass.types.api.cipher;
    exports dev.medzik.librepass.types.api.collection;
    exports dev.medzik.librepass.types.api.user;
    exports dev.medzik.librepass.types.cipher;
    exports dev.medzik.librepass.types.cipher.data;

    exports dev.medzik.librepass.utils;

    opens dev.medzik.librepass.types.api to com.google.gson;
    opens dev.medzik.librepass.types.api.auth to com.google.gson;
    opens dev.medzik.librepass.types.api.cipher to com.google.gson;
    opens dev.medzik.librepass.types.api.collection to com.google.gson;
    opens dev.medzik.librepass.types.api.user to com.google.gson;
    opens dev.medzik.librepass.types.cipher to com.google.gson;
    opens dev.medzik.librepass.types.cipher.data to com.google.gson;
}
