import sys
sys.path.extend(["../../devTools/maven", './scripts'])
import distBuilder, ProjectParser, simpleClientScriptBuilder

def buildDist(proj):
    extras = []
    scripts = simpleClientScriptBuilder.buildScripts(proj)
    distBuilder.buildDist(proj, True, extras, scripts, 'simpleClients')

if __name__ == "__main__":
    buildDist(ProjectParser.ProjectParser('./project.xml'))