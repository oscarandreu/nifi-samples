###############################################################################
#
# Variables provided in scope by script engine:
#
#    session - ProcessSession
#    context - ProcessContext
#    log - ComponentLog
#    REL_SUCCESS - Relationship
#    REL_FAILURE - Relationship
###############################################################################

def executeTask() :
    global flowFile
    
    flowFile = session.putAttribute(flowFile, 'foo-bar', 'foo')

def releaseResources() :    
    log.info("releasing stuff...")

# Use of LogData atttribute in JSON format like:
# {"level":"error",  "step":"OP-FetchS3", "message":"Error downloading ${filename}"}
def addErrorInformation(exception) :
    global flowFile

    message = '{"level":"error",  "step":"Undefined", "message":"'+ str(exception) +'"}'
    flowFile = session.putAttribute(flowFile, 'LogData', message)


flowFile = session.get()
if flowFile != None :
    try:
        executeTask()
        session.transfer(flowFile, REL_SUCCESS)
        pass
    except Exception as ex:
        addErrorInformation(ex)
        session.transfer(flowFile, REL_FAILURE)
        pass
    finally:
        releaseResources()
        pass
