package com.zionhuang.music.extractor;

interface SignatureFunction {
    /**
     * Decrypt an encrypted signature.
     *
     * @param s the encrypted signature string
     * @return the decrypted signature string
     * @throws JSInterpreter.InterpretException if error occurs in interpretation
     */
    String apply(String s) throws JSInterpreter.InterpretException;
}