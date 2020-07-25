package com.zionhuang.music.extractor;

abstract class SignatureFunction {
    public abstract String apply(String s) throws JSInterpreter.InterpretException;
}