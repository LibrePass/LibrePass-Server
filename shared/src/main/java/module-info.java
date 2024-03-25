module dev.medzik.librepass.shared {
    requires dev.medzik.libcrypto;
    requires com.google.gson;
    requires kotlin.stdlib;

    exports dev.medzik.librepass.errors;

    exports dev.medzik.librepass.types.api;
    exports dev.medzik.librepass.types.cipher;
    exports dev.medzik.librepass.types.cipher.data;

    exports dev.medzik.librepass.utils;

    opens dev.medzik.librepass.types.api to com.google.gson;
    opens dev.medzik.librepass.types.cipher to com.google.gson;
    opens dev.medzik.librepass.types.cipher.data to com.google.gson;
}
