#! /usr/bin/python -O
import sys, os
sys.path.extend(["../devTools/maven", './scripts'])
import distBuilder, ProjectParser, simpleClientScriptBuilder

def buildDist(proj):
    scripts = simpleClientScriptBuilder.buildScripts(proj)
    extras = [(script, script) for script in  scripts]
    distBuilder.buildDist(proj, extras, 'simpleClients')
    for script in scripts: os.remove(script)

if __name__ == "__main__":
    buildDist(ProjectParser.ProjectParser('./project.xml'))