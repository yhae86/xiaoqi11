package com.m.sheishiwodi.word;


public class Word{
    private String word1 = null;
    private String word2 = null;
    public String getWord1() {
        return word1;
    }

    public String getWord2() {
        return word2;
    }

    public Word(String word1, String word2){
        this.word1 = word1;
        this.word2 = word2;
    }
}