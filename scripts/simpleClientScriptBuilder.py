import sys, os
sys.path.append("../../devTools/maven")
import scriptBuilder, depCopy, ProjectParser
build = scriptBuilder.build
class simpleScriptParameters(scriptBuilder.jacorbParameters):
    def __init__(self, mods, mainclass, name):
        scriptBuilder.jacorbParameters.__init__(self)
        for mod in mods: self.update(mod)
        propLoc = self.add('PROP', 'simpleClient.prop', 'initial')
        self.args.insert(0, '-props')
        self.args.insert(1, propLoc.interp)
        self.name = name
        self.mainclass = mainclass


def buildNetScripts(proj):
    mainclasses = [("edu.sc.seis.fissuresUtil.simple.SimpleNetworkClient", "simpleNet"),
                   ("edu.sc.seis.fissuresUtil.simple.ThreadedNetClient", "threadedNet")]
    return __build(proj, mainclasses)
    
        
def buildScripts(proj):
    mainclasses = [("edu.sc.seis.fissuresUtil.simple.ThreadedEventClient", "threadedEvent"),
               ("edu.sc.seis.fissuresUtil.simple.SimpleEventClient", "simpleEvent"),
               ("edu.sc.seis.fissuresUtil.simple.SimpleNetworkClient", "simpleNet"),
               ("edu.sc.seis.fissuresUtil.simple.ThreadedNetClient", "threadedNet"),
               ("edu.sc.seis.fissuresUtil.simple.SimpleSeismogramClient", "simpleSeis"),
               ("edu.sc.seis.fissuresUtil.simple.ThreadedSeisClient", "threadedSeis")]
    return __build(proj, mainclasses)

def __build(proj, mainclasses):
    depCopy.copy(proj)
    filenames = []
    for mainclass, name in mainclasses:
        scriptBuilder.setVarSh()
        shParams = simpleScriptParameters([], mainclass, name)
        filenames.append(build(shParams, proj))
        scriptBuilder.setVarWindows()
        winParams = simpleScriptParameters([scriptBuilder.windowsParameters()], mainclass, name)
        filenames.append(build(winParams, proj))
    return filenames

if __name__ == "__main__":
    buildScripts(ProjectParser.ProjectParser('../project.xml'))