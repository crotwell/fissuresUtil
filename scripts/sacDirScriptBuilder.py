#! /usr/bin/python -O
import sys, os
sys.path.extend(["../../devTools/maven", "../"])
import scriptBuilder, depCopy, ProjectParser, mavenExecutor, buildFisUtil

class nsCopyParams(scriptBuilder.jacorbParameters):
    def __init__(self, mods):
        scriptBuilder.jacorbParameters.__init__(self)
        for mod in mods: self.update(mod)
        self.name = 'sacDirToDSML'
        self.mainclass = 'edu.sc.seis.fissuresUtil.sac.SacDirToDataSet'
    
if __name__ == "__main__":
    scriptBuilder.absLibPath = True
    proj = ProjectParser.ProjectParser('../project.xml')
    buildFisUtil.build(proj)
    depCopy.copy(proj)
    scriptBuilder.setVarSh()
    shParams = nsCopyParams([])
    scriptBuilder.build(shParams, proj)
    scriptBuilder.setVarWindows()
    winParams = nsCopyParams([scriptBuilder.windowsParameters()])
    scriptBuilder.build(winParams, proj)
    print 'built sacDirToDSML.sh and sacDirToDSML.bat'
