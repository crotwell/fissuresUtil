package edu.sc.seis.fissuresUtil.display.borders;

public class UnchangingTitleProvider implements TitleProvider{
    public UnchangingTitleProvider(String title){ this.title = title; }

    public String getTitle() { return title; }

    private String title;
}

