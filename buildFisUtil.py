#! /usr/bin/python -O
import sys, os
sys.path.append("../devTools/maven")
import ProjectParser, mavenExecutor

def build(fisProj):
    curdir = os.path.abspath('.')
    os.chdir(fisProj.path)
    allProj = [ProjectParser.ProjectParser('../fissures/project.xml'),
               fisProj]
    for proj in allProj: mavenExecutor.mavenExecutor(proj).jarinst()
    os.chdir(curdir)

if __name__ == "__main__":
    proj = ProjectParser.ProjectParser()
    build(proj)