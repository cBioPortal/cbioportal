import sys

def main(argv):
    inputFileName = sys.argv[1]
    outputFileName = sys.argv[2]
    inFile = open(inputFileName)
    outFile = open(outputFileName, 'wb')

    #read out meta line
    meta = inFile.readline()
    outFile.write(meta)

    totalCount = 0
    filteredInteractionCount = 0

    for line in inFile:
        lineSize = len(line.split('\t'))

        # This means we are in the section where node data is included in EXTENDED_BINARY_SIF
        if lineSize != 1:
            sourceName = line.split('\t')[3].lower()
            sourceNames = sourceName.split(';')
            totalCount = totalCount + 1
            for sourceName in sourceNames:
                if checkInteractionSource(sourceName):
                    outFile.write(line)
                    filteredInteractionCount = filteredInteractionCount + 1
                    break

    inFile.close()
    outFile.close()
    print 'Wrote ' + str(filteredInteractionCount) + " interactions out of " + str(totalCount) + " interactions after filtering"


def checkInteractionSource(source):
    return (source=='humancyc' or source=='panther' or source=='phosposite' or source=='reactome' or source=='pid')

if __name__ == "__main__":
    main(sys.argv)
