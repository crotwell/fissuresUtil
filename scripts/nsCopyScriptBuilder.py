#! /usr/bin/python -O
import sys, os
sys.path.extend(["../../devTools/maven", "../"])
import scriptBuilder, depCopy, ProjectParser, mavenExecutor, buildFisUtil

class nsCopyParams(scriptBuilder.jacorbParameters):
    def __init__(self, mods):
        scriptBuilder.jacorbParameters.__init__(self)
        for mod in mods: self.update(mod)
        propLoc = self.add('PROP', 'nsCopy.prop', 'initial')
        self.args.insert(0, '-props')
        self.args.insert(1, propLoc.interp)
        self.name = 'nsCopy'
        self.mainclass = 'edu.sc.seis.fissuresUtil.namingService.NameServiceCopy'
    
if __name__ == "__main__":
    proj = ProjectParser.ProjectParser('../project.xml')
    buildFisUtil.build(proj)
    depCopy.copy(proj)
    scriptBuilder.setVarSh()
    shParams = nsCopyParams([])
    scriptBuilder.build(shParams, proj)
    scriptBuilder.setVarWindows()
    winParams = nsCopyParams([scriptBuilder.windowsParameters()])
    scriptBuilder.build(winParams, proj)
    print 'built nsCopy.sh and nsCopy.bat'