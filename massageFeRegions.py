#turns the all uppercase geographical regions 
#into more gramatically correct geographical regions
def decap(l):
    if l.startswith("GeogRegion"):
        num, region = l.split("=")
        result = ""
        for i in range(len(region)):
            if shouldBeCaps(region, i):
                result += region[i].upper()
            else:
                result += region[i].lower()
        return num + '=' + result
    else:
        return l
        
def shouldBeCaps(string, position):
    prevChar = string[position - 1]
    return position == 0 or prevChar == '.' or prevChar == '-' or prevChar == ' '
    
lines = open('src/edu/sc/seis/fissuresUtil/display/FERegions.prop').readlines()
for line in lines: print decap(line),