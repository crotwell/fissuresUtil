#! /usr/bin/python -O
import sys, os
sys.path.extend(["../devTools/maven", './scripts'])
import distBuilder, ProjectParser, simpleClientScriptBuilder

def buildDist(proj):
    __build(proj, simpleClientScriptBuilder.buildScripts(proj))

def buildNetDist(proj):
    __build(proj, simpleClientScriptBuilder.buildNetScripts(proj), 'netClients')

def __build(proj, scripts, name='simpleClients'):
    extras = [(script, script) for script in  scripts]
    extras.append(('scripts/simpleClient.prop', 'simpleClient.prop'))
    extras.append(('src', 'src'))
    distBuilder.buildDist(proj, extras, name)
    for script in scripts: os.remove(script)
    

if __name__ == "__main__":
    proj = ProjectParser.ProjectParser('./project.xml')
    if len(sys.argv) > 1: buildNetDist(proj)
    else : buildDist(proj)