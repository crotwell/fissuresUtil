#! /usr/bin/python -O
import sys, os
sys.path.append("../devTools/maven")
import ProjectParser, mavenExecutor, scriptBuilder, depCopy, distBuilder
from optparse import OptionParser

class simpleScriptParameters(scriptBuilder.jacorbParameters):
    def __init__(self, mods, mainclass, name, propFile):
        scriptBuilder.jacorbParameters.__init__(self)
        for mod in mods: self.update(mod)
        propLoc = self.add('PROP', propFile, 'initial')
        self.args.insert(0, '-props')
        self.args.insert(1, propLoc.interp)
        self.name = name
        self.mainclass = mainclass

def buildSACDirScripts(proj):
    return __buildScripts(proj,
                   [('edu.sc.seis.fissuresUtil.sac.SacDirToDataSet', 'sacDirToDSML')])

def buildAllScripts(proj):
    scripts = buildNetScripts(proj)
    scripts.extend(buildEventScripts(proj))
    scripts.extend(buildSeisScripts(proj))
    scripts.extend(buildNSCopyScripts(proj))
    scripts.extend(buildSACDirScripts(proj))
    return scripts

def buildNetScripts(proj):
    mainclasses = [("edu.sc.seis.fissuresUtil.simple.SimpleNetworkClient", "simpleNet"),
                   ("edu.sc.seis.fissuresUtil.simple.ThreadedNetClient", "threadedNet")]
    return __buildScripts(proj, mainclasses)

def buildEventScripts(proj):
    mainclasses = [("edu.sc.seis.fissuresUtil.simple.ThreadedEventClient", "threadedEvent"),
                ("edu.sc.seis.fissuresUtil.simple.SimpleEventClient", "simpleEvent")]
    return __buildScripts(proj, mainclasses)

def buildSeisScripts(proj):
    mainclasses = [("edu.sc.seis.fissuresUtil.simple.SimpleSeismogramClient", "simpleSeis"),
                 ("edu.sc.seis.fissuresUtil.simple.ThreadedSeisClient", "threadedSeis")]
    return __buildScripts(proj, mainclasses)

def buildNSCopyScripts(proj):
    return __buildScripts(proj,
                   [('edu.sc.seis.fissuresUtil.namingService.NameServiceCopy',
                           'nsCopy')],
                   'nsCopy.prop')

def __buildScripts(proj, mainclasses, propFile='simpleClient.prop'):
    filenames = []
    for mainclass, name in mainclasses:
        scriptBuilder.setVarSh()
        shParams = simpleScriptParameters([], mainclass, name, propFile)
        filenames.append(scriptBuilder.build(shParams, proj))
        scriptBuilder.setVarWindows()
        winParams = simpleScriptParameters([scriptBuilder.windowsParameters()], mainclass, name, propFile)
        filenames.append(scriptBuilder.build(winParams, proj))
    return filenames

def buildJars(fisProj):
    curdir = os.path.abspath('.')
    os.chdir(fisProj.path)
    allProj = [ProjectParser.ProjectParser('../fissures/project.xml'),
               fisProj]
    for proj in allProj: mavenExecutor.mavenExecutor(proj).jarinst()
    os.chdir(curdir)

def buildDist(proj):
    __buildDist(proj, buildAllScripts(proj))

def buildNetDist(proj):
    __buildDist(proj, buildNetScripts(proj), 'netClients')

def __buildDist(proj, scripts, name='simpleClients'):
    extras = [(script, script) for script in  scripts]
    extras.append(('scripts/simpleClient.prop', 'simpleClient.prop'))
    extras.append(('src', 'src'))
    buildJars(proj)
    distBuilder.buildDist(proj, extras, name)
    for script in scripts: os.remove(script)

if __name__ == "__main__":
    proj = ProjectParser.ProjectParser('./project.xml')
    parser = OptionParser()
    parser.add_option("-n", "--net", dest="net",
                      help="build dist with only simple net client stuff",
                      default=False,
                      action="store_true")
    parser.add_option("-d", "--dist", dest="dist",
                      help="build dist with all scripts",
                      default=False,
                      action="store_true")
    parser.add_option("-s", "--scripts", dest="scripts",
                      help="compile fissuresUtil and build all scripts(default option)",
                      default=True,
                      action="store_true")
    parser.add_option("-c", "--nscopy", dest="nscopy",
                      help="compile fissuresUtil and build the nscopy scripts",
                      default=False,
                      action="store_true")
    parser.add_option("-t", "--sac-to-dsml", dest="sactodsml",
                      help="compile fissuresUtil and build the sacDirToDSML scripts",
                      default=False,
                      action="store_true")
    options = parser.parse_args()[0]
    if options.net : buildNetDist(proj)
    elif options.dist: buildDist(proj)
    else :
        buildJars(proj)
        os.chdir('scripts')
        if options.nscopy:
            buildNSCopyScripts(proj)
        elif options.sactodsml:
            buildSACDirScripts(proj)
        else:
            buildAllScripts(proj)
        depCopy.copy(proj)