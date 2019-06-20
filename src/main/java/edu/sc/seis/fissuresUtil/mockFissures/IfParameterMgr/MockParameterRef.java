package edu.sc.seis.fissuresUtil.mockFissures.IfParameterMgr;

import edu.iris.Fissures.IfParameterMgr.ParameterRef;

public class MockParameterRef {

    public static ParameterRef[] createParams() {
        return new ParameterRef[] {new ParameterRef("aid7", "creator7"),
                                   new ParameterRef("aid8", "creator8"),
                                   new ParameterRef("aid9", "creator9")};
    }

    /**
     * @deprecated - use create methods instead of sharing fields in case
     *             someone changes the internals
     */
    @Deprecated
    public static final ParameterRef[] params = createParams();
}
