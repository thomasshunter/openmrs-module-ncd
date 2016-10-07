package org.openmrs.module.ncd.nlp;


public interface INlpAnalyzer
{
    public boolean analyze(String condition, String resultChunk);
}