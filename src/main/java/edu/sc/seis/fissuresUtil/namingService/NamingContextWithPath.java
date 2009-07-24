package edu.sc.seis.fissuresUtil.namingService;

import org.omg.CosNaming.NamingContext;

class NamingContextWithPath {

    public NamingContextWithPath(NamingContext namingContext, String path) {
        this.namingContext = namingContext;
        this.path = path;
    }

    public String trimFissuresPath() {
        String tmp = path;
        if(tmp.endsWith("/")) {
            tmp = tmp.substring(0, tmp.length() - 1);
        }
        if(tmp.startsWith(FISSURES_SLASH)) { return tmp.substring(FISSURES_SLASH.length()); }
        return tmp;
    }

    public String getPath() {
        return path;
    }

    public NamingContext getNamingContext() {
        return namingContext;
    }

    private NamingContext namingContext;

    private String path;

    public static final String FISSURES_SLASH = "/"
            + FissuresNamingService.FISSURES + "/";
}